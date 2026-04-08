package org.c2sim.server.api.apis

// Modified version
 
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import io.javalin.http.pathParamAsClass
import io.javalin.http.queryParamAsClass

import org.c2sim.server.api.models.C2SimError
import org.c2sim.server.api.models.ResponseSend

class PublishApi(private val service: PublishApiService) {
    /**
     * Publish C2SIM document
     * Publish XML C2SIM document (root element Message)). The document will be validated against the schema version in the session. Only one message (&lt;Message&gt;&lt;/Message&gt;) is allowed in the body.
     * @param clientId Client identifier (random created ID, doesn&#39;t change for entire lifetime of the C2SIM client instance). 
     * @param sessionId Shared session identifier 
     * @param file  (optional)
     */
    fun send(ctx: Context) {
        val result = service.send(ctx.header("clientId") ?: throw io.javalin.http.BadRequestResponse("Required header clientId not present") , ctx.pathParamAsClass<kotlin.String>("sessionId").get(), ctx.uploadedFile("file"), ctx)
        ctx.status(200).json(result)
    }

}
