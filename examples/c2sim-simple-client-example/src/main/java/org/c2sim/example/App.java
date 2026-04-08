package org.c2sim.example;

import org.c2sim.client.C2SimClient;
import org.c2sim.client.exceptions.C2ClientException;
import org.c2sim.client.exceptions.C2SimRestException;
import org.c2sim.client.helpers.MessageQueue;
import org.c2sim.client.invoker.ApiException;
import org.c2sim.client.model.DynamicSessionInfo;
import org.c2sim.client.model.RequestCreateSession;
import org.c2sim.client.model.SessionInfo;
import org.c2sim.client.model.StateType;
import org.c2sim.client.security.OidcCredentialFlow;
import org.c2sim.client.security.OidcCredentialFlowConfig;
import org.c2sim.client.security.OidcHardCodedToken;
import org.c2sim.client.security.OidcTokenProvider;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.lox.exceptions.ValidationException;
import org.c2sim.lox.helpers.MessageBodyTypeHelper;
import org.c2sim.lox.helpers.XmlFactoryHelper;
import org.c2sim.lox.helpers.builders.MessageTypeBuilder;
import org.c2sim.lox.schema.C2SIMInitializationBodyType;
import org.c2sim.lox.schema.MessageBodyType;
import org.c2sim.lox.schema.MessageType;
import org.c2sim.statemachine.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple example how to use the C2SIM client
 *
 */
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static final AtomicBoolean quit = new AtomicBoolean(false);


    public static void main(String[] args) {

        String systemName = "NLD-DEMO";
        String clientDisplayName = "NLD-DEMO-DEBUG";
        String sharedSessionName = "default";
        URI c2simServer = URI.create("http://localhost:9999/api");

        // The OIDC config provider
        /*
        OidcCredentialFlowConfig authCfg =
                new OidcCredentialFlowConfig(
                        URI.create("http://localhost:8080/realms/c2sim/.well-known/openid-configuration"),
                        "client",
                        "secret");
        OidcTokenProvider oidcProvider = new OidcCredentialFlow(authCfg);
        or for testing
        OidcHardCodedToken oidcProvider = new OidcHardCodedToken("<TOKEN>");
        */


        var client =
                new C2SimClient.Builder()
                        .url(c2simServer)
                        .systemName(systemName)
                        .clientIdDisplayName(clientDisplayName)
                        // .oidcProvider(oidcProvider)
                        .sharedSessionName(sharedSessionName)
                        .enableSendMessageValidation() // Enable XSD client side validation when sending msg to
                        // C2SIM sever
                        .enableReceivedMessageDecode() // Enable XML decoding to java POJO
                        .enableReceivedMessageValidation() // Enable XSD client side validation when receiving
                        // msg
                        .listener(
                                new C2SimClient.C2SimClientListener() {
                                    @Override
                                    public void onStreamConnected(C2SimClient client) {
                                        logger.info("Stream is connected (WebSocket)");
                                    }

                                    @Override
                                    public void onStateChanged(
                                            C2SimClient client, StateType oldState, StateType newState) {
                                        logger.info(
                                                "C2SIM server state changed from {} to {}.",
                                                oldState, newState);
                                    }

                                    @Override
                                    public void onJoined(C2SimClient client, DynamicSessionInfo info) {
                                        logger.info("Joined shared session.");
                                    }

                                    @Override
                                    public void onResigned(C2SimClient client) {
                                        logger.info("Resigned from shared session");
                                    }

                                    @Override
                                    public void onC2SIMInitialization(C2SimClient client, C2SIMInitializationBodyType init) {
                                        logger.info("Received C2SIMInitialization.");
                                    }
                                })
                        .build();

        client.onReceivedMessage(App::c2simMessageReceived);

        // The shared session 'default' is always present on C2SIM Server
        // If shared session is not existing on server, the client will create the session
        // This info is used to create the shared session
        client.whenCreatingSharedSession(() -> {
            RequestCreateSession session = new RequestCreateSession();
            SessionInfo si = new SessionInfo();
            si.setC2simSchemaVersion("1.0.2"); // The C2SIM schema version to be used
            si.setDescription("Auto created session by C2SIM client");
            si.setDisplayName("auto-created-session");
            session.setData(si);
            return session;
        });

        try {
            // This connect call is blocking (while establishing the connection),
            // use `client.connectAsync()` for non blocking
            client.connect();
            if (!client.isJoined()) {
                logger.error("Client failed to join.");
                System.exit(-1);
            }
            bringC2SimServerIntoRunningState(client);
            if (client.getCachedC2SimServerState() != StateType.RUNNING) {
                logger.error("C2SIM Server state should be RUNNING.");
                System.exit(-1);
            }

            // Thread waiting for ENTER on console
            new Thread(() -> {
                new Scanner(System.in).nextLine();
                quit.set(true);
            }).start();

            // Send with an interval a position report
            while (!quit.get()) {
                if (client.getCachedC2SimServerState() == StateType.RUNNING) {
                    var report = ReportCreator.create(systemName, UUID.randomUUID(), UUID.randomUUID(), (short) 1);
                    logger.info("Publish position report to C2SIM server");
                    client.publishC2SimDocument(report);
                } else {
                    logger.info("C2SIM server state: {}", client.getCachedC2SimServerState());
                }
                Thread.sleep(2000);
            }

            // Try catch for all exception in program
        } catch (C2SimRestException e) {
            logger.error("C2SimRestException:: Error returned by C2SIM server: {}", e.getMessage());
        } catch (ApiException e) {
            logger.error("ApiException:: REST network error: {}", e.getMessage());
        } catch (C2ClientException e) {
            logger.error("C2SIM client exception: {}", e.getMessage());
        } catch (ValidationException e) {
            logger.error("ValidationException:: XSD validation error: {}", e.getMessage());
        } catch (LoxException e) {
            logger.error("LoxException:: Invalid LOX message: {}", e.getMessage());
        } catch (InterruptedException | IOException e) {
            logger.error("Generic error: {}", e.getMessage());
        }

    }

    private static void bringC2SimServerIntoRunningState(C2SimClient client)
            throws ValidationException, C2SimRestException, ApiException, LoxException, IOException {

        if (client.getCachedC2SimServerState() == StateType.UNINITIALIZED) {
            logger.info("Bring C2SIM server state from UNINITIALIZED to INITIALIZING");
            client.sendTrigger(Trigger.SUBMIT_INITIALIZATION);
        }

        if (client.getCachedC2SimServerState() == StateType.INITIALIZING) {
            logger.info("Bring C2SIM server state from INITIALIZING to INITIALIZED");
            // Send C2SIMInitializationBody
            client.publishC2SimDocument(getC2SimInitialization(client.getSystemName()));
            // Notify ready
            client.sendTrigger(Trigger.INITIALIZATION_COMPLETE);
            // Switch to state INITIALIZED, C2SIM server will send C2SIMInitializationBody to other C2SIM clients
            client.sendTrigger(Trigger.SHARE_SCENARIO);
        }

        if (client.getCachedC2SimServerState() == StateType.INITIALIZED) {
            logger.info("Bring C2SIM server state from INITIALIZED to RUNNING");
            client.sendTrigger(Trigger.START_SCENARIO);
        }
    }

    private static MessageType getC2SimInitialization(String systemName) throws IOException, LoxException {
        InputStream is = App.class
                .getClassLoader()
                .getResourceAsStream("CWIX2025-6jun2025.xml");
        if (is == null) {
            throw new IOException("Initialization file could not be loaded");
        }
        MessageBodyType initBody = MessageBodyTypeHelper.readMessageBody(is);
        var header = XmlFactoryHelper.createC2SimHeader(systemName);
        return MessageTypeBuilder.create()
                .c2SIMHeader(header)
                .messageBody(initBody)
                .build();
    }

    /**
     * Called when there is a C2SIM message is received
     * This is method has an own thread (not main (GUI) thread)!
     * @param msg The received message
     */
    private static void c2simMessageReceived(MessageQueue.C2SimMessage msg) {
        String status = "";
        // Is possible that the C2SIM message was not valid (C2SIM server does also validation, so not likely)
        if (msg.validationException() != null) {
            status = "INVALID XML";
        } else if (msg.validation() != null) {
            status =
                    msg.validation().isValid() ? "XML passed XSD validation" : "XML failed XSD validation";
        } else {
            status = "XSD validation disabled";
        }
        // if the option `enableReceivedMessageDecode` was set in the client,
        // the `msg.decodedMsg()` contains the POJO, `msg.xmlMessage()` contains the XML as text
        logger.info("Received C2SIM xml message of type {} ({}).", msg.kind(), status);
    }

}
