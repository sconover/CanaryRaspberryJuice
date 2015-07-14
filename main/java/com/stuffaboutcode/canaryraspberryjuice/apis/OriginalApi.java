package com.stuffaboutcode.canaryraspberryjuice.apis;

import com.google.common.base.Joiner;
import com.stuffaboutcode.canaryraspberryjuice.CuboidReference;
import com.stuffaboutcode.canaryraspberryjuice.RPC;
import com.stuffaboutcode.canaryraspberryjuice.RawArgString;
import com.stuffaboutcode.canaryraspberryjuice.RemoteSession;
import com.stuffaboutcode.canaryraspberryjuice.ServerWrapper;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Location;
import net.canarymod.api.world.position.Position;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.logger.Logman;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class OriginalApi {
  // origin is the spawn location on the world
  private final Location origin;
  private final ServerWrapper serverWrapper;
  private final ArrayDeque<BlockRightClickHook> blockHitQueue;
  private final Logman logman;

  public OriginalApi(
      Location origin,
      ServerWrapper serverWrapper,
      ArrayDeque<BlockRightClickHook> blockHitQueue,
      Logman logman) {
    this.origin = origin;
    this.serverWrapper = serverWrapper;
    this.blockHitQueue = blockHitQueue;
    this.logman = logman;
  }

  @RPC("world.getBlock")
  public BlockType worldGetBlock(int x, int y, int z) {
    return CuboidReference.relativeTo(origin, new Position(x, y, z))
        .fetchBlocks(serverWrapper.getWorld())
        .firstBlock()
        .getType();
  }

  @RPC("world.getBlockWithData")
  public Pair<BlockType, Short> worldGetBlockWithData(int x, int y, int z) {
    BlockType blockType = worldGetBlock(x, y, z);
    return ImmutablePair.of(blockType, blockType.getData());
  }

  @RPC("world.setBlock")
  public void setBlock(int x, int y, int z, short blockTypeId) {
    setBlock(x, y, z, blockTypeId, (short) 0);
  }

  @RPC("world.setBlock")
  public void setBlock(int x, int y, int z, short blockTypeId, short blockData) {
    setBlocks(
        x, y, z,
        x, y, z,
        blockTypeId, blockData);
  }

  @RPC("world.setBlocks")
  public void setBlocks(
      int x1, int y1, int z1,
      int x2, int y2, int z2,
      short blockTypeId) {
    setBlocks(
        x1, y1, z1,
        x2, y2, z2,
        blockTypeId, (short) 0);
  }

  @RPC("world.setBlocks")
  public void setBlocks(
      int x1, int y1, int z1,
      int x2, int y2, int z2,
      short blockTypeId, short blockData) {
    CuboidReference.relativeTo(origin,
        new Position(x1, y1, z1),
        new Position(x2, y2, z2))
        .fetchBlocks(serverWrapper.getWorld())
        .changeBlocksToType(BlockType.fromIdAndData(blockTypeId, blockData));
  }

  @RPC("world.getPlayerEntityIds")
  public Player[] getPlayerEntityIds() {
    List<Player> allPlayers = serverWrapper.getPlayers();
    return allPlayers.toArray(new Player[allPlayers.size()]);
  }

  @RPC("chat.post")
  public void chatPost(@RawArgString String chatStr) {
    serverWrapper.broadcastMessage(chatStr);
  }

  @RPC("events.clear")
  public void clearEvents() {
    blockHitQueue.clear();
  }

  @RPC("events.block.hits")
  public String consumeBlockHitEvents() {
    // this doesn't work with multiplayer! need to think about how this should work
    // [this was an existing comment -steve]

    List<String> events = new ArrayList<String>();
    BlockRightClickHook event;
    while ((event = blockHitQueue.poll()) != null) {
      Block block = event.getBlockClicked();
      Location loc = block.getLocation();
      String relativeLocation = (loc.getBlockX() - origin.getBlockX())
          + ","
          + (loc.getBlockY() - origin.getBlockY())
          + ","
          +
          (loc.getBlockZ() - origin.getBlockZ());
      StringBuilder b = new StringBuilder();
      b.append(relativeLocation);
      b.append(",");
      b.append(RemoteSession.blockFaceToNotch(block.getFaceClicked()));
      b.append(",");
      b.append(event.getPlayer().getID());
      events.add(b.toString());
    }
    return Joiner.on("|").join(events);
  }
}