package org.c2sim.lox.helpers.builders;

import org.c2sim.lox.schema.*;

/**
 * Builder for {@link DomainMessageBodyType} objects.
 *
 * <p>{@link DomainMessageBodyType} is a choice type; only one body kind may be set at a time. Each
 * setter calls {@code clearAll()} before setting the new value to enforce the XSD choice
 * constraint.
 */
public class DomainMessageBodyTypeBuilder {
  private final DomainMessageBodyType instance = new DomainMessageBodyType();

  /**
   * Creates a new, empty builder.
   *
   * @return a new builder instance
   */
  public static DomainMessageBodyTypeBuilder create() {
    return new DomainMessageBodyTypeBuilder();
  }

  /**
   * Sets the acknowledgement body (clears any previously set body kind).
   *
   * @param acknowledgementBody the acknowledgement body
   * @return this builder
   */
  public DomainMessageBodyTypeBuilder acknowledgementBody(
      AcknowledgementBodyType acknowledgementBody) {
    clearAll();
    instance.setAcknowledgementBody(acknowledgementBody);
    return this;
  }

  /**
   * Sets the order body (clears any previously set body kind).
   *
   * @param orderBody the order body
   * @return this builder
   */
  public DomainMessageBodyTypeBuilder orderBody(OrderBodyType orderBody) {
    clearAll();
    instance.setOrderBody(orderBody);
    return this;
  }

  /**
   * Sets the plan body (clears any previously set body kind).
   *
   * @param planBody the plan body
   * @return this builder
   */
  public DomainMessageBodyTypeBuilder planBody(PlanBodyType planBody) {
    clearAll();
    instance.setPlanBody(planBody);
    return this;
  }

  /**
   * Sets the report body from a {@link ReportBodyTypeBuilder} (clears any previously set body
   * kind).
   *
   * @param reportBody a builder whose {@link ReportBodyTypeBuilder#build()} result is used
   * @return this builder
   */
  public DomainMessageBodyTypeBuilder reportBody(ReportBodyTypeBuilder reportBody) {
    reportBody(reportBody.build());
    return this;
  }

  /**
   * Sets the report body (clears any previously set body kind).
   *
   * @param reportBody the report body
   * @return this builder
   */
  public DomainMessageBodyTypeBuilder reportBody(ReportBodyType reportBody) {
    clearAll();
    instance.setReportBody(reportBody);
    return this;
  }

  /**
   * Sets the request body (clears any previously set body kind).
   *
   * @param requestBody the request body
   * @return this builder
   */
  public DomainMessageBodyTypeBuilder requestBody(RequestBodyType requestBody) {
    clearAll();
    instance.setRequestBody(requestBody);
    return this;
  }

  private void clearAll() {
    instance.setRequestBody(null);
    instance.setReportBody(null);
    instance.setPlanBody(null);
    instance.setOrderBody(null);
    instance.setAcknowledgementBody(null);
  }

  /**
   * Builds and returns the {@link DomainMessageBodyType}.
   *
   * @return the constructed domain message body
   */
  public DomainMessageBodyType build() {
    return instance;
  }
}
