package com.stuffaboutcode.canaryraspberryjuice;

import com.google.common.base.Preconditions;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.position.Position;

/**
 * A "potential" cuboid (collection of blocks) in a minecraft world.
 *
 * Intentionally does not depend on any Minecraft server connection.
 */
public class CuboidReference {

  public static CuboidReference fromCorners(Position p1, Position p2) {
    int minX = p1.getBlockX() < p2.getBlockX() ? p1.getBlockX() : p2.getBlockX();
    int maxX = p1.getBlockX() >= p2.getBlockX() ? p1.getBlockX() : p2.getBlockX();
    int minY = p1.getBlockY() < p2.getBlockY() ? p1.getBlockY() : p2.getBlockY();
    int maxY = p1.getBlockY() >= p2.getBlockY() ? p1.getBlockY() : p2.getBlockY();
    int minZ = p1.getBlockZ() < p2.getBlockZ() ? p1.getBlockZ() : p2.getBlockZ();
    int maxZ = p1.getBlockZ() >= p2.getBlockZ() ? p1.getBlockZ() : p2.getBlockZ();

    Position start = new Position(minX, minY, minZ);

    return new CuboidReference(start, maxX-minX+1, maxY-minY+1, maxZ-minZ+1);
  }

  private final Position start;
  private final int xSize;
  private final int ySize;
  private final int zSize;

  public CuboidReference(Position start, int xSize, int ySize, int zSize) {
    Preconditions.checkArgument(
        xSize >= 1 && ySize >= 1 && zSize >= 1,
        "cuboid must be at least 1x1x1");
    this.start = start;
    this.xSize = xSize;
    this.ySize = ySize;
    this.zSize = zSize;
  }

  public Cuboid fetchBlocks(World world) {
    //TODO real-world bounds checking

    Block corner = world.getBlockAt(start);

    Block[][][] result = new Block[xSize][ySize][zSize];
    for (int x = 0; x < xSize; x++) {
      for (int y = 0; y < ySize; y++) {
        for (int z = 0; z < zSize; z++) {
          result[x][y][z] = corner.getRelative(x, y, z);
        }
      }
    }
    return new Cuboid(result);
  }

  public CuboidReference center() {
    int centerXStart = xSize % 2 == 0 ? xSize / 2 - 1 : xSize / 2;
    int centerXSize = xSize % 2 == 0 ? 2 : 1;

    int centerYStart = ySize % 2 == 0 ? ySize / 2 - 1 : ySize / 2;
    int centerYSize = ySize % 2 == 0 ? 2 : 1;

    int centerZStart = zSize % 2 == 0 ? zSize / 2 - 1 : zSize / 2;
    int centerZSize = zSize % 2 == 0 ? 2 : 1;

    Position newStart = new Position(
        start.getBlockX() + centerXStart,
        start.getBlockY() + centerYStart,
        start.getBlockZ() + centerZStart);

    return new CuboidReference(newStart, centerXSize, centerYSize, centerZSize);
  }
}
