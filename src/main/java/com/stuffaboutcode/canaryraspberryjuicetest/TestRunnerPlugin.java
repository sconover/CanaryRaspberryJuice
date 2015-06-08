package com.stuffaboutcode.canaryraspberryjuicetest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import net.canarymod.plugin.Plugin;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestRunnerPlugin extends Plugin {
  @Override public boolean enable() {

    JUnitCore runner = new JUnitCore();
    runner.addListener(new TestRunListener(new BufferedWriter(new OutputStreamWriter(System.out))));
    runner.run(
        FooTest.class
    );

    return true;
  }

  @Override public void disable() {

  }

  public static class TestRunListener extends org.junit.runner.notification.RunListener {
    private final BufferedWriter writer;

    public TestRunListener(BufferedWriter writer) {
      this.writer = writer;
    }

    @Override public void testRunStarted(Description description) throws Exception {
      printDescription("RUNNING: ", description);
    }

    @Override public void testRunFinished(Result result) throws Exception {
      writer.append("RESULT: " + result.toString());
      writer.newLine();
      writer.flush();
    }

    @Override public void testStarted(Description description) throws Exception {
      printDescription("STARTED: ", description);
    }

    @Override public void testFinished(Description description) throws Exception {
      printDescription("FINISHED: ", description);
    }

    @Override public void testFailure(Failure failure) throws Exception {
      writer.append("FAILURE: " + failure.toString());
      writer.newLine();
      writer.append(failure.getTrace());
      writer.newLine();
      writer.flush();
    }

    @Override public void testAssumptionFailure(Failure failure) {
      try {
        writer.append("FAILURE: " + failure.toString());
        writer.newLine();
        writer.append(failure.getTrace());
        writer.newLine();
        writer.flush();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override public void testIgnored(Description description) throws Exception {
      printDescription("IGNORED: ", description);
    }

    private void printDescription(String prefix, Description description) throws IOException {
      writer.append(prefix + description.toString());
      writer.newLine();
      writer.flush();
    }
  }
}
