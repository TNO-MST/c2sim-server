package org.c2sim.server.services;

/** Only needed for mocking system environment in JUNIT test */
public interface EnvService {
  String getEnv(String key);

  java.util.Map<String, String> getenv();
}
