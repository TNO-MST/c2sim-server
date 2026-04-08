package org.c2sim.server.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.c2sim.server.api.models.C2SimError;
import org.c2sim.server.services.C2SimService;
import org.c2sim.server.services.ConfigService;
import org.c2sim.server.services.MetricService;
import org.c2sim.server.services.WebSocketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultWebServiceTest {

  @Mock private ConfigService configService;

  @Mock private C2SimService c2SimService;

  @Mock private MetricService metricService;

  @Mock private WebSocketService webSocketService;

  private ObjectMapper mapper;

  private DefaultWebService webService;

  @BeforeEach
  void setUp() {
    mapper = new ObjectMapper();

    when(configService.getWebServerPortNumber()).thenReturn(0); // random port
    when(configService.getIsMetricsEnabled()).thenReturn(false);
    when(configService.getDocsDirectory()).thenReturn(null);
    // when(configService.getConfigEndpointIsExposed()).thenReturn(false);

    webService = new DefaultWebService(
        mapper, configService, c2SimService, metricService, webSocketService);
  }

  @Test
  void shouldReturnHealthStatus() {
    Javalin app = webService.getJavalin();
    JavalinTest.test(
        app,
        (server, client) -> {
          var response = client.get("/health");
          assertEquals(200, response.code());
          assertNotNull(response.body());
          assertTrue(response.body().string().contains("UP"));
        });
  }

  @Test
  @DisplayName("REST header must have clientId")
  void shouldFailWhenClientIdMissing() {
    Javalin app = webService.getJavalin();

    JavalinTest.test(
        app,
        (server, client) -> {
          var response = client.get("/api/test"); // any /api route
          assertEquals(400, response.code());
          assertNotNull(response.body());
          String json = response.body().string();
          assertNotNull(json);
          Gson gson = new Gson();
          C2SimError err = gson.fromJson(json, C2SimError.class);
          assertNotNull(err);
          assertEquals("NO_CLIENT_ID", err.getCode());
        });
  }

  @Test
  @DisplayName("OpenAPI spec endpoint test")
  void shouldReturnOpenApiYaml() {
    Javalin app = webService.getJavalin();

    JavalinTest.test(
        app,
        (server, client) -> {
          var response = client.get("/openapi.yaml");
          assertTrue(response.code() == 200);
        });
  }
}
