package org.c2sim.lox.helpers;

import java.util.function.Consumer;
import org.c2sim.lox.schema.MessageBodyType;
import org.c2sim.lox.schema.ReportBodyType;
import org.c2sim.lox.schema.TaskStatusType;

/** Utility methods for extracting typed report content from {@link ReportBodyType} messages. */
public class ReportBodyTypeHelper {

  // Prevent instantiation
  private ReportBodyTypeHelper() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Invokes {@code consumer} for each {@link TaskStatusType} found inside the domain-message report
   * body of the given {@link MessageBodyType}.
   *
   * <p>Does nothing when {@code msg}, its domain-message body, or its report body is {@code null}.
   *
   * @param msg the message body to inspect; may be {@code null}
   * @param consumer the callback invoked for every task-status report entry
   */
  public static void getTaskStatusReports(
      final MessageBodyType msg, final Consumer<TaskStatusType> consumer) {
    if ((msg != null)
        && (msg.getDomainMessageBody() != null)
        && (msg.getDomainMessageBody().getReportBody() != null)) {
      getTaskStatusReports(msg.getDomainMessageBody().getReportBody(), consumer);
    }
  }

  /**
   * Invokes {@code consumer} for each {@link TaskStatusType} found in the given {@link
   * ReportBodyType}.
   *
   * <p>Does nothing when {@code msg} is {@code null}.
   *
   * @param msg the report body to inspect; may be {@code null}
   * @param consumer the callback invoked for every task-status report entry
   */
  public static void getTaskStatusReports(
      final ReportBodyType msg, final Consumer<TaskStatusType> consumer) {
    if (msg != null) {
      msg.getReportContent().stream()
          .filter(x -> x.getTaskStatus() != null)
          .forEach(y -> consumer.accept(y.getTaskStatus()));
    }
  }
}
