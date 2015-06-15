package com.stuffaboutcode.canaryraspberryjuicetest.support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.input.ReversedLinesFileReader;

/**
 * Convenience methods for manipulating files, for testing purposes.
 */
public class FileHelper {
  public static String readEndOfLogfile() {
    return readFileBackwards("logs/latest.log", 20);
  }

  public static String readFileBackwards(String path, int numLines) {
    try {
      ReversedLinesFileReader reader =
          new ReversedLinesFileReader(new File(path));

      List<String> lines = new ArrayList<>();
      int count = numLines;
      String line = reader.readLine();
      while (count > 0 && line != null) {
        lines.add(line);
        count--;
        line = reader.readLine();
      }
      return lines.stream().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
