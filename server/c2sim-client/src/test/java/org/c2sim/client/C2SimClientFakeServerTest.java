package org.c2sim.client;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.c2sim.client.exceptions.C2SimRestException;
import org.c2sim.client.model.C2SimError;
import org.c2sim.client.model.StateType;
import org.c2sim.client.security.OidcHardCodedToken;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM Client library")
@Story("Test REST calls (with C2SIM server stub)")
class C2SimClientFakeServerTest {
  private MockWebServer server;

  @BeforeEach
  void setup() throws Exception {
    server = new MockWebServer();
    server.start();
  }

  @AfterEach
  void teardown() throws Exception {
    server.shutdown();
  }

  @DisplayName("REST get shared session from server, success (without bearer token)")
  @Test
  void shouldFetchSharedSessionsSuccess() throws Exception {

    server.enqueue(
        new MockResponse()
            .setResponseCode(HttpStatus.OK_200)
            .setBody(
                CreateJson.toJson(
                    CreateJson.createSessionsDummyData(StateType.UNINITIALIZED, "default"))));

    C2SimClient client =
        C2SimClient.create().url(server.url("/").toString()).systemName("TEST-SYSTEM").build();

    var sessions = client.getSharedSessionsFromC2SimServer();
    assertEquals(1, sessions.size());
  }

  @DisplayName("REST get shared session from server, fail (without bearer token)")
  @Test
  void shouldFetchSharedSessionsFail() {

    C2SimError errorResponse = new C2SimError();
    errorResponse.setCode("ERROR");
    errorResponse.setMessage("Error");
    server.enqueue(
        new MockResponse()
            .setResponseCode(HttpStatus.BAD_REQUEST_400)
            .setBody(errorResponse.toJson()));

    C2SimClient client =
        C2SimClient.create().url(server.url("/").toString()).systemName("TEST-SYSTEM").build();
    var error = assertThrows(C2SimRestException.class, client::getSharedSessionsFromC2SimServer);

    assertEquals("Error", error.getError().getMessage());
  }

  @DisplayName("REST get shared session from server (with bearer token)")
  @Test
  void shouldFetchSharedSessionsWithToken() throws Exception {
    server.enqueue(
        new MockResponse()
            .setResponseCode(HttpStatus.OK_200)
            .setBody(
                CreateJson.toJson(
                    CreateJson.createSessionsDummyData(StateType.UNINITIALIZED, "default"))));

    C2SimClient client =
        C2SimClient.create()
            .url(server.url("/").toString())
            .systemName("TEST-SYSTEM")
            .oidcProvider(new OidcHardCodedToken("THIS-IS-THE-TOKEN"))
            .build();

    var sessions = client.getSharedSessionsFromC2SimServer();
    assertEquals(1, sessions.size());
    RecordedRequest request = server.takeRequest();
    // Check if token is passed in header
    assertEquals("Bearer THIS-IS-THE-TOKEN", request.getHeader("Authorization"));
  }
}
