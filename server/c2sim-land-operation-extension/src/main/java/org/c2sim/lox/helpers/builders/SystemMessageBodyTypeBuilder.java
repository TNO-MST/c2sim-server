package org.c2sim.lox.helpers.builders;

import java.math.BigDecimal;
import org.c2sim.lox.schema.*;

/**
 * Factory methods for creating {@link SystemMessageBodyType} objects for each supported C2SIM
 * system command.
 *
 * <p>Each method creates a {@link SystemMessageBodyType} with exactly one command type set, ready
 * to be embedded in a {@link MessageBodyTypeBuilder}.
 */
public class SystemMessageBodyTypeBuilder {

  private SystemMessageBodyTypeBuilder() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Creates a {@link SystemMessageBodyType} containing a {@code CheckpointRestore} command.
   *
   * @param name the name of the checkpoint to restore
   * @return the system message body
   */
  public static SystemMessageBodyType createCheckpointRestore(String name) {
    var sysMsg = new SystemMessageBodyType();
    var msg = new CheckpointRestoreType();
    msg.setName(name);
    sysMsg.setCheckpointRestore(msg);
    return sysMsg;
  }

  /**
   * Creates a {@link SystemMessageBodyType} containing a {@code SetSimulationRealtimeMultiple}
   * command.
   *
   * @param factor the simulation speed multiplier (e.g. {@code 2.0} for double speed)
   * @return the system message body
   */
  public static SystemMessageBodyType createSimulationRealtimeMultiple(double factor) {
    var sysMsg = new SystemMessageBodyType();
    var rate = new SetSimulationRealtimeMultipleType();
    rate.setSimulationRealtimeMultiple(BigDecimal.valueOf(factor));
    sysMsg.setSetSimulationRealtimeMultiple(rate);
    return sysMsg;
  }

  /**
   * Creates a {@link SystemMessageBodyType} containing a {@code StartScenario} command.
   *
   * @return the system message body
   */
  public static SystemMessageBodyType createStartScenario() {
    var sysMsg = new SystemMessageBodyType();
    sysMsg.setStartScenario(new StartScenarioType());
    return sysMsg;
  }

  /**
   * Creates a {@link SystemMessageBodyType} containing a {@code StopScenario} command.
   *
   * @return the system message body
   */
  public static SystemMessageBodyType createStopScenario() {
    var sysMsg = new SystemMessageBodyType();
    sysMsg.setStopScenario(new StopScenarioType());
    return sysMsg;
  }

  /**
   * Creates a {@link SystemMessageBodyType} containing a {@code SubmitInitialization} command.
   *
   * @return the system message body
   */
  public static SystemMessageBodyType createSubmitInitialization() {
    var sysMsg = new SystemMessageBodyType();
    sysMsg.setSubmitInitialization(new SubmitInitializationType());
    return sysMsg;
  }

  /**
   * Creates a {@link SystemMessageBodyType} containing a {@code ShareScenario} command.
   *
   * @return the system message body
   */
  public static SystemMessageBodyType createShareScenario() {
    var sysMsg = new SystemMessageBodyType();
    sysMsg.setShareScenario(new ShareScenarioType());
    return sysMsg;
  }

  /**
   * Creates a {@link SystemMessageBodyType} containing a {@code PauseScenario} command.
   *
   * @return the system message body
   */
  public static SystemMessageBodyType createPauseScenario() {
    var sysMsg = new SystemMessageBodyType();
    sysMsg.setPauseScenario(new PauseScenarioType());
    return sysMsg;
  }

  /**
   * Creates a {@link SystemMessageBodyType} containing a {@code ResumeScenario} command.
   *
   * @return the system message body
   */
  public static SystemMessageBodyType createResumeScenario() {
    var sysMsg = new SystemMessageBodyType();
    sysMsg.setResumeScenario(new ResumeScenarioType());
    return sysMsg;
  }

  /**
   * Creates a {@link SystemMessageBodyType} containing a {@code ResetScenario} command.
   *
   * @return the system message body
   */
  public static SystemMessageBodyType createReset() {
    var sysMsg = new SystemMessageBodyType();
    sysMsg.setResetScenario(new ResetScenarioType());
    return sysMsg;
  }

  /**
   * Creates a {@link SystemMessageBodyType} containing an {@code InitializationComplete} command.
   *
   * @return the system message body
   */
  public static SystemMessageBodyType createInitializationComplete() {
    var sysMsg = new SystemMessageBodyType();
    sysMsg.setInitializationComplete(new InitializationCompleteType());
    return sysMsg;
  }

  /**
   * Creates a {@link SystemMessageBodyType} containing a {@code MagicMove} command.
   *
   * @param magicMove a builder whose {@link MagicMoveTypeBuilder#build()} result is used
   * @return the system message body
   */
  public static SystemMessageBodyType createMagicMove(MagicMoveTypeBuilder magicMove) {
    var sysMsg = new SystemMessageBodyType();
    sysMsg.setMagicMove(magicMove.build());
    return sysMsg;
  }
}
