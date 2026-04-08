package org.c2sim.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import io.javalin.testtools.JavalinTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.UUID;
import org.c2sim.client.api.SessionApi;
import org.c2sim.server.api.models.DynamicSessionInfo;
import org.c2sim.server.api.models.SessionInfo;
import org.c2sim.server.api.models.StateType;
import org.c2sim.server.services.C2SimService;
import org.c2sim.server.services.ConfigService;
import org.c2sim.server.services.MetricService;
import org.c2sim.server.services.WebSocketService;
import org.c2sim.server.services.impl.DefaultWebService;
import org.c2sim.server.utils.C2SimObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@Epic("C2SIM Server")
@Feature("C2SIM Client library")
@Story("Client test")
class C2SimClientTest {

  @Test
  void testRestClient() {
    String clientId = UUID.randomUUID().toString();

    var mapper = C2SimObjectMapper.mapper;

    var configService = Mockito.mock(ConfigService.class);
    var c2SimService = Mockito.mock(C2SimService.class);
    var metricService = Mockito.mock(MetricService.class);
    var webSocketService = Mockito.mock(WebSocketService.class);
    var webService = new DefaultWebService(
        mapper, configService, c2SimService, metricService, webSocketService);

    when(configService.getWebServerPortNumber()).thenReturn(7777);
    var x = new ArrayList<DynamicSessionInfo>();
    var item =
        new DynamicSessionInfo(
            "xx",
            OffsetDateTime.now(),
            new SessionInfo("1.02", "ok", "default"),
            StateType.INITIALIZED);

    x.add(item);

    when(c2SimService.getSessionsFromServer()).thenReturn(x);
    JavalinTest.test(
        webService.getJavalin(),
        (javalin, ignoredHttpClient) -> {
          var apiClient = new org.c2sim.client.invoker.ApiClient();

          apiClient.setBasePath("http://localhost:" + javalin.port() + "/api");

          final SessionApi api = new SessionApi(apiClient);

          var res = api.getSessionsWithHttpInfo(clientId);

          assertEquals(200, res.getStatusCode());
        });
  }
}
