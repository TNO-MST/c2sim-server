package org.c2sim.authorizer;

import static org.junit.jupiter.api.Assertions.assertSame;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.c2sim.authorization.lox.enums.ELoxCommands;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM Auth module")
@Story("Claims with enums")
class TestEnumHelper {

  @Test
  void testEnumCommand() {
    var command = ELoxCommands.fromText("START");
    assertSame(ELoxCommands.START, command);
  }

  @Test
  void testEnumCommandInvalid() {
    var command = ELoxCommands.fromText("INVALID_COMMAND");
    assertSame(ELoxCommands.UNKNOWN, command);
  }
}
