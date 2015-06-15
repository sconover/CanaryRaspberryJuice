package com.stuffaboutcode.canaryraspberryjuicetest;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.stuffaboutcode.canaryraspberryjuice.CommandHandler;
import com.stuffaboutcode.canaryraspberryjuice.RemoteSession;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.canarymod.Canary;
import net.canarymod.api.Server;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.CanarySign;
import net.canarymod.api.world.blocks.properties.helpers.BlockProperties;
import net.canarymod.api.world.blocks.properties.helpers.StandingSignProperties;
import net.canarymod.api.world.position.Location;
import net.canarymod.api.world.position.Position;
import net.canarymod.logger.Logman;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FooTest {

  private ServerHelper serverHelper;
  private static int xOffset;
  private CuboidReference testCuboidRef;

  @BeforeClass
  public static void setUpOnce() {
    xOffset = 2;
  }

  @Before
  public void setUp() throws Exception {
    serverHelper = new ServerHelper(Canary.getServer());
    serverHelper.getWorld().setSpawnLocation(new Location(serverHelper.getWorld(), new Position(0,0,0)));
  }

  private Position nextTestPosition(String name) {
    Position testPosition = new Position(xOffset, 100, 2);
    Position justBeforeTestPosition = new Position(xOffset-1, 99, 1);
    CuboidReference ref = new CuboidReference(justBeforeTestPosition, 31, 51, 31);
    ref.fetchBlocks(serverHelper.getWorld()).makeEmpty();

    Block block = serverHelper.getWorld().getBlockAt(justBeforeTestPosition);
    block.setType(BlockType.GoldBlock);
    block.update();

    Block blockAbove = block.getRelative(0, 1, 0);
    blockAbove.setType(BlockType.StandingSign);
    StandingSignProperties.applyRotation(blockAbove, BlockProperties.Rotation.NORTH);
    blockAbove.update();

    CanarySign sign = (CanarySign)blockAbove.getTileEntity();
    sign.setTextOnLine(name, 0);
    //sign.setTextOnLine("bar", 1);
    //sign.setTextOnLine("zzz", 2);
    //sign.setTextOnLine("yyy", 3);

    serverHelper.getFirstPlayer().teleportTo(
        LocationHelper.getLocationFacingPosition(testPosition, 0, 10, -30));
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    xOffset += 30;

    return testPosition;
  }

  /**
   * TODO:
   *
   * - Shift player to a location relative to a position
   * - Locate tests at 50 blocks above ground level at 0,0
   * - Shift player to each new test area...
   * ...shift test start position accordingly
   *
   *
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
  public void test_chat_post() throws Exception {
    String chatMessage = String.format("this-is-the-chat-message-%d", System.currentTimeMillis());

    CommandHandler commandHandler =
        new CommandHandler(
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

  @Test
  public void test_world_getBlock() throws Exception {
    Position p = nextTestPosition("world.getBlock");

    Block block = serverHelper.getWorld().getBlockAt(p);
    block.setType(BlockType.RedstoneBlock);
    block.update();

    TestOut out = new TestOut();
    CommandHandler commandHandler =
        new CommandHandler(
            Canary.getServer(),
            Logman.getLogman("FooTest-logman"),
            out);

    commandHandler.handleLine(
        String.format("world.getBlock(%d,%d,%d)",
            (int)p.getX(),
            (int)p.getY(),
            (int)p.getZ()));

    assertEquals(
        Lists.newArrayList(String.valueOf(BlockType.RedstoneBlock.getId())),
        out.sends);
  }

  @Test
  public void testChangeType() {
    Position p = nextTestPosition("testChangeType");
    Cuboid cuboid = new CuboidReference(p, 10, 10, 10)
        .fetchBlocks(serverHelper.getWorld());

    cuboid.makeEmpty();
    assertEquals(BlockType.Air, serverHelper.getWorld().getBlockAt(p).getType());
    cuboid.changeBlocksToType(BlockType.GoldBlock);
    assertEquals(BlockType.GoldBlock, serverHelper.getWorld().getBlockAt(p).getType());
  }

  @Test
  public void testCuboid() {
    Position topOfWorld = new Position(1, 250, 1);

    CuboidReference topRef = new CuboidReference(topOfWorld, 1, 1, 1);
    Cuboid cuboid = topRef.fetchBlocks(serverHelper.getWorld());
    List<Relative<Block>> blockLocations = Lists.newArrayList(cuboid);
    assertEquals(1, blockLocations.size());
    assertEquals(new Position(1, 250, 1), blockLocations.get(0).object.getPosition());
    assertEquals(BlockType.Air, blockLocations.get(0).object.getType());

    CuboidReference topLargerRef = new CuboidReference(topOfWorld, 2, 3, 2);
    cuboid = topLargerRef.fetchBlocks(serverHelper.getWorld());
    blockLocations = Lists.newArrayList(cuboid);
    assertEquals(12, blockLocations.size());
    assertEquals(new Position(1, 250, 1), blockLocations.get(0).object.getPosition());
    assertEquals(new Position(1, 250, 2), blockLocations.get(1).object.getPosition());
    assertEquals(new Position(1, 251, 1), blockLocations.get(2).object.getPosition());
    assertEquals(new Position(1, 251, 2), blockLocations.get(3).object.getPosition());
    assertEquals(new Position(1, 252, 1), blockLocations.get(4).object.getPosition());
    assertEquals(new Position(1, 252, 2), blockLocations.get(5).object.getPosition());
    assertEquals(new Position(2, 250, 1), blockLocations.get(6).object.getPosition());
    assertEquals(new Position(2, 250, 2), blockLocations.get(7).object.getPosition());
    assertEquals(new Position(2, 251, 1), blockLocations.get(8).object.getPosition());
    assertEquals(new Position(2, 251, 2), blockLocations.get(9).object.getPosition());
    assertEquals(new Position(2, 252, 1), blockLocations.get(10).object.getPosition());
    assertEquals(new Position(2, 252, 2), blockLocations.get(11).object.getPosition());

    assertTrue(cuboid.isAir());

    Position bottomOfWorld = new Position(0, 0, 0);
    cuboid = new CuboidReference(bottomOfWorld, 2, 4, 2).fetchBlocks(serverHelper.getWorld());
    blockLocations = Lists.newArrayList(cuboid);
    assertEquals(16, blockLocations.size());

    assertFalse(cuboid.isAir());
  }

  @Test
  public void testCuboidCenter() {
    Position topOfWorld = new Position(1, 250, 1);

    assertEquals(
        Arrays.asList(
            new Position(1, 251, 1),
            new Position(1, 251, 2),
            new Position(2, 251, 1),
            new Position(2, 251, 2)),
        new CuboidReference(topOfWorld, 2, 3, 2)
            .center()
            .fetchBlocks(serverHelper.getWorld())
            .stream()
            .map(rb -> rb.object.getPosition())
            .collect(Collectors.toList()));

    assertEquals(
        Arrays.asList(new Position(1, 250, 1)),
        new CuboidReference(topOfWorld, 1, 1, 1)
            .center()
            .fetchBlocks(serverHelper.getWorld())
            .stream()
            .map(rb -> rb.object.getPosition())
            .collect(Collectors.toList()));
  }

  @Test
  public void testMovePlayer() throws Exception {
    CuboidReference ref = new CuboidReference(new Position(1, 100, 1), 5, 5, 5);
    Cuboid goldCube = ref.fetchBlocks(serverHelper.getWorld());
    goldCube.changeBlocksToType(BlockType.GoldBlock);
    serverHelper.getFirstPlayer().teleportTo(
        LocationHelper.getLocationFacingPosition(new Position(1, 100, 1), 30, 10, 0));

    assertEquals(new Position(31, 110, 1), serverHelper.getFirstPlayer().getPosition());
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

    // get the host player, i.e. the first player on the server
    public Player getFirstPlayer() {
      Preconditions.checkState(
          ! server.getPlayerList().isEmpty(),
          "must have logged-in players in order for this to work");

      return server.getPlayerList().get(0);
    }

    // get the host player, i.e. the first player on the server
    public boolean hasPlayers() {
      return ! server.getPlayerList().isEmpty();
    }
  }

  static class LocationHelper {
    public static Location getLocationFacingPosition(Position p, int xOffset, int yOffset, int zOffset) {
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

      Location newLocation = new Location(p.getBlockX() + xOffset, p.getBlockY() + yOffset, p.getBlockZ() + zOffset);
      newLocation.setRotation(rotation);
      newLocation.setPitch(pitch);
      return newLocation;
    }
  }

  static class Relative<T> {
    public final T object;
    public final int x;
    public final int y;
    public final int z;

    public Relative(T object, int x, int y, int z) {
      this.object = object;
      this.x = x;
      this.y = y;
      this.z = z;
    }
  }

  static class CuboidReference {
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

  static class Cuboid implements Iterable<Relative<Block>> {
    private final Block[][][] blocks;

    public Cuboid(Block[][][] blocks) {
      this.blocks = blocks;
    }

    public Set<BlockType> blockTypes() {
      Set<BlockType> blockTypes = new LinkedHashSet<>();
      for (Relative<Block> blockLocation : this) {
        blockTypes.add(blockLocation.object.getType());
      }
      return blockTypes;
    }

    public boolean isUniformType(BlockType blockType) {
      return blockTypes().equals(Sets.newHashSet(blockType));
    }

    public boolean isAir() {
      return isUniformType(BlockType.Air);
    }

    public Iterator<Relative<Block>> iterator() {
      return new RelativeBlockIterator(blocks);
    }

    public Stream<Relative<Block>> stream() {
      return StreamSupport.stream(
          Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED),
          false);
    }

    public Cuboid changeBlocksToType(BlockType newType) {
      stream().forEach((relativeBlock) -> {
        relativeBlock.object.setType(newType);
        relativeBlock.object.update();
      });
      return this;
    }

    public Cuboid makeEmpty() {
      changeBlocksToType(BlockType.Air);
      return this;
    }

    public Block firstBlock() {
      return blocks[0][0][0];
    }

    class RelativeBlockIterator implements Iterator<Relative<Block>> {
      private final Block[][][] blocks;
      private int x;
      private int y;
      private int z;
      private Relative next;

      public RelativeBlockIterator(Block[][][] blocks) {
        this.blocks = blocks;

        Preconditions.checkState(
            blocks.length >= 1 &&
                blocks[0].length >= 1 &&
                blocks[0][0].length >= 1,
            "3D block array must be at least 1x1x1");
        this.next = new Relative<Block>(blocks[0][0][0], 0, 0, 0);
      }

      @Override public boolean hasNext() {
        return next != null;
      }

      @Override public Relative<Block> next() {
        Relative<Block> result = next;
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
        next = new Relative<Block>(blocks[x][y][z], x, y, z);
      }
    }
  }

  public static class TestOut implements RemoteSession.Out {
    public List<String> sends = new ArrayList<>();

    @Override public void send(String str) {
      sends.add(str);
    }
  }
}