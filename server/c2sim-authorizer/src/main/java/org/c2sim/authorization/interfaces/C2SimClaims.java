package org.c2sim.authorization.interfaces;

import org.c2sim.authorization.datatypes.ClaimValueList;

/**
 * Typed view of the C2SIM-specific claims extracted from a validated JWT Bearer token.
 *
 * <p>Each claim maps to a custom JWT claim name defined in the C2SIM PEP specification. Claim
 * values are modelled as {@link ClaimValueList} — a set of strings that can represent either a
 * single value or multiple semicolon-delimited values. The special sentinel value {@link
 * #CLAIM_ANY} ({@code "ANY"}) indicates that the claim permits any value.
 *
 * <p>Instances are created by {@code C2SimClaimsBuilder#build(String)}.
 */
public interface C2SimClaims {

  /** Separator used to delimit multiple values within a single JWT claim string. */
  String CLAIM_LIST_SEPARATOR = ";";

  /** Sentinel value indicating that a claim allows any value (wildcard). */
  String CLAIM_ANY = "ANY";

  // Claim names.
  /** JWT claim name for the communicative-act type code. */
  String COMMUNICATIVE_ACT_TYPE_CODE = "communicativeActTypeCode";

  /** JWT claim name identifying the sending system. */
  String FROM_SENDING_SYSTEM = "fromSendingSystem";

  /** JWT claim name identifying the system to reply to. */
  String REPLY_TO_SYSTEM = "replyToSystem";

  /** JWT claim name for the security-classification code. */
  String SECURITY_CLASSIFICATION_CODE = "securityClassificationCode";

  /** JWT claim name identifying the receiving system. */
  String TO_RECEIVING_SYSTEM = "toReceivingSystem";

  /** JWT claim name for the C2SIM message type. */
  String MESSAGE_TYPE = "messageType";

  /** JWT claim name for the system-level message type. */
  String SYSTEM_MESSAGE_TYPE = "systemMessageType";

  //  ClaimValueList is the datatype "set of STRING" in C2SIM PEP documentation
  //  -> Converting from JWT claim token value to ClaimValueList is done in C2SimClaimBuilder

  /**
   * Returns the communicative-act type codes permitted by this token.
   *
   * @return the claim value list for {@link #COMMUNICATIVE_ACT_TYPE_CODE}
   */
  // Enum type CommunicativeActTypeCodeType
  ClaimValueList getCommunicativeActTypeCode();

  /**
   * Returns the set of permitted sending-system identifiers.
   *
   * @return the claim value list for {@link #FROM_SENDING_SYSTEM}
   */
  ClaimValueList getFromSendingSystem();

  /**
   * Returns the set of permitted reply-to system identifiers.
   *
   * @return the claim value list for {@link #REPLY_TO_SYSTEM}
   */
  ClaimValueList getReplyToSystem();

  /**
   * Returns the permitted security-classification codes.
   *
   * @return the claim value list for {@link #SECURITY_CLASSIFICATION_CODE}
   */
  // Enum ELoxClassification
  ClaimValueList getSecurityClassificationCode();

  /**
   * Returns the set of permitted receiving-system identifiers.
   *
   * @return the claim value list for {@link #TO_RECEIVING_SYSTEM}
   */
  ClaimValueList getToReceivingSystem();

  /**
   * Returns the permitted C2SIM message types.
   *
   * @return the claim value list for {@link #MESSAGE_TYPE}
   */
  // Enum ELoxMessageType
  ClaimValueList getMessageType();

  /**
   * Returns the permitted system-level message types.
   *
   * @return the claim value list for {@link #SYSTEM_MESSAGE_TYPE}
   */
  ClaimValueList getSystemMessageType();

  /**
   * Returns the client name extracted from the JWT {@code client_id} (AZP) claim.
   *
   * @return the OAuth 2.0 client name, or an empty string if the claim is absent
   */
  String getClientName(); // The AZP field in JWT
}
