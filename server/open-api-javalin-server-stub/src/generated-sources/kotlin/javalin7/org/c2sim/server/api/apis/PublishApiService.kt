package org.c2sim.server.api.apis

import org.c2sim.server.api.models.C2SimError
import org.c2sim.server.api.models.ResponseSend
import io.javalin.http.Context

interface PublishApiService {

    /**
     * POST /c2sim/session/{sessionId}/send : Publish C2SIM document
     * Publish XML C2SIM document (root element Message)). The document will be validated against the schema version in the session. Only one message (&lt;Message&gt;&lt;/Message&gt;) is allowed in the body.
     *
     * @param clientId Client identifier (random created ID, doesn&#39;t change for entire lifetime of the C2SIM client instance). (required)
     * @param sessionId Shared session identifier (required)
     * @param file  (optional)
     * @param ctx The Javalin context. Especially handy if you need to access things like authentication headers in your service. (required)
     * @return successful operation (status code 200)
     *         or Request could not be handled (status code 400)
     * @see PublishApi#send
     */
    fun send(clientId: kotlin.String, sessionId: kotlin.String, file: io.javalin.http.UploadedFile?, ctx: Context): ResponseSend
}
