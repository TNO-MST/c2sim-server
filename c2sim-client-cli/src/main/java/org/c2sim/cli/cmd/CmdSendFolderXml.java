package org.c2sim.cli.cmd;

import org.c2sim.cli.Main;
import org.c2sim.cli.app.MainMenu;
import org.c2sim.cli.ui.Console;
import org.c2sim.cli.utils.ExceptionHandler;
import org.c2sim.client.exceptions.C2SimRestException;
import org.c2sim.client.invoker.ApiException;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.lox.exceptions.ValidationException;
import org.c2sim.lox.helpers.DateTimeTypeHelper;
import org.c2sim.lox.helpers.MessageBodyTypeHelper;
import org.c2sim.lox.helpers.MessageTypeHelper;
import org.c2sim.lox.helpers.XmlFactoryHelper;
import org.c2sim.lox.helpers.builders.MessageTypeBuilder;
import org.c2sim.lox.sax.DetectXmlRootElement;
import org.c2sim.lox.schema.C2SIMHeaderType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Parameterised command that lists XML files from a given folder
 * and publishes the one the user selects.
 */
public class CmdSendFolderXml extends MenuCommand {

    private final String id;
    private final String title;
    private final String folder;

    public CmdSendFolderXml(MainMenu mainMenu,
                            String id,
                            String title,
                            String folder) {
        super(mainMenu);
        this.id = id;
        this.title = title;
        this.folder = folder;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public boolean isActive() {
        return mainMenu.getClient().isJoined();
    }

    @Override
    public boolean execute() {
        Console.clearScreen();
        Console.printBanner("Select XML file for " + folder);

        Path messagesFolder = resolveMessagesFolder();
        if (messagesFolder == null || !Files.isDirectory(messagesFolder)) {
            Console.error("Folder not found: " + messagesFolder);
            return true;
        }

        List<String> files = listXmlFiles(messagesFolder);
        if (files.isEmpty()) {
            Console.warning("No .xml files found in: " + messagesFolder);
            return true;
        }

        printMenu(files);

        String filename = Console.readListChoice(files);
        if (filename == null || filename.isBlank()) {
            return true;
        }

        Path filePath = messagesFolder.resolve(
                filename.endsWith(".xml") ? filename : filename + ".xml"
        );

        if (!Files.exists(filePath)) {
            Console.error("File not found: " + filePath);
            return true;
        }

        publishXmlFile(filePath);
        return true;
    }

    /**
     * Resolves the folder containing XML messages relative to the JAR location.
     */
    private Path resolveMessagesFolder() {
        try {
            Path jarPath = Paths.get(
                    Main.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).getParent();

            if (jarPath == null) {
                return null;
            }

            return jarPath
                    .resolve("c2sim_messages")
                    .resolve(folder);

        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * Publishes a selected XML file.
     */
    private void publishXmlFile(Path filePath) {
        Console.clearScreen();
        Console.printBanner("Publishing C2SIM message");


        String rootElement = DetectXmlRootElement.getRootElementName(filePath);
        if (rootElement == null) {
            Console.error("Failed to parse XML in file: " + filePath);
            return;
        }

        try (InputStream inputStream = Files.newInputStream(filePath)) {

            if ("Message".equalsIgnoreCase(rootElement)) {
                handleMessage(inputStream);
            } else if ("MessageBody".equalsIgnoreCase(rootElement)) {
                handleMessageBody(inputStream);
            } else {
                Console.error("XML Root element must be 'Message' or 'MessageBody'");
                return;
            }

            Console.success("C2SIM Message Published.");

        } catch (IOException |
                 C2SimRestException |
                 ApiException |
                 ValidationException |
                 LoxException e) {

            ExceptionHandler.handle(e);
        }
    }

    /**
     * Handles XML files whose root element is <Message>.
     */
    private void handleMessage(InputStream inputStream)
            throws
            C2SimRestException,
            ApiException,
            ValidationException,
            LoxException {

        var message = MessageTypeHelper.readMessage(inputStream);
        Console.println("Update 'sending time' and 'from system' in C2SIM header");
        message.getC2SIMHeader().setSendingTime(DateTimeTypeHelper.createDateTimeTypeNow());
        message.getC2SIMHeader().setFromSendingSystem(mainMenu.getClient().getSystemName());
        Console.println(getC2SimHeaderAsText(message.getC2SIMHeader()));
        Console.newLine();

        mainMenu.getClient().publishC2SimDocument(message);

    }

    /**
     * Handles XML files whose root element is <MessageBody>.
     * Wraps MessageBody into a Message with header.
     */
    private void handleMessageBody(InputStream inputStream)
            throws
            C2SimRestException,
            ApiException,
            ValidationException,
            LoxException {

        Console.info("File contains XML root element 'MessageBody', wrapping into XML root element 'Message'");

        var msgBody = MessageBodyTypeHelper.readMessageBody(inputStream);

        var header = XmlFactoryHelper.createC2SimHeader(
                mainMenu.getConfig().getSystemName()
        );


        

        var message = MessageTypeBuilder.create()
                .c2SIMHeader(header)
                .messageBody(msgBody)
                .build();

        Console.println(getC2SimHeaderAsText(message.getC2SIMHeader()));
        Console.newLine();


            mainMenu.getClient().publishC2SimDocument(message);

    }

    /**
     * Lists all XML files in a folder.
     */
    private List<String> listXmlFiles(Path folder) {
        List<String> files = new ArrayList<>();

        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(folder, "*.xml")) {

            for (Path path : stream) {
                files.add(path.getFileName().toString());
            }

        } catch (IOException e) {
            Console.error("Failed to read folder: " + folder);
        }

        files.sort(String.CASE_INSENSITIVE_ORDER);
        return files;
    }

    private void printMenu(List<String> files) {
        for (int i = 0; i < files.size(); i++) {
            Console.printMenuOption(i + 1, files.get(i));
        }
        Console.newLine();
    }

    private static String isNull(String string) {
        return string == null ? "<not set>" : string;
    }

    private  String getC2SimHeaderAsText(C2SIMHeaderType header) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("C2SIMHeaderType:");

        // sb.append("\n  authorizationHeader=").append(header.getAuthorizationHeader().));
        sb.append("\n-   communicativeActTypeCode=").append(header.getCommunicativeActTypeCode());
        sb.append("\n-   conversationID=").append(isNull(header.getConversationID()));
        sb.append("\n-   fromSendingSystem=").append(isNull(header.getFromSendingSystem()));
        sb.append("\n-   inReplyToMessageID=").append(isNull(header.getInReplyToMessageID()));
        sb.append("\n-   messageID=").append(isNull(header.getMessageID()));
        sb.append("\n-   protocol=").append(isNull(header.getProtocol()));
        sb.append("\n-   protocolVersion=").append(isNull(header.getProtocolVersion()));
        sb.append("\n-   replyToSystem=").append(isNull(header.getReplyToSystem()));
        sb.append("\n-   securityClassificationCode=").append(header.getSecurityClassificationCode());
        sb.append("\n-   sendingTime=").append(header.getSendingTime());
        sb.append("\n-   toReceivingSystem=").append(isNull(header.getToReceivingSystem()));

        sb.append("\n\n");

        return sb.toString();
    }
}