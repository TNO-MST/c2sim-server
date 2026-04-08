package org.c2sim.authorization.impl;

import org.c2sim.authorization.datatypes.ClaimValueList;
import org.c2sim.authorization.interfaces.C2SimClaims;

/**
 * Package-private implementation of {@link C2SimClaims} populated by {@link C2SimClaimsBuilder}.
 *
 * <p>All claim values are immutable once the object is constructed. Instances should only be
 * created via {@link C2SimClaimsBuilder#build(String)}.
 */
class C2SimClaimsImpl implements C2SimClaims {
  private final String clientId;

  // Claims
  private final ClaimValueList communicativeActTypeCode;
  private final ClaimValueList fromSendingSystem;
  private final ClaimValueList replyToSystem;
  private final ClaimValueList securityClassificationCode;
  private final ClaimValueList toReceivingSystem;
  private final ClaimValueList messageType;
  private final ClaimValueList systemMessageType;

  /**
   * Creates a fully populated claims object.
   *
   * @param clientId the OAuth 2.0 client name from the JWT {@code client_id} claim
   * @param communicativeActTypeCode permitted communicative-act type codes
   * @param fromSendingSystem permitted sending-system identifiers
   * @param replyToSystem permitted reply-to system identifiers
   * @param securityClassificationCode permitted security-classification codes
   * @param toReceivingSystem permitted receiving-system identifiers
   * @param messageType permitted C2SIM message types
   * @param systemMessageType permitted system-level message types
   */
  C2SimClaimsImpl(
      String clientId,
      ClaimValueList communicativeActTypeCode,
      ClaimValueList fromSendingSystem,
      ClaimValueList replyToSystem,
      ClaimValueList securityClassificationCode,
      ClaimValueList toReceivingSystem,
      ClaimValueList messageType,
      ClaimValueList systemMessageType) {
    this.clientId = clientId;
    this.communicativeActTypeCode = communicativeActTypeCode;
    this.fromSendingSystem = fromSendingSystem;
    this.replyToSystem = replyToSystem;
    this.securityClassificationCode = securityClassificationCode;
    this.toReceivingSystem = toReceivingSystem;
    this.messageType = messageType;
    this.systemMessageType = systemMessageType;
  }

  /** {@inheritDoc} */
  public ClaimValueList getCommunicativeActTypeCode() {
    return communicativeActTypeCode;
  }

  /** {@inheritDoc} */
  public ClaimValueList getFromSendingSystem() {
    return fromSendingSystem;
  }

  /** {@inheritDoc} */
  public ClaimValueList getReplyToSystem() {
    return replyToSystem;
  }

  /** {@inheritDoc} */
  public ClaimValueList getSecurityClassificationCode() {
    return securityClassificationCode;
  }

  /** {@inheritDoc} */
  public ClaimValueList getToReceivingSystem() {
    return toReceivingSystem;
  }

  /** {@inheritDoc} */
  public ClaimValueList getMessageType() {
    return messageType;
  }

  /** {@inheritDoc} */
  public ClaimValueList getSystemMessageType() {
    return systemMessageType;
  }

  /** {@inheritDoc} */
  public String getClientName() {
    return clientId;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("C2SIM Claims{");
    sb.append("clientId=").append(clientId);

    sb.append(", communicativeActTypeCode=").append(String.valueOf(communicativeActTypeCode));
    sb.append(", fromSendingSystem=").append(String.valueOf(fromSendingSystem));
    sb.append(", replyToSystem=").append(String.valueOf(replyToSystem));
    sb.append(", securityClassificationCode=").append(String.valueOf(securityClassificationCode));
    sb.append(", toReceivingSystem=").append(String.valueOf(toReceivingSystem));
    sb.append(", messageType=").append(String.valueOf(messageType));
    sb.append(", systemMessageType=").append(String.valueOf(systemMessageType));

    sb.append('}');
    return sb.toString();
  }
}
