package com.stuffaboutcode.canaryraspberryjuice;

import com.google.common.base.Joiner;
import net.canarymod.api.world.blocks.BlockType;

public class ApiResultSerializer {
  public static String serialize(Object objectResult) {
    if (objectResult instanceof BlockType) {
      return String.valueOf(((BlockType) objectResult).getId());
    } else if (objectResult instanceof BlockType[]) {
      BlockType[] blockTypes = (BlockType[]) objectResult;
      String[] strings = new String[blockTypes.length];
      for (int i = 0; i < blockTypes.length; i++) {
        strings[i] = serialize(blockTypes[i]);
      }
      return serialize(strings);
    } else if (objectResult instanceof String[]) {
      return Joiner.on(",").join((String[]) objectResult);
    }
    throw new RuntimeException(String.format(
        "not sure how to serialize %s %s",
        objectResult.getClass().getName(),
        objectResult.toString()));
  }
}
