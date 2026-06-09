package org.c2sim.server.services.impl;

import org.c2sim.authorization.exceptions.AuthorisationException;
import org.c2sim.server.services.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class DefaultAuditService  implements AuditService {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    private static final String PROP_SHARED_SESSION = "c2sim.sharedSession";
    private static final String PROP_SYSTEM_NAME = "c2sim.systemName";
    private static final String PROP_ACTION = "c2sim.action";
    private static final String PROP_STATUS = "c2sim.status";
    private static final String PROP_IP_ADDRESS = "c2sim.clientIP";
    private static final String PROP_AZP = "c2sim.azp";
    private static final String PROP_JWT = "c2sim.jwt";
    private static final String PROP_CLIENT_ID = "c2sim.clientId";
    private static final String PROP_TRACKING_ID = "c2sim.trackingId";
    private static final String PROP_ERROR_MSG = "c2sim.errorMsg";
    private static final String PROP_AUTH_FAILURE_CODE = "c2sim.authFailureCode";
    
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILURE = "FAILURE";


    @Override
    public void joinedSharedSessionSuccessfully(
            String sharedSession,
            String systemName,
            String azp,
            String ip,
            String jwtToken) {
        MDC.put(PROP_SHARED_SESSION, sharedSession);
        MDC.put(PROP_SYSTEM_NAME, systemName);
        MDC.put(PROP_IP_ADDRESS, ip);
        MDC.put(PROP_AZP, azp);
        MDC.put(PROP_JWT, jwtToken);
        MDC.put(PROP_ACTION, "JOIN");
        MDC.put(PROP_STATUS, STATUS_SUCCESS);
        auditLogger.info("System '{}' successfully joined Shared Session '{}'",
                systemName, sharedSession);
        MDC.clear();
    }

    @Override
    public void resignedSharedSessionSuccessfully(String sharedSession, String systemName, String azp, String ip) {
        MDC.put(PROP_SHARED_SESSION, sharedSession);
        MDC.put(PROP_SYSTEM_NAME, systemName);
        MDC.put(PROP_IP_ADDRESS, ip);
        MDC.put(PROP_AZP, azp);
        MDC.put(PROP_ACTION, "RESIGN");
        MDC.put(PROP_STATUS, STATUS_SUCCESS);
        auditLogger.info("System '{}' successfully resigned from Shared Session '{}'",
                systemName, sharedSession);
        MDC.clear();
    }

    @Override
    public void authorizationFailure(
            AuthorisationException.AuthErrorCode errorCode,
            String message,
            String trackingId,
            String sharedSession,
            String ip,
            String clientId) {
        MDC.put(PROP_SHARED_SESSION, sharedSession);
        MDC.put(PROP_CLIENT_ID,  clientId);
        MDC.put(PROP_TRACKING_ID,  trackingId);
        MDC.put(PROP_ERROR_MSG, message);
        MDC.put(PROP_IP_ADDRESS, ip);
        MDC.put(PROP_ACTION, "AUTH_FAILURE");
        MDC.put(PROP_AUTH_FAILURE_CODE, errorCode.toString());
        MDC.put(PROP_STATUS, STATUS_FAILURE);
        auditLogger.info("C2SIM client '{}' with IP Address '{}' auth failure (error code '{}').",
                clientId, ip, errorCode.name());
        MDC.clear();
    }

    @Override
    public void startAudit() {
        MDC.put(PROP_ACTION, "START_AUDIT");
        MDC.put(PROP_STATUS, STATUS_SUCCESS);
        auditLogger.info("C2SIM Server started, audit log started.");
        MDC.clear();
    }
}

