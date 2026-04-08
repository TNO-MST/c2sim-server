package org.c2sim.server.api.apis

import org.c2sim.server.api.models.C2SimError
import org.c2sim.server.api.models.ResponseStreamEndpoints
import io.javalin.http.Context

interface NotificationsApiService {

    /**
     * GET /c2sim/session/{sessionId}/stream-endpoints : Request information how to connect to stream, to receive C2SIM messages from C2SIM Server
     * In order for the C2SIM client to receive C2SIM messages from the C2SIM server, the C2SIM client needs to connect to a WebSocket.  The C2SIM client can always connect to a stream, but the C2SIM server only will start sending C2SIM messages when the join request is received and approved. The clientId is used to apply message filtering per streaming connection. 
     *
     * @param clientId Client identifier (random created ID, doesn&#39;t change for entire lifetime of the C2SIM client instance). (required)
     * @param sessionId Shared session identifier (required)
     * @param ctx The Javalin context. Especially handy if you need to access things like authentication headers in your service. (required)
     * @return Enpoint to create an stream (status code 200)
     *         or Request could not be handled (status code 400)
     * @see NotificationsApi#getStreamEndpoints
     */
    fun getStreamEndpoints(clientId: kotlin.String, sessionId: kotlin.String, ctx: Context): ResponseStreamEndpoints
}
