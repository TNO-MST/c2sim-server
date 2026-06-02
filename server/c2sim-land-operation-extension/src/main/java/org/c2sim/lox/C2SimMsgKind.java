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
  /** Check point save system message */
  CHECKPOINT_SAVE,
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
  /** Request simulation realtime multiple */
  REQUEST_SIMULATION_REALTIME_MULTIPLE,
  /** Simulation speed factor */
  SIMULATION_REALTIME_MULTIPLE_REPORT,
  /** Start recording */
  START_RECORDING,
  /** Stop recording */
  STOP_RECORDING,
  /** Pause recording */
  PAUSE_RECORDING,
  /** Resume recoding */
  RESUME_RECORDING,
  /** Recording status report */
  RECORDING_STATUS_REPORT,
  /* Request recording status */
  REQUEST_RECORDING_STATUS,
  /** Resume playback */
  RESUME_PLAYBACK,
  /** Start playback */
  START_PLAYBACK,
  /** Stop playback */
  STOP_PLAYBACK,
  /** Pause playback */
  PAUSE_PLAYBACK,
  /** Request playback realtime multiple */
  REQUEST_PLAYBACK_REALTIME_MULTIPLE,
  /** Playback speed */
  PLAYBACK_REALTIME_MULTIPLE_REPORT,
  /** Playback status report */
  PLAYBACK_STATUS_REPORT,
  /** Request playback status */
  REQUEST_PLAYBACK_STATUS,
  /** set speed factor playback */
  SET_PLAYBACK_REALTIME_MULTIPLE,
  /** Refresh init */
  REFRESH_INIT,

  /** The XML root element is MESSAGE_BODY but it should be wrapped in MESSAGE */
  MESSAGE_BODY_NOT_WRAPPED,
  /** The document was parsed but did not match any known XSD path. */
  UNKNOWN,
  /** The document could not be parsed (XML syntax error or I/O failure). */
  ERROR;

  private static final Map<String, C2SimMsgKind> LOOKUP;

  static final String SYSTEM_MSG = "/Message/MessageBody/SystemMessageBody/";
  static {
    LOOKUP =
        Map.ofEntries(
            Map.entry("/MessageBody", MESSAGE_BODY_NOT_WRAPPED),
            Map.entry("/Message/MessageBody/C2SIMInitializationBody", C2SIM_INITIALIZATION),
            Map.entry("/Message/MessageBody/ObjectInitializationBody", OBJECT_INITIALIZATION),
            Map.entry("/Message/MessageBody/DomainMessageBody/OrderBody", ORDER),
            Map.entry("/Message/MessageBody/DomainMessageBody/ReportBody", REPORT),
            Map.entry(SYSTEM_MSG + "CheckpointRestore", CHECKPOINT_RESTORE),
            Map.entry(SYSTEM_MSG + "CheckpointSave", CHECKPOINT_SAVE),
            Map.entry(SYSTEM_MSG + "StartScenario", START_SCENARIO),
            Map.entry(SYSTEM_MSG + "StopScenario", STOP_SCENARIO),
            Map.entry(SYSTEM_MSG + "ResumeScenario", RESUME_SCENARIO),
            Map.entry(SYSTEM_MSG + "ResetScenario", RESET_SCENARIO),
            Map.entry(SYSTEM_MSG + "PauseScenario", PAUSE_SCENARIO),
            Map.entry(SYSTEM_MSG + "ShareScenario", SHARE_SCENARIO),
            Map.entry(SYSTEM_MSG + "SubmitInitialization", SUBMIT_INITIALIZATION),
            Map.entry(SYSTEM_MSG + "InitializationComplete", INITIALIZATION_COMPLETE),
            Map.entry(SYSTEM_MSG + "MagicMove", MAGIC_MOVE),
            Map.entry(SYSTEM_MSG + "SimulationRealtimeMultipleReport", SIMULATION_REALTIME_MULTIPLE_REPORT),
            Map.entry(SYSTEM_MSG + "SetSimulationRealtimeMultiple", SET_SIMULATION_REALTIME_MULTIPLE),
            Map.entry(SYSTEM_MSG + "RequestSimulationRealtimeMultiple", REQUEST_SIMULATION_REALTIME_MULTIPLE),
            Map.entry(SYSTEM_MSG + "StartRecording", START_RECORDING),
            Map.entry(SYSTEM_MSG + "StopRecording", STOP_RECORDING),
            Map.entry(SYSTEM_MSG + "PauseRecording", PAUSE_RECORDING),
            Map.entry(SYSTEM_MSG + "ResumeRecording", RESUME_RECORDING),
            Map.entry(SYSTEM_MSG + "RecordingStatusReport", RECORDING_STATUS_REPORT),
            Map.entry(SYSTEM_MSG + "RequestRecordingStatus", REQUEST_RECORDING_STATUS),
            Map.entry(SYSTEM_MSG + "ResumePlayback", RESUME_PLAYBACK),
            Map.entry(SYSTEM_MSG + "StopPlayback", STOP_PLAYBACK), Map.entry(SYSTEM_MSG + "StartPlayback",
                        START_PLAYBACK),
            Map.entry(SYSTEM_MSG + "PausePlayback", PAUSE_PLAYBACK),
            Map.entry(SYSTEM_MSG + "PlaybackRealtimeMultipleReport", PLAYBACK_REALTIME_MULTIPLE_REPORT),
            Map.entry(SYSTEM_MSG + "PlaybackStatusReport", PLAYBACK_STATUS_REPORT),
            Map.entry(SYSTEM_MSG + "RequestPlaybackStatus", REQUEST_PLAYBACK_STATUS),
            Map.entry(SYSTEM_MSG + "RequestPlaybackRealtimeMultiple", REQUEST_PLAYBACK_REALTIME_MULTIPLE),
            Map.entry(SYSTEM_MSG + "SetPlaybackRealtimeMultiple", SET_PLAYBACK_REALTIME_MULTIPLE),
            Map.entry(SYSTEM_MSG + "RefreshInit", REFRESH_INIT)







        );
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
