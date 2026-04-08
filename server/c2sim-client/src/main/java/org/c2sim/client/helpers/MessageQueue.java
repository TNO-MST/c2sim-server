package org.c2sim.client.helpers;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.c2sim.client.C2SimClient;
import org.c2sim.lox.C2SimMsgKind;
import org.c2sim.lox.exceptions.ValidationException;
import org.c2sim.lox.helpers.MessageTypeHelper;
import org.c2sim.lox.sax.DetectMsgKind;
import org.c2sim.lox.schema.MessageType;
import org.c2sim.lox.validation.LoxXsdValidator;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bounded, asynchronous queue that processes incoming C2SIM XML messages.
 *
 * <p>Messages are enqueued via {@link #publish(String)} and dispatched to an application-supplied
 * {@link Consumer} through {@link #onReceivedMessage(Consumer)}. For each message the queue
 * optionally:
 *
 * <ul>
 *   <li>detects the message kind using SAX ({@link DetectMsgKind}),
 *   <li>validates the XML against the LOX XSD schema ({@link LoxXsdValidator}), and
 *   <li>fully decodes the XML into a {@link MessageType} via JAXB ({@link MessageTypeHelper}).
 * </ul>
 *
 * <p>Implements {@link AutoCloseable}; call {@link #close()} to stop processing.
 */
public final class MessageQueue implements AutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(MessageQueue.class);

  private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(10_000);
  private final Consumer<C2SimMessage> inform;
  private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
  private final AtomicBoolean running = new AtomicBoolean(true);
  private final Semaphore permits;
  private final C2SimClient owner;
  private static final int MAX_NUMBER_OF_THREADS = 1;

  private final boolean decodeXmlMessage;
  private final boolean validateXmlMessage;

  /**
   * Creates a new message queue.
   *
   * @param owner the owning {@link C2SimClient} instance, passed to each {@link C2SimMessage}
   * @param decodeXmlMessage {@code true} to JAXB-decode each message into a {@link MessageType}
   * @param validateXmlMessage {@code true} to XSD-validate each message
   * @param inform internal consumer called for each processed message (before the application
   *     handler)
   */
  public MessageQueue(
      C2SimClient owner,
      boolean decodeXmlMessage,
      boolean validateXmlMessage,
      Consumer<C2SimMessage> inform) {
    // Messages are ordered
    this.owner = owner;
    this.inform = inform;
    this.decodeXmlMessage = decodeXmlMessage;
    this.validateXmlMessage = validateXmlMessage;
    this.permits = new Semaphore(MAX_NUMBER_OF_THREADS);
  }

  /**
   * Enqueues an XML message for processing, blocking if the queue is full.
   *
   * @param msg the raw XML message string
   * @throws InterruptedException if the calling thread is interrupted while waiting
   */
  public void publish(String msg) throws InterruptedException {
    queue.put(msg);
  }

  /**
   * Starts a background thread that drains the queue and invokes {@code handler} for each processed
   * message.
   *
   * @param handler the application callback invoked with the fully processed {@link C2SimMessage}
   */
  public void onReceivedMessage(Consumer<C2SimMessage> handler) {
    Thread.ofPlatform().start(() -> processQueue(handler));
  }

  private void processQueue(Consumer<C2SimMessage> handler) {
    while (running.get() || !queue.isEmpty()) {
      try {
        String xmlMsg = queue.take();
        permits.acquire();
        submitForProcessing(xmlMsg, handler);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }

  private void submitForProcessing(String xmlMsg, Consumer<C2SimMessage> handler) {
    executor.submit(
        () -> {
          try {
            C2SimMessage container = buildMessage(xmlMsg);
            inform.accept(container);
            handler.accept(container);
          } catch (Exception t) {
            logger.error("Web socket message processor: {}.", t.getMessage(), t);
          } finally {
            permits.release();
          }
        });
  }

  private C2SimMessage buildMessage(String xmlMsg) {

    var kind = DetectMsgKind.determineMsgKind(xmlMsg);

    ValidationOutcome validation = validateXmlMessage ? performValidation(xmlMsg) : null;

    MessageType msg = decodeXmlMessage ? getMessageType(xmlMsg) : null;

    if (validation == null) {
      validation = new ValidationOutcome(null, null);
    }

    return new C2SimMessage(owner, kind, xmlMsg, validation.exception(), validation.result(), msg);
  }

  private static MessageType getMessageType(String xmlMsg) {
    MessageType msg = null;
    try {
      msg = MessageTypeHelper.readMessage(xmlMsg);
    } catch (Exception ex) {
      logger.error("Decode C2SIM xml error: {}", ex.getMessage());
    }
    return msg;
  }

  private record ValidationOutcome(LoxXsdValidator result, ValidationException exception) {}

  private ValidationOutcome performValidation(String xmlMsg) {
    try {
      var result = LoxXsdValidator.doValidation(xmlMsg);
      return new ValidationOutcome(result, null);
    } catch (ValidationException ve) {
      return new ValidationOutcome(null, ve);
    }
  }

  /**
   * Stops the queue processor and shuts down the internal executor.
   *
   * <p>Already-queued messages that have not yet been dispatched may be dropped.
   */
  @Override
  public void close() {
    running.set(false);
    executor.shutdown();
  }

  /**
   * A fully processed C2SIM message delivered to application callbacks.
   *
   * @param client the {@link C2SimClient} that received this message
   * @param kind the detected message kind
   * @param xmlMessage the raw XML string
   * @param validationException the parse-level exception if XSD validation could not run, or {@code
   *     null}
   * @param validation the {@link LoxXsdValidator} result if validation was performed, or {@code
   *     null}
   * @param decodedMsg the JAXB-decoded {@link MessageType} if decoding was enabled, or {@code null}
   */
  public record C2SimMessage(
      C2SimClient client,
      C2SimMsgKind kind,
      String xmlMessage,
      @Nullable ValidationException validationException,
      @Nullable LoxXsdValidator validation,
      MessageType decodedMsg) {}
}
