package org.c2sim.lox.helpers.builders;

import java.util.List;
import java.util.UUID;
import org.c2sim.lox.schema.ReportBodyType;

/**
 * Builder for {@link ReportBodyType} objects.
 *
 * <p>A report body requires {@code reportID}, {@code reportingEntity}, {@code fromSender}, {@code
 * toReceiver}, and at least one report content entry (position, observation, or task status) before
 * calling {@link #build()}.
 *
 * <p>A random {@code reportID} is generated at construction time and can be overridden via {@link
 * #reportID(UUID)}.
 */
public class ReportBodyTypeBuilder {
  private final ReportBodyType reportBody;

  private ReportBodyTypeBuilder() {

    this.reportBody = new ReportBodyType();
    this.reportBody.setReportID(UUID.randomUUID().toString());
  }

  /**
   * Creates a new builder with a random report ID.
   *
   * @return a new builder instance
   */
  public static ReportBodyTypeBuilder create() {
    return new ReportBodyTypeBuilder();
  }

  /**
   * Sets the sender of the report.
   *
   * @param fromSender the UUID of the sending system
   * @return this builder
   */
  public ReportBodyTypeBuilder fromSender(UUID fromSender) {
    reportBody.setFromSender(fromSender.toString());
    return this;
  }

  /**
   * Sets the receiver of the report.
   *
   * @param toReceiver the UUID of the receiving system
   * @return this builder
   */
  public ReportBodyTypeBuilder toReceiver(UUID toReceiver) {
    reportBody.setToReceiver(toReceiver.toString());
    return this;
  }

  /**
   * Overrides the report ID.
   *
   * @param reportID the UUID to use as the report ID
   * @return this builder
   */
  public ReportBodyTypeBuilder reportID(UUID reportID) {
    reportBody.setReportID(reportID.toString());
    return this;
  }

  /**
   * Sets the reporting entity.
   *
   * @param reportingEntity the UUID of the entity filing the report
   * @return this builder
   */
  public ReportBodyTypeBuilder reportingEntity(UUID reportingEntity) {
    reportBody.setReportingEntity(reportingEntity.toString());
    return this;
  }

  /**
   * Adds multiple observation report entries.
   *
   * @param observationReports the list of observation report builders
   * @return this builder
   */
  public ReportBodyTypeBuilder addObservationReports(
      List<ObservationReportContentTypeBuilder> observationReports) {
    for (ObservationReportContentTypeBuilder observationReport : observationReports) {
      addObservationReport(observationReport);
    }
    return this;
  }

  /**
   * Adds a single observation report entry.
   *
   * @param observationReport a builder for the observation report content
   * @return this builder
   */
  public ReportBodyTypeBuilder addObservationReport(
      ObservationReportContentTypeBuilder observationReport) {
    reportBody
        .getReportContent()
        .add(
            ReportContentTypeBuilder.create()
                .observationReportContent(observationReport.build())
                .build());
    return this;
  }

  /**
   * Adds multiple position report entries.
   *
   * @param positionReports the list of position report builders
   * @return this builder
   */
  public ReportBodyTypeBuilder addPositionReports(
      List<PositionReportContentTypeBuilder> positionReports) {
    for (PositionReportContentTypeBuilder positionReport : positionReports) {
      addPositionReport(positionReport);
    }
    return this;
  }

  /**
   * Adds a single position report entry.
   *
   * @param positionReport a builder for the position report content
   * @return this builder
   */
  public ReportBodyTypeBuilder addPositionReport(PositionReportContentTypeBuilder positionReport) {
    reportBody
        .getReportContent()
        .add(
            ReportContentTypeBuilder.create()
                .positionReportContent(positionReport.build())
                .build());
    return this;
  }

  /**
   * Adds a task-status report entry.
   *
   * @param taskStatusReport a builder for the task status
   * @return this builder
   */
  public ReportBodyTypeBuilder addTaskStatusReport(TaskStatusTypeBuilder taskStatusReport) {
    reportBody
        .getReportContent()
        .add(ReportContentTypeBuilder.create().taskStatus(taskStatusReport.build()).build());
    return this;
  }

  /**
   * Builds and returns the {@link ReportBodyType}.
   *
   * @return the constructed report body
   * @throws IllegalArgumentException if any required field is missing or the report content list is
   *     empty
   */
  public ReportBodyType build() throws IllegalArgumentException {
    if (reportBody.getReportID() == null) {
      throw new IllegalArgumentException("ReportID is null");
    }
    if (reportBody.getReportingEntity() == null) {
      throw new IllegalArgumentException("ReportingEntity is null");
    }
    if (reportBody.getFromSender() == null) {
      throw new IllegalArgumentException("FromSender is null");
    }
    if (reportBody.getToReceiver() == null) {
      throw new IllegalArgumentException("ToReceiver is null");
    }
    if (reportBody.getReportContent().isEmpty()) {
      throw new IllegalArgumentException("ReportContent is empty (add position/observation/task)");
    }

    return reportBody;
  }
}
