package org.c2sim.lox.helpers.builders;

import org.c2sim.lox.schema.C2SIMHeaderType;
import org.c2sim.lox.schema.MessageBodyType;
import org.c2sim.lox.schema.MessageType;

/**
 * Builder for top-level {@link MessageType} objects.
 *
 * <p>A complete C2SIM message consists of a {@link C2SIMHeaderType} and a {@link MessageBodyType}.
 */
public class MessageTypeBuilder {

  private final MessageType instance = new MessageType();

  /**
   * Creates a new, empty builder.
   *
   * @return a new builder instance
   */
  public static MessageTypeBuilder create() {
    return new MessageTypeBuilder();
  }

  /**
   * Sets the C2SIM header.
   *
   * @param c2SIMHeader the header to attach
   * @return this builder
   */
  public MessageTypeBuilder c2SIMHeader(C2SIMHeaderType c2SIMHeader) {
    instance.setC2SIMHeader(c2SIMHeader);
    return this;
  }

  /**
   * Sets the message body from a {@link MessageBodyTypeBuilder}.
   *
   * @param messageBody a builder whose {@link MessageBodyTypeBuilder#build()} result is used
   * @return this builder
   */
  public MessageTypeBuilder messageBody(MessageBodyTypeBuilder messageBody) {
    messageBody(messageBody.build());
    return this;
  }

  /**
   * Sets the message body directly.
   *
   * @param messageBody the message body to attach
   * @return this builder
   */
  public MessageTypeBuilder messageBody(MessageBodyType messageBody) {
    instance.setMessageBody(messageBody);
    return this;
  }

  /**
   * Builds and returns the {@link MessageType}.
   *
   * @return the constructed message
   */
  public MessageType build() {
    return instance;
  }
}
