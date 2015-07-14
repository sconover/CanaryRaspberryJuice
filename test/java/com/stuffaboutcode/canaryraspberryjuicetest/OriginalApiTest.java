package com.stuffaboutcode.canaryraspberryjuicetest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.stuffaboutcode.canaryraspberryjuice.CuboidReference;
import com.stuffaboutcode.canaryraspberryjuicetest.support.FileHelper;
import com.stuffaboutcode.canaryraspberryjuicetest.support.InWorldTestSupport;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.inventory.ItemType;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Position;
import net.canarymod.hook.player.BlockRightClickHook;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests all api methods expected to be available on the Minecraft Pi edition.
 */
public class OriginalApiTest extends InWorldTestSupport {

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

    Block block = getServerWrapper().getWorld().getBlockAt(p);
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

    Block block = getServerWrapper().getWorld().getBlockAt(p);
    block.setType(BlockType.RedstoneBlock);
    block.update();

    Block block2 = block.getRelative(1, 0, 0);
    block2.setType(BlockType.LimeWool);
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
            BlockType.LimeWool.getId(),
            BlockType.LimeWool.getData()),
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

    Block block = getServerWrapper().getWorld().getBlockAt(p);
    assertEquals(BlockType.RedstoneBlock, block.getType());

    getCommandHandler().handleLine(
        String.format("world.setBlock(%d,%d,%d,%d,%d)",
            (int) p.getX() + 1,
            (int) p.getY(),
            (int) p.getZ(),
            BlockType.LimeWool.getId(),
            BlockType.LimeWool.getData()));

    Block block2 = getServerWrapper().getWorld().getBlockAt(
        (int)p.getX() + 1,
        (int)p.getY(),
        (int)p.getZ());

    assertEquals(BlockType.LimeWool, block2.getType());
    assertEquals(BlockType.LimeWool.getData(), block2.getType().getData());
  }

  @Test
  public void test_world_setBlocks_simple() throws Exception {
    Position cubeCorner = nextTestPosition("world.setBlocks simple");

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

    Map<BlockType, List<Block>> blockTypeToBlocks =
      CuboidReference.fromCorners(cubeCorner, otherCubeCorner)
          .fetchBlocks(getServerWrapper().getWorld())
          .blockTypeToBlocks();

    // there's a 2x2x2 set of redstone blocks
    assertEquals(Sets.newHashSet(BlockType.RedstoneBlock), blockTypeToBlocks.keySet());
    assertEquals(8, blockTypeToBlocks.get(BlockType.RedstoneBlock).size());

    Position pastOtherCubeCorner =
        new Position(
            cubeCorner.getX() + 2,
            cubeCorner.getY() + 2,
            cubeCorner.getZ() + 2);

    Map<BlockType, List<Block>> blockTypeToBlocks2 =
        CuboidReference.fromCorners(cubeCorner, pastOtherCubeCorner)
            .fetchBlocks(getServerWrapper().getWorld())
            .blockTypeToBlocks();

    // out of this 3x3x3 cube, there's a 2x2x2 set of redstone blocks,
    // and the rest is air
    assertEquals(Sets.newHashSet(
        BlockType.RedstoneBlock,
        BlockType.Air),
        blockTypeToBlocks2.keySet());
    assertEquals(8, blockTypeToBlocks2.get(BlockType.RedstoneBlock).size());
    assertEquals(27-8, blockTypeToBlocks2.get(BlockType.Air).size());
  }

  @Test
  public void test_world_setBlocks_withData_whichIsTheColor() throws Exception {
    Position cubeCorner = nextTestPosition("world.setBlocks with data");

    Position otherCubeCorner =
        new Position(
            cubeCorner.getX() + 1,
            cubeCorner.getY() + 1,
            cubeCorner.getZ() + 1);

    getCommandHandler().handleLine(
        String.format("world.setBlocks(%d,%d,%d,%d,%d,%d,%d,%d)",
            (int) cubeCorner.getX(),
            (int) cubeCorner.getY(),
            (int) cubeCorner.getZ(),
            (int) otherCubeCorner.getX(),
            (int) otherCubeCorner.getY(),
            (int) otherCubeCorner.getZ(),
            BlockType.LimeWool.getId(),
            BlockType.LimeWool.getData()));

    Map<BlockType, List<Block>> blockTypeToBlocks =
        CuboidReference.fromCorners(cubeCorner, otherCubeCorner)
            .fetchBlocks(getServerWrapper().getWorld())
            .blockTypeToBlocks();

    // there's a 2x2x2 set of green wool blocks
    assertEquals(Sets.newHashSet(BlockType.LimeWool), blockTypeToBlocks.keySet());
    assertEquals(8, blockTypeToBlocks.get(BlockType.LimeWool).size());
  }

  @Test
  public void test_world_getPlayerEntityIds() throws Exception {
    if (getServerWrapper().hasPlayers()) {

      String expectedPlayerIdsStr = getServerWrapper().getPlayers().stream()
          .map(Player::getID)
          .map(String::valueOf)
          .collect(Collectors.joining("|"));

      getCommandHandler().handleLine("world.getPlayerEntityIds()");

      assertEquals(
          Lists.newArrayList(expectedPlayerIdsStr),
          getTestOut().sends);
    }
  }


  //[09:14] <svdragster> Are you trying to force a player to select a certain slot, or are you trying to put an item in the players hand?
  //    [09:25] <Guest32326> put an item in a player's hand
  //    [09:55] <svdragster> then do and do inventory.setSlot(getSelectedHotbarSlotId(), item)
  //    [09:55] <svdragster> Guest32326
  //[09:56] <svdragster> you might have to set the item to '(Item) null' first

  @Test
  public void test_events_block_hits() throws Exception {
    if (getServerWrapper().hasPlayers()) {

      // TODO: make PlayerWrapper?
      makeFirstPlayerWieldItem(getServerWrapper().getFirstPlayer(), ItemType.GoldSword);

      Position p = nextTestPosition("block hit event");

      getCommandHandler().handleLine(
          String.format("world.setBlock(%d,%d,%d,%d)",
              (int) p.getX(),
              (int) p.getY(),
              (int) p.getZ(),
              BlockType.RedstoneBlock.getId()));

      Block b = getServerWrapper().getWorld().getBlockAt(p);

      getPluginListener().onBlockHit(new BlockRightClickHook(getServerWrapper().getFirstPlayer(), b));

      getCommandHandler().handleLine("events.block.hits()");

      int expectedFace = 7;

      assertEquals(
          Lists.newArrayList(String.format("%d,%d,%d,%d,%d",
              (int)p.getX(),
              (int)p.getY(),
              (int)p.getZ(),
              expectedFace,
              getServerWrapper().getFirstPlayer().getID())),
          getTestOut().sends);
    }
  }

  @Test
  public void test_events_clear() throws Exception {
    if (getServerWrapper().hasPlayers()) {

      // TODO: make PlayerWrapper?
      makeFirstPlayerWieldItem(getServerWrapper().getFirstPlayer(), ItemType.GoldSword);

      Position p = nextTestPosition("block hit event");

      getCommandHandler().handleLine(
          String.format("world.setBlock(%d,%d,%d,%d)",
              (int) p.getX(),
              (int) p.getY(),
              (int) p.getZ(),
              BlockType.RedstoneBlock.getId()));

      Block b = getServerWrapper().getWorld().getBlockAt(p);

      getPluginListener().onBlockHit(new BlockRightClickHook(getServerWrapper().getFirstPlayer(), b));

      getCommandHandler().handleLine("events.clear()");

      getPluginListener().onBlockHit(
          new BlockRightClickHook(getServerWrapper().getFirstPlayer(), b));

      getCommandHandler().handleLine("events.block.hits()");

      int expectedFace = 7;

      assertEquals(
          Lists.newArrayList(String.format("%d,%d,%d,%d,%d",
              (int)p.getX(),
              (int)p.getY(),
              (int)p.getZ(),
              expectedFace,
              getServerWrapper().getFirstPlayer().getID())),
          getTestOut().sends);
    }
  }

  private void makeFirstPlayerWieldItem(Player player, ItemType itemType) {
    player.getInventory().setSlot(itemType.getId(), 0, 0);

    player.getInventory().setSlot(
        player.getInventory().getSelectedHotbarSlotId(),
        player.getInventory().getSlot(0));
  }
}
