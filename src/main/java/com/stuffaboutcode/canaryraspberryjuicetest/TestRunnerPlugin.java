package com.stuffaboutcode.canaryraspberryjuicetest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.canarymod.Canary;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.commandsys.CommandListener;
import net.canarymod.plugin.Plugin;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestRunnerPlugin extends Plugin {
  @Override public boolean enable() {

    try {
      Canary.commands().registerCommands(new TestCommands(this.getPath()), this, true);
    } catch (CommandDependencyException e) {
      throw new RuntimeException(e);
    }

    return true;
  }

  @Override public void disable() {

  }

  public static class TestCommands implements CommandListener {
    private final String rootPath;

    public TestCommands(String rootPath) {
      this.rootPath = rootPath;
    }

    @Command(aliases = { "test" },
        helpLookup = "test",
        description = "run a test or tests",
        permissions = { "canary.command.test" },
        toolTip = "/test [class or method] (blank for all tests)",
        min = 1)
    public void testRun(MessageReceiver caller, String[] parameters) {
      ClassesAndClassesAndMethods classesAndClassesAndMethods = scanDir();

      Request request = null;
      if (parameters.length == 2) {
        request = junitRequestFromSearchString(parameters[1], classesAndClassesAndMethods);
        if (request == null) {
          caller.message(String.format("'%s' not found.", parameters[1]));
          return;
        } else {
          rememberTest(parameters[1]);
        }
      } else {
        request = Request.classes(
            classesAndClassesAndMethods.classes.toArray(
                new Class[classesAndClassesAndMethods.classes.size()]));
      }

      JUnitCore runner = new JUnitCore();
      runner.addListener(new TestRunListener(caller));
      runner.run(request);
    }

    @Command(aliases = { "t" },
        helpLookup = "t",
        description = "runs the previous test, or all tests if no last test is set",
        permissions = { "canary.command.t" },
        toolTip = "/t",
        min = 1)
    public void testRunLastTest(MessageReceiver caller, String[] parameters) {
      caller.message(String.format("Last test run is '%s'", getLastTest()));

      ClassesAndClassesAndMethods classesAndClassesAndMethods = scanDir();

      Request request = null;
      if (getLastTest() == null) {
        request = Request.classes(
            classesAndClassesAndMethods.classes.toArray(
                new Class[classesAndClassesAndMethods.classes.size()]));
      } else {
        request = junitRequestFromSearchString(getLastTest(), classesAndClassesAndMethods);
        if (request == null) {
          caller.message(String.format("'%s' not found.", getLastTest()));
          return;
        }
      }

      JUnitCore runner = new JUnitCore();
      runner.addListener(new TestRunListener(caller));
      runner.run(request);
    }

    private void rememberTest(String testName) {
      //hackey way of getting last test to survive class reload, which makes /t much more convenient
      System.setProperty("lastTest", testName);
    }

    private String getLastTest() {
      return System.getProperty("lastTest");
    }

    private ClassesAndClassesAndMethods scanDir() {
      List<Class> testClasses = new ArrayList<>();
      List<ClassAndMethod> testClassAndMethod = new ArrayList<>();
      try {
        Files.walkFileTree(new File(rootPath).toPath(), new SimpleFileVisitor<Path>() {
          @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            if (file.toString().endsWith("Test.class")) {
              String className =
                  file.toString()
                      .replace(rootPath + "/", "")
                      .replace(".class", "")
                      .replace('/', '.');
              try {
                Class testClass = Class.forName(className);
                testClasses.add(testClass);

                for (Method m : testClass.getDeclaredMethods()) {
                  if (m.isAnnotationPresent(Test.class)) {
                    testClassAndMethod.add(new ClassAndMethod(testClass, m.getName()));
                  }
                }
              } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
              }
            }
            return FileVisitResult.CONTINUE;
          }
        });
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return new ClassesAndClassesAndMethods(testClasses, testClassAndMethod);
    }

    private Request junitRequestFromSearchString(String search, ClassesAndClassesAndMethods classesAndClassesAndMethods) {
      Request request = null;
      Optional<Class> classResult = classesAndClassesAndMethods.classes.stream()
          .filter(k -> k.getSimpleName().equals(search))
          .findFirst();
      if (classResult.isPresent()) {
        request = Request.classes(classResult.get());
      } else {
        Optional<ClassAndMethod> methodResult = classesAndClassesAndMethods.classesAndMethods.stream()
            .filter(classAndMethod -> classAndMethod.getMethod().equals(search))
            .findFirst();
        if (methodResult.isPresent()) {
          request = Request.method(methodResult.get().getKlass(), methodResult.get().getMethod());
        } else {
          return null;
        }
      }
      return request;
    }
  }

  public static class ClassesAndClassesAndMethods {
    public final List<Class> classes;
    public final List<ClassAndMethod> classesAndMethods;

    public ClassesAndClassesAndMethods(List<Class> classes,
        List<ClassAndMethod> classesAndMethods) {
      this.classes = classes;
      this.classesAndMethods = classesAndMethods;
    }
  }

  public static class ClassAndMethod {
    private final Class klass;
    private final String method;

    public ClassAndMethod(Class klass, String method) {
      this.klass = klass;
      this.method = method;
    }

    public Class getKlass() {
      return klass;
    }

    public String getMethod() {
      return method;
    }

    public String toString() {
      return String.format("%s#%s", klass.getName(), method);
    }
  }

  public static class TestRunListener extends org.junit.runner.notification.RunListener {
    private MessageReceiver caller;

    public TestRunListener(MessageReceiver caller) {
      this.caller = caller;
    }

    private void println(String s) {
      caller.message("[TEST] " + s);
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
