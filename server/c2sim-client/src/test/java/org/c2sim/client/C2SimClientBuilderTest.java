package org.c2sim.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.net.URI;
import org.c2sim.client.security.OidcTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM Client library")
@Story("Builder pattern")
class C2SimClientBuilderTest {

  @Test
  @DisplayName("Builder must fail when C2SIM URL is missing")
  void buildFailsWithoutUrl() {
    C2SimClient.Builder builder = new C2SimClient.Builder().systemName("Sys");
    assertThrows(IllegalArgumentException.class, builder::build);
  }

  @Test
  @DisplayName("Build fails when systemName is missing")
  void buildFailsWithoutSystemName() {
    C2SimClient.Builder builder = new C2SimClient.Builder().url("http://localhost:8080/api");
    assertThrows(IllegalArgumentException.class, builder::build);
  }

  @Test
  @DisplayName("Mock listener is stored & not executed during build")
  void listenerMockIsStoredNotInvoked() {
    C2SimClient.C2SimClientListener listener = mock(C2SimClient.C2SimClientListener.class);

    C2SimClient client =
        new C2SimClient.Builder()
            .url("http://localhost:8080/api")
            .systemName("Sys")
            .listener(listener)
            .build();

    assertNotNull(client);
    // Ensure builder does NOT call listener during build
    verifyNoInteractions(listener);
  }

  @Test
  @DisplayName("OIDC provider mock is stored correctly")
  void oidcProviderMockStoredCorrectly() {
    OidcTokenProvider provider = mock(OidcTokenProvider.class);

    C2SimClient client =
        new C2SimClient.Builder()
            .url(URI.create("http://server"))
            .systemName("Sys1")
            .oidcProvider(provider)
            .build();
    assertNotNull(client);
    assertNotNull(client.getOidcTokenProvider());
    verifyNoInteractions(provider); // builder should not call it yet
  }

  @Test
  @DisplayName("Flags set on builder create correct client state")
  void flagsArePropagatedCorrectly() {
    C2SimClient.C2SimClientListener listener = mock(C2SimClient.C2SimClientListener.class);
    OidcTokenProvider provider = mock(OidcTokenProvider.class);

    C2SimClient client =
        new C2SimClient.Builder()
            .url("http://localhost:8080/api")
            .systemName("SystemX")
            .clientId("123456790")
            .clientIdDisplayName("SystemX-client")
            .sharedSessionName("sessionA")
            .enableReceivedMessageDecode()
            .enableReceivedMessageValidation()
            .enableSendMessageValidation()
            .beautifyXml()
            .oidcProvider(provider)
            .listener(listener)
            .build();

    assertEquals("http://localhost:8080/api", client.getBasePathUrl());
    assertEquals("SystemX", client.getSystemName());
    assertEquals("123456790", client.getClientId());
    assertEquals("SystemX-client", client.getClientIdDisplayName());
    assertEquals("sessionA", client.getSharedSessionName());

    // Flags
    assertTrue(client.isReceivedMsgDecodeEnabled());
    assertTrue(client.isReceivedMsgValidationEnabled());
    assertTrue(client.isSendMsgValidationEnabled());
    assertTrue(client.isBeautifySendEnabled());

    // Objects
    assertEquals(provider, client.getOidcTokenProvider());
    assertEquals(listener, client.getC2SimClientListener());
  }

  @Test
  @DisplayName("Verify disabling flags works")
  void disablingFlagsWorks() {
    C2SimClient client =
        new C2SimClient.Builder()
            .url("http://server")
            .systemName("System")
            .disableReceivedMessageDecode()
            .disableReceivedMessageValidation()
            .build();

    assertFalse(client.isReceivedMsgDecodeEnabled());
    assertFalse(client.isReceivedMsgValidationEnabled());
  }
}
