package org.c2sim.lox;

import java.util.*;

/**
 * Enumeration of recognised C2SIM XML message kinds.
 *
 * <p>Each constant maps to a unique XSD element path inside a {@code MessageType} document. The
 * mapping is used by SAX-based detection (see {@link org.c2sim.lox.sax.DetectMsgKind}).
 *
 * <p>The special values {@link #UNKNOWN} and {@link #ERROR} are returned when the document does not
 * match any known path or cannot be parsed, respectively.
 */
public enum C2SimMsgKind {
  /* C2SIM initialization */
  C2SIM_INITIALIZATION,
  /** C2SIM Object initialization */
  OBJECT_INITIALIZATION,
  /** C2SIM order */
  ORDER,
  /** C2SIM report (position, observation, etc.) */
  REPORT,
  /** Check point restore system message */
  CHECKPOINT_RESTORE,
  /** C2SIM State machine (trigger / notification) */
  START_SCENARIO,
  /** C2SIM State machine (trigger / notification) */
  STOP_SCENARIO,
  /** C2SIM State machine (trigger / notification) */
  RESUME_SCENARIO,
  /** C2SIM State machine (trigger / notification) */
  RESET_SCENARIO,
  /** C2SIM State machine (trigger / notification) */
  PAUSE_SCENARIO,
  /** C2SIM State machine (trigger / notification) */
  SHARE_SCENARIO,
  /** C2SIM State machine (trigger / notification) */
  SUBMIT_INITIALIZATION,
  /** C2SIM State machine (trigger / notification) */
  INITIALIZATION_COMPLETE,
  /** C2SIM teleport */
  MAGIC_MOVE,
  /** Set simulation speed */
  SET_SIMULATION_REALTIME_MULTIPLE,
  /** The XML root element is MESSAGE_BODY but it should be wrapped in MESSAGE */
  MESSAGE_BODY_NOT_WRAPPED,
  /** The document was parsed but did not match any known XSD path. */
  UNKNOWN,
  /** The document could not be parsed (XML syntax error or I/O failure). */
  ERROR;

  private static final Map<String, C2SimMsgKind> LOOKUP;

  static {
    LOOKUP =
        Map.ofEntries(
            Map.entry("/MessageBody", MESSAGE_BODY_NOT_WRAPPED),
            Map.entry("/Message/MessageBody/C2SIMInitializationBody", C2SIM_INITIALIZATION),
            Map.entry("/Message/MessageBody/ObjectInitializationBody", OBJECT_INITIALIZATION),
            Map.entry("/Message/MessageBody/DomainMessageBody/OrderBody", ORDER),
            Map.entry("/Message/MessageBody/DomainMessageBody/ReportBody", REPORT),
            Map.entry(
                "/Message/MessageBody/SystemMessageBody/CheckpointRestore", CHECKPOINT_RESTORE),
            Map.entry("/Message/MessageBody/SystemMessageBody/StartScenario", START_SCENARIO),
            Map.entry("/Message/MessageBody/SystemMessageBody/StopScenario", STOP_SCENARIO),
            Map.entry("/Message/MessageBody/SystemMessageBody/ResumeScenario", RESUME_SCENARIO),
            Map.entry("/Message/MessageBody/SystemMessageBody/ResetScenario", RESET_SCENARIO),
            Map.entry("/Message/MessageBody/SystemMessageBody/PauseScenario", PAUSE_SCENARIO),
            Map.entry("/Message/MessageBody/SystemMessageBody/ShareScenario", SHARE_SCENARIO),
            Map.entry(
                "/Message/MessageBody/SystemMessageBody/SubmitInitialization",
                SUBMIT_INITIALIZATION),
            Map.entry(
                "/Message/MessageBody/SystemMessageBody/InitializationComplete",
                INITIALIZATION_COMPLETE),
            Map.entry("/Message/MessageBody/SystemMessageBody/MagicMove", MAGIC_MOVE),
            Map.entry(
                "/Message/MessageBody/SystemMessageBody/SetSimulationRealtimeMultiple",
                SET_SIMULATION_REALTIME_MULTIPLE));
  }

  /**
   * Returns the {@code C2SimMsgKind} corresponding to the given XPATH.
   *
   * @param path the slash-separated element path extracted from the XML document, e.g. {@code
   *     "/Message/MessageBody/DomainMessageBody/ReportBody"}; may be {@code null} or empty
   * @return the matching enum constant, or {@link #UNKNOWN} if the path is not recognised or blank
   */
  public static C2SimMsgKind fromPath(String path) {
    if (path == null || path.isEmpty()) {
      return UNKNOWN;
    }
    return LOOKUP.getOrDefault(path, UNKNOWN);
  }

  /**
   * Returns the XPATH associated with the given {@code C2SimMsgKind}.
   *
   * @param kind the enum constant whose path is requested
   * @return the slash-separated XSD element path, or an empty string if {@code kind} has no
   *     registered path (e.g. {@link #UNKNOWN} or {@link #ERROR})
   */
  public static String getPath(C2SimMsgKind kind) {
    for (Map.Entry<String, C2SimMsgKind> entry : LOOKUP.entrySet()) {
      if (entry.getValue() == kind) {
        return entry.getKey();
      }
    }
    return "";
  }
}
