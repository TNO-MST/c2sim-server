package org.c2sim.server.services;

import io.javalin.Javalin;
import org.c2sim.authorization.exceptions.AuthorisationException;
import org.c2sim.authorization.impl.C2SimClaimsBuilder;

/**
 * Service that owns and manages the Javalin HTTP server instance.
 *
 * <p>Provides access to the shared {@link ConfigService}, the underlying {@link Javalin} instance,
 * and a lazily-built {@link C2SimClaimsBuilder} for JWT/OIDC token validation.
 */
public interface WebService {

  /**
   * Returns the configuration service used by this web service.
   *
   * @return the configuration service
   */
  ConfigService getConfigService();

  /**
   * Returns the underlying {@link Javalin} instance.
   *
   * @return the Javalin application
   */
  Javalin getJavalin();

  /** Starts the Javalin HTTP server and begins accepting requests. */
  void serve();

  /** Stops the Javalin HTTP server. */
  void stop();

  /**
   * Returns the {@link C2SimClaimsBuilder} used to parse and validate Bearer tokens.
   *
   * @return the claims builder
   * @throws AuthorisationException if the builder cannot be constructed (e.g. invalid OIDC config)
   */
  C2SimClaimsBuilder getClaimsBuilder() throws AuthorisationException;
}
