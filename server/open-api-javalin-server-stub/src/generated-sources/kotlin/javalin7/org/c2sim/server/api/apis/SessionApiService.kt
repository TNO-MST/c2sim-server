package org.c2sim.server.api.apis

import org.c2sim.server.api.models.C2SimError
import org.c2sim.server.api.models.DynamicSessionInfo
import org.c2sim.server.api.models.RequestCreateSession
import org.c2sim.server.api.models.RequestJoinSession
import org.c2sim.server.api.models.RequestResignSession
import org.c2sim.server.api.models.ResponseCreateSession
import org.c2sim.server.api.models.ResponseDeleteSession
import org.c2sim.server.api.models.ResponseJoinSession
import org.c2sim.server.api.models.ResponseResignSession
import io.javalin.http.Context

interface SessionApiService {

    /**
     * PUT /c2sim/session/{sessionId}/create : Create or update a shared session in the C2SIM Server
     * Create or update a shared session in the C2SIM Server. When the shared session already exsist, the request body information will be used to update the shared session information. The C2SIM Schema version during the session (changing this will result in 400 C2SimError)
     *
     * @param clientId Client identifier (random created ID, doesn&#39;t change for entire lifetime of the C2SIM client instance). (required)
     * @param sessionId Shared session identifier (required)
     * @param requestCreateSession Information needed to create a shared session (required)
     * @param ctx The Javalin context. Especially handy if you need to access things like authentication headers in your service. (required)
     * @return The C2SIM session was succesfully created/update (status code 200)
     *         or Request could not be handled (status code 400)
     * @see SessionApi#createSession
     */
    fun createSession(clientId: kotlin.String, sessionId: kotlin.String, requestCreateSession: RequestCreateSession, ctx: Context): ResponseCreateSession

    /**
     * DELETE /c2sim/session/{sessionId} : Delete shared session
     * Delete shared session on the C2SIM Server
     *
     * @param clientId Client identifier (random created ID, doesn&#39;t change for entire lifetime of the C2SIM client instance). (required)
     * @param sessionId Shared session identifier (required)
     * @param force Force session delete, even if C2SIm clients are connected (default is true) (optional)
     * @param ctx The Javalin context. Especially handy if you need to access things like authentication headers in your service. (required)
     * @return The shared session was succesfully created (status code 200)
     *         or Request could not be handled (status code 400)
     * @see SessionApi#deleteSession
     */
    fun deleteSession(clientId: kotlin.String, sessionId: kotlin.String, force: kotlin.Boolean?, ctx: Context): ResponseDeleteSession

    /**
     * GET /c2sim/session/{sessionId}/info : Get shared session info
     * Get information for shared session (is updated at runtime).
     *
     * @param clientId Client identifier (random created ID, doesn&#39;t change for entire lifetime of the C2SIM client instance). (required)
     * @param sessionId Shared session identifier (required)
     * @param ctx The Javalin context. Especially handy if you need to access things like authentication headers in your service. (required)
     * @return successful operation (status code 200)
     *         or Request could not be handled (status code 400)
     * @see SessionApi#getSessionInfo
     */
    fun getSessionInfo(clientId: kotlin.String, sessionId: kotlin.String, ctx: Context): DynamicSessionInfo

    /**
     * GET /c2sim/session/{sessionId}/initialization : Get XML C2SIMInitializationBody.
     * When the scenario is executing phase, the XML C2SIMInitializationBody is returned. This is the state when the scenario started, the C2SIMInitializationBody will not be updated during the execution phase.
     *
     * @param clientId Client identifier (random created ID, doesn&#39;t change for entire lifetime of the C2SIM client instance). (required)
     * @param sessionId Shared session identifier (required)
     * @param ctx The Javalin context. Especially handy if you need to access things like authentication headers in your service. (required)
     * @return C2SIMInitializationBody (status code 200)
     *         or Request could not be handled (status code 400)
     * @see SessionApi#getSessionInitialization
     */
    fun getSessionInitialization(clientId: kotlin.String, sessionId: kotlin.String, ctx: Context): kotlin.String

    /**
     * GET /c2sim/session/list : Get all active shared sessions
     * Returns a list of all active C2SIM sessions
     *
     * @param clientId Client identifier (random created ID, doesn&#39;t change for entire lifetime of the C2SIM client instance). (required)
     * @param ctx The Javalin context. Especially handy if you need to access things like authentication headers in your service. (required)
     * @return successful operation (status code 200)
     *         or Request could not be handled (status code 400)
     * @see SessionApi#getSessions
     */
    fun getSessions(clientId: kotlin.String, ctx: Context): List<DynamicSessionInfo>

    /**
     * POST /c2sim/session/{sessionId}/join : Join a shared session
     * Join a shared session in C2SIM server. Use &#39;default&#39; session to connect to default session.
     *
     * @param clientId Client identifier (random created ID, doesn&#39;t change for entire lifetime of the C2SIM client instance). (required)
     * @param sessionId Shared session identifier (required)
     * @param requestJoinSession  (required)
     * @param ctx The Javalin context. Especially handy if you need to access things like authentication headers in your service. (required)
     * @return Joined the shared session succesfully (status code 200)
     *         or C2SimError handling request (status code 400)
     * @see SessionApi#joinSession
     */
    fun joinSession(clientId: kotlin.String, sessionId: kotlin.String, requestJoinSession: RequestJoinSession, ctx: Context): ResponseJoinSession

    /**
     * POST /c2sim/session/{sessionId}/resign : Resign from shared session
     * Resign from shared session
     *
     * @param clientId Client identifier (random created ID, doesn&#39;t change for entire lifetime of the C2SIM client instance). (required)
     * @param sessionId Shared session identifier (required)
     * @param requestResignSession  (required)
     * @param ctx The Javalin context. Especially handy if you need to access things like authentication headers in your service. (required)
     * @return Resigned from shared session (status code 200)
     *         or C2SimError handling request (status code 400)
     * @see SessionApi#resignFromSession
     */
    fun resignFromSession(clientId: kotlin.String, sessionId: kotlin.String, requestResignSession: RequestResignSession, ctx: Context): ResponseResignSession
}
