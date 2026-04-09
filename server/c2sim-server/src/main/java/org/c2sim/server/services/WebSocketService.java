package org.c2sim.server.services;

import io.javalin.websocket.WsConfig;

/** Service that registers C2SIM WebSocket endpoints on the Javalin server. */
public interface WebSocketService {

  /** Stops the WebSocket service and releases any associated resources. */
  void stop();

  /** New web socket */
  void onNewWebSocket(WsConfig consumer);
}
