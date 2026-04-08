package org.c2sim.lox.helpers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.c2sim.lox.Global;
import org.c2sim.lox.helpers.builders.*;
import org.c2sim.lox.schema.*;

/**
 * Factory methods for creating fully-populated C2SIM {@link MessageType} objects ready for
 * transmission.
 *
 * <p>This class composes the lower-level builder types ({@link MessageTypeBuilder}, {@link
 * MessageBodyTypeBuilder}, {@link SystemMessageBodyTypeBuilder}, etc.) into domain-specific
 * convenience factories, covering the most common C2SIM message types: scenario lifecycle commands,
 * position reports, task-status reports, orders, and {@code MagicMove} commands.
 */
public final class XmlFactoryHelper {

  // Prevent instantiation
  private XmlFactoryHelper() {
    throw new AssertionError("Only static functions!");
  }

  /**
   * Creates a {@link C2SIMHeaderType} pre-populated with a random message ID and conversation ID,
   * the global protocol identifiers, security classification {@code UNCLASSIFIED}, and the current
   * UTC sending time.
   *
   * @param senderSystemName the name of the sending system (used for both {@code FromSendingSystem}
   *     and {@code ToReceivingSystem})
   * @return a new, fully-populated {@link C2SIMHeaderType}
   */
  public static C2SIMHeaderType createC2SimHeader(String senderSystemName) {
    var header = new C2SIMHeaderType();
    header.setMessageID(UUID.randomUUID().toString());
    header.setProtocol(Global.C2SIM_PROTOCOL);
    header.setProtocolVersion(Global.C2SIM_PROTOCOL_VERSION);
    header.setSecurityClassificationCode(SecurityClassificationCodeType.UNCLASSIFIED);
    header.setInReplyToMessageID(null);
    header.setReplyToSystem(null);
    header.setConversationID(UUID.randomUUID().toString());
    header.setCommunicativeActTypeCode(CommunicativeActTypeCodeType.ACCEPT);

    header.setSendingTime(DateTimeTypeHelper.createDateTimeType(Instant.now()));
    header.setAuthorizationHeader(null);
    header.setFromSendingSystem(senderSystemName);
    header.setToReceivingSystem(senderSystemName);
    return header;
  }

  /**
   * Creates a {@code MagicMove} {@link MessageType} that teleports the entity identified by {@code
   * reference} to the given geodetic position.
   *
   * @param header the C2SIM message header
   * @param reference the UUID of the entity to move
   * @param latitude latitude in decimal degrees
   * @param longitude longitude in decimal degrees
   * @param altitude altitude value
   * @param alt the altitude type qualifier
   * @return the complete {@code MagicMove} message
   */
  public static MessageType createMagicMove(
      C2SIMHeaderType header,
      UUID reference,
      double latitude,
      double longitude,
      double altitude,
      GeodeticCoordinateTypeBuilder.EAltitude alt) {

    return MessageTypeBuilder.create()
        .c2SIMHeader(header)
        .messageBody(
            MessageBodyTypeBuilder.create()
                .systemMessageBody(
                    SystemMessageBodyTypeBuilder.createMagicMove(
                        MagicMoveTypeBuilder.create()
                            .entityReference(reference)
                            .location(latitude, longitude, altitude, alt))))
        .build();
  }

  /**
   * Creates a {@link ManeuverWarfareTaskType} for the given entity with the given action code.
   *
   * @param performingEntity the UUID of the entity that will perform the task
   * @param code the action to perform
   * @return a new {@link ManeuverWarfareTaskType} with a random UUID
   */
  public static ManeuverWarfareTaskType createManeuverWarfareTask(
      UUID performingEntity, TaskActionCodeType code) {
    var task = new ManeuverWarfareTaskType();
    // Optional task.getActionTemporalRelationship()
    // Optional task.getLocation()
    // Optional task.getMapGraphicID()
    task.setName(null);
    task.setUUID(UUID.randomUUID().toString());
    // Optional task.getAffectedEntity()
    // optional task.getDesiredEffectCode()
    task.setDuration(null);
    task.setEndTime(null);
    task.setPerformingEntity(performingEntity.toString());
    task.setStartTime(null);
    task.setTaskActionCode(code);
    // Optional task.getRuleOfEngagement()
    // Optional task.getTaskFunctionalRelation()
    return task;
  }

  /**
   * Creates a {@code C2SIMInitialization} message and serializes it to an XML string.
   *
   * @param header the C2SIM message header
   * @param systemName the name of the system being initialized
   * @param beautifyXml {@code true} to produce indented (pretty-printed) XML
   * @return the XML representation of the initialization message, or an empty string on error
   */
  // Create XML string for C2SIMInitialization message
  public static String createC2SIMInitialization(
      C2SIMHeaderType header, String systemName, boolean beautifyXml) {
    return MessageTypeHelper.writeMessage(
        createC2SIMInitialization(header, systemName), false, beautifyXml, "");
  }

  /**
   * Creates a {@code C2SIMInitialization} {@link MessageType} for the given system name.
   *
   * @param header the C2SIM message header
   * @param systemName the name of the system being initialized
   * @return the complete initialization message
   */
  // Create POJO for C2SIMInitialization message
  public static MessageType createC2SIMInitialization(C2SIMHeaderType header, String systemName) {
    return createC2SIMInitialization(
        header, C2SIMInitializationBodyTypeBuilder.create(systemName).build());
  }

  /**
   * Creates a {@code C2SIMInitialization} {@link MessageType} wrapping the given body.
   *
   * @param header the C2SIM message header
   * @param initialize the pre-built initialization body
   * @return the complete initialization message
   */
  public static MessageType createC2SIMInitialization(
      C2SIMHeaderType header, C2SIMInitializationBodyType initialize) {
    return MessageTypeBuilder.create()
        .c2SIMHeader(header)
        .messageBody(MessageBodyTypeBuilder.create().c2SIMInitializationBody(initialize))
        .build();
  }

  private static MessageType createCmd(C2SIMHeaderType header, SystemMessageBodyType cmd) {
    return MessageTypeBuilder.create()
        .c2SIMHeader(header)
        .messageBody(MessageBodyTypeBuilder.create().systemMessageBody(cmd))
        .build();
  }

  /**
   * Serializes a {@code StartScenario} command to an XML string.
   *
   * @param header the C2SIM message header
   * @param beautifyXml {@code true} to produce pretty-printed XML
   * @return the XML string, or an empty string on error
   */
  public static String createStartScenario(C2SIMHeaderType header, boolean beautifyXml) {
    return MessageTypeHelper.writeMessage(createStartScenario(header), false, beautifyXml, "");
  }

  /**
   * Creates a {@code StartScenario} {@link MessageType}.
   *
   * @param header the C2SIM message header
   * @return the complete {@code StartScenario} message
   */
  public static MessageType createStartScenario(C2SIMHeaderType header) {
    return createCmd(header, SystemMessageBodyTypeBuilder.createStartScenario());
  }

  /**
   * Serializes a {@code StopScenario} command to an XML string.
   *
   * @param header the C2SIM message header
   * @param beautifyXml {@code true} to produce pretty-printed XML
   * @return the XML string, or an empty string on error
   */
  public static String createStopScenario(C2SIMHeaderType header, boolean beautifyXml) {
    return MessageTypeHelper.writeMessage(createStopScenario(header), false, beautifyXml, "");
  }

  /**
   * Creates a {@code StopScenario} {@link MessageType}.
   *
   * @param header the C2SIM message header
   * @return the complete {@code StopScenario} message
   */
  public static MessageType createStopScenario(C2SIMHeaderType header) {
    return createCmd(header, SystemMessageBodyTypeBuilder.createStopScenario());
  }

  /**
   * Serializes a {@code ShareScenario} command to an XML string.
   *
   * @param header the C2SIM message header
   * @param beautifyXml {@code true} to produce pretty-printed XML
   * @return the XML string, or an empty string on error
   */
  public static String createShareScenario(C2SIMHeaderType header, boolean beautifyXml) {
    return MessageTypeHelper.writeMessage(createShareScenario(header), false, beautifyXml, "");
  }

  /**
   * Creates a {@code ShareScenario} {@link MessageType}.
   *
   * @param header the C2SIM message header
   * @return the complete {@code ShareScenario} message
   */
  public static MessageType createShareScenario(C2SIMHeaderType header) {
    return createCmd(header, SystemMessageBodyTypeBuilder.createShareScenario());
  }

  /**
   * Creates a {@code PauseScenario} {@link MessageType}.
   *
   * @param header the C2SIM message header
   * @return the complete {@code PauseScenario} message
   */
  public static MessageType createPauseScenario(C2SIMHeaderType header) {
    return createCmd(header, SystemMessageBodyTypeBuilder.createPauseScenario());
  }

  /**
   * Creates a {@code ResumeScenario} {@link MessageType}.
   *
   * @param header the C2SIM message header
   * @return the complete {@code ResumeScenario} message
   */
  public static MessageType createResumeScenario(C2SIMHeaderType header) {
    return createCmd(header, SystemMessageBodyTypeBuilder.createResumeScenario());
  }

  /**
   * Creates a {@code SetSimulationRealtimeMultiple} {@link MessageType} with the given speed
   * factor.
   *
   * @param header the C2SIM message header
   * @param factor the simulation speed multiplier (e.g. {@code 2.0} for double speed)
   * @return the complete {@code SetSimulationRealtimeMultiple} message
   */
  public static MessageType createSimulationRealtimeMultiple(
      C2SIMHeaderType header, double factor) {
    return createCmd(header, SystemMessageBodyTypeBuilder.createSimulationRealtimeMultiple(factor));
  }

  /**
   * Serializes a {@code ResetScenario} command to an XML string.
   *
   * @param header the C2SIM message header
   * @param beautifyXml {@code true} to produce pretty-printed XML
   * @return the XML string, or an empty string on error
   */
  public static String createReset(C2SIMHeaderType header, boolean beautifyXml) {
    return MessageTypeHelper.writeMessage(createReset(header), false, beautifyXml, "");
  }

  /**
   * Creates a {@code ResetScenario} {@link MessageType}.
   *
   * @param header the C2SIM message header
   * @return the complete {@code ResetScenario} message
   */
  public static MessageType createReset(C2SIMHeaderType header) {
    return createCmd(header, SystemMessageBodyTypeBuilder.createReset());
  }

  /**
   * Creates a {@code SubmitInitialization} {@link MessageType}.
   *
   * @param header the C2SIM message header
   * @return the complete {@code SubmitInitialization} message
   */
  public static MessageType create(C2SIMHeaderType header) {
    return createCmd(header, SystemMessageBodyTypeBuilder.createSubmitInitialization());
  }

  /**
   * Serializes a {@code SubmitInitialization} command to an XML string.
   *
   * @param header the C2SIM message header
   * @param beautifyXml {@code true} to produce pretty-printed XML
   * @return the XML string, or an empty string on error
   */
  public static String createSubmitInitialization(C2SIMHeaderType header, boolean beautifyXml) {
    return MessageTypeHelper.writeMessage(
        createSubmitInitialization(header), false, beautifyXml, "");
  }

  /**
   * Creates a {@code SubmitInitialization} {@link MessageType}.
   *
   * @param header the C2SIM message header
   * @return the complete {@code SubmitInitialization} message
   */
  public static MessageType createSubmitInitialization(C2SIMHeaderType header) {
    return createCmd(header, SystemMessageBodyTypeBuilder.createSubmitInitialization());
  }

  /**
   * Serializes an {@code InitializationComplete} command to an XML string.
   *
   * @param header the C2SIM message header
   * @param beautifyXml {@code true} to produce pretty-printed XML
   * @return the XML string, or an empty string on error
   */
  public static String createInitializationComplete(C2SIMHeaderType header, boolean beautifyXml) {
    return MessageTypeHelper.writeMessage(
        createInitializationComplete(header), false, beautifyXml, "");
  }

  /**
   * Creates an {@code InitializationComplete} {@link MessageType}.
   *
   * @param header the C2SIM message header
   * @return the complete {@code InitializationComplete} message
   */
  public static MessageType createInitializationComplete(C2SIMHeaderType header) {
    return createCmd(header, SystemMessageBodyTypeBuilder.createInitializationComplete());
  }

  /**
   * Creates a position-report {@link MessageType} and serializes it to an XML string.
   *
   * @param header the C2SIM message header
   * @param reportingEntity the UUID of the entity filing the report
   * @param fromSender the UUID of the sending system
   * @param toReceiver the UUID of the receiving system
   * @param positionReports the list of position-report content builders
   * @param beautifyXml {@code true} to produce pretty-printed XML
   * @return the XML string, or an empty string on error
   */
  public static String createPositionReport(
      C2SIMHeaderType header,
      UUID reportingEntity,
      UUID fromSender,
      UUID toReceiver,
      List<PositionReportContentTypeBuilder> positionReports,
      boolean beautifyXml) {
    return MessageTypeHelper.writeMessage(
        createPositionReport(header, reportingEntity, fromSender, toReceiver, positionReports),
        false,
        beautifyXml,
        "");
  }

  /**
   * Creates a task-status-report {@link MessageType}.
   *
   * @param header the C2SIM message header
   * @param reportingEntity the UUID of the entity filing the report
   * @param fromSender the UUID of the sending system
   * @param toReceiver the UUID of the receiving system
   * @param taskId the UUID of the task being reported on
   * @param status the current status of the task
   * @return the complete task-status-report message
   */
  public static MessageType createTaskStatusReport(
      C2SIMHeaderType header,
      UUID reportingEntity,
      UUID fromSender,
      UUID toReceiver,
      UUID taskId,
      TaskStatusCodeType status) {
    ReportBodyTypeBuilder report = createReport(reportingEntity, fromSender, toReceiver);
    report.addTaskStatusReport(
        TaskStatusTypeBuilder.create()
            .currentTask(taskId)
            .timeOfObservation(Instant.now())
            .taskStatusCode(status));
    return MessageTypeBuilder.create()
        .c2SIMHeader(header)
        .messageBody(
            MessageBodyTypeBuilder.create()
                .domainMessageBody(DomainMessageBodyTypeBuilder.create().reportBody(report)))
        .build();
  }

  /**
   * Creates a position-report {@link MessageType}.
   *
   * @param header the C2SIM message header
   * @param reportingEntity the UUID of the entity filing the report
   * @param fromSender the UUID of the sending system
   * @param toReceiver the UUID of the receiving system
   * @param positionReports the list of position-report content builders
   * @return the complete position-report message
   */
  public static MessageType createPositionReport(
      C2SIMHeaderType header,
      UUID reportingEntity,
      UUID fromSender,
      UUID toReceiver,
      List<PositionReportContentTypeBuilder> positionReports) {
    ReportBodyTypeBuilder report = createReport(reportingEntity, fromSender, toReceiver);
    report.addPositionReports(positionReports);
    return MessageTypeBuilder.create()
        .c2SIMHeader(header)
        .messageBody(
            MessageBodyTypeBuilder.create()
                .domainMessageBody(DomainMessageBodyTypeBuilder.create().reportBody(report)))
        .build();
  }

  private static ReportBodyTypeBuilder createReport(
      UUID reportingEntity, UUID fromSender, UUID toReceiver) {
    return ReportBodyTypeBuilder.create()
        .reportID(UUID.randomUUID())
        .reportingEntity(reportingEntity)
        .fromSender(fromSender)
        .toReceiver(toReceiver);
  }

  /**
   * Creates a maneuver-warfare-task order {@link MessageType} directing {@code performingEntity} to
   * move to the given geodetic location.
   *
   * @param header the C2SIM message header
   * @param performingEntity the UUID of the entity that will execute the task
   * @param geodetic the target geodetic coordinate
   * @return the complete order message containing the maneuver-warfare task
   */
  public static MessageType createManeuverWarfareTaskMsg(
      C2SIMHeaderType header, UUID performingEntity, GeodeticCoordinateType geodetic) {
    var taskManeuverWarfare =
        ManeuverWarfareTaskTypeBuilder.create()
            .uuid(UUID.randomUUID())
            .performingEntity(performingEntity)
            .taskActionCode(TaskActionCodeType.MOVE_TO_LOCATION /* DEFEND SCOUT ARTTACK */)
            .addLocation(geodetic)
            .build();

    return MessageTypeBuilder.create()
        .c2SIMHeader(header)
        .messageBody(
            MessageBodyTypeBuilder.create()
                .domainMessageBody(
                    DomainMessageBodyTypeBuilder.create()
                        .orderBody(
                            OrderBodyTypeBuilder.create()
                                .orderId(UUID.randomUUID())
                                .fromSender(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                                .toReceiver(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                                .issuedTime(Instant.now())
                                .addManeuverWarfareTask(taskManeuverWarfare)
                                .build())))
        .build();
  }
}
