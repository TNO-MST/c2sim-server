package org.c2sim.server.services.impl;

import static org.mockito.Mockito.when;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.io.IOException;
import java.util.Map;
import org.c2sim.server.security.EAuthLevel;
import org.c2sim.server.services.EnvService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@Epic("C2SIM Server")
@Feature("C2SIM Server module")
@Story("Config service")
class DefaultConfigServiceTest {
  DefaultConfigService configService;

  @BeforeEach
  void setup() {
    EnvService envService = Mockito.mock(EnvService.class);
    Map<String, String> map =
        Map.of(
            "C2SIM_BEARER_PUBLIC_KEY", "XXX",
            "C2SIM_AUTH_MODE", "MIXED_AUTH",
            "C2SIM_EXPOSE_CFG_ENDPOINT", "true",
            "C2SIM_EXTERNAL_HOSTNAME", "localhost",
            "C2SIM_EXTERNAL_PORT_NUMBER", "1234",
            "C2SIM_MAX_MSG_SIZE_MB", "5678",
            "C2SIM_IDENTITY_PROVIDER_URL", "http://localhost:8080");
    when(envService.getenv()).thenReturn(map);
    configService = new DefaultConfigService(envService);
  }

  @Test
  @Description("Config getBearerPublicKey")
  void getBearerPublicKey() {
    Assertions.assertEquals("XXX", configService.getBearerPublicKey());
  }

  @Test
  @Description("Config getAuthMode")
  void getAuthMode() {
    Assertions.assertEquals(EAuthLevel.MIXED_AUTH, configService.getAuthMode());
  }

  @Test
  @Description("Config getConfigEndpointIsExposed")
  void getConfigEndpointIsExposed() {
    Assertions.assertTrue(configService.getConfigEndpointIsExposed());
  }

  @Test
  @Description("Config getExternalHostname")
  void getExternalHostname() {
    Assertions.assertEquals("localhost", configService.getExternalHostname());
  }

  @Test
  @Description("Config getIdentityProviderUrl")
  void getIdentityProviderUrl() {
    Assertions.assertEquals("http://localhost:8080", configService.getIdentityProviderUrl());
  }

  @Test
  @Description("Config getExternalPort")
  void getExternalPort() {
    Assertions.assertEquals(1234, configService.getExternalPort());
  }

  @Test
  @Description("Config getWebServerPortNumber")
  void getWebServerPortNumber() {
    Assertions.assertEquals(7777, configService.getWebServerPortNumber());
  }

  @Test
  @Description("Config getShowLicenceInConsole")
  void getShowLicenceInConsole() {
    Assertions.assertTrue(configService.getShowLicenceInConsole());
  }

  @Test
  @Description("Config getMaxC2SimMessageSizeInMb")
  void getMaxC2SimMessageSizeInMb() {
    Assertions.assertEquals(5678, configService.getMaxC2SimMessageSizeInMb());
  }

  @Test
  void getServerConfig() throws IOException {
    var cfg = configService.getServerConfiguration();

    Assertions.assertNotNull(cfg);
    Assertions.assertFalse(cfg.getDefaultSessions().isEmpty());
  }
}
