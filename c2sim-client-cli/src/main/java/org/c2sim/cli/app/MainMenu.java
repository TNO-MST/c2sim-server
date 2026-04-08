package org.c2sim.cli.app;


import org.c2sim.cli.cmd.*;
import org.c2sim.cli.config.AppConfig;
import org.c2sim.cli.ui.Console;
import org.c2sim.client.C2SimClient;
import org.c2sim.client.helpers.MessageQueue;
import org.c2sim.client.model.DynamicSessionInfo;
import org.c2sim.client.model.RequestCreateSession;
import org.c2sim.client.model.SessionInfo;
import org.c2sim.client.model.StateType;
import org.c2sim.client.security.OidcCredentialFlow;
import org.c2sim.client.security.OidcCredentialFlowConfig;
import org.c2sim.client.security.OidcHardCodedToken;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.lox.helpers.MessageTypeHelper;
import org.c2sim.lox.schema.C2SIMInitializationBodyType;
import org.c2sim.statemachine.C2SimStateMachine;
import org.c2sim.statemachine.State;
import org.c2sim.statemachine.Trigger;
import org.jetbrains.annotations.NotNull;


import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.c2sim.statemachine.Trigger.INITIALIZATION_COMPLETE;


/**
 * Main interactive menu and application service facade.
 * Commands interact with the C2SIM server exclusively through the public methods
 * of this class; they hold no direct reference to {@link C2SimClient}.
 */
public class MainMenu implements C2SimClient.C2SimClientListener {

    @NotNull
    private final C2SimClient client;
    private final DisplayMode displayMode;

    private final ArrayList<String> events = new ArrayList<>();
    private final C2SimStateMachine stateMachine;
    private final AppConfig config;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                    .withZone(ZoneOffset.UTC);


    public MainMenu(    @NotNull AppConfig config,     @NotNull DisplayMode displayMode) {
        this.client      = Objects.requireNonNull(createClientFromConfig(config));
        this.displayMode = displayMode;
        this.stateMachine = new C2SimStateMachine(new C2SimStateMachine.StateMachineListener() {
        });
        this.config = config;
        this.client.onReceivedMessage(this::onReceivedMessage);

        this.client.whenCreatingSharedSession(() ->
                 {
                    var request = new RequestCreateSession();
                    var session = new SessionInfo();
                    session.setDescription(newSharedSessionDescription);
                    session.setC2simSchemaVersion(schemaVersion);
                    session.setDisplayName(getClient().getSharedSessionName());
                    request.setData(session);
                    return request;
                });

    }



    public String schemaVersion = "1.0.2";
    public String newSharedSessionDescription = "TEST";


    public AppConfig getConfig() {
        return config;
    }

    private C2SimClient createClientFromConfig(AppConfig config) {
        var builder = C2SimClient.create()
                .url(config.getServerUrl())
                .systemName(config.getSystemName())
                .clientIdDisplayName(config.getClientDisplayName())
                .beautifyXml()
                .enableReceivedMessageDecode()
                .enableReceivedMessageValidation();

        if (config.isUseOidc()) {
            if (config.isUseIdentityProvider()) {
                var idp = URI.create(config.getOidcIdpUrl());
                var cfg = new OidcCredentialFlowConfig(
                        idp,
                        config.getOidcClientId(),
                        config.getOidcClientSecret());
                builder.oidcProvider(new OidcCredentialFlow(cfg));
            } else {
                builder.oidcProvider(new OidcHardCodedToken(config.getAuthFixedToken()));
            }
        }

        builder.listener(this);

        return builder.build();
    }

    private void onReceivedMessage(MessageQueue.C2SimMessage msg) {
        if (!config.getWriteReceivedMsgToDisk()) {
            return;
        }
        var id = msg.decodedMsg().getC2SIMHeader().getMessageID();
        String timestamp = FORMATTER.format(Instant.now());
        Path file = Path.of("received_messages", msg.kind().name(), timestamp + "_" + id + ".xml" );
        try {
            // Create parent directories if they do not exist
            Files.createDirectories(file.getParent());

            // Create file only if it does not already exist
            if (Files.notExists(file)) {
                Files.createFile(file);
            }
            Files.writeString(file,
                    MessageTypeHelper.writeMessageAsString(msg.decodedMsg(), false, true));

        } catch (IOException | LoxException e) {

        }
    }

    // ── Entry point ────────────────────────────────────────────────────────

    public void run() {
        new ContainerCommand(this, "root", "Main Menu", displayMode, buildCommands(), this::showC2SimClientState)
                .runAsRoot();
    }

    @NotNull
    public C2SimClient getClient() {
        return client;
    }

    // ── Private helpers ────────────────────────────────────────────────────

    public List<String> getEvents() {
       return events;
    }
    private void showC2SimClientState() {
        Console.newLine();
        if (client.isJoined()) {
            Console.info(String.format("C2SIM client joined shared session '%s' as system '%s' (%s) ",
                    client.getSharedSessionName(),
                    client.getSystemName(),
                    client.getClientIdDisplayName()));
            Console.info(String.format("%s",
                    client.hasStreamToSharedSession() ? "Websocket connected to C2SIM server" : "Websocket NOT connected to C2SIM server"));
            Console.info(String.format("C2SIM server state '%s'", client.getCachedC2SimServerState()));

        } else {
            Console.info(String.format("Not joined shared session '%s' (yet)", client.getSharedSessionName()));
        }
        Console.newLine();
    }

    private List<MenuCommand> buildCommands() {
        return List.of(
                new CmdShowEventLog(this),
                new CmdShowSessions(this),
                new CmdChangeSharedSession(this),
                new CmdConnect(this),
                new ContainerCommand(this,"state_machine", "Change C2SIM server state machine (system command)", displayMode,
                        List.of(
                                new CmdSendStateMachineTrigger(this, Trigger.SUBMIT_INITIALIZATION),
                                new CmdSendFolderXml(this, "init_xml",     "Send C2SIM Init body msg",      "init"),
                                new CmdSendStateMachineTrigger(this,INITIALIZATION_COMPLETE),
                                new CmdSendStateMachineTrigger(this, Trigger.SHARE_SCENARIO),
                                new CmdSendStateMachineTrigger(this, Trigger.START_SCENARIO),
                                new CmdSendStateMachineTrigger(this, Trigger.STOP_SCENARIO),
                                new CmdSendStateMachineTrigger(this, Trigger.PAUSE_SCENARIO ),
                                new CmdSendStateMachineTrigger(this, Trigger.RESUME_SCENARIO),
                                new CmdSendStateMachineTrigger(this, Trigger.RESET_SCENARIO)
                        ), this::showC2SimClientState),

                new ContainerCommand(this,"send_docs", "Send C2SIM message", displayMode,
                        List.of(
                                new CmdSendFolderXml(this, "init_xml",     "Initialization",      "init"),
                                new CmdSendFolderXml(this, "order_xml",    "Orders",    "orders"),
                                new CmdSendFolderXml(this, "report_xml", "Reports", "reports"),
                                new CmdSendFolderXml(this, "invalid_xml", "Invalid XML", "invalid"),
                                new CmdSendFolderXml(this, "other_xml", "Other XML", "other")
                        ), this::showC2SimClientState),

                new CmdResign(this),
                new CmdQuit(this)
        );
    }

    public boolean isTriggerAllowed(Trigger trigger) {
        if (!client.isJoined()) { return false;}
        this.stateMachine.setState(toState(client.getCachedC2SimServerState()));

    return stateMachine.isTriggerAllowed(trigger);
    }

    // JSON type to INTERNAL type
    private static State toState(StateType type) {
        if (type == null) {
            throw new IllegalArgumentException("StateType cannot be null");
        }
        return State.valueOf(type.name());
    }

    private String getTimeAsText() {
        LocalTime time = LocalTime.now();
        return String.format("%tH:%tM:%tS", time, time, time);
    }

    @Override
    public void onStateChanged(C2SimClient client, StateType oldState, StateType newState) {
        events.add(String.format("%s C2SIM server state changed from '%s' to '%s'.",
                getTimeAsText(), oldState, newState));
    }

    @Override
    public void onStreamDisconnected(C2SimClient client, int code, String reason) {
        events.add(String.format("%s WebSocket stream disconnected with reason '%s'.",
                getTimeAsText(), reason));
    }

    @Override
    public void onStreamConnected(C2SimClient client) {
        events.add(String.format("%s WebSocket stream connected.",
                getTimeAsText()));
    }

    @Override
    public void onStreamFault(C2SimClient client, String reason) {
        events.add(String.format("%s Stream fault '%s'", getTimeAsText(), reason));
    }

    @Override
    public void onC2SIMInitialization(C2SimClient client, C2SIMInitializationBodyType init) {
        events.add(String.format("%s Received C2SIM initialization body", getTimeAsText()));
    }

    @Override
    public void onResigned(C2SimClient client) {
        events.add(String.format("%s Resigned from shared session", getTimeAsText()));
    }

    @Override
    public void onJoined(C2SimClient client, DynamicSessionInfo info) {
        events.add(String.format("%s Joined shared session '%s' with schema version '%s'.",
                getTimeAsText(),
                info.getSessionName(),
                info.getInfo().getC2simSchemaVersion()));
    }

}
