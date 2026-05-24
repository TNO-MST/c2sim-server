package org.c2sim.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.c2sim.server.api.models.RequestJoinSession;
import org.c2sim.server.exceptions.C2SimException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM Server module")
@Story("Joining C2SIM shared session")
class SharedSessionTest extends BaseTest {
  @Test
  void joinSharedSessionTwice() {
    var sharedSession = getSession();

    sharedSession.joinSharedSession(
        CLIENT_ID_A,
        "joining A",
        new RequestJoinSession(CLIENT_ID_A_SYSTEM_NAME, CLIENT_ID_A_SYSTEM_NAME),
        null,
        null);

    C2SimException exception =
        Assertions.assertThrowsExactly(
            C2SimException.class,
            () ->
                sharedSession.joinSharedSession(
                    CLIENT_ID_A,
                    "joining A",
                    new RequestJoinSession(CLIENT_ID_A_SYSTEM_NAME, CLIENT_ID_A_SYSTEM_NAME),
                    null,
                    null));
    var codeExpected = C2SimException.ErrorCode.C2SIM_CLIENT_ALREADY_JOINED.getCode();
    var codeReceived = exception.getError().getCode();
    assertEquals(codeExpected, codeReceived);
  }
}
