package org.c2sim.lox;

public enum C2SimMsgKindCategory {
    /** C2SIM Object initialization */
    C2SIM_INITIALIZATION("C2SIMInitialization"),
    /** Domain message */
    DOMAIN_MESSAGE("DomainMessage"),
    /** Object initialization */
    OBJECT_INITIALIZATION("ObjectInitialization"),
    /** System ACK */
    SYSTEM_ACKNOWLEDGEMENT("SystemAcknowledgement"),
    /** System messages */
    SYSTEM_MESSAGE("SystemMessage"),
    /** Unknown message type */
    UNKNOWN("Unknown");

    private final String value;

    C2SimMsgKindCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
