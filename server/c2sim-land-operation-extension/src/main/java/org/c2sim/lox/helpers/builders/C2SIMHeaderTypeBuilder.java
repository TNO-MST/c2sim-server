package org.c2sim.lox.helpers.builders;

import org.c2sim.lox.Global;
import org.c2sim.lox.schema.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class C2SIMHeaderTypeBuilder {


        // Prevent instantiation
  private C2SIMHeaderTypeBuilder() {

    }

    private final C2SIMHeaderType header = new C2SIMHeaderType();

    /**
     * Creates a new builder
     *

     * @return a new builder instance
     */
    public static C2SIMHeaderTypeBuilder create() {
        return new C2SIMHeaderTypeBuilder();
    }

    /**
     * Set authentication in header
     * @param authType Type of authorization
     * @param credentials The credentials
     * @return builder object
     */
    public C2SIMHeaderTypeBuilder authorization(String authType, String credentials) {
        var auth = new AuthorizationHeaderType();
        auth.setAuthorizationType(authType);
        auth.setAuthorizationCredentials(credentials);
        this.header.setAuthorizationHeader(auth);
        return this;
    }

    /**
     * Set bearer authentication in header
     * @param bearer Bearer token
     * @return builder object
     */
    public C2SIMHeaderTypeBuilder bearerAuthorization( String bearer ) {
        Objects.requireNonNull(bearer);
        var auth = new AuthorizationHeaderType();
        auth.setAuthorizationType("bearer");
        auth.setAuthorizationCredentials(
                bearer.toLowerCase().startsWith("bearer:") ? bearer : "bearer: " + bearer);
        this.header.setAuthorizationHeader(auth);
        return this;
    }

    /**
     * Set conversation ID in header
     * @param id conversation ID
     * @return builder object
     */
    public C2SIMHeaderTypeBuilder conversationId( UUID id ) {
        Objects.requireNonNull(id);
        this.header.setConversationID(id.toString());
        return this;
    }

    /**
     * Set conversation ID in header
     * @param id conversation ID
     * @return builder object
     */
    public C2SIMHeaderTypeBuilder inReplyToMessageId( UUID id ) {
        this.header.setInReplyToMessageID(id != null ? id.toString() : null);
        return this;
    }

    /**
     * Set reply to system ID in header
     * @param id reply to ID
     * @return builder object
     */
    public C2SIMHeaderTypeBuilder repleyToSytem( String id ) {
        this.header.setReplyToSystem(id);
        return this;
    }

    /**
     * Set message ID in header
     * @param id message ID
     * @return builder object
     */
    public C2SIMHeaderTypeBuilder messageId( UUID id ) {
        Objects.requireNonNull(id);
        this.header.setMessageID(id.toString());
        return this;
    }

    /**
     * Set send time in header
     * @param timestamp timestamp when message was send, or null
     * @return builder object
     */
    public C2SIMHeaderTypeBuilder sendingTime( Instant timestamp ) {

        this.header.setSendingTime(
                timestamp != null ? DateTimeTypeBuilder.create(timestamp).build() : null);
        return this;
    }

    /**
     * Set toReceivingSystems in header
     *
     * @param systems systems to send to
     * @return builder object
     */
    public C2SIMHeaderTypeBuilder toReceivingSystem( String systems ) {
        Objects.requireNonNull(systems);
        this.header.setToReceivingSystem(systems);
        return this;
    }

    /**
     * Set fromSendingSystem in header
     *
     * @param systems systems from
     * @return builder object
     */
    public C2SIMHeaderTypeBuilder fromSendingSystem( String systems ) {
        Objects.requireNonNull(systems);
        this.header.setFromSendingSystem(systems);
        return this;
    }

    /**
     * Builds and returns the {@link C2SIMHeaderTypeBuilder}.
     *
     * @return the constructed header
     */
    public C2SIMHeaderType build() {
        if (header.getFromSendingSystem() == null || header.getFromSendingSystem().isEmpty()) {
            throw new IllegalStateException("FromSendingSystem must be set in C2SIMHeaderType");
        }
        if (header.getToReceivingSystem() == null || header.getToReceivingSystem().isEmpty()) {
            throw new IllegalStateException("ToReceivingSystem must be set in C2SIMHeaderType");
        }
        if (header.getMessageID() == null) {
            header.setMessageID(java.util.UUID.randomUUID().toString());
        }
        if (header.getProtocol() == null) {
            header.setProtocol(Global.C2SIM_PROTOCOL);
        }
        if (header.getProtocolVersion() == null) {
            header.setProtocol(Global.C2SIM_PROTOCOL_VERSION);
        }

        if (header.getConversationID() == null) {
            header.setConversationID("00000000-0000-0000-0000-000000000000");
        }

        header.setSecurityClassificationCode(null); // Not used; will be removed
        header.setCommunicativeActTypeCode(CommunicativeActTypeCodeType.ACCEPT); // Not used; will be removed
        return header;
    }
}
