package com.stuffaboutcode.canaryraspberryjuice;

import com.stuffaboutcode.canaryraspberryjuice.apis.ExtendedApi;
import com.stuffaboutcode.canaryraspberryjuice.apis.OriginalApi;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import net.canarymod.api.world.position.Position;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.logger.Logman;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class CommandHandler {
  private final Logman logman;
  private final RemoteSession.Out out;

  private final ArrayDeque<BlockRightClickHook> blockHitQueue =
      new ArrayDeque<BlockRightClickHook>();

  private final Map<Pair<String, Integer>, Pair<Object, Method>>
      apiMethodNameAndParameterCountToApiObjectAndMethod =
      new LinkedHashMap<Pair<String, Integer>, Pair<Object, Method>>();
  private final Map<String, Pair<Object, Method>>
      apiMethodNameAcceptingRawArgStringToApiObjectAndMethod =
      new LinkedHashMap<String, Pair<Object, Method>>();

  public CommandHandler(ServerWrapper serverWrapper, Logman logman, RemoteSession.Out out) {
    this.logman = logman;
    this.out = out;

    Position origin = serverWrapper.getSpawnPosition();
    registerApiMethods(new OriginalApi(origin, serverWrapper, blockHitQueue, logman));
    registerApiMethods(new ExtendedApi(origin, serverWrapper, logman));
  }

  private void registerApiMethods(Object api) {
    for (Method m : api.getClass().getMethods()) {
      if (m.isAnnotationPresent(RPC.class)) {
        String apiMethodName = m.getDeclaredAnnotation(RPC.class).value();

        if (m.getParameterAnnotations().length == 1 &&
            m.getParameterAnnotations()[0].getClass().equals(RawArgString.class)) {
          apiMethodNameAcceptingRawArgStringToApiObjectAndMethod.put(
              apiMethodName,
              ImmutablePair.of(api, m));
        } else {
          apiMethodNameAndParameterCountToApiObjectAndMethod.put(
              ImmutablePair.of(apiMethodName, m.getParameterCount()),
              ImmutablePair.of(api, m));
        }
      }
    }
  }

  public void handleLine(String line) {
    //System.out.println(line);
    String methodName = line.substring(0, line.indexOf("("));
    //split string into args, handles , inside " i.e. ","
    String rawArgStr = line.substring(line.indexOf("(") + 1, line.length() - 1);
    String[] args = rawArgStr.equals("") ? new String[] {} : rawArgStr.split(",");
    //System.out.println(methodName + ":" + Arrays.toString(args));
    handleCommand(methodName, args, rawArgStr);
  }

  protected void handleCommand(String c, String[] args, String rawArgStr) {

    try {
      Pair<String, Integer> key = ImmutablePair.of(c, args.length);

      Object apiObject = null;
      Method method = null;
      Object[] convertedArgs = null;

      if (apiMethodNameAcceptingRawArgStringToApiObjectAndMethod.containsKey(c)) {
        Pair<Object, Method> apiObjectAndMethod =
            apiMethodNameAcceptingRawArgStringToApiObjectAndMethod.get(c);
        apiObject = apiObjectAndMethod.getLeft();
        method = apiObjectAndMethod.getRight();

        convertedArgs = new String[] {rawArgStr};
      } else if (apiMethodNameAndParameterCountToApiObjectAndMethod.containsKey(key)) {
        Pair<Object, Method> apiObjectAndMethod =
            apiMethodNameAndParameterCountToApiObjectAndMethod.get(key);
        apiObject = apiObjectAndMethod.getLeft();
        method = apiObjectAndMethod.getRight();

        convertedArgs = ApiIO.convertArguments(args, method);
      }

      if (method != null) {
        if (method.getReturnType().equals(Void.TYPE)) {
          method.invoke(apiObject, convertedArgs);
        } else {
          out.send(ApiIO.serializeResult(method.invoke(apiObject, convertedArgs)));
        }
        return;
      }

      logman.warn(c + " is not supported.");
      out.send("Fail");
    } catch (Exception e) {

      logman.warn("Error occured handling command");
      e.printStackTrace();
      out.send("Fail");
    }
  }

  // add a block hit to the queue to be processed
  public void queueBlockHit(BlockRightClickHook hitHook) {
    blockHitQueue.add(hitHook);
  }
}
