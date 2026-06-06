package org.c2sim.server.services;

import org.c2sim.authorization.exceptions.AuthorisationException;

/**
 * Log audit information
 */
public interface AuditService {

    void startAudit();


    void joinedSharedSessionSuccessfully(
            String sharedSession,
            String systemName,
            String azp,
            String ip,
            String jwtToken);

    void resignedSharedSessionSuccessfully(
            String sharedSession,
            String systemName,
            String azp,
            String ip);

    void authorizationFailure(
            AuthorisationException.AuthErrorCode errorCode,
            String message,
            String trackingId,
            String sharedSession,
            String ip,
            String clientId);
}
