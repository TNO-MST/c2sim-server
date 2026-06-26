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
  private final String jwt;
  private final String clientId;

  // Claims
  private final ClaimValueList fromSendingSystem;
  private final ClaimValueList replyToSystem;
  private final ClaimValueList toReceivingSystem;
  private final ClaimValueList messageType;
  private final ClaimValueList systemMessageType;

  /**
   * Creates a fully populated claims object.
   *
   * @param jwt original JWT token  (only for logging)
   * @param clientId the OAuth 2.0 client name from the JWT {@code client_id} claim
   * @param fromSendingSystem permitted sending-system identifiers
   * @param replyToSystem permitted reply-to system identifiers
   * @param toReceivingSystem permitted receiving-system identifiers
   * @param messageType permitted C2SIM message types
   * @param systemMessageType permitted system-level message types
   */
  C2SimClaimsImpl(
          String jwt,
      String clientId,
      ClaimValueList fromSendingSystem,
      ClaimValueList replyToSystem,
      ClaimValueList toReceivingSystem,
      ClaimValueList messageType,
      ClaimValueList systemMessageType) {
    this.jwt = jwt;
    this.clientId = clientId;
    this.fromSendingSystem = fromSendingSystem;
    this.replyToSystem = replyToSystem;
    this.toReceivingSystem = toReceivingSystem;
    this.messageType = messageType;
    this.systemMessageType = systemMessageType;
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

  /** {@inheritDoc} */
  public String getJwtToken() {
    return jwt;
  }


  /** {@inheritDoc} */
  public String toTextDescription() {

    StringBuilder sb = new StringBuilder("C2SIM Claims {");
    sb.append("\nclientId=").append(clientId);
    sb.append("\n, fromSendingSystem=").append(fromSendingSystem.getClaimIsAny() ? "ANY" : String.valueOf(fromSendingSystem));
    sb.append("\n, replyToSystem=").append(replyToSystem.getClaimIsAny() ? "ANY" :String.valueOf(replyToSystem));
    sb.append("\n, toReceivingSystem=").append(toReceivingSystem.getClaimIsAny() ? "ANY" :String.valueOf(toReceivingSystem));
    sb.append("\n, messageType=").append(messageType.getClaimIsAny() ? "ANY" :String.valueOf(messageType));
    sb.append("\n, systemMessageType=").append(systemMessageType.getClaimIsAny() ? "ANY" :String.valueOf(systemMessageType));
    sb.append("\n }");
    return sb.toString();
  }

  @Override
  public String toString() {
    return toTextDescription();
  }
}
