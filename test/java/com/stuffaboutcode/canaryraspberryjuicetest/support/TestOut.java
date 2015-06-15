package com.stuffaboutcode.canaryraspberryjuicetest.support;

import com.stuffaboutcode.canaryraspberryjuice.RemoteSession;
import java.util.ArrayList;
import java.util.List;

/**
 * "Fake" output that may be provided to CommandHandler. Instead of sending output back
 * over a socket connection, the output is captured in a list of strings,
 * for use in assertions.
 */
public class TestOut implements RemoteSession.Out {
  public List<String> sends = new ArrayList<>();

  @Override public void send(String str) {
    sends.add(str);
  }
}
