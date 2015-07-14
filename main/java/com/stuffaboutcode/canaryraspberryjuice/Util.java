package com.stuffaboutcode.canaryraspberryjuice;

import net.canarymod.api.world.position.Position;

public class Util {
  public static Position positionRelativeTo(Position p, Position relativeTo) {
    return new Position(
        (int)p.getX() - (int)relativeTo.getX(),
        (int)p.getY() - (int)relativeTo.getY(),
        (int)p.getZ() - (int)relativeTo.getZ());
  }

  public static String positionToApiString(Position p) {
    return String.format("%.1f,%.1f,%.1f", p.getX(), p.getY(), p.getZ());
  }
}
