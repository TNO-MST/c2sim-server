package org.c2sim.authorization.impl;

import java.util.*;
import java.util.stream.Collectors;
import org.c2sim.authorization.interfaces.C2SimAuthorizer;
import org.c2sim.authorization.interfaces.C2SimClaims;
import org.c2sim.authorization.lox.enums.*;
import org.c2sim.lox.schema.CommunicativeActTypeCodeType;
import org.c2sim.lox.schema.MessageBodyType;
import org.c2sim.lox.schema.MessageType;
import org.c2sim.lox.schema.SecurityClassificationCodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record C2SimAuthorizerImpl(C2SimClaims c2SimClaims) implements C2SimAuthorizer {

  private static final Logger logger = LoggerFactory.getLogger(C2SimAuthorizerImpl.class);

  private static final EnumSet<ELoxCommands> simanCommands =
      EnumSet.of(ELoxCommands.PAUSE, ELoxCommands.INITCOMP);

  private static final EnumSet<ELoxCommands> simanResponseCommands =
      EnumSet.of(ELoxCommands.INITCOMP);

  private static String getAccessDeniedMsg(
      String claimName, String allowedClaims, String requiredClaims) {
    return String.format(
        "JWT claim '%s' has permissions for [%s], where [%s] is provided.'",
        claimName, allowedClaims, requiredClaims);
  }

  @Override
  public AuthorizationResult authorizeMessageTypeBody(
      MessageType requiredPermission, String protocol, String protocolVersion) {
    if (requiredPermission == null) {
      return AuthorizationResult.create(
          AuthorizationResult.Code.UNAUTHORIZED,
          "Access denied: No C2SIM XML message, nothing to authorize");
    }
    try {
      List<AuthorizationResult> checks = new ArrayList<>();
      // Check CommunicativeActTypeCode
      var act = requiredPermission.getC2SIMHeader().getCommunicativeActTypeCode();
      checks.add(authorizeCommunicativeActTypeCode(act));

      // Check message type
      var msgType = getMessageEnumType(requiredPermission.getMessageBody());
      checks.add(authorizeMessageType(msgType));

      // Check Sending System
      var fromSendingSystem = requiredPermission.getC2SIMHeader().getFromSendingSystem();
      if (fromSendingSystem == null) {
        fromSendingSystem = "";
      }
      checks.add(authorizeFromSendingSystem(fromSendingSystem));

      // Check Reply to system
      var replyToSystem = requiredPermission.getC2SIMHeader().getReplyToSystem();
      if (replyToSystem == null) {
        replyToSystem = "";
      }
      checks.add(authorizeReplyToSystem(replyToSystem));

      // Check To Receiving system
      var receivingSystem = requiredPermission.getC2SIMHeader().getToReceivingSystem();
      if (receivingSystem == null) {
        receivingSystem = "";
      }
      checks.add(authorizeToReceivingSystem(Set.of(receivingSystem))); // TODO when multiple systems

      // Check SecurityClassificationCode
      var classificationCode = requiredPermission.getC2SIMHeader().getSecurityClassificationCode();
      checks.add(authorizeSecurityClassificationCode(classificationCode));

      // Protocol
      if (!protocol.equals(requiredPermission.getC2SIMHeader().getProtocol())) {
        checks.add(
            AuthorizationResult.create(
                AuthorizationResult.Code.UNAUTHORIZED,
                String.format(
                    "Protocol value is '%s' where '%s' is required",
                    requiredPermission.getC2SIMHeader().getProtocol(), protocol)));
      }

      // Protocol version
      if (!protocolVersion.equals(requiredPermission.getC2SIMHeader().getProtocolVersion())) {
        checks.add(
            AuthorizationResult.create(
                AuthorizationResult.Code.UNAUTHORIZED,
                String.format(
                    "Protocol version value is '%s' where '%s' is required",
                    requiredPermission.getC2SIMHeader().getProtocolVersion(), protocolVersion)));
      }

      if (!checks.stream().allMatch(item -> item.code == AuthorizationResult.Code.AUTHORIZED)) {
        // There are authorization errors!
        var errors =
            checks.stream()
                .filter(x -> x.code != AuthorizationResult.Code.AUTHORIZED)
                .map(y -> y.message)
                .collect(Collectors.joining("\n"));
        return AuthorizationResult.create(
            AuthorizationResult.Code.UNAUTHORIZED, "C2SIM header:\n " + errors);
      }
      return AuthorizationResult.OK;
    } catch (Exception ex) {
      logger.error("authorizeMessageTypeBody failed", ex);
      return AuthorizationResult.create(
          AuthorizationResult.Code.UNAUTHORIZED,
          "Access denied: Internal error while processing authorizeMessageTypeBody:"
              + ex.getMessage());
    }
  }

  private static ELoxMessageType getMessageEnumType(MessageBodyType messageBody) {
    if (messageBody == null) {
      return ELoxMessageType.UNKNOWN;
    }
    if (messageBody.getDomainMessageBody() != null) {
      return ELoxMessageType.DOMAIN_MESSAGE;
    }
    if (messageBody.getC2SIMInitializationBody() != null) {
      return ELoxMessageType.C2SIM_INITIALIZATION;
    }
    if (messageBody.getObjectInitializationBody() != null) {
      return ELoxMessageType.OBJECT_INITIALIZATION;
    }
    if (messageBody.getSystemMessageBody() != null) {
      return ELoxMessageType.SYSTEM_MESSAGE;
    }
    if (messageBody.getSystemAcknowledgementBody() != null) {
      return ELoxMessageType.SYSTEM_ACKNOWLEDGEMENT;
    }
    return ELoxMessageType.UNKNOWN;
  }

  @Override
  public AuthorizationResult authorizeCommunicativeActTypeCode(
      CommunicativeActTypeCodeType requiredPermission) {
    if (requiredPermission == null) {
      return AuthorizationResult.OK;
    }
    var enumAsString = requiredPermission.value();
    return (c2SimClaims.getCommunicativeActTypeCode().getHasPermissionFor(enumAsString))
        ? AuthorizationResult.OK
        : AuthorizationResult.create(
            AuthorizationResult.Code.UNAUTHORIZED,
            getAccessDeniedMsg(
                C2SimClaims.COMMUNICATIVE_ACT_TYPE_CODE,
                c2SimClaims.getCommunicativeActTypeCode().toText(),
                requiredPermission.value()));
  }

  @Override
  public AuthorizationResult authorizeFromSendingSystem(String requiredPermission) {
    Objects.requireNonNull(requiredPermission);
    // Assume there is always one sender in the requiredPermission!
    return (c2SimClaims.getFromSendingSystem().getHasPermissionFor(requiredPermission))
        ? AuthorizationResult.OK
        : AuthorizationResult.create(
            AuthorizationResult.Code.UNAUTHORIZED,
            getAccessDeniedMsg(
                C2SimClaims.FROM_SENDING_SYSTEM,
                c2SimClaims.getFromSendingSystem().toText(),
                requiredPermission));
  }

  @Override
  public AuthorizationResult authorizeReplyToSystem(String requiredPermission) {
    Objects.requireNonNull(requiredPermission);
    return (c2SimClaims.getReplyToSystem().getHasPermissionFor(requiredPermission))
        ? AuthorizationResult.OK
        : AuthorizationResult.create(
            AuthorizationResult.Code.UNAUTHORIZED,
            getAccessDeniedMsg(
                C2SimClaims.REPLY_TO_SYSTEM,
                c2SimClaims.getReplyToSystem().toText(),
                requiredPermission));
  }

  @Override
  public AuthorizationResult authorizeSecurityClassificationCode(
      SecurityClassificationCodeType requiredPermission) {
    var enumAsText = requiredPermission != null ? requiredPermission.value() : "Unclassified";
    return (c2SimClaims.getSecurityClassificationCode().getHasPermissionFor(enumAsText))
        ? AuthorizationResult.OK
        : AuthorizationResult.create(
            AuthorizationResult.Code.UNAUTHORIZED,
            getAccessDeniedMsg(
                C2SimClaims.SECURITY_CLASSIFICATION_CODE,
                c2SimClaims.getSecurityClassificationCode().toText(),
                enumAsText));
  }

  @Override
  public AuthorizationResult authorizeToReceivingSystem(Set<String> requiredPermission) {
    Objects.requireNonNull(requiredPermission);

    return (c2SimClaims.getToReceivingSystem().getHasPermissionFor(requiredPermission))
        ? AuthorizationResult.OK
        : AuthorizationResult.create(
            AuthorizationResult.Code.UNAUTHORIZED,
            getAccessDeniedMsg(
                C2SimClaims.TO_RECEIVING_SYSTEM,
                c2SimClaims.getToReceivingSystem().toText(),
                String.join(",", requiredPermission)));
  }

  @Override
  public AuthorizationResult authorizeMessageType(ELoxMessageType requiredPermission) {
    Objects.requireNonNull(requiredPermission);
    var enumAsText = requiredPermission.getText();

    return (c2SimClaims.getMessageType().getHasPermissionFor(enumAsText))
        ? AuthorizationResult.OK
        : AuthorizationResult.create(
            AuthorizationResult.Code.UNAUTHORIZED,
            getAccessDeniedMsg(
                C2SimClaims.MESSAGE_TYPE,
                c2SimClaims.getMessageType().toText(),
                requiredPermission.getText()));
  }

  @Override
  public AuthorizationResult authorizeSystemMessageType(String command) {
    Objects.requireNonNull(command);
    if (c2SimClaims.getSystemMessageType() == null) {
      return new AuthorizationResult(
          AuthorizationResult.Code.UNAUTHORIZED, "Access to systemMessageType is not permitted");
    }

    if (c2SimClaims.getSystemMessageType().isEmpty()) {
      return AuthorizationResult.OK;
    }

    String systemMessageType = null;
    boolean siman = false;
    boolean simanresponse = false;
    boolean query = false;
    if (Arrays.stream(ESiman.values()).anyMatch(parameter -> parameter.name().equals(command))) {
      systemMessageType = "SIMAN";
      siman = true;
    }

    if (c2SimClaims.getSystemMessageType().contains(systemMessageType)) {
      return AuthorizationResult.OK;
    }

    if (Arrays.stream(ESimanResponse.values())
        .anyMatch(parameter -> parameter.name().equals(command))) {
      systemMessageType = "SIMAN_RESPONSE";
      simanresponse = true;
    }

    if (c2SimClaims.getSystemMessageType().contains(systemMessageType)) {
      return AuthorizationResult.OK;
    }

    if (Arrays.stream(EQuery.values()).anyMatch(parameter -> parameter.name().equals(command))) {
      systemMessageType = "QUERY";
      query = true;
    }

    if (c2SimClaims.getSystemMessageType().contains(systemMessageType)) {
      return AuthorizationResult.OK;
    }

    if (siman && query) {
      return new AuthorizationResult(
          AuthorizationResult.Code.UNAUTHORIZED,
          "Access to systemMessageType 'SIMAN' and 'QUERY' is not permitted");
    } else if (siman && simanresponse) {
      return new AuthorizationResult(
          AuthorizationResult.Code.UNAUTHORIZED,
          "Access to systemMessageType 'SIMAN' and 'SIMAN_RESPONSE' is not permitted");
    } else if (systemMessageType == null) {
      return new AuthorizationResult(
          AuthorizationResult.Code.UNAUTHORIZED, "Invalid systemMessageType command");
    } else {
      return new AuthorizationResult(
          AuthorizationResult.Code.UNAUTHORIZED,
          "Access to messageType '" + systemMessageType + "' is not permitted");
    }
  }
}
