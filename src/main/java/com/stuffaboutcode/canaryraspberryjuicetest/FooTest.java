package com.stuffaboutcode.canaryraspberryjuicetest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FooTest {
  @Test
  public void testPass() {
    assertEquals(1, 1);
  }

  @Test
  public void testFail() {
    assertEquals(1, 2);
  }

  @Test
  public void testPlayerChat() throws Exception {
    String chatMessage = String.format("this-is-the-chat-message--%d", System.currentTimeMillis());

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

  @Test
  public void testDaggerBasic() {
    FooLocator locator = DaggerFooLocator.create();
    System.out.println(locator.server().getClass().getName());
    System.out.println(locator.world().getClass().getName());
  }
}
