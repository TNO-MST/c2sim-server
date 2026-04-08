package org.c2sim.client_app;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.c2sim.lox.helpers.XmlFactoryHelper;
import org.c2sim.lox.helpers.builders.GeodeticCoordinateTypeBuilder;
import org.c2sim.lox.helpers.builders.PositionReportContentTypeBuilder;
import org.c2sim.lox.schema.MessageType;
import org.c2sim.lox.schema.OperationalStatusCodeType;

/**
 * Factory that creates randomised C2SIM position-report messages for load and integration testing.
 */
public class ReportCreator {

  private static final SecureRandom random = new SecureRandom();

  private ReportCreator() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Creates a C2SIM position-report message with randomised content.
   *
   * @param sender the C2SIM system name used in the message header
   * @param reportingActor the UUID of the actor being reported on
   * @param senderActor the UUID of the actor sending the report
   * @param numberOfReports the number of individual position reports to include
   * @return a fully populated {@link MessageType} ready for serialisation
   */
  public static MessageType create(
      String sender, UUID reportingActor, UUID senderActor, short numberOfReports) {

    List<PositionReportContentTypeBuilder> positionReports = new ArrayList<>();
    for (int i = 0; i <= numberOfReports; i++) {
      positionReports.add(createPositionReport());
    }

    return XmlFactoryHelper.createPositionReport(
        XmlFactoryHelper.createC2SimHeader(sender),
        reportingActor,
        senderActor,
        UUID.fromString("00000000-0000-0000-0000-000000000000"),
        positionReports);
  }

  private static PositionReportContentTypeBuilder createPositionReport() {

    return PositionReportContentTypeBuilder.create()
        .timeOfObservation(Instant.now())
        .operationalStatus(OperationalStatusCodeType.FULLY_OPERATIONAL)
        .strength(random.nextInt(101))
        .headingAngle((double) random.nextInt(361))
        .location(
            GeodeticCoordinateTypeBuilder.create(
                (double) random.nextInt((90 * 2) + 1) - 90,
                (double) random.nextInt((180 * 2) + 1) - 180,
                0.0f,
                GeodeticCoordinateTypeBuilder.EAltitude.ABOVE_GROUND_LEVEL))
        .speed((double) random.nextInt(101))
        .subjectEntity(UUID.randomUUID());
  }
}
