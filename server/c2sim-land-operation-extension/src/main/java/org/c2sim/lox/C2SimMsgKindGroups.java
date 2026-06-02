package org.c2sim.lox;

import java.util.EnumSet;
import java.util.stream.Collectors;

/**
 * Utility class to group C2SimMsgKind enumerations into related groups, and check if a message belongs
 */
public class C2SimMsgKindGroups {

    // Prevent instantiation
    private C2SimMsgKindGroups() {
        throw new AssertionError("Only static functions!");
    }

    public static final EnumSet<C2SimMsgKind> stateManagement =
            EnumSet.of(
                    C2SimMsgKind.START_SCENARIO,
                    C2SimMsgKind.STOP_SCENARIO,
                    C2SimMsgKind.RESET_SCENARIO,
                    C2SimMsgKind.PAUSE_SCENARIO,
                    C2SimMsgKind.SHARE_SCENARIO,
                    C2SimMsgKind.RESUME_SCENARIO,
                    C2SimMsgKind.SUBMIT_INITIALIZATION,
                    C2SimMsgKind.C2SIM_INITIALIZATION,
                    C2SimMsgKind.INITIALIZATION_COMPLETE

            );

    public static final EnumSet<C2SimMsgKind> simulationSpeedMessages =
            EnumSet.of(
                    C2SimMsgKind.REQUEST_SIMULATION_REALTIME_MULTIPLE,
                    C2SimMsgKind.SET_SIMULATION_REALTIME_MULTIPLE,
                    C2SimMsgKind.SIMULATION_REALTIME_MULTIPLE_REPORT

            );

    public static final EnumSet<C2SimMsgKind> checkPointMessages =
            EnumSet.of(
                    C2SimMsgKind.CHECKPOINT_RESTORE,
                    C2SimMsgKind.CHECKPOINT_SAVE
            );

    public static final EnumSet<C2SimMsgKind> recordingMessages =
            EnumSet.of(
                    C2SimMsgKind.START_RECORDING,
                    C2SimMsgKind.STOP_RECORDING,
                    C2SimMsgKind.PAUSE_RECORDING,
                    C2SimMsgKind.RESUME_RECORDING,
                    C2SimMsgKind.RECORDING_STATUS_REPORT,
                    C2SimMsgKind.REQUEST_RECORDING_STATUS
            );

    public static final EnumSet<C2SimMsgKind> playbackMessages =
            EnumSet.of(
                    C2SimMsgKind.RESUME_PLAYBACK,
                    C2SimMsgKind.START_PLAYBACK,
                    C2SimMsgKind.STOP_PLAYBACK,
                    C2SimMsgKind.PAUSE_PLAYBACK,
                    C2SimMsgKind.REQUEST_PLAYBACK_REALTIME_MULTIPLE,
                    C2SimMsgKind.PLAYBACK_REALTIME_MULTIPLE_REPORT,
                    C2SimMsgKind.SET_PLAYBACK_REALTIME_MULTIPLE,
                    C2SimMsgKind.PLAYBACK_STATUS_REPORT,
                    C2SimMsgKind.REQUEST_PLAYBACK_STATUS
            );

    public static final EnumSet<C2SimMsgKind> otherMessages =
            EnumSet.of(
                    C2SimMsgKind.MAGIC_MOVE,
                    C2SimMsgKind.REFRESH_INIT
            );

    public static final EnumSet<C2SimMsgKind> queryMessages =
            EnumSet.of(
                    C2SimMsgKind.SIMULATION_REALTIME_MULTIPLE_REPORT,
                    C2SimMsgKind.REQUEST_PLAYBACK_STATUS,
                    C2SimMsgKind.REQUEST_RECORDING_STATUS,
                    C2SimMsgKind.REQUEST_PLAYBACK_REALTIME_MULTIPLE,
                    C2SimMsgKind.REQUEST_SIMULATION_REALTIME_MULTIPLE
            );

    public static final EnumSet<C2SimMsgKind> simanMessages =

                    union(
                            playbackMessages,
                            recordingMessages,
                            stateManagement,
                            simulationSpeedMessages,
                            checkPointMessages,
                            otherMessages
                    );


    @SafeVarargs
    private static EnumSet<C2SimMsgKind> union(EnumSet<C2SimMsgKind>... sets) {
        EnumSet<C2SimMsgKind> result = EnumSet.noneOf(C2SimMsgKind.class);
        for (EnumSet<C2SimMsgKind> set : sets) {
            result.addAll(set);
        }
        return result;
    }

    public static boolean isSimulationSpeedMsgGroup(C2SimMsgKind msgKind) {
        return simulationSpeedMessages.contains(msgKind);
    }


    public static boolean isCheckpointMsgGroup(C2SimMsgKind msgKind) {
        return checkPointMessages.contains(msgKind);
    }

    public static boolean isRecordingMsgGroup(C2SimMsgKind msgKind) {
        return recordingMessages.contains(msgKind);
    }

    public static boolean isPlaybackMsgGroup(C2SimMsgKind msgKind) {
        return playbackMessages.contains(msgKind);
    }

    public static boolean isStateManagementMsgGroup(C2SimMsgKind msgKind) {
        return stateManagement.contains(msgKind);
    }

    public static boolean isSimanResponseMsgGroup(C2SimMsgKind msgKind) {
        return false;
    }

    public static boolean isQueryMsgGroup(C2SimMsgKind msgKind) {
        return queryMessages.contains(msgKind);
    }

    public static boolean isSimanMsgGroup(C2SimMsgKind msgKind) {
        return simanMessages.contains(msgKind);
    }

    public static String getSimanMsgGroupAsText() {
        return "["+ simanMessages.stream()
                .map(Enum::name)
                .collect(Collectors.joining(", ")) + "]";
    }

}
