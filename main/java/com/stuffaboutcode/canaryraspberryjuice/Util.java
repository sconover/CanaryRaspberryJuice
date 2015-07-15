package com.stuffaboutcode.canaryraspberryjuice;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import net.canarymod.api.world.World;
import net.canarymod.api.world.position.Position;
import net.canarymod.api.world.position.Vector3D;

public class Util {
  public static Position positionRelativeTo(Position p, Position relativeTo) {
    return new Position(
        (int) p.getX() - (int) relativeTo.getX(),
        (int) p.getY() - (int) relativeTo.getY(),
        (int) p.getZ() - (int) relativeTo.getZ());
  }

  public static String vectorToApiString(Vector3D v) {
    return String.format("%f,%f,%f", v.getX(), v.getY(), v.getZ());
  }

  public static String positionToApiString(Position p) {
    return String.format("%.1f,%.1f,%.1f", p.getX(), p.getY(), p.getZ());
  }

  public static void makeSureChunksHaveBeenGenerated(World world, Position start) {
    makeSureChunksHaveBeenGenerated(world, start, 0, 0);
  }

  public static void makeSureChunksHaveBeenGenerated(
      final World world,
      Position start, int xSize, int zSize) {
    final int chunkX1 = start.getBlockX() >> 4;
    final int chunkX2 = (start.getBlockX() + xSize - 1) >> 4;
    final int chunkZ1 = start.getBlockZ() >> 4;
    final int chunkZ2 = (start.getBlockZ() + zSize - 1) >> 4;

    waitForChunkToLoadWithTimeout(world, chunkX1, chunkZ1, 5, TimeUnit.SECONDS);

    if (chunkX2 != chunkX1) {
      waitForChunkToLoadWithTimeout(world, chunkX2, chunkZ1, 5, TimeUnit.SECONDS);
    }

    if (chunkZ2 != chunkZ1) {
      waitForChunkToLoadWithTimeout(world, chunkX1, chunkZ2, 5, TimeUnit.SECONDS);
    }

    if (chunkZ2 != chunkZ1 && chunkX2 != chunkX1) {
      waitForChunkToLoadWithTimeout(world, chunkX2, chunkZ2, 5, TimeUnit.SECONDS);
    }
  }

  private static void waitForChunkToLoadWithTimeout(
      final World world,
      final int chunkX,
      final int chunkZ,
      int timeout,
      TimeUnit timeUnit) {
    try {
      new SimpleTimeLimiter().callWithTimeout(new Callable<Object>() {
        @Override public Object call() throws Exception {
          //TODO: time me
          while (!world.loadChunk(chunkX, chunkZ).isTerrainPopulated()) {
            Thread.sleep(10);
          }
          return null;
        }
      }, timeout, timeUnit, true);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  // creates a unit vector from rotation and pitch
  // origin is https://github.com/Bukkit/Bukkit/blob/master/src/main/java/org/bukkit/Location.java
  // "setDirection"
  public static Vector3D calculateDirection(float pitch, float rotation) {
    double rotationRad = Math.toRadians(rotation);
    double pitchRad = Math.toRadians(pitch);
    double x = (Math.sin(rotationRad) * Math.cos(pitchRad)) * -1;
    double y = Math.sin(pitchRad) * -1;
    double z = Math.cos(rotationRad) * Math.cos(pitchRad);
    return new Vector3D(x, y, z);
  }
}
