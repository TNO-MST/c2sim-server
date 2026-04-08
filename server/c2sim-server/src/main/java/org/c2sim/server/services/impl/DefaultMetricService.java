package org.c2sim.server.services.impl;

import com.google.inject.Inject;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.c2sim.server.services.MetricService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Provides access to PrometheusMeterRegistry */
public class DefaultMetricService implements MetricService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultMetricService.class);

  // Metric names
  // Convention:
  // - <namespace>.<metric_name>.<unit>
  // - <system>.<object>.<metric>.<unit>
  // Counter = count; Histogram = duration; UpDownCounter = usage; Gauge = utilization
  // lowercase and dot-separated

  private static final String METRIC_NAME_MSG_VALID_TOTAL = "c2sim.client.msg_valid.count";
  private static final String METRIC_NAME_MSG_INVALID_TOTAL = "c2sim.client.msg_invalid.count";
  private static final String METRIC_NAME_REQUEST_DURATION = "c2sim.request.duration";
  private static final String METRIC_NAME_HTTP_ACTIVE_REQUEST = "c2sim.server.active_requests";
  private static final String METRIC_NAME_BYTES_SEND = "c2sim.client.msg_bytes_sent";
  private static final String METRIC_NAME_AUTH_FAILED_TOTAL = "c2sim.request_auth_failed.count";

  // Metric attributes (also called labels / tags)
  // lowercase and dot-separated
  private static final String METRIC_ATTRIB_APPLICATION = "c2sim.app";
  private static final String METRIC_ATTRIB_SHARED_SESSION_NAME = "c2sim.shared_session";
  private static final String METRIC_ATTRIB_SYSTEM_NAME = "c2sim.system_name";
  private static final String METRIC_ATTRIB_MSG_KIND = "c2sim.msg_kind";
  private static final String METRIC_ATTRIB_ERROR_KIND = "c2sim.error_kind";
  private static final String METRIC_ATTRIB_REQUEST_METHOD = "c2sim.request.method";
  private static final String METRIC_ATTRIB_REQUEST_ENDPOINT = "c2sim.request.endpoint";
  private static final String METRIC_ATTRIB_REQUEST_STATUS = "c2sim.request.status";

  private final PrometheusMeterRegistry registry =
      new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

  private final AtomicInteger activeRequests;

  @Inject
  public DefaultMetricService() {
    logger.info("Metric Service Started");
    // Add tag application=c2sim-server
    registry.config().commonTags(METRIC_ATTRIB_APPLICATION, "c2sim-server");

    activeRequests = registry.gauge(METRIC_NAME_HTTP_ACTIVE_REQUEST, new AtomicInteger(0));
  }

  /** {@inheritDoc} */
  @Override
  public void incActiveHttpRequests() {
    activeRequests.incrementAndGet();
  }

  /** {@inheritDoc} */
  @Override
  public void decActiveHttpRequests() {
    activeRequests.decrementAndGet();
  }

  /** {@inheritDoc} */
  @Override
  public PrometheusMeterRegistry getRegistry() {
    return registry;
  }

  /** {@inheritDoc} */
  @Override
  public long incValidMessagesSendByC2SimClient(
      String sharedSessionName, String systemName, MetricMsgType msgType) {
    Counter counter =
        Counter.builder(METRIC_NAME_MSG_VALID_TOTAL)
            .description(
                "Number of C2SIM messages send by system (C2SIM client) for a shared session")
            .tag(METRIC_ATTRIB_SHARED_SESSION_NAME, sharedSessionName)
            .tag(METRIC_ATTRIB_SYSTEM_NAME, systemName)
            .tag(METRIC_ATTRIB_MSG_KIND, msgType.toString())
            .register(registry);
    counter.increment();
    return (long) counter.count();
  }

  /** {@inheritDoc} */
  @Override
  public long getValidMessagesSendByC2SimClient(
      String sharedSessionName, String systemName, MetricMsgType msgType) {
    var entry =
        registry
            .find(METRIC_NAME_MSG_VALID_TOTAL)
            .tags(
                METRIC_ATTRIB_SHARED_SESSION_NAME, sharedSessionName,
                METRIC_ATTRIB_SYSTEM_NAME, systemName,
                METRIC_ATTRIB_ERROR_KIND, msgType.toString())
            .counter();
    return (entry != null) ? (long) entry.count() : (long) -1;
  }

  /** {@inheritDoc} */
  @Override
  public long incInvalidMessagesSendByC2SimClient(
      String sharedSessionName, String systemName, MetricInvalidMsgReasonType errorType) {
    Counter counter =
        Counter.builder(METRIC_NAME_MSG_INVALID_TOTAL)
            .description(
                "Number of invalid C2SIM messages send by system (C2SIM client) for a shared session")
            .tag(METRIC_ATTRIB_SHARED_SESSION_NAME, sharedSessionName)
            .tag(METRIC_ATTRIB_SYSTEM_NAME, systemName)
            .tag(METRIC_ATTRIB_ERROR_KIND, errorType.toString())
            .register(registry);
    counter.increment();
    return (long) counter.count();
  }

  /** {@inheritDoc} */
  @Override
  public long incAuthFailed() {
    Counter counter =
        Counter.builder(METRIC_NAME_AUTH_FAILED_TOTAL)
            .description("Authorization failed (invalid token)")
            .register(registry);
    counter.increment();
    return (long) counter.count();
  }

  /** {@inheritDoc} */
  @Override
  public void requestDuration(String method, String route, String status, Long durationNs) {

    Timer.builder(METRIC_NAME_REQUEST_DURATION)
        .description("C2SIM HTTP request duration for all endpoints")
        .tags(
            METRIC_ATTRIB_REQUEST_METHOD, method,
            METRIC_ATTRIB_REQUEST_ENDPOINT, route,
            METRIC_ATTRIB_REQUEST_STATUS, status)
        .publishPercentileHistogram()
        .publishPercentiles(0.5, 0.95, 0.99) // Max duration for 50%, 95% and 99% calls
        .register(registry)
        .record(durationNs, TimeUnit.NANOSECONDS);
  }

  /** {@inheritDoc} */
  @Override
  public long incBytesSendByC2SimClient(
      String sharedSessionName, String systemName, long numberOfBytes) {
    Counter counter =
        Counter.builder(METRIC_NAME_BYTES_SEND)
            .description(
                "Total size in bytes received (C2SIM XML MSG) by C2SIM server from C2SIM client (system)")
            .tag(METRIC_ATTRIB_SHARED_SESSION_NAME, sharedSessionName)
            .tag(METRIC_ATTRIB_SYSTEM_NAME, systemName)
            .register(registry);
    counter.increment(numberOfBytes);
    return (long) counter.count();
  }
}
