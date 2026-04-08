package org.c2sim.authorizer;

import static org.c2sim.authorization.impl.C2SimClaimsBuilder.createWithKeycloakConfiguration;
import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import org.c2sim.authorization.exceptions.AuthorisationException;
import org.c2sim.authorization.utils.ResourceHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM Auth module")
@Story("OIDC config")
class TestOidcConfig {

  private static final int OPENID_SERVER_PORT = 40000;
  private static final String REALM = "hla";
  private static final String OIDC_CFG_PATH =
      String.format("/realms/%s/.well-known/openid-configuration", REALM);
  private static final String OIDC_CERTS_PATH =
      String.format("/realms/%s/protocol/openid-connect/certs", REALM);

  private static final String OPENID_PROVIDER =
      String.format("http://localhost:%d", OPENID_SERVER_PORT);

  private static byte[] oidcConfigResponse;
  private static byte[] oidcCertsResponse;

  private static String testJwtToken;

  private static HttpServer server;

  // Example certs url: http://xxxxx:32123/realms/hla/protocol/openid-connect/certs

  @BeforeAll
  static void startServer() throws IOException {
    oidcConfigResponse = prepareConfigResponse();
    oidcCertsResponse =
        ResourceHelper.readResourceAsString(TestOidcConfig.class, "/openid-jwks-certs")
            .getBytes(StandardCharsets.UTF_8);

    testJwtToken =
        ResourceHelper.readUntilFirstSpace(
            ResourceHelper.readResourceAsString(TestOidcConfig.class, "/jwt-token-pep.txt"));

    System.out.println("Mock OIDC server on port " + OPENID_SERVER_PORT);
    server = HttpServer.create(new InetSocketAddress(OPENID_SERVER_PORT), 0);
    server.createContext(
        "/realms",
        exchange -> {
          String path = exchange.getRequestURI().getPath();
          System.out.println("Mock OIDC server: incoming request: " + path);

          if (path.equals(OIDC_CFG_PATH)) {
            System.out.println("Mock OIDC server: return OIDC config to client (200)");
            exchange.sendResponseHeaders(200, oidcConfigResponse.length);
            try (OutputStream os = exchange.getResponseBody()) {
              os.write(oidcConfigResponse);
            }
          } else if (path.equals(OIDC_CERTS_PATH)) {
            System.out.println("Mock OIDC server: return OIDC certs to client (200)");
            exchange.sendResponseHeaders(200, oidcCertsResponse.length);
            try (OutputStream os = exchange.getResponseBody()) {
              os.write(oidcCertsResponse);
            }
          } else {
            String notFound = "Mock OIDC server: endpoint not found";
            exchange.sendResponseHeaders(404, notFound.length());
            try (OutputStream os = exchange.getResponseBody()) {
              os.write(notFound.getBytes());
            }
          }
        });
    server.setExecutor(null); // creates a default executor
    server.start();
    System.out.println("Mock OIDC server listening on port " + OPENID_SERVER_PORT);
  }

  @AfterAll
  static void stopServer() {
    server.stop(0);
    System.out.println("Mock server stopped");
  }

  private static byte[] prepareConfigResponse() throws IOException {
    var responseOrg = ResourceHelper.readResourceAsString(TestOidcConfig.class, "/openid-config");
    var certsUrl = OPENID_PROVIDER + OIDC_CERTS_PATH;
    System.out.println("Certificate information can be downloaded from: " + certsUrl);
    return responseOrg.replace("<<REPLACE_JWK_URI>>", certsUrl).getBytes(StandardCharsets.UTF_8);
  }

  @Test
  void testWrongAudience() throws MalformedURLException, AuthorisationException {

    URL url = URI.create(OPENID_PROVIDER + OIDC_CFG_PATH).toURL();
    var builder = createWithKeycloakConfiguration(url).addAudience("non-existing");

    Exception exception =
        assertThrows(AuthorisationException.class, () -> builder.build(testJwtToken));
    assertNotNull(exception, "Wrong audience should throw exception");
  }

  @Test
  void testJwtValidationDisabled() throws MalformedURLException, AuthorisationException {

    // This should throw 'aud' AuthorisationException when validation is not disabled
    createWithKeycloakConfiguration(URI.create(OPENID_PROVIDER + OIDC_CFG_PATH).toURL())
        .addAudience("non-existing")
        .disableValidation()
        .build(testJwtToken);
    assertTrue(true, "No exception was thrown!");
  }

  @Test
  void testDownloadOidcConfig() throws AuthorisationException, IOException {

    var claimsBuilder =
        createWithKeycloakConfiguration(URI.create(OPENID_PROVIDER + OIDC_CFG_PATH).toURL())
            .addAudience("c2sim")
            .disableValidation() // TODO -> for now disable validation
            .build(testJwtToken);

    // ASSERT Claim fromSendingSystem
    var expectedFromSendingSystems = List.of("LOX_NLD");
    assertEquals(
        new HashSet<>(claimsBuilder.getFromSendingSystem()),
        new HashSet<>(expectedFromSendingSystems),
        String.format(
            "Claim 'fromSendingSystem' : Expected [%s], got [%s]",
            String.join(",", expectedFromSendingSystems),
            String.join(",", claimsBuilder.getFromSendingSystem())));

    // ASSERT Claim messageType
    // ASSERT Claim securityClassificationCode
    // ASSERT Claim communicativeActTypeCode
    // ASSERT Claim toReceivingSystem
    // ASSERT Claim systemMessageType

  }
}
