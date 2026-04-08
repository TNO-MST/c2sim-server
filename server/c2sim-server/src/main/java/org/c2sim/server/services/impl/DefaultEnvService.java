package org.c2sim.server.services.impl;

import org.c2sim.server.services.EnvService;

public class DefaultEnvService implements EnvService {
  @Override
  public String getEnv(String key) {
    return System.getenv(key);
  }

  public java.util.Map<String, String> getenv() {
    return System.getenv();
  }
}
