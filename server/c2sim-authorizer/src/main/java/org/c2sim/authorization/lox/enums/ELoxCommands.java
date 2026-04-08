package org.c2sim.authorization.lox.enums;

import org.c2sim.authorization.interfaces.TextEnum;
import org.c2sim.authorization.utils.EnumUtil;

public enum ELoxCommands implements TextEnum {
  /** C2SIM State machine trigger */
  INITIALIZE("INITIALIZE"),
  /** C2SIM State machine trigger */
  SHARE("SHARE"),
  /** C2SIM State machine trigger */
  START("START"),
  /** C2SIM State machine trigger */
  PAUSE("PAUSE"),
  /** C2SIM State machine trigger */
  RESUME("RESUME"),
  STOP("STOP"),
  /** C2SIM State machine trigger */
  RESET("RESET"),
  /** C2SIM recording */
  STARTREC("STARTREC"),
  /** C2SIM recording */
  PAUSEREC("PAUSEREC"),
  /** C2SIM recording */
  RESTARTREC("RESTARTREC"),
  /** C2SIM recording */
  STOPREC("STOPREC"),
  /** C2SIM recording */
  GETRECSTAT("GETRECSTAT"),
  /** C2SIM recording */
  GETPLAYSTAT("GETPLAYSTAT"),
  /** Set simulation speed */
  SETSIMMULT("SETSIMMULT"),
  /** C2SIM replay */
  STARTPLAY("STARTPLAY"),
  /** C2SIM replay */
  PAUSEPLAY("PAUSEPLAY"),
  /** C2SIM replay */
  RESUMEPLAY("RESUMEPLAY"),
  /** C2SIM replay */
  STOPPLAY("STOPPLAY"),
  /** C2SIM replay */
  GETPLAYMULT("GETPLAYMULT"),
  /** C2SIM replay */
  SETPLAYMULT("SETPLAYMULT"),
  /** C2SIM replay */
  //    SETSIMSTAT("SETSIMSTAT"),
  /** C2SIM replay */
  RESTART("RESTART"),
  /** Not used */
  INITCOMP("INITCOMP"),
  /** Teleport */
  MAGIC("MAGIC"),
  /** Query init */
  QUERYINIT("QUERYINIT"),
  STATUS("STATUS"),
  /** Control points */
  CPRESTORE("CPRESTORE"),
  /** Control points */
  CPSAVE("CPSAVE"),
  /** Not used */
  LOAD("LOAD"),
  /** Teleport */
  MAGICMOVE("MAGICMOVE"),
  /** Get simulation speed */
  GETSIMMULT("GETSIMMULT"),
  /** Unknown command */
  UNKNOWN("Unknown");

  private final String text;

  ELoxCommands(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public static ELoxCommands fromText(String command) {
    if (command == null) {
      return ELoxCommands.UNKNOWN;
    }
    try {
      return EnumUtil.fromText(ELoxCommands.class, command);
    } catch (Exception e) {
      return ELoxCommands.UNKNOWN;
    }
  }
}
