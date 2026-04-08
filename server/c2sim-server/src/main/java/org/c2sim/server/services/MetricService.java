package org.c2sim.server.services;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

/**
 * Provides application metrics using the OpenTelemetry standard.
 *
 * <p>IMPORTANT: keep number of tag combinations small (< 1000 combinations)
 */
public interface MetricService {

  /** Metric tag for invalid message */
  public enum MetricInvalidMsgReasonType {
    /** not compliant with schema */
    INVALID_FORMAT,
    /** client is not authorized */
    UNAUTHORIZED,
    /** client provided invalid request data to process the message */
    BAD_REQUEST,
    /** Other */
    OTHER
  }

  public enum MetricMsgType {
    /** C2SIM System messages */
    SYSTEM_MESSAGE,
    /** C2SIM report messages */
    REPORT,
    /** C2SIM order messages */
    ORDER,
    /** C2SIM report messages */
    INIT,
    /** C2SIM other messages */
    OTHER
  }

  /**
   * Returns the underlying Prometheus meter registry.
   *
   * @return the PrometheusMeterRegistry used to register and expose metrics
   */
  PrometheusMeterRegistry getRegistry();

  /**
   * Increments the counter tracking the number of messages sent by a specific client within a
   * shared session.
   *
   * @param sharedSessionName the name of the shared session
   * @param systemName the unique system identifier
   * @param msgType kind of message
   * @return the updated total number of messages sent by the system
   */
  long incValidMessagesSendByC2SimClient(
      String sharedSessionName, String systemName, MetricMsgType msgType);

  /**
   * Returns the current number of messages sent by a specific client within a given session.
   *
   * @param sharedSessionName the name of the shared session
   * @param systemName the unique system identifier
   * @param msgType kind of message
   * @return the total number of messages sent by the system
   */
  long getValidMessagesSendByC2SimClient(
      String sharedSessionName, String systemName, MetricMsgType msgType);

  /**
   * Increments the counter tracking the number of invalid messages sent by a specific system within
   * a shared session.
   *
   * @param sharedSessionName the name of the shared session
   * @param systemName the unique system identifier
   * @param errorType kind of error
   * @return the updated total number of failed messages
   */
  long incInvalidMessagesSendByC2SimClient(
      String sharedSessionName, String systemName, MetricInvalidMsgReasonType errorType);

  /**
   * Store the metrics for call duration of REST request
   *
   * @param method The VERB
   * @param route The routing (using matched route, so e.g. /users/{id}/info)
   * @param status The HTTP status code
   * @param durationNs Duration of call in nanoseconds
   */
  void requestDuration(String method, String route, String status, Long durationNs);

  /** Increase total http request */
  void incActiveHttpRequests();

  /** Decrease total http request */
  void decActiveHttpRequests();

  /**
   * @param sharedSessionName The shared session name
   * @param systemName The system name of C2SIM client
   * @param numberOfBytes C2SIM messages (XML) size (bytes)
   * @return new total bytes send
   */
  long incBytesSendByC2SimClient(String sharedSessionName, String systemName, long numberOfBytes);

  /**
   * This metrics is for all requests. E.g. invalid token The incInvalidMessagesSendByC2SimClient is
   * used when a C2SIM message could not be processed due to auth error
   *
   * @return the number of auth failures
   */
  long incAuthFailed();
}
