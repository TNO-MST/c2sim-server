package org.c2sim.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import org.c2sim.server.services.*;
import org.c2sim.server.services.impl.*;
import org.c2sim.server.utils.C2SimObjectMapper;

// Google guice DI framework (https://github.com/google/guice)

/**
 * Google Guice dependency-injection module that binds the C2SIM server service interfaces to their
 * default singleton implementations.
 *
 * <p>Bindings:
 *
 * <ul>
 *   <li>{@link ConfigService} → {@link DefaultConfigService}
 *   <li>{@link C2SimSchemaService} → {@link DefaultC2SimSchemaService}
 *   <li>{@link C2SimService} → {@link DefaultC2SimService}
 *   <li>{@link WebService} → {@link DefaultWebService}
 *   <li>{@link WebSocketService} → {@link DefaultWebSocketService}
 *   <li>{@link MetricService} → {@link DefaultMetricService}
 * </ul>
 *
 * <p>Also provides the shared {@link ObjectMapper} instance via {@link #provideObjectMapper()}.
 */
public class DefaultModule extends AbstractModule {

  /** Dependency injection controller for C2SIm server */
  public DefaultModule() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  protected void configure() {
    bind(EnvService.class).to(DefaultEnvService.class).in(Scopes.SINGLETON);
    bind(ConfigService.class).to(DefaultConfigService.class).in(Scopes.SINGLETON);
    bind(C2SimSchemaService.class).to(DefaultC2SimSchemaService.class).in(Scopes.SINGLETON);
    bind(C2SimService.class).to(DefaultC2SimService.class).in(Scopes.SINGLETON);
    bind(WebService.class).to(DefaultWebService.class).in(Scopes.SINGLETON);
    bind(WebSocketService.class).to(DefaultWebSocketService.class).in(Scopes.SINGLETON);
    bind(MetricService.class).to(DefaultMetricService.class).in(Scopes.SINGLETON);
  }

  /**
   * Provides the shared {@link ObjectMapper} instance configured for C2SIM JSON serialization.
   *
   * @return the application-wide {@link ObjectMapper}
   */
  @Provides
  ObjectMapper provideObjectMapper() {
    // Configure the ObjectMapper if needed (e.g., register modules, set properties)
    return C2SimObjectMapper.mapper;
  }
}
