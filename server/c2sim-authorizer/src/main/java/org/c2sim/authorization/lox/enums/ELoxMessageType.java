package org.c2sim.authorization.lox.enums;

import java.util.Set;
import java.util.stream.Collectors;
import org.c2sim.authorization.interfaces.C2SimClaims;
import org.c2sim.authorization.interfaces.TextEnum;
import org.c2sim.authorization.utils.EnumUtil;

public enum ELoxMessageType implements TextEnum {
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

  private final String text;

  ELoxMessageType(String text) {
    this.text = text;
  }

  public static Set<ELoxMessageType> fromTextSet(Set<String> texts) {
    return EnumUtil.fromTextSet(ELoxMessageType.class, texts);
  }

  public String getText() {
    return text;
  }

  public static String toText(Set<ELoxMessageType> msgType) {
    return String.join(
        C2SimClaims.CLAIM_LIST_SEPARATOR,
        msgType.stream().map(ELoxMessageType::getText).collect(Collectors.toSet()));
  }
}
