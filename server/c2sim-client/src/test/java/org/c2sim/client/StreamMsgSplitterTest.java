package org.c2sim.client;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.ArrayList;
import org.c2sim.client.websockets.StreamMsgSplitter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM Client library")
@Story("Test stream splitting")
class StreamMsgSplitterTest {

  ArrayList<String> lines = new ArrayList<String>();
  StreamMsgSplitter splitter;

  @BeforeEach
  void setup() throws Exception {
    splitter =
        new StreamMsgSplitter(
            line -> {
              lines.add(line.toString());
            });
  }

  @Test
  @Description("Splitting stream into messages")
  void streamSplittingFullTest() {

    String largeMsg = "A".repeat(4096 * 10);
    splitter.accept("<a>dit is een"); // Chunk test
    splitter.accept(" test<\\a>\n");
    splitter.accept("<b>message2<\\b>\r\n"); // Windows ending
    splitter.accept("<c>message3<\\c>\n  \n"); // Empty line
    splitter.accept(largeMsg + "\n"); // Large message
    splitter.accept("last line"); // Flush
    splitter.flush();
    Assertions.assertEquals(5, lines.size());
    Assertions.assertEquals("<a>dit is een test<\\a>", lines.get(0));
    Assertions.assertEquals("<b>message2<\\b>", lines.get(1));
    Assertions.assertEquals("<c>message3<\\c>", lines.get(2));
    Assertions.assertEquals(largeMsg, lines.get(3));
    Assertions.assertEquals("last line", lines.get(4));
  }

  @Test
  @Description("Splitting stream: chunks")
  void streamSplittingMultipleChunksTest() {
    splitter.accept("<a>dit is een"); // Chunk test
    splitter.accept(" test<\\a>\n");
    Assertions.assertEquals(1, lines.size());
    Assertions.assertEquals("<a>dit is een test<\\a>", lines.getFirst());
  }

  @Test
  @Description("Splitting stream: windows ending 13+10")
  void streamSplittingWindowsEndingTest() {
    splitter.accept("<b>message2<\\b>\r\n"); // Windows ending
    Assertions.assertEquals(1, lines.size());
    Assertions.assertEquals("<b>message2<\\b>", lines.getFirst());
  }

  @Test
  @Description("Splitting stream: empty lines")
  void streamSplittingEmptyLineTest() {
    splitter.accept("\n  \n");
    Assertions.assertEquals(0, lines.size());
  }

  @Test
  @Description("Splitting stream: large line")
  void streamSplittingLargeLinesTest() {
    String largeMsg = "A".repeat(4096 * 10);
    splitter.accept(largeMsg + "\nline 2\n");
    Assertions.assertEquals(2, lines.size());
    Assertions.assertEquals(largeMsg, lines.getFirst());
    Assertions.assertEquals("line 2", lines.get(1));
  }
}
