package org.c2sim.server.api.apis

// Modified version
 
import io.javalin.http.Context
import io.javalin.http.bodyAsClass
import io.javalin.http.pathParamAsClass
import io.javalin.http.queryParamAsClass

import org.c2sim.server.api.models.C2SimError
import org.c2sim.server.api.models.DynamicSessionInfo
import org.c2sim.server.api.models.RequestCreateSession
import org.c2sim.server.api.models.RequestJoinSession
import org.c2sim.server.api.models.RequestResignSession
import org.c2sim.server.api.models.ResponseCreateSession
import org.c2sim.server.api.models.ResponseDeleteSession
import org.c2sim.server.api.models.ResponseJoinSession
import org.c2sim.server.api.models.ResponseResignSession

class SessionApi(private val service: SessionApiService) {
    /**
     * Create or update a shared session in the C2SIM Server
     * Create or update a shared session in the C2SIM Server. When the shared session already exsist, the request body information will be used to update the shared session information. The C2SIM Schema version during the session (changing this will result in 400 C2SimError)
     * @param clientId Client identifier (random created ID, doesn&#39;t change for entire lifetime of the C2SIM client instance). 
     * @param sessionId Shared session identifier 
     * @param requestCreateSession Information needed to create a shared session 
     */
    fun createSession(ctx: Context) {
        val result = service.createSession(ctx.header("clientId") ?: throw io.javalin.http.BadRequestResponse("Required header clientId not present") , ctx.pathParamAsClass<kotlin.String>("sessionId").get(), ctx.bodyAsClass<RequestCreateSession>(), ctx)
        ctx.status(200).json(result)
    }

    /**
     * Delete shared session
     * Delete shared session on the C2SIM Server
     * @param clientId Client identifier (random created ID, doesn&#39;t change for entire lifetime of the C2SIM client instance). 
     * @param sessionId Shared session identifier 
     * @param force Force session delete, even if C2SIm clients are connected (default is true) (optional)
     */
    fun deleteSession(ctx: Context) {
        val result = service.deleteSession(ctx.header("clientId") ?: throw io.javalin.http.BadRequestResponse("Required header clientId not present") , ctx.pathParamAsClass<kotlin.String>("sessionId").get(), ctx.queryParamAsClass<kotlin.Boolean>("force").getOrNull(), ctx)
        ctx.status(200).json(result)
    }

    /**
     * Get shared session info
     * Get information for shared session (is updated at runtime).
     * @param clientId Client identifier (random created ID, doesn&#39;t change for entire lifetime of the C2SIM client instance). 
     * @param sessionId Shared session identifier 
     */
    fun getSessionInfo(ctx: Context) {
        val result = service.getSessionInfo(ctx.header("clientId") ?: throw io.javalin.http.BadRequestResponse("Required header clientId not present") , ctx.pathParamAsClass<kotlin.String>("sessionId").get(), ctx)
        ctx.status(200).json(result)
    }

    /**
     * Get XML C2SIMInitializationBody.
     * When the scenario is executing phase, the XML C2SIMInitializationBody is returned. This is the state when the scenario started, the C2SIMInitializationBody will not be updated during the execution phase.
     * @param clientId Client identifier (random created ID, doesn&#39;t change for entire lifetime of the C2SIM client instance). 
     * @param sessionId Shared session identifier 
     */
    fun getSessionInitialization(ctx: Context) {
        val result = service.getSessionInitialization(ctx.header("clientId") ?: throw io.javalin.http.BadRequestResponse("Required header clientId not present") , ctx.pathParamAsClass<kotlin.String>("sessionId").get(), ctx)
        ctx.status(200).json(result)
    }

    /**
     * Get all active shared sessions
     * Returns a list of all active C2SIM sessions
     * @param clientId Client identifier (random created ID, doesn&#39;t change for entire lifetime of the C2SIM client instance). 
     */
    fun getSessions(ctx: Context) {
        val result = service.getSessions(ctx.header("clientId") ?: throw io.javalin.http.BadRequestResponse("Required header clientId not present") , ctx)
        ctx.status(200).json(result)
    }

    /**
     * Join a shared session
     * Join a shared session in C2SIM server. Use &#39;default&#39; session to connect to default session.
     * @param clientId Client identifier (random created ID, doesn&#39;t change for entire lifetime of the C2SIM client instance). 
     * @param sessionId Shared session identifier 
     * @param requestJoinSession  
     */
    fun joinSession(ctx: Context) {
        val result = service.joinSession(ctx.header("clientId") ?: throw io.javalin.http.BadRequestResponse("Required header clientId not present") , ctx.pathParamAsClass<kotlin.String>("sessionId").get(), ctx.bodyAsClass<RequestJoinSession>(), ctx)
        ctx.status(200).json(result)
    }

    /**
     * Resign from shared session
     * Resign from shared session
     * @param clientId Client identifier (random created ID, doesn&#39;t change for entire lifetime of the C2SIM client instance). 
     * @param sessionId Shared session identifier 
     * @param requestResignSession  
     */
    fun resignFromSession(ctx: Context) {
        val result = service.resignFromSession(ctx.header("clientId") ?: throw io.javalin.http.BadRequestResponse("Required header clientId not present") , ctx.pathParamAsClass<kotlin.String>("sessionId").get(), ctx.bodyAsClass<RequestResignSession>(), ctx)
        ctx.status(200).json(result)
    }

}
