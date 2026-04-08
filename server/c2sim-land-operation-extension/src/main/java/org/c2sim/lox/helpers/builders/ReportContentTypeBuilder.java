package org.c2sim.lox.helpers.builders;

import org.c2sim.lox.schema.ObservationReportContentType;
import org.c2sim.lox.schema.PositionReportContentType;
import org.c2sim.lox.schema.ReportContentType;
import org.c2sim.lox.schema.TaskStatusType;

/**
 * Builder for {@link ReportContentType} objects.
 *
 * <p>{@link ReportContentType} is a choice type; exactly one of the available content kinds
 * (observation report, position report, or task status) should be set.
 */
public class ReportContentTypeBuilder {

  private final ReportContentType reportContent;

  private ReportContentTypeBuilder() {
    this.reportContent = new ReportContentType();
  }

  /**
   * Creates a new, empty builder.
   *
   * @return a new builder instance
   */
  public static ReportContentTypeBuilder create() {
    return new ReportContentTypeBuilder();
  }

  /**
   * Sets the observation report content.
   *
   * @param content the observation report content to include
   * @return this builder
   */
  public ReportContentTypeBuilder observationReportContent(ObservationReportContentType content) {
    reportContent.setObservationReportContent(content);
    return this;
  }

  /**
   * Sets the position report content.
   *
   * @param content the position report content to include
   * @return this builder
   */
  public ReportContentTypeBuilder positionReportContent(PositionReportContentType content) {
    reportContent.setPositionReportContent(content);
    return this;
  }

  /**
   * Sets the task status.
   *
   * @param status the task status to include
   * @return this builder
   */
  public ReportContentTypeBuilder taskStatus(TaskStatusType status) {
    reportContent.setTaskStatus(status);
    return this;
  }

  /**
   * Builds and returns the {@link ReportContentType}.
   *
   * @return the constructed report content
   */
  public ReportContentType build() {
    return reportContent;
  }
}
