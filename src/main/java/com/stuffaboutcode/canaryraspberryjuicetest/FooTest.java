package com.stuffaboutcode.canaryraspberryjuicetest;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.stuffaboutcode.canaryraspberryjuice.RemoteSession;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.canarymod.Canary;
import net.canarymod.api.Server;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Position;
import net.canarymod.logger.Logman;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FooTest {

  private ServerHelper serverHelper;

  @Before
  public void setup() {
    serverHelper = new ServerHelper(Canary.getServer());
  }

  /**
   * - literal fixtures :) e.g. set up some blocks such and such way
   *
   * - literal test run e.g. you can see a line of things executing place the current player in
   * position to watch shift to each new test position? slow things down... take ticks/sec into
   * consideration... adjust to wall clock time. e.g. 200 ms / write operation
   *
   * - stay end to end for a while...
   */

  //TODO(steve) extract methods
  @Test
  public void testPlayerChat() throws Exception {
    String chatMessage = String.format("this-is-the-chat-message-%d", System.currentTimeMillis());

    RemoteSession.CommandHandler commandHandler =
        new RemoteSession.CommandHandler(
            Canary.getServer(),
            Logman.getLogman("FooTest-logman"),
            new TestOut());

    commandHandler.handleLine(String.format("chat.post(%s)", chatMessage));

    ReversedLinesFileReader reader =
        new ReversedLinesFileReader(new File("logs/latest.log"));

    List<String> lines = new ArrayList<>();
    int count = 20;
    String line = reader.readLine();
    while (count > 0 && line != null) {
      lines.add(line);
      count--;
      line = reader.readLine();
    }
    String last20LinesOfLogFile = lines.stream().collect(Collectors.joining("\n"));

    assertTrue(
        String.format("expected '%s' to be present, but was not. full text:\n\n%s",
            chatMessage,
            last20LinesOfLogFile),
        last20LinesOfLogFile.contains(chatMessage));
    // extract assert string contains
  }

  static class ServerHelper {
    private final Server server;
    private final World firstWorld;

    public ServerHelper(Server server) {
      this.server = server;

      Preconditions.checkState(
          server.getWorldManager().getAllWorlds().size() == 1,
          "only supports single-world servers");
      this.firstWorld = server.getWorldManager().getAllWorlds().iterator().next();
    }

    public World getWorld() {
      return firstWorld;
    }
  }

  static class BlockLocation {
    public final Block block;
    public final int x;
    public final int y;
    public final int z;

    public BlockLocation(Block block, int x, int y, int z) {
      this.block = block;
      this.x = x;
      this.y = y;
      this.z = z;
    }
  }

  static class CuboidReference {
    private final World world;
    private final Position start;
    private final int xSize;
    private final int ySize;
    private final int zSize;

    public CuboidReference(World world, Position start, int xSize, int ySize, int zSize) {
      this.world = world;
      this.start = start;
      this.xSize = xSize;
      this.ySize = ySize;
      this.zSize = zSize;
    }

    public Cuboid read() {
      Block[][][] result = new Block[xSize][ySize][zSize];
      for (int x = 0; x < xSize; x++) {
        for (int y = 0; y < ySize; y++) {
          for (int z = 0; z < zSize; z++) {
            Block block =
                world.getBlockAt(
                    start.getBlockX() + x,
                    start.getBlockY() + y,
                    start.getBlockZ() + z);
            result[x][y][z] = block;
          }
        }
      }
      return new Cuboid(result);
    }
  }

  static class Cuboid implements Iterable<BlockLocation> {
    private final Block[][][] blocks;

    public Cuboid(Block[][][] blocks) {
      this.blocks = blocks;
    }

    public Set<BlockType> blockTypes() {
      Set<BlockType> blockTypes = new LinkedHashSet<>();
      for (BlockLocation blockLocation : this) {
        blockTypes.add(blockLocation.block.getType());
      }
      return blockTypes;
    }

    public boolean isUniformType(BlockType blockType) {
      return blockTypes().equals(Sets.newHashSet(blockType));
    }

    public boolean isAir() {
      return isUniformType(BlockType.Air);
    }

    class BlockLocationIterator implements Iterator<BlockLocation> {
      private final Block[][][] blocks;
      private int x;
      private int y;
      private int z;
      private BlockLocation next;

      public BlockLocationIterator(Block[][][] blocks) {
        this.blocks = blocks;

        Preconditions.checkState(
            blocks.length >= 1 &&
                blocks[0].length >= 1 &&
                blocks[0][0].length >= 1,
            "3D block array must be at least 1x1x1");
        this.next = new BlockLocation(blocks[0][0][0], 0, 0, 0);
      }

      @Override public boolean hasNext() {
        return next != null;
      }

      @Override public BlockLocation next() {
        BlockLocation result = next;
        advance();
        return result;
      }

      private void advance() {
        if (x == blocks.length - 1 &&
            y == blocks[0].length - 1 &&
            z == blocks[0][0].length - 1) {
          next = null;
          return;
        } else if (z < blocks[x][y].length - 1) {
          z += 1;
        } else if (y < blocks[x].length - 1) {
          y += 1;
          z = 0;
        } else if (x < blocks.length - 1) {
          x += 1;
          y = 0;
          z = 0;
        }
        next = new BlockLocation(blocks[x][y][z], x, y, z);
      }
    }

    public Iterator<BlockLocation> iterator() {
      return new BlockLocationIterator(blocks);
    }
  }

  @Test
  public void testCuboid() {
    Position topOfWorld = new Position(1, 250, 1);

    Cuboid cuboid = new CuboidReference(serverHelper.getWorld(), topOfWorld, 1, 1, 1).read();
    List<BlockLocation> blockLocations = Lists.newArrayList(cuboid);
    assertEquals(1, blockLocations.size());
    assertEquals(new Position(1, 250, 1), blockLocations.get(0).block.getPosition());
    assertEquals(BlockType.Air, blockLocations.get(0).block.getType());

    cuboid = new CuboidReference(serverHelper.getWorld(), topOfWorld, 2, 3, 2).read();
    blockLocations = Lists.newArrayList(cuboid);
    assertEquals(12, blockLocations.size());
    assertEquals(new Position(1, 250, 1), blockLocations.get(0).block.getPosition());
    assertEquals(new Position(1, 250, 2), blockLocations.get(1).block.getPosition());
    assertEquals(new Position(1, 251, 1), blockLocations.get(2).block.getPosition());
    assertEquals(new Position(1, 251, 2), blockLocations.get(3).block.getPosition());
    assertEquals(new Position(1, 252, 1), blockLocations.get(4).block.getPosition());
    assertEquals(new Position(1, 252, 2), blockLocations.get(5).block.getPosition());
    assertEquals(new Position(2, 250, 1), blockLocations.get(6).block.getPosition());
    assertEquals(new Position(2, 250, 2), blockLocations.get(7).block.getPosition());
    assertEquals(new Position(2, 251, 1), blockLocations.get(8).block.getPosition());
    assertEquals(new Position(2, 251, 2), blockLocations.get(9).block.getPosition());
    assertEquals(new Position(2, 252, 1), blockLocations.get(10).block.getPosition());
    assertEquals(new Position(2, 252, 2), blockLocations.get(11).block.getPosition());

    assertTrue(cuboid.isAir());

    Position bottomOfWorld = new Position(0, 0, 0);
    cuboid = new CuboidReference(serverHelper.getWorld(), bottomOfWorld, 2, 4, 2).read();
    blockLocations = Lists.newArrayList(cuboid);
    assertEquals(16, blockLocations.size());

    assertFalse(cuboid.isAir());
    //
    //Block block = serverHelper.getWorld().getBlockAt(0, 255, 0);
    //block.setType(BlockType.Air);
    //block.update();
    // serverHelper.getWorld().

    // find the first 15x15x15 empty space
    // check backward
    // then check upward in 15 (or 30?) block increments until a 15x15x15 air cube is found
    // if there's a current player, move the player there.

    //// get a cuboid of lots of blocks
    //private String  getBlocks(Location pos1, Location pos2) {
    //  StringBuilder blockData = new StringBuilder();
    //
    //  int minX, maxX, minY, maxY, minZ, maxZ;
    //  World world = pos1.getWorld();
    //  minX = pos1.getBlockX() < pos2.getBlockX() ? pos1.getBlockX() : pos2.getBlockX();
    //  maxX = pos1.getBlockX() >= pos2.getBlockX() ? pos1.getBlockX() : pos2.getBlockX();
    //  minY = pos1.getBlockY() < pos2.getBlockY() ? pos1.getBlockY() : pos2.getBlockY();
    //  maxY = pos1.getBlockY() >= pos2.getBlockY() ? pos1.getBlockY() : pos2.getBlockY();
    //  minZ = pos1.getBlockZ() < pos2.getBlockZ() ? pos1.getBlockZ() : pos2.getBlockZ();
    //  maxZ = pos1.getBlockZ() >= pos2.getBlockZ() ? pos1.getBlockZ() : pos2.getBlockZ();
    //
    //  for (int y = minY; y <= maxY; ++y) {
    //    for (int x = minX; x <= maxX; ++x) {
    //      for (int z = minZ; z <= maxZ; ++z) {
    //        blockData.append(new Integer(world.getBlockAt(x, y, z).getTypeId()).toString() + ",");
    //      }
    //    }
    //  }
    //
    //  return blockData.substring(0, blockData.length() > 0 ? blockData.length() - 1 : 0);	// We don't want last comma
    //}

    // Location loc = parseRelativeBlockLocation(args[0], args[1], args[2]);
    // updateBlock(world, loc, Short.parseShort(args[3]), args.length > 4? Short.parseShort(args[4]) : (short) 0);

    // updates a block
    //private void updateBlock(World world, Location loc, short blockType, short blockData) {
    //  Block thisBlock = world.getBlockAt(loc);
    //  updateBlock(thisBlock, blockType, blockData);
    //}
    //
    //private void updateBlock(World world, int x, int y, int z, short blockType, short blockData) {
    //  Block thisBlock = world.getBlockAt(x,y,z);
    //  updateBlock(thisBlock, blockType, blockData);
    //}
    //
    //private void updateBlock(Block thisBlock, short blockType, short blockData) {
    //  // check to see if the block is different - otherwise leave it
    //  if ((thisBlock.getTypeId() != blockType) || (thisBlock.getData() != blockData)) {
    //    thisBlock.setTypeId(blockType);
    //    thisBlock.setData(blockData);
    //    thisBlock.update();
    //  }
    //}

    // "world.getBlock"

  }

  public static class TestOut implements RemoteSession.Out {
    public List<String> sends = new ArrayList<>();

    @Override public void send(String str) {
      sends.add(str);
    }
  }
}
