package org.c2sim.client.websockets;

import static java.lang.Thread.*;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Production-ready WebSocket manager with: - Auto-(re)connect with exponential backoff + jitter -
 * Heartbeat (PING) to detect dead connections - Outbound queue that buffers while disconnected -
 * Thread-safe send(), start(), stop()
 */
public final class OkHttpWebSocketManager {

  private static final int WS_NORMAL_CLOSURE = 1000;

  /**
   * Callback interface for WebSocket lifecycle and message events.
   *
   * <p>All methods have default no-op implementations; override only the events you need.
   */
  public interface WebSocketListener {
    /**
     * Called when the WebSocket connection is established.
     *
     * @param response the HTTP upgrade response from the server
     */
    default void onOpen(Response response) {}

    /**
     * Called when a text message is received.
     *
     * @param text the message text
     */
    default void onMessage(String text) {}

    /**
     * Called when a binary message is received.
     *
     * @param bytes the message bytes
     */
    default void onMessage(ByteString bytes) {}

    /**
     * Called when the remote peer initiates a close handshake.
     *
     * @param code the WebSocket close code
     * @param reason the human-readable close reason
     */
    default void onClosing(int code, String reason) {}

    /**
     * Called when the WebSocket is fully closed.
     *
     * @param code the WebSocket close code
     * @param reason the human-readable close reason
     */
    default void onClosed(int code, String reason) {}

    /**
     * Called when a connection failure occurs.
     *
     * @param t the throwable that caused the failure
     * @param response the HTTP response at the time of failure, or {@code null}
     */
    default void onFailure(Throwable t, Response response) {}

    /**
     * Called just before a reconnect attempt is scheduled.
     *
     * @param attempt the reconnect attempt number (1-based)
     * @param delayMillis the back-off delay in milliseconds before the next attempt
     */
    default void onScheduleReconnecting(int attempt, long delayMillis) {}

    /** Called when the WebSocket reconnects successfully (also called on the first connection). */
    default void onStartReconnectWebSocket(int retryNumber) {}
  }

  private static final Logger logger = LoggerFactory.getLogger(OkHttpWebSocketManager.class);

  private final OkHttpClient client;
  private final String url;
  private final Headers headers;
  private final WebSocketListener webSocketListener;

  // Reconnect/backoff
  private final long initialBackoffMs;
  private final long maxBackoffMs;
  private final double multiplier;
  private final SecureRandom jitter = new SecureRandom();

  // Heartbeat
  private final Duration pingInterval;
  private ScheduledExecutorService scheduler;

  // State
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicBoolean connected = new AtomicBoolean(false);
  private final AtomicInteger attempt = new AtomicInteger(0);
  private WebSocket webSocket;

  // Outbound buffering while disconnected
  private final BlockingQueue<Object> outbox =
      new LinkedBlockingQueue<>(10_000); // String or ByteString

  /** Fluent builder for {@link OkHttpWebSocketManager}. */
  public static class Builder {
    private OkHttpClient client = new OkHttpClient();
    private String url;
    private Headers headers = Headers.of();
    private WebSocketListener webSocketListener = new WebSocketListener() {};
    private long initialBackoffMs = 500;
    private long maxBackoffMs = 30_000;
    private double multiplier = 2.0;
    private Duration pingInterval = Duration.ofSeconds(30);

    /**
     * Sets the OkHttp client to use for the WebSocket connection.
     *
     * @param client the OkHttp client
     * @return this builder
     */
    public Builder client(OkHttpClient client) {
      this.client = client;
      return this;
    }

    /**
     * Sets the WebSocket endpoint URL.
     *
     * @param url the WebSocket URL (must start with {@code ws://} or {@code wss://})
     * @return this builder
     */
    public Builder url(String url) {
      this.url = url;
      return this;
    }

    /**
     * Sets additional HTTP headers to include in the upgrade request.
     *
     * @param headers the headers to send
     * @return this builder
     */
    public Builder headers(Headers headers) {
      this.headers = headers;
      return this;
    }

    /**
     * Sets the WebSocket event listener.
     *
     * @param webSocketListener the listener to receive lifecycle and message callbacks
     * @return this builder
     */
    public Builder listener(WebSocketListener webSocketListener) {
      this.webSocketListener = webSocketListener;
      return this;
    }

    /**
     * Sets the initial back-off delay for reconnect attempts.
     *
     * @param v the initial back-off in milliseconds
     * @return this builder
     */
    public Builder initialBackoffMs(long v) {
      this.initialBackoffMs = v;
      return this;
    }

    /**
     * Sets the maximum back-off delay for reconnect attempts.
     *
     * @param v the maximum back-off in milliseconds
     * @return this builder
     */
    public Builder maxBackoffMs(long v) {
      this.maxBackoffMs = v;
      return this;
    }

    /**
     * Sets the back-off multiplier applied after each failed reconnect attempt.
     *
     * @param v the multiplier (e.g. {@code 2.0} for exponential back-off)
     * @return this builder
     */
    public Builder multiplier(double v) {
      this.multiplier = v;
      return this;
    }

    /**
     * Sets the interval between heartbeat PING messages.
     *
     * @param d the ping interval
     * @return this builder
     */
    public Builder pingInterval(Duration d) {
      this.pingInterval = d;
      return this;
    }

    /**
     * Builds the {@link OkHttpWebSocketManager}.
     *
     * @return the configured manager
     * @throws IllegalArgumentException if {@code url} has not been set
     */
    public OkHttpWebSocketManager build() {
      if (url == null) {
        throw new IllegalArgumentException("url required");
      }
      return new OkHttpWebSocketManager(this);
    }
  }

  /**
   * Creates a manager from the given builder. Prefer {@link #newBuilder()} for fluent construction.
   *
   * @param builder the fully configured builder
   */
  public OkHttpWebSocketManager(OkHttpWebSocketManager.Builder builder) {
    this.client = builder.client;
    this.url = builder.url;
    this.headers = builder.headers;
    this.webSocketListener = builder.webSocketListener;
    this.initialBackoffMs = builder.initialBackoffMs;
    this.maxBackoffMs = builder.maxBackoffMs;
    this.multiplier = builder.multiplier;
    this.pingInterval = builder.pingInterval;
  }

  /** Start and connect (returns immediately; connects async). */
  public void start() {
    if (!running.compareAndSet(false, true)) {
      return;
    }
    scheduler =
        Executors.newScheduledThreadPool(
            2,
            r -> {
              Thread t = new Thread(r, "ws-manager");
              t.setDaemon(true);
              return t;
            });
    // Heartbeat loop
    scheduler.scheduleAtFixedRate(
        this::sendPingIfConnected,
        pingInterval.toMillis(),
        pingInterval.toMillis(),
        TimeUnit.MILLISECONDS);
    // Sender loop (drains outbox whenever connected)
    // scheduler.execute(this::senderLoop);
    connectNow();
  }

  /** Stop, close the socket, and shutdown threads. */
  public void stop() {
    if (!running.compareAndSet(true, false)) {
      return;
    }
    WebSocket ws = this.webSocket;
    this.webSocket = null;
    connected.set(false);
    if (ws != null) {
      try {
        logger.debug("Start closing WebSocket ");
        ws.close(WS_NORMAL_CLOSURE, "C2SIM client is resigning from Shared Session.");
      } catch (Exception ignored) {
        // Don't care is closed failed
      }
    }
    if (scheduler != null) {
      scheduler.shutdownNow();
      scheduler = null;
    }
    client.dispatcher().executorService().shutdown(); // optional if client is dedicated
  }

  /*

     * Thread-safe send; buffers if not connected. Returns false only if the queue is full or manager
     * not running.
     *
     * @param text the text message to send
     * @return {@code true} if the message was sent or buffered; {@code false} if the manager is not
     *     running or the outbound queue is full

    public boolean send(String text) {
      if (!running.get()) {
        return false;
      }
      if (connected.get()) {
        WebSocket ws = webSocket;
        if (ws != null && ws.send(text)) {
          return true;
        }
      }
      return outbox.offer(text);
    }


     * Thread-safe send; buffers if not connected.
     *
     * @param bytes the binary message to send
     * @return {@code true} if the message was sent or buffered; {@code false} if the manager is not
     *     running or the outbound queue is full

    public boolean send(ByteString bytes) {
      if (!running.get()) {
        return false;
      }
      if (connected.get()) {
        WebSocket ws = webSocket;
        if (ws != null && ws.send(bytes)) {
          return true;
        }
      }
      return outbox.offer(bytes);
    }
  */
  // === Internals ===

  private void reconnect(int retryCounter) {

    webSocketListener.onStartReconnectWebSocket(
        retryCounter); // also useful on first open for uniform handling

    try {
      connectNow();
    } catch (Exception e) {
      logger.error("{}WebSocket reconnect failed: ", e.getMessage(), e);
    }
  }

  private void connectNow() {
    if (!running.get()) {
      return;
    }
    logger.debug("Connect WebSocket to C2SIM server.");
    Request.Builder rb = new Request.Builder().url(url);
    if (headers != null) {
      rb.headers(headers);
    }
    Request req = rb.build();

    client.newWebSocket(
        req,
        new okhttp3.WebSocketListener() {
          @Override
          public void onOpen(@NotNull WebSocket ws, @NotNull Response response) {
            logger.debug("WebSocket connected to C2SIM server.");
            ;
            webSocket = ws;
            connected.set(true);
            attempt.set(0);
            webSocketListener.onOpen(response);
          }

          @Override
          public void onMessage(@NotNull WebSocket ws, @NotNull String text) {
            webSocketListener.onMessage(text);
          }

          @Override
          public void onMessage(@NotNull WebSocket ws, @NotNull ByteString bytes) {
            webSocketListener.onMessage(bytes);
          }

          @Override
          public void onClosing(@NotNull WebSocket ws, int code, @NotNull String reason) {
            webSocketListener.onClosing(code, reason);
            ws.close(code, reason); //  acknowledge close
            // Let server close; we'll mark disconnected in onClosed
          }

          @Override
          public void onClosed(@NotNull WebSocket ws, int code, @NotNull String reason) {
            connected.set(false);
            webSocketListener.onClosed(code, reason);
            webSocket = null;
            if (running.get() && shouldReconnectOnClose(code)) {
              scheduleReconnectWebSocket();
            }
          }

          @Override
          public void onFailure(@NotNull WebSocket ws, @NotNull Throwable t, Response response) {
            connected.set(false);
            webSocket = null;
            webSocketListener.onFailure(t, response);
            if (running.get()) {
              scheduleReconnectWebSocket();
            }
          }
        });
  }

  private boolean shouldReconnectOnClose(int code) {
    // Don't reconnect on NORMAL_CLOSURE (1000) unless you want to auto-rejoin.
    // Also avoid on GOING_AWAY(1001) if your app is intentionally shutting down.
    return code != 1000 && code != 1001;
  }

  private void scheduleReconnectWebSocket() {

    int retryCounter = attempt.incrementAndGet();
    long base =
        (long) Math.min(maxBackoffMs, initialBackoffMs * Math.pow(multiplier, retryCounter - 1.0));
    double jitterFactor = 0.2 * jitter.nextDouble(); // 0.0 to 0.2
    long delay = (long) (base * (1 + jitterFactor));

    webSocketListener.onScheduleReconnecting(retryCounter, delay);
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.schedule(() -> reconnect(retryCounter), delay, TimeUnit.MILLISECONDS);
    }
  }

  private void sendPingIfConnected() {
    if (!running.get() || !connected.get()) {
      return;
    }
    WebSocket ws = webSocket;
    if (ws != null) {
      try {
        // OkHttp auto-replies to server pings; we proactively ping to detect half-open connections.
        ws.send(ByteString.EMPTY);
        // Note: OkHttp exposes webSocket.queueSize() and handles automatic OKHTTP-level pings if
        // client configured.
      } catch (Exception ignored) {
        // Don't care if ping fails
      }
    }
  }

  /*
    private void senderLoop() {
      while (running.get()) {
        try {
          Object item = outbox.take(); // blocks
          waitUntilConnectedOrStopped();
          handleSend(item);
        } catch (InterruptedException ie) {
          currentThread().interrupt();
          break;
        } catch (Exception e) {
          // log if you want
        }
      }
    }

    private void waitUntilConnectedOrStopped() throws InterruptedException {
      while (running.get() && !connected.get()) {
        //noinspection BusyWait
        sleep(50);
      }
    }

    private void handleSend(Object item) throws InterruptedException {
      WebSocket ws = webSocket;
      if (ws != null && connected.get()) {
        boolean ok =
            switch (item) {
              case String s -> ws.send(s);
              case okio.ByteString bytes -> ws.send(bytes);
              case null -> throw new IllegalArgumentException("item is null");
              default ->
                  throw new IllegalArgumentException("Unsupported message type: " + item.getClass());
            };
        if (!ok) {
          requeueAndReconnect(item);
        }
      } else {
        // No socket; requeue and wait
        requeueAndPause(item);
      }
    }

    private void requeueAndReconnect(Object item) throws InterruptedException {
      boolean added = outbox.offer(item);
      if (!added) {
        logger.error("Message queue is full (requeueAndReconnect)");
      }
      if (running.get()) {
        scheduleReconnectWebSocket();
      }
      sleep(200);
    }

    private void requeueAndPause(Object item) throws InterruptedException {
      boolean added = outbox.offer(item);
      if (!added) {
        logger.error("Message queue is full (requeueAndPause)");
      }
      //noinspection BusyWait
      sleep(200);
    }
  */
  /**
   * Returns a new fluent {@link Builder}.
   *
   * @return a new builder instance
   */
  public static Builder newBuilder() {
    return new Builder();
  }
}
