package com.stuffaboutcode.canaryraspberryjuicetest.support;

import net.canarymod.api.world.position.Location;
import net.canarymod.api.world.position.Position;

/**
 * Convenience methods for use in tests, for determining a CanaryMod "Location"
 * (position + pitch + rotation)
 */
public class LocationHelper {
  public static Location getLocationFacingPosition(Position p, int xOffset, int yOffset,
      int zOffset) {
    float rotation = 0.0f;

    //TODO this is crude, calculate tangent instead
    if (xOffset < 0) {
      rotation = 270.0f;
    }
    if (xOffset > 0) {
      rotation = 90.0f;
    }
    if (zOffset < 0) {
      rotation = 0.0f;
    }
    if (zOffset > 0) {
      rotation = 180.0f;
    }

    float pitch = 0.0f;

    Location newLocation =
        new Location(p.getBlockX() + xOffset, p.getBlockY() + yOffset, p.getBlockZ() + zOffset);
    newLocation.setRotation(rotation);
    newLocation.setPitch(pitch);
    return newLocation;
  }
}
