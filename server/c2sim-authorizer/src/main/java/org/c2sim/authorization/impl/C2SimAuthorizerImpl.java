package org.c2sim.authorization.impl;

import java.util.*;
import java.util.stream.Collectors;
import org.c2sim.authorization.interfaces.C2SimAuthorizer;
import org.c2sim.authorization.interfaces.C2SimClaims;
import org.c2sim.authorization.lox.enums.*;
import org.c2sim.lox.C2SimMsgKind;
import org.c2sim.lox.C2SimMsgKindCategory;
import org.c2sim.lox.C2SimMsgKindGroups;
import org.c2sim.lox.schema.C2SIMHeaderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.c2sim.authorization.lox.enums.ELoxSystemMessageType.*;

public record C2SimAuthorizerImpl(C2SimClaims c2SimClaims) implements C2SimAuthorizer {

  private static final Logger logger = LoggerFactory.getLogger(C2SimAuthorizerImpl.class);


  private static String getAccessDeniedMsg(
      String claimName, String allowedClaims, String requestedClaim) {
    return String.format(
        "Client C2SIM header attribute '%s' has value [%s], but claim '%s' only give permissions for [%s].",
            claimName, requestedClaim, claimName,
            allowedClaims);
  }

  private static String getAccessDeniedMessageTypeMsg(
          String claimName, String allowedClaims, String requestedClaim) {
    return String.format(
            "Client C2SIM message is of category [%s], but claim '%s' only give permissions for [%s].",
             requestedClaim, claimName,
             allowedClaims);
  }

  @Override
  public AuthorizationResult authorizeComleteMessage(
          C2SIMHeaderType header,
          C2SimMsgKind msgKind,
          C2SimMsgKindCategory msgKindCategory,
          String protocol,
          String protocolVersion) {
    if (header == null) {
      return AuthorizationResult.create(
          AuthorizationResult.Code.UNAUTHORIZED,
          "Access denied: No C2SIM header, nothing to authorize");
    }
    try {
      List<AuthorizationResult> checks = new ArrayList<>();

      // Check message type
      checks.add(authorizeMessageType(convertEnum(msgKindCategory)));

      // Check Sending System
      var fromSendingSystem = header.getFromSendingSystem();
      if (fromSendingSystem == null) {
        fromSendingSystem = "";
      }
      checks.add(authorizeFromSendingSystem(fromSendingSystem));

      // Check Reply to system
      var replyToSystem = header.getReplyToSystem();
      if (replyToSystem == null) {
        replyToSystem = "";
      }
      checks.add(authorizeReplyToSystem(replyToSystem));

      // Check to receiving system
      // receivingSystem = header.getToReceivingSystem();
      // if (receivingSystem == null) {
      //   receivingSystem = "";
      // }
      // checks.add(authorizeToReceivingSystem(receivingSystem));

      // Auth system message
      if (msgKind != null) {
        checks.add(authorizeSystemMessageType(msgKind));
      }

      // Protocol
      if (!protocol.equals(header.getProtocol())) {
        checks.add(
            AuthorizationResult.create(
                AuthorizationResult.Code.UNAUTHORIZED,
                String.format(
                    "Protocol value in C2SIM header is '%s' where '%s' is required",
                    header.getProtocol(), protocol)));
      }

      // Protocol version
      if (!protocolVersion.equals(header.getProtocolVersion())) {
        checks.add(
            AuthorizationResult.create(
                AuthorizationResult.Code.UNAUTHORIZED,
                String.format(
                    "Protocol version in C2SIM header is '%s' where '%s' is required",
                    header.getProtocolVersion(), protocolVersion)));
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

  // TODO Merge both types?
  private ELoxMessageType convertEnum(C2SimMsgKindCategory msgKindCategory) {
    switch(msgKindCategory) {
      case DOMAIN_MESSAGE -> {
        return ELoxMessageType.DOMAIN_MESSAGE;
      }
      case C2SIM_INITIALIZATION -> {
        return ELoxMessageType.C2SIM_INITIALIZATION;
      }
      case OBJECT_INITIALIZATION -> {
          return ELoxMessageType.OBJECT_INITIALIZATION;
      }
      case SYSTEM_ACKNOWLEDGEMENT -> {
        return ELoxMessageType.SYSTEM_ACKNOWLEDGEMENT;
      }
      case SYSTEM_MESSAGE -> {
        return ELoxMessageType.SYSTEM_MESSAGE;
      }
      default -> {
        return ELoxMessageType.UNKNOWN;
      }
    }
  }


  @Override
  public AuthorizationResult authorizeFromSendingSystem(String requestedPermission) {
    Objects.requireNonNull(requestedPermission);
    // Assume there is always one sender in the requiredPermission!
    return (c2SimClaims.getFromSendingSystem().getHasPermissionFor(requestedPermission))
        ? AuthorizationResult.OK
        : AuthorizationResult.create(
            AuthorizationResult.Code.UNAUTHORIZED,
            getAccessDeniedMsg(
                C2SimClaims.FROM_SENDING_SYSTEM,
                c2SimClaims.getFromSendingSystem().toText(),
                    requestedPermission));
  }

  @Override
  public AuthorizationResult authorizeReplyToSystem(String requestedPermission) {
    Objects.requireNonNull(requestedPermission);
    return (c2SimClaims.getReplyToSystem().getHasPermissionFor(requestedPermission))
        ? AuthorizationResult.OK
        : AuthorizationResult.create(
            AuthorizationResult.Code.UNAUTHORIZED,
            getAccessDeniedMsg(
                C2SimClaims.REPLY_TO_SYSTEM,
                c2SimClaims.getReplyToSystem().toText(),
                requestedPermission));
  }

  @Override
  public AuthorizationResult authorizeToReceivingSystem(Set<String> requestedPermission) {
    Objects.requireNonNull(requestedPermission);

    return (c2SimClaims.getToReceivingSystem().getHasPermissionFor(requestedPermission))
        ? AuthorizationResult.OK
        : AuthorizationResult.create(
            AuthorizationResult.Code.UNAUTHORIZED,
            getAccessDeniedMsg(
                C2SimClaims.TO_RECEIVING_SYSTEM,
                c2SimClaims.getToReceivingSystem().toText(),
                String.join(",", requestedPermission)));
  }

  @Override
  public AuthorizationResult authorizeMessageType(ELoxMessageType requiredPermission) {
    Objects.requireNonNull(requiredPermission);
    var enumAsText = requiredPermission.getText();

    return (c2SimClaims.getMessageType().getHasPermissionFor(enumAsText))
        ? AuthorizationResult.OK
        : AuthorizationResult.create(
            AuthorizationResult.Code.UNAUTHORIZED,
            getAccessDeniedMessageTypeMsg(
                C2SimClaims.MESSAGE_TYPE,
                c2SimClaims.getMessageType().toText(),
                requiredPermission.getText()));
  }



  @Override
  public AuthorizationResult authorizeSystemMessageType(C2SimMsgKind command) {

    // Are all system messages allowed?
    if (c2SimClaims.getSystemMessageType() == null || c2SimClaims.getSystemMessageType().getClaimIsAny()) {
      return AuthorizationResult.OK;
    }

    Set<ELoxSystemMessageType> belongsToGroups = new HashSet<>();

    // Check in what groups the command is in
    if (C2SimMsgKindGroups.isSimanMsgGroup(command)) {
      belongsToGroups.add(SIMAN);
    }
    if (C2SimMsgKindGroups.isSimanResponseMsgGroup(command)) {
      belongsToGroups.add(SIMAN_RESPONSE);
    }
    if  (C2SimMsgKindGroups.isQueryMsgGroup(command)) {
      belongsToGroups.add(QUERY);
    }

    // Is it a system message that is under control of the authorizer?
    if (belongsToGroups.isEmpty()) {
      logger.info("System message {} is in any groups, so it is allowed.", command);
      return AuthorizationResult.OK;
    }

    String groups = belongsToGroups.stream()
            .map(Enum::name)
            .collect(Collectors.joining(";"));

    logger.info("System message {} is part of group(s) '{}'.",
            command,
            groups);

    // Check if claim contains any of the groups
    for (ELoxSystemMessageType group : belongsToGroups) {
      if (c2SimClaims.getSystemMessageType().getHasPermissionFor(group.getText())) {
        return AuthorizationResult.OK;
      }
    }

    String groupsAsText =
             belongsToGroups.stream()
            .map(ELoxSystemMessageType::getText)
            .collect(Collectors.joining(", "));

    String errorMsg = String.format("SystemMessage '%s' belongs to group(s) [%s], " +
                    "claim '%s' only give permissions for groups [%s]",
            command.name(),
            groupsAsText,
            C2SimClaims.SYSTEM_MESSAGE_TYPE,
            c2SimClaims.getSystemMessageType().toText());
    return new AuthorizationResult(
            AuthorizationResult.Code.UNAUTHORIZED, errorMsg
    );



  }
}
