package com.stuffaboutcode.canaryraspberryjuice;

import com.google.common.base.Joiner;
import java.lang.reflect.Method;
import net.canarymod.api.world.blocks.BlockType;

public class ApiIO {
  public static Object[] convertArguments(String[] args, Method m) {
    // TODO: validate args length == method param length

    Object[] convertedArgs = new Object[args.length];
    Class<?>[] parameterTypes = m.getParameterTypes();
    for (int i=0; i<args.length; i++) {
      convertedArgs[i] = convertArgument(args[i], parameterTypes[i]);
    }
    return convertedArgs;
  }

  public static Object convertArgument(String arg, Class parameterType) {
    if (parameterType.equals(String.class)) {
      return arg;
    } else if (parameterType.equals(int.class)) {
      // TODO: validate the string
      return Integer.parseInt(arg);
    }
    throw new RuntimeException(String.format(
        "not sure how to convert arg %s to %s", arg, parameterType.getName()));
  }

  public static String serializeResult(Object objectResult) {
    if (objectResult instanceof BlockType) {
      return String.valueOf(((BlockType) objectResult).getId());
    } else if (objectResult instanceof BlockType[]) {
      BlockType[] blockTypes = (BlockType[]) objectResult;
      String[] strings = new String[blockTypes.length];
      for (int i = 0; i < blockTypes.length; i++) {
        strings[i] = serializeResult(blockTypes[i]);
      }
      return serializeResult(strings);
    } else if (objectResult instanceof String[]) {
      return Joiner.on(",").join((String[]) objectResult);
    }
    throw new RuntimeException(String.format(
        "not sure how to serialize %s %s",
        objectResult.getClass().getName(),
        objectResult.toString()));
  }
}
