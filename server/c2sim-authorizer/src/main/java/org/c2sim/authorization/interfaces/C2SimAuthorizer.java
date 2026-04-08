package org.c2sim.authorization.interfaces;

import java.util.Set;
import org.c2sim.authorization.impl.AuthorizationResult;
import org.c2sim.authorization.lox.enums.ELoxMessageType;
import org.c2sim.lox.schema.CommunicativeActTypeCodeType;
import org.c2sim.lox.schema.MessageType;
import org.c2sim.lox.schema.SecurityClassificationCodeType;

public interface C2SimAuthorizer {

  C2SimClaims c2SimClaims();

  AuthorizationResult authorizeMessageTypeBody(
      MessageType requiredPermission, String protocol, String protocolVersion);

  AuthorizationResult authorizeCommunicativeActTypeCode(
      CommunicativeActTypeCodeType requiredPermission);

  AuthorizationResult authorizeFromSendingSystem(String fromSendingSystem);

  AuthorizationResult authorizeReplyToSystem(String replyToSystem);

  AuthorizationResult authorizeSecurityClassificationCode(
      SecurityClassificationCodeType requiredPermission);

  AuthorizationResult authorizeToReceivingSystem(Set<String> toReceivingSystem);

  AuthorizationResult authorizeMessageType(ELoxMessageType messageType);

  AuthorizationResult authorizeSystemMessageType(String systemMessageType);
}
