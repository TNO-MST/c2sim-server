package org.c2sim.lox.helpers.builders;

import java.time.Instant;
import java.util.UUID;
import org.c2sim.lox.schema.*;

/**
 * Builder for {@link OrderBodyType} objects.
 *
 * <p>The fields {@code fromSender}, {@code toReceiver}, {@code issuedTime}, and {@code orderID} are
 * all required before calling {@link #build()}.
 */
public class OrderBodyTypeBuilder {

  private final OrderBodyType order = new OrderBodyType();

  /**
   * Creates a new, empty builder.
   *
   * @return a new builder instance
   */
  public static OrderBodyTypeBuilder create() {
    return new OrderBodyTypeBuilder();
  }

  /**
   * Sets the sender of the order.
   *
   * @param sender the UUID of the sending system
   * @return this builder
   */
  public OrderBodyTypeBuilder fromSender(UUID sender) {
    order.setFromSender(sender.toString());
    return this;
  }

  /**
   * Sets the receiver of the order.
   *
   * @param receiver the UUID of the receiving system
   * @return this builder
   */
  public OrderBodyTypeBuilder toReceiver(UUID receiver) {
    order.setToReceiver(receiver.toString());
    return this;
  }

  /**
   * Adds an entity to the order.
   *
   * @param entity the entity to include
   * @return this builder
   */
  public OrderBodyTypeBuilder addEntity(EntityType entity) {
    order.getEntity().add(entity);
    return this;
  }

  /**
   * Sets the issued time from an {@link Instant}.
   *
   * @param dateTime the point in time when the order was issued
   * @return this builder
   */
  public OrderBodyTypeBuilder issuedTime(Instant dateTime) {
    return issuedTime(DateTimeTypeBuilder.create(dateTime));
  }

  /**
   * Sets the issued time from a {@link DateTimeTypeBuilder}.
   *
   * @param dateTime a builder whose {@link DateTimeTypeBuilder#build()} result is used
   * @return this builder
   */
  public OrderBodyTypeBuilder issuedTime(DateTimeTypeBuilder dateTime) {
    order.setIssuedTime(dateTime.build());
    return this;
  }

  /**
   * Sets the order ID.
   *
   * @param id the UUID to assign to this order
   * @return this builder
   */
  public OrderBodyTypeBuilder orderId(UUID id) {
    order.setOrderID(id.toString());
    return this;
  }

  /**
   * Sets the requesting entity.
   *
   * @param id the UUID of the entity requesting the order
   * @return this builder
   */
  public OrderBodyTypeBuilder requestingEntity(UUID id) {
    order.setRequestingEntity(id.toString());
    return this;
  }

  /**
   * Adds a maneuver warfare task from a {@link ManeuverWarfareTaskTypeBuilder}.
   *
   * @param maneuver a builder whose {@link ManeuverWarfareTaskTypeBuilder#build()} result is used
   * @return this builder
   */
  public OrderBodyTypeBuilder addManeuverWarfareTask(ManeuverWarfareTaskTypeBuilder maneuver) {
    return addManeuverWarfareTask(maneuver.build());
  }

  /**
   * Adds a maneuver warfare task directly.
   *
   * @param maneuver the task to include
   * @return this builder
   */
  public OrderBodyTypeBuilder addManeuverWarfareTask(ManeuverWarfareTaskType maneuver) {
    var task = new TaskType();
    task.setManeuverWarfareTask(maneuver);
    order.getTask().add(task);
    return this;
  }

  /**
   * Adds a task reference UUID (only if not already present).
   *
   * @param id the UUID of the referenced task
   * @return this builder
   */
  public OrderBodyTypeBuilder addTaskReference(UUID id) {
    var idText = id.toString();
    if (!order.getTaskReference().contains(idText)) {
      order.getTaskReference().add(idText);
    }
    return this;
  }

  /**
   * Builds and returns the {@link OrderBodyType}.
   *
   * @return the constructed order body
   * @throws IllegalStateException if any required field ({@code fromSender}, {@code toReceiver},
   *     {@code issuedTime}, or {@code orderID}) is missing
   */
  public OrderBodyType build() {
    if (order.getFromSender() == null) {
      throw new IllegalStateException("fromSender is required");
    }
    if (order.getToReceiver() == null) {
      throw new IllegalStateException("toReceiver is required");
    }
    if (order.getIssuedTime() == null) {
      throw new IllegalStateException("IssuedTime is required");
    }
    if (order.getOrderID() == null) {
      throw new IllegalStateException("OrderID is required");
    }
    return order;
  }
}
