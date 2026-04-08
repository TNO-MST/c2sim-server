package org.c2sim.client_app;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.c2sim.client.C2SimClient;
import org.c2sim.client.exceptions.C2SimRestException;
import org.c2sim.client.helpers.MessageQueue;
import org.c2sim.client.invoker.ApiException;
import org.c2sim.client.model.DynamicSessionInfo;
import org.c2sim.client.model.StateType;
import org.c2sim.client.security.OidcTokenProvider;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.lox.exceptions.ValidationException;
import org.c2sim.lox.helpers.MessageTypeHelper;
import org.c2sim.lox.helpers.XmlFactoryHelper;
import org.c2sim.lox.schema.C2SIMInitializationBodyType;
import org.c2sim.statemachine.Trigger;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/** A C2SIM client that monitors a shared session and periodically publishes position reports. */
public class MonitorClient implements Runnable, AutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(MonitorClient.class);

  /** MDC key used to correlate log output with a specific client instance. */
  private static final String INSTANCE_ID = "INSTANCE_ID";

  private final AtomicBoolean running = new AtomicBoolean(true);
  private final AtomicBoolean sendMessages = new AtomicBoolean(false);

  private final C2SimClient client;
  private final long instanceNumber;

  /**
   * Creates and configures a monitor client.
   *
   * <p>The underlying {@link C2SimClient} is built with:
   *
   * <ul>
   *   <li>XSD validation enabled for both sent and received messages
   *   <li>Received-message decoding to Java POJOs enabled
   *   <li>A listener wired to log lifecycle events and release {@code connectedSignal} once the
   *       client has joined the session
   * </ul>
   *
   * @param oidcProvider the OIDC token provider used for Bearer-token authentication, or {@code
   *     null} for unauthenticated access
   * @param uri the base URI of the C2SIM server REST API (e.g. {@code http://localhost:7777/api})
   * @param instanceNumber a zero-based index used to generate deterministic client and system names
   * @param connectedSignal a latch decremented when this client has successfully joined the shared
   *     session
   */
  public MonitorClient(
      @Nullable OidcTokenProvider oidcProvider,
      URI uri,
      long instanceNumber,
      CountDownLatch connectedSignal) {
    this.instanceNumber = instanceNumber;
    this.client =
        new C2SimClient.Builder()
            .url(uri)
            .systemName(generateSystemId(instanceNumber))
            .clientIdDisplayName(generateClientId(instanceNumber))
            .oidcProvider(oidcProvider)
            .sharedSessionName("default")
            .enableSendMessageValidation() // Enable XSD client side validation when sending msg to
            // C2SIM sever
            .enableReceivedMessageDecode() // Enable XML decoding to java POJO
            .enableReceivedMessageValidation() // Enable XSD client side validation when receiving
            // msg
            .listener(
                new C2SimClient.C2SimClientListener() {
                  @Override
                  public void onStreamConnected(C2SimClient client) {
                    MDC.put(INSTANCE_ID, generateClientId(instanceNumber)); // set context
                    try {
                      logger.info(
                          "===> C2SIM client {}({}): Stream is connected (WebSocket)",
                          client.getClientIdDisplayName(),
                          client.getClientId());
                    } finally {
                      MDC.remove(INSTANCE_ID); // clean up to avoid cross-thread leakage
                    }
                  }

                  @Override
                  public void onStateChanged(
                      C2SimClient client, StateType oldState, StateType newState) {
                    MDC.put(INSTANCE_ID, generateClientId(instanceNumber)); // set context
                    try {

                      logger.info(
                          "===> C2SIM client '{}({})': C2SIM server state changed from {} to {}.",
                          client.getClientIdDisplayName(),
                          client.getClientId(),
                          oldState,
                          newState);
                    } finally {
                      MDC.remove(INSTANCE_ID); // clean up to avoid cross-thread leakage
                    }
                    sendMessages.set(newState == StateType.RUNNING);
                  }

                  @Override
                  public void onJoined(C2SimClient client, DynamicSessionInfo info) {
                    MDC.put(INSTANCE_ID, generateClientId(instanceNumber)); // set context
                    try {

                      logger.info(
                          "===> C2SIM client {}({}): Joined shared session '{}' as system '{}'.",
                          client.getClientIdDisplayName(),
                          client.getClientId(),
                          client.getSharedSessionName(),
                          client.getSystemName());
                      connectedSignal.countDown();
                    } finally {
                      MDC.remove(INSTANCE_ID); // clean up to avoid cross-thread leakage
                    }
                  }

                  @Override
                  public void onResigned(C2SimClient client) {
                    MDC.put(INSTANCE_ID, generateClientId(instanceNumber)); // set context
                    try {

                      logger.info(
                          "===> C2SIM client {}({}): Resigned from shared session '{}' as system '{}'.",
                          client.getClientIdDisplayName(),
                          client.getClientId(),
                          client.getSharedSessionName(),
                          client.getSystemName());
                    } finally {
                      MDC.remove(INSTANCE_ID); // clean up to avoid cross-thread leakage
                    }
                  }

                  @Override
                  public void onC2SIMInitialization(
                      C2SimClient client, C2SIMInitializationBodyType init) {
                    MDC.put(INSTANCE_ID, generateClientId(instanceNumber)); // set context
                    try {

                      logger.info(
                          "===> C2SIM client '{}({})': Received C2SIMInitialization.",
                          client.getClientIdDisplayName(),
                          client.getClientId());
                    } finally {
                      MDC.remove(INSTANCE_ID); // clean up to avoid cross-thread leakage
                    }
                  }
                })
            .build();

    client.onReceivedMessage(this::c2simMessageReceived);
  }

  /**
   * Moves the C2SIM server state machine from UNINITIALIZED to RUNNING.
   *
   * @throws ValidationException if the initialization document fails XSD validation
   * @throws C2SimRestException if a REST call to the server fails
   * @throws ApiException if the generated API client reports an error
   * @throws LoxException if the C2SIM XML factory fails to create the message
   */
  public void bringIntoRunningState()
      throws ValidationException, C2SimRestException, ApiException, LoxException {
    client.sendTrigger(Trigger.SUBMIT_INITIALIZATION);
    client.publishC2SimDocument(
        XmlFactoryHelper.createC2SIMInitialization(
            XmlFactoryHelper.createC2SimHeader(generateSystemId(instanceNumber)),
            generateSystemId(instanceNumber)));
    client.sendTrigger(Trigger.SHARE_SCENARIO);
    client.sendTrigger(Trigger.START_SCENARIO);
  }

  private String generateSystemId(long instanceNumber) {
    return "SYSTEM_" + instanceNumber;
  }

  private String generateClientId(long instanceNumber) {
    return "CLIENT_" + instanceNumber;
  }

  /**
   * Returns the underlying {@link C2SimClient} for this monitor.
   *
   * @return the C2SIM client instance
   */
  public C2SimClient getC2SimClient() {
    return client;
  }

  /**
   * Callback invoked when a C2SIM message is received from the server over the WebSocket stream.
   *
   * <p>Logs the message kind and XSD validation result at INFO level using MDC for instance
   * correlation.
   *
   * @param msg the received message, including the raw XML, decoded POJO, and validation result
   */
  public void c2simMessageReceived(MessageQueue.C2SimMessage msg) {
    String status = "";
    if (msg.validationException() != null) {
      status = "INVALID XML";
    } else if (msg.validation() != null) {
      status =
          msg.validation().isValid() ? "XML passed XSD validation" : "XML failed XSD validation";
    } else {
      status = "XSD validation disabled";
    }
    MDC.put(INSTANCE_ID, generateClientId(instanceNumber)); // set context
    try {
      logger.info(
          "===> C2SIM client '{}({})': Received C2SIM xml message of type {} ({}).",
          msg.client().getClientIdDisplayName(),
          msg.client().getClientId(),
          msg.kind(),
          status);
    } finally {
      MDC.remove(INSTANCE_ID); // clean up to avoid cross-thread leakage
    }
  }

  /**
   * Publishes a randomised position report to the C2SIM server.
   *
   * <p>Creates the report via {@link ReportCreator#create} and serialises it to XML before
   * publishing. Logs the report ID on success; logs an error and continues on failure.
   */
  public void sendReport() {

    String xml = null;
    try {
      var report =
          ReportCreator.create(
              generateSystemId(instanceNumber), UUID.randomUUID(), UUID.randomUUID(), (short) 100);
      xml = MessageTypeHelper.writeMessageAsString(report, true, false);
      client.publishC2SimDocument(xml);
      MDC.put(INSTANCE_ID, generateClientId(instanceNumber)); // set context
      try {
        logger.info(
            "===> C2SIM client '{}({})': Published positions with report ID {}.",
            client.getClientIdDisplayName(),
            client.getClientId(),
            report.getMessageBody().getDomainMessageBody().getReportBody().getReportID());
      } finally {
        MDC.remove(INSTANCE_ID); // clean up to avoid cross-thread leakage
      }
    } catch (Exception e) {
      logger.error("Failed to publish report");
    }
  }

  /** Signals this client to stop its run loop. */
  @Override
  public void close() throws Exception {
    running.set(false);
  }

  /**
   * Run loop executed on a virtual thread.
   *
   * <p>Sends a position report every 5 seconds while {@link #sendMessages} is {@code true} (i.e.
   * the server is in the RUNNING state). The loop exits when {@link #close()} is called or the
   * thread is interrupted.
   */
  @Override
  public void run() {
    while (running.get()) {
      try {
        if (sendMessages.get()) {
          sendReport();
        }
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }
}
