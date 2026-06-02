package org.c2sim.server.utils;

import org.c2sim.authorization.interfaces.C2SimAuthorizer;
import org.c2sim.lox.schema.C2SIMHeaderType;

public class SystemNameHelper {

    private SystemNameHelper() {}
    /**
     *
     * @param auth auth object  (optional)
     * @param header C2SIM header (optional)
     * @return the system name
     */
    public static String getSystemName(C2SimAuthorizer auth, C2SIMHeaderType header) {
        // First check if auth CLAIM can be used to extract system name
        if ((auth != null) &&
                (auth.c2SimClaims().getFromSendingSystem() != null) &&
                (auth.c2SimClaims().getFromSendingSystem().size() == 1)) {
            return auth.c2SimClaims().getFromSendingSystem().iterator().next();
        }
        if ((header != null) &&
                (header.getFromSendingSystem() != null) &&
                (!header.getFromSendingSystem().trim().isEmpty())) {
            return header.getFromSendingSystem();
        }
        return "UNKNOWN";
    }

}
