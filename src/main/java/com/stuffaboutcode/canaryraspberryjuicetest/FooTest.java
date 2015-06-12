package com.stuffaboutcode.canaryraspberryjuicetest;

import com.stuffaboutcode.canaryraspberryjuice.RemoteSession;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.canarymod.Canary;
import net.canarymod.logger.Logman;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FooTest {

  /**
   * - literal fixtures :)
   *   e.g. set up some blocks such and such way
   *
   * - literal test run
   *   e.g. you can see a line of things executing
   *     place the current player in position to watch
   *     shift to each new test position?
   *     slow things down...
   *       take ticks/sec into consideration...
   *       adjust to wall clock time.
   *         e.g. 200 ms / write operation
   */

  @Test
  public void testPlayerChat() throws Exception {
    String chatMessage = String.format("this-is-the-chat-message--%d", System.currentTimeMillis());

    RemoteSession.CommandHandler commandHandler =
        new RemoteSession.CommandHandler(
            Canary.getServer(),
            Logman.getLogman("FooTest-logman"),
            new TestOut());

    commandHandler.handleLine(String.format("chat.post(%s)", chatMessage));

    ReversedLinesFileReader reader =
        new ReversedLinesFileReader(new File("logs/latest.log"));

    List<String> lines = new ArrayList<>();
    int count = 20;
    String line = reader.readLine();
    while (count > 0 && line != null) {
      lines.add(line);
      count--;
      line = reader.readLine();
    }
    String last20LinesOfLogFile = lines.stream().collect(Collectors.joining("\n"));

    assertTrue(
        String.format("expected '%s' to be present, but was not. full text:\n\n%s",
            chatMessage,
            last20LinesOfLogFile),
        last20LinesOfLogFile.contains(chatMessage));
    // extract assert string contains
  }

  public static class TestOut implements RemoteSession.Out {
    public List<String> sends = new ArrayList<>();

    @Override public void send(String str) {
      sends.add(str);
    }
  }
}
