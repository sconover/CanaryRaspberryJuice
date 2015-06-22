package com.stuffaboutcode.canaryraspberryjuicetest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.stuffaboutcode.canaryraspberryjuice.Blocks;
import com.stuffaboutcode.canaryraspberryjuicetest.support.FileHelper;
import com.stuffaboutcode.canaryraspberryjuicetest.support.InWorldTestSupport;
import java.util.Map;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Position;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests all api methods expected to be available on the Minecraft Pi edition.
 */
public class MinecraftPiApiTest extends InWorldTestSupport {

  /**
   * TODO:
   *
   * - slow things down... take ticks/sec into
   * consideration... adjust to wall clock time. e.g. 200 ms / write operation
   *
   * - stay end to end for a while...
   */

  @Test
  public void test_chat_post() throws Exception {
    String chatMessage = String.format("this-is-the-chat-message-%d", System.currentTimeMillis());

    getCommandHandler().handleLine(String.format("chat.post(%s)", chatMessage));

    String last20LinesOfLogFile = FileHelper.readEndOfLogfile();

    assertTrue(
        String.format("expected '%s' to be present, but was not. full text:\n\n%s",
            chatMessage,
            last20LinesOfLogFile),
        last20LinesOfLogFile.contains(chatMessage));
  }

  @Test
  public void test_world_getBlock() throws Exception {
    Position p = nextTestPosition("world.getBlock");

    Block block = getServerHelper().getWorld().getBlockAt(p);
    block.setType(BlockType.RedstoneBlock);
    block.update();

    getCommandHandler().handleLine(
        String.format("world.getBlock(%d,%d,%d)",
            (int) p.getX(),
            (int) p.getY(),
            (int) p.getZ()));

    assertEquals(
        Lists.newArrayList(String.valueOf(BlockType.RedstoneBlock.getId())),
        getTestOut().sends);
  }

  @Test
  public void test_world_getBlockWithData() throws Exception {
    Position p = nextTestPosition("world.getBlockWithData");

    Block block = getServerHelper().getWorld().getBlockAt(p);
    block.setType(BlockType.RedstoneBlock);
    block.update();

    Block block2 = block.getRelative(1, 0, 0);
    block2.setType(BlockType.GreenWool);
    block2.update();

    getCommandHandler().handleLine(
        String.format("world.getBlockWithData(%d,%d,%d)",
            (int) p.getX(),
            (int) p.getY(),
            (int) p.getZ()));

    assertEquals(
        String.format("%d,%d",
            BlockType.RedstoneBlock.getId(),
            BlockType.RedstoneBlock.getData()),
        getTestOut().sends.get(0));

    getCommandHandler().handleLine(
        String.format("world.getBlockWithData(%d,%d,%d)",
            (int) p.getX() + 1,
            (int) p.getY(),
            (int) p.getZ()));

    assertEquals(
        String.format("%d,%d",
            BlockType.GreenWool.getId(),
            BlockType.GreenWool.getData()),
        getTestOut().sends.get(1));
  }

  @Test
  public void test_world_setBlock() throws Exception {
    Position p = nextTestPosition("world.setBlock");

    getCommandHandler().handleLine(
        String.format("world.setBlock(%d,%d,%d,%d)",
            (int) p.getX(),
            (int) p.getY(),
            (int) p.getZ(),
            BlockType.RedstoneBlock.getId()));

    Block block = getServerHelper().getWorld().getBlockAt(p);
    assertEquals(BlockType.RedstoneBlock, block.getType());

    getCommandHandler().handleLine(
        String.format("world.setBlock(%d,%d,%d,%d,%d)",
            (int) p.getX() + 1,
            (int) p.getY(),
            (int) p.getZ(),
            BlockType.GreenWool.getId(),
            BlockType.GreenWool.getData()));

    Block block2 = getServerHelper().getWorld().getBlockAt(
        (int)p.getX() + 1,
        (int)p.getY(),
        (int)p.getZ());

    assertEquals(BlockType.GreenWool, block2.getType());
    assertEquals(BlockType.GreenWool.getData(), block2.getType().getData());
  }

  @Test
  public void test_world_setBlocks() throws Exception {
    Position cubeCorner = nextTestPosition("world.setBlocks");

    Position otherCubeCorner =
        new Position(
            cubeCorner.getX() + 1,
            cubeCorner.getY() + 1,
            cubeCorner.getZ() + 1);

    getCommandHandler().handleLine(
        String.format("world.setBlocks(%d,%d,%d,%d,%d,%d,%d)",
            (int) cubeCorner.getX(),
            (int) cubeCorner.getY(),
            (int) cubeCorner.getZ(),
            (int) otherCubeCorner.getX(),
            (int) otherCubeCorner.getY(),
            (int) otherCubeCorner.getZ(),
            BlockType.RedstoneBlock.getId()));

    Map<BlockType, Blocks> blockTypeToBlocks =
        getServerHelper().getBlocks(
        cubeCorner, otherCubeCorner).toBlockTypeToBlocks();

    // there's a 2x2x2 set of redstone blocks
    assertEquals(Sets.newHashSet(BlockType.RedstoneBlock), blockTypeToBlocks.keySet());
    assertEquals(8, blockTypeToBlocks.get(BlockType.RedstoneBlock).size());

    Position pastOtherCubeCorner =
        new Position(
            cubeCorner.getX() + 2,
            cubeCorner.getY() + 2,
            cubeCorner.getZ() + 2);

    Map<BlockType, Blocks> blockTypeToBlocks2 =
        getServerHelper().getBlocks(
            cubeCorner, pastOtherCubeCorner).toBlockTypeToBlocks();

    // out of this 3x3x3 cube, there's a 2x2x2 set of redstone blocks,
    // and the rest is air
    assertEquals(Sets.newHashSet(
        BlockType.RedstoneBlock,
        BlockType.Air),
        blockTypeToBlocks2.keySet());
    assertEquals(8, blockTypeToBlocks2.get(BlockType.RedstoneBlock).size());
    assertEquals(27-8, blockTypeToBlocks2.get(BlockType.Air).size());

    //assertEquals(BlockType.RedstoneBlock, block.getType());
    //
    //getCommandHandler().handleLine(
    //    String.format("world.setBlock(%d,%d,%d,%d,%d)",
    //        (int) cubeCorner.getX() + 1,
    //        (int) cubeCorner.getY(),
    //        (int) cubeCorner.getZ(),
    //        BlockType.GreenWool.getId(),
    //        BlockType.GreenWool.getData()));
    //
    //Block block2 = getServerHelper().getWorld().getBlockAt(
    //    (int)cubeCorner.getX() + 1,
    //    (int)cubeCorner.getY(),
    //    (int)cubeCorner.getZ());
    //
    //assertEquals(BlockType.GreenWool, block2.getType());
    //assertEquals(BlockType.GreenWool.getData(), block2.getType().getData());
  }
}
