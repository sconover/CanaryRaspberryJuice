package com.stuffaboutcode.canaryraspberryjuicetest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import net.canarymod.plugin.Plugin;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestRunnerPlugin extends Plugin {
  @Override public boolean enable() {

    Request request = Request.classes(FooTest.class);
    //Request request = Request.method(FooTest.class, "testPlayerChat");

    JUnitCore runner = new JUnitCore();
    runner.addListener(new TestRunListener(new BufferedWriter(new OutputStreamWriter(System.out))));
    runner.run(request);

    return true;
  }

  @Override public void disable() {

  }

  public static class TestRunListener extends org.junit.runner.notification.RunListener {
    private final BufferedWriter writer;

    public TestRunListener(BufferedWriter writer) {
      this.writer = writer;
    }

    private void println(String s) {
      try {
        writer.append("[TEST] " + s);
        writer.newLine();
        writer.flush();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }

    @Override public void testRunFinished(Result result) throws Exception {
      println(
          String.format(
              "RESULT: %s -- %d tests, %d failures, %d ignored, in %d seconds.",
              (result.wasSuccessful() ? "PASS" : "FAIL"),
              result.getRunCount(),
              result.getFailureCount(),
              result.getIgnoreCount(),
              (result.getRunTime() / 1000)
          ));
    }

    @Override public void testStarted(Description description) throws Exception {
      printDescription("-- ", description);
    }

    @Override public void testFinished(Description description) throws Exception {
      // printDescription("FINISHED: ", description);
    }

    @Override public void testFailure(Failure failure) throws Exception {
      println("FAILURE: " + failure.toString());
      println(failure.getTrace());
    }

    @Override public void testAssumptionFailure(Failure failure) {
      println("FAILURE: " + failure.toString());
      println(failure.getTrace());
    }

    @Override public void testIgnored(Description description) throws Exception {
      printDescription("IGNORED: ", description);
    }

    private void printDescription(String prefix, Description description) throws IOException {
      println(prefix + description.toString());
    }
  }
}
