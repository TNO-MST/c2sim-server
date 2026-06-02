package org.c2sim.authorization.interfaces;

import java.util.Set;
import org.c2sim.authorization.impl.AuthorizationResult;
import org.c2sim.authorization.lox.enums.ELoxMessageType;
import org.c2sim.lox.C2SimMsgKind;
import org.c2sim.lox.C2SimMsgKindCategory;
import org.c2sim.lox.schema.C2SIMHeaderType;

public interface C2SimAuthorizer {

  C2SimClaims c2SimClaims();

  AuthorizationResult authorizeComleteMessage(
          C2SIMHeaderType header,
          C2SimMsgKind msgKind,
          C2SimMsgKindCategory msgKindCategory,
          String protocol,
          String protocolVersion);

  AuthorizationResult authorizeFromSendingSystem(String fromSendingSystem);

  AuthorizationResult authorizeReplyToSystem(String replyToSystem);

  AuthorizationResult authorizeToReceivingSystem(Set<String> toReceivingSystem);

  AuthorizationResult authorizeMessageType(ELoxMessageType messageType);

  AuthorizationResult authorizeSystemMessageType(C2SimMsgKind systemMessageType);
}
