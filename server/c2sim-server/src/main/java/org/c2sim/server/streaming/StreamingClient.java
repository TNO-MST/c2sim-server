package org.c2sim.server.streaming;

import io.javalin.websocket.WsCloseStatus;
import java.util.function.Consumer;

/**
 * Abstraction for a one-way C2SIM streaming connection to a client.
 *
 * <p>The server uses this interface to push C2SIM XML documents to connected clients without
 * depending on the underlying transport (currently WebSocket). Clients are not expected to send
 * data back; any incoming messages are handled as an error condition by the implementation.
 */
public interface StreamingClient {

  /**
   * Registers a listener that is notified when the streaming client sends data to the server.
   *
   * <p>Per the current C2SIM specification clients should not send data; this callback is a
   * placeholder for potential future use.
   *
   * @param listener the callback receiving the raw XML string
   */
  // Events:
  // The C2SIM streaming client send information (should new happen, TODO maybe demo 3?)
  void onC2SimMessageReceived(Consumer<String> listener);

  /**
   * Registers a listener that is notified when the streaming connection is closed.
   *
   * @param listener the callback (the {@link Void} parameter is always {@code null})
   */
  // The C2SIM streaming client closed the connection
  void onClosed(Consumer<Void> listener);

  /**
   * Registers a listener that is notified when a transport-level error occurs.
   *
   * @param listener the callback receiving the error description
   */
  void onError(Consumer<String> listener);

  /**
   * Closes the streaming connection to the C2SIM client.
   *
   * @param status the WebSocket close status code
   * @param message the human-readable close reason
   */
  // Close streaming connection to C2SIM Client
  void closeConnection(WsCloseStatus status, String message);

  /**
   * Sends a C2SIM XML document to the connected client.
   *
   * @param msg the minified XML message to transmit (newline-delimited)
   */
  // Send XML C2SIM message to C2SIM Client
  void sendC2SimMessage(String msg);
}
