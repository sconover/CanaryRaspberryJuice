package com.stuffaboutcode.canaryraspberryjuicetest;

import com.google.common.base.Strings;
import com.stuffaboutcode.canaryraspberryjuice.CuboidReference;
import com.stuffaboutcode.canaryraspberryjuicetest.support.InWorldTestSupport;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Position;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests api methods that are in addition to the original Minecraft Pi Api
 */
public class ExtendedApiTest  extends InWorldTestSupport {
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
