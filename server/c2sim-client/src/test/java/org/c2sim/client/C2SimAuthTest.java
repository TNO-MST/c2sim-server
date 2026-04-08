package org.c2sim.client;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nimbusds.jwt.SignedJWT;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.c2sim.client.exceptions.C2SimRestAuthException;
import org.c2sim.client.exceptions.C2SimRestException;
import org.c2sim.client.invoker.ApiException;
import org.c2sim.client.model.DynamicSessionInfo;
import org.c2sim.client.security.OidcCredentialFlow;
import org.c2sim.client.security.OidcCredentialFlowConfig;
import org.c2sim.client.security.OidcHardCodedToken;
import org.c2sim.client.security.OidcTokenProvider;
import org.junit.jupiter.api.*;

@Epic("C2SIM Server")
@Feature("C2SIM Client library")
@Story("Authorization")
class C2SimAuthTest {

  private MockWebServer mockC2SimRestServer;

  @BeforeEach
  void setUp() throws Exception {
    mockC2SimRestServer = new MockWebServer();
    mockC2SimRestServer.start();
  }

  @AfterEach
  void tearDown() throws Exception {
    if (mockC2SimRestServer != null) {
      mockC2SimRestServer.shutdown();
    }
  }

  @Test
  void offlineKeycloakIdpServer() {
    var cfg =
        new OidcCredentialFlowConfig(
            URI.create("http://hostdown:1234/realms/c2sim"),
            "c2sim-client",
            "RQOwgVBNzS3frhpLqoFatVJ2xTyPQBDV");

    OidcTokenProvider oidcProvider = new OidcCredentialFlow(cfg);
    var client =
        new C2SimClient.Builder()
            .url("http://localhost/empty") // Dont't care: test doesn't connect to C2SIM server
            .systemName("DUMMY")
            .clientIdDisplayName("IGNORE")
            .oidcProvider(oidcProvider)
            .build();
    ApiException exception =
        assertThrows(ApiException.class, client::getSharedSessionsFromC2SimServer);

    assertInstanceOf(C2SimRestAuthException.class, exception.getCause());
    assertEquals(
        C2SimRestAuthException.ErrorType.ENDPOINT_DISCOVERY_ACCESS_TOKEN_FAILED,
        ((C2SimRestAuthException) exception.getCause()).getErrorType());
  }

  @Test
  void shouldIncludeBearerTokenTest()
      throws C2SimRestException, ApiException, InterruptedException {
    String token =
        "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJiLUJ5RGZrdG5YQV9jbjdKc1VnRW5nSGV3eTdmMkJZNTcxRVZiTnc5YUFnIn0.eyJleHAiOjE3NzA4MzM5ODYsImlhdCI6MTc3MDgzMjE4NiwianRpIjoiNjk3ODNiYzEtMWY3OC00YzgwLWE5MWUtZTliYTBjMmY3NTA4IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9jMnNpbSIsInN1YiI6ImQxNGFkZjg3LWI4ODItNGU5OS1hZWFmLWIyZTZkMGFlOGExMCIsInR5cCI6IkJlYXJlciIsImF6cCI6ImMyc2ltLWNsaWVudCIsInNjb3BlIjoiYzJzaW0iLCJzZWN1cml0eUNsYXNzaWZpY2F0aW9uQ29kZSI6IlVOQ0xBU1NJRklFRCIsImNsaWVudEhvc3QiOiIxNzIuMTkuMC4xIiwibWVzc2FnZVR5cGUiOiJPUkRFUiIsImNsaWVudEFkZHJlc3MiOiIxNzIuMTkuMC4xIiwiZnJvbVNlbmRpbmdTeXN0ZW0iOiJDMlNJTSIsImNsaWVudF9pZCI6ImMyc2ltLWNsaWVudCJ9.mNSRBvznVqVNbdJevhTsHxD0wnC-0OwQ0yu1IGmJEZPxZJmEfoF4BIyU6YX3Lqs2ACktqXPz3rDsGNm9zBR2qFURF7w7Sf75lsVqn5aWCfon-Y7J3hQiCpyvrQf1KqpegOB5Fd1v5xRWGkiceO66f300o9jxdiKc36UArfY3SbnXwhZF9_bzMCt8aP3ntitekAXhoB0cQeL-Ke6EEj4MZfp5vJ909Oa05gX_9cadPl6TalLEn2ZWH92gALPK_MgTuBgmCyvPUxA2iWQgegTsgfMo66m6F9nvBYs2ySvw8iEATucPTWTaDbndzV9KodDgXN5CYBFOXC0nhxQHiGTGBA";
    OidcTokenProvider oidcProvider = new OidcHardCodedToken(token);
    var client =
        new C2SimClient.Builder()
            .url(mockC2SimRestServer.url("/").toString())
            .systemName("DUMMY")
            .clientIdDisplayName("IGNORE")
            .oidcProvider(oidcProvider)
            .build();

    // The REST server should return this on first request (don't care what is returned)
    var dummyResponse = new ArrayList<DynamicSessionInfo>();
    mockC2SimRestServer.enqueue(
        new MockResponse().setResponseCode(200).setBody(new Gson().toJson(dummyResponse)));
    client.getSharedSessionsFromC2SimServer();
    RecordedRequest request = mockC2SimRestServer.takeRequest();
    assertEquals(
        "Bearer " + token,
        request.getHeader("Authorization"),
        "Bearer token was not add to REST header");
  }

  @Disabled("There must be local IDP provider locally")
  @Test
  void shouldIncludeBearerTokenLiveKeycloakTest()
      throws C2SimRestException, ApiException, InterruptedException, ParseException {
    var cfg =
        new OidcCredentialFlowConfig(
            URI.create("http://localhost:8080/realms/c2sim"),
            "c2sim-client",
            "RQOwgVBNzS3frhpLqoFatVJ2xTyPQBDV");

    OidcTokenProvider oidcProvider = new OidcCredentialFlow(cfg);
    var client =
        new C2SimClient.Builder()
            .url(mockC2SimRestServer.url("/").toString())
            .systemName("DUMMY")
            .clientIdDisplayName("IGNORE")
            .oidcProvider(oidcProvider)
            .build();

    // The REST server should return this on first request
    var dummyResponse = new ArrayList<DynamicSessionInfo>();
    mockC2SimRestServer.enqueue(
        new MockResponse().setResponseCode(200).setBody(new Gson().toJson(dummyResponse)));
    client.getSharedSessionsFromC2SimServer();
    RecordedRequest request = mockC2SimRestServer.takeRequest();
    var bearerToken = request.getHeader("Authorization");
    assertNotNull(bearerToken);
    assertTrue(bearerToken.startsWith("Bearer "));
    var jwt = bearerToken.replace("Bearer ", "");
    SignedJWT signedJWT = SignedJWT.parse(jwt);

    // Get claims as JSON object
    var claimsSet = signedJWT.getJWTClaimsSet();

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    String json = gson.toJson(claimsSet);
    Assertions.assertNotNull(json);
  }
}
