package org.c2sim.lox.helpers.builders;

import org.c2sim.lox.schema.*;

/**
 * Builder for {@link MessageBodyType} objects.
 *
 * <p>{@link MessageBodyType} is a choice type; only one body kind may be set at a time. Each setter
 * calls {@code clearAll()} before setting the new value to enforce the XSD choice constraint.
 */
public class MessageBodyTypeBuilder {
  private final MessageBodyType instance = new MessageBodyType();

  /**
   * Creates a new, empty builder.
   *
   * @return a new builder instance
   */
  public static MessageBodyTypeBuilder create() {
    return new MessageBodyTypeBuilder();
  }

  /**
   * Sets the C2SIM initialization body (clears any previously set body kind).
   *
   * @param body the initialization body
   * @return this builder
   */
  public MessageBodyTypeBuilder c2SIMInitializationBody(C2SIMInitializationBodyType body) {
    clearAll();
    instance.setC2SIMInitializationBody(body);
    return this;
  }

  /**
   * Sets the domain message body from a {@link DomainMessageBodyTypeBuilder} (clears any previously
   * set body kind).
   *
   * @param body a builder whose {@link DomainMessageBodyTypeBuilder#build()} result is used
   * @return this builder
   */
  public MessageBodyTypeBuilder domainMessageBody(DomainMessageBodyTypeBuilder body) {
    domainMessageBody(body.build());
    return this;
  }

  /**
   * Sets the domain message body (clears any previously set body kind).
   *
   * @param body the domain message body
   * @return this builder
   */
  public MessageBodyTypeBuilder domainMessageBody(DomainMessageBodyType body) {
    clearAll();
    instance.setDomainMessageBody(body);
    return this;
  }

  /**
   * Sets the object initialization body (clears any previously set body kind).
   *
   * @param body the object initialization body
   * @return this builder
   */
  public MessageBodyTypeBuilder objectInitializationBody(ObjectInitializationBodyType body) {
    clearAll();
    instance.setObjectInitializationBody(body);
    return this;
  }

  /**
   * Sets the system acknowledgement body (clears any previously set body kind).
   *
   * @param body the system acknowledgement body
   * @return this builder
   */
  public MessageBodyTypeBuilder systemAcknowledgementBody(SystemAcknowledgementBodyType body) {
    clearAll();
    instance.setSystemAcknowledgementBody(body);
    return this;
  }

  /**
   * Sets the system message body (clears any previously set body kind).
   *
   * @param body the system message body
   * @return this builder
   */
  public MessageBodyTypeBuilder systemMessageBody(SystemMessageBodyType body) {
    clearAll();
    instance.setSystemMessageBody(body);
    return this;
  }

  private void clearAll() {
    instance.setSystemMessageBody(null);
    instance.setSystemAcknowledgementBody(null);
    instance.setObjectInitializationBody(null);
    instance.setDomainMessageBody(null);
    instance.setC2SIMInitializationBody(null);
  }

  /**
   * Builds and returns the {@link MessageBodyType}.
   *
   * @return the constructed message body
   */
  public MessageBodyType build() {
    return instance;
  }
}
