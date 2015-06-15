package com.stuffaboutcode.canaryraspberryjuicetest;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.stuffaboutcode.canaryraspberryjuicetest.support.CuboidReference;
import com.stuffaboutcode.canaryraspberryjuicetest.support.FileHelper;
import com.stuffaboutcode.canaryraspberryjuicetest.support.InWorldTestSupport;
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
  public void test_world_getBlocks() throws Exception {
    Position p = nextTestPosition("world.getBlocks");
    int px = (int)p.getX();
    int py = (int)p.getY();
    int pz = (int)p.getZ();

    new CuboidReference(p, 3, 3, 3)
        .fetchBlocks(getServerHelper().getWorld())
        .changeBlocksToType(BlockType.RedstoneBlock);

    String redstoneBlockIdStr = String.valueOf(BlockType.RedstoneBlock.getId());
    String twentySevernRedstoneBlocksString = Strings.repeat(redstoneBlockIdStr + ",", 26) + redstoneBlockIdStr;

    getCommandHandler().handleLine(
        String.format("world.getBlocks(%d,%d,%d,%d,%d,%d)",
            px + 0, py + 0, pz + 0,
            px + 0, py + 0, pz + 0));
    assertEquals(redstoneBlockIdStr, getTestOut().sends.get(0));

    getCommandHandler().handleLine(
        String.format("world.getBlocks(%d,%d,%d,%d,%d,%d)",
            px + 0, py + 0, pz + 0,
            px + 2, py + 2, pz + 2));
    assertEquals(
        twentySevernRedstoneBlocksString,
        getTestOut().sends.get(1));

    getCommandHandler().handleLine(
        String.format("world.getBlocks(%d,%d,%d,%d,%d,%d)",
            px + 2, py + 0, pz + 0,
            px + 0, py + 2, pz + 2));
    assertEquals(
        twentySevernRedstoneBlocksString,
        getTestOut().sends.get(2));

    getCommandHandler().handleLine(
        String.format("world.getBlocks(%d,%d,%d,%d,%d,%d)",
            px + 2, py + 0, pz + 2,
            px + 0, py + 2, pz + 0));
    assertEquals(
        twentySevernRedstoneBlocksString,
        getTestOut().sends.get(3));

    getCommandHandler().handleLine(
        String.format("world.getBlocks(%d,%d,%d,%d,%d,%d)",
            px + 0, py + 0, pz + 2,
            px + 2, py + 2, pz + 0));
    assertEquals(
        twentySevernRedstoneBlocksString,
        getTestOut().sends.get(4));
  }
}
