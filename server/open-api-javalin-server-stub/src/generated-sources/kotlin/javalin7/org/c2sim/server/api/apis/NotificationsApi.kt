package org.c2sim.server.api.apis

// Modified version
 
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import io.javalin.http.pathParamAsClass
import io.javalin.http.queryParamAsClass

import org.c2sim.server.api.models.C2SimError
import org.c2sim.server.api.models.ResponseStreamEndpoints

class NotificationsApi(private val service: NotificationsApiService) {
    /**
     * Request information how to connect to stream, to receive C2SIM messages from C2SIM Server
     * In order for the C2SIM client to receive C2SIM messages from the C2SIM server, the C2SIM client needs to connect to a WebSocket.  The C2SIM client can always connect to a stream, but the C2SIM server only will start sending C2SIM messages when the join request is received and approved. The clientId is used to apply message filtering per streaming connection.

     * @param clientId Client identifier (random created ID, doesn&#39;t change for entire lifetime of the C2SIM client instance). 
     * @param sessionId Shared session identifier 
     */
    fun getStreamEndpoints(ctx: Context) {
        val result = service.getStreamEndpoints(ctx.header("clientId") ?: throw io.javalin.http.BadRequestResponse("Required header clientId not present") , ctx.pathParamAsClass<kotlin.String>("sessionId").get(), ctx)
        ctx.status(200).json(result)
    }

}
