package com.stuffaboutcode.canaryraspberryjuice.apis;

import com.stuffaboutcode.canaryraspberryjuice.CuboidReference;
import com.stuffaboutcode.canaryraspberryjuice.RPC;
import com.stuffaboutcode.canaryraspberryjuice.RawArgString;
import com.stuffaboutcode.canaryraspberryjuice.RemoteSession;
import com.stuffaboutcode.canaryraspberryjuice.ServerWrapper;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.canarymod.api.entity.Entity;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockFace;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Location;
import net.canarymod.api.world.position.Position;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.logger.Logman;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import static com.stuffaboutcode.canaryraspberryjuice.Util.positionRelativeTo;
import static com.stuffaboutcode.canaryraspberryjuice.Util.positionToApiString;

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
  public BlockType world_getBlock(int x, int y, int z) {
    return CuboidReference.relativeTo(origin, new Position(x, y, z))
        .fetchBlocks(serverWrapper.getWorld())
        .firstBlock()
        .getType();
  }

  @RPC("world.getBlockWithData")
  public Pair<BlockType, Short> world_setBlockWithData(int x, int y, int z) {
    BlockType blockType = world_getBlock(x, y, z);
    return ImmutablePair.of(blockType, blockType.getData());
  }

  @RPC("world.setBlock")
  public void world_setBlock(int x, int y, int z, short blockTypeId) {
    world_setBlock(x, y, z, blockTypeId, (short) 0);
  }

  @RPC("world.setBlock")
  public void world_setBlock(int x, int y, int z, short blockTypeId, short blockData) {
    world_setBlocks(
        x, y, z,
        x, y, z,
        blockTypeId, blockData);
  }

  @RPC("world.setBlocks")
  public void world_setBlocks(
      int x1, int y1, int z1,
      int x2, int y2, int z2,
      short blockTypeId) {
    world_setBlocks(
        x1, y1, z1,
        x2, y2, z2,
        blockTypeId, (short) 0);
  }

  @RPC("world.setBlocks")
  public void world_setBlocks(
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
  public Player[] world_getPlayerEntityIds() {
    List<Player> allPlayers = serverWrapper.getPlayers();
    return allPlayers.toArray(new Player[allPlayers.size()]);
  }

  @RPC("chat.post")
  public void chat_post(@RawArgString String chatStr) {
    serverWrapper.broadcastMessage(chatStr);
  }

  @RPC("events.clear")
  public void events_clear() {
    blockHitQueue.clear();
  }

  @RPC("events.block.hits")
  public BlockEvent[] events_block_hits() {
    // this doesn't work with multiplayer! need to think about how this should work
    // [this was an existing comment -steve]

    List<BlockEvent> blockEventList = new ArrayList<BlockEvent>();
    BlockRightClickHook event;
    while ((event = blockHitQueue.poll()) != null) {
      blockEventList.add(BlockEvent.fromBlockRightClock(event, origin));
    }
    return blockEventList.toArray(new BlockEvent[blockEventList.size()]);
  }

  @RPC("player.getTile")
  public Position player_getTile() {
    //TODO: what do we do here if there's no player logged in?
    return player_getTile(serverWrapper.getFirstPlayer().getName());
  }

  @RPC("player.getTile")
  public Position player_getTile(String playerName) {
    Player player = serverWrapper.getPlayerByName(playerName);
    return positionRelativeTo(player.getLocation(), origin);
  }

  @RPC("player.setTile")
  public void player_setTile(int relativeX, int relativeY, int relativeZ) {
    player_setTile(serverWrapper.getFirstPlayer().getName(), relativeX, relativeY, relativeZ);
  }

  @RPC("player.setTile")
  public void player_setTile(String playerName, int relativeX, int relativeY, int relativeZ) {
    Player player = serverWrapper.getPlayerByName(playerName);
    Position newPosition =
        positionRelativeTo(new Position(relativeX, relativeY, relativeZ), origin);
    player.teleportTo(newPosition); // note: this appears to automatically retain pitch/yaw of player
  }

  public static class BlockEvent {
    public static BlockEvent fromBlockRightClock(
        BlockRightClickHook blockRightClick,
        Position relativeToPosition) {

      Block block = blockRightClick.getBlockClicked();
      return new BlockEvent(
          positionRelativeTo(block.getLocation(), relativeToPosition),
          block.getFaceClicked(),
          blockRightClick.getPlayer());
    }

    private final Position pos;
    private final BlockFace face;
    private final Entity entity;

    public BlockEvent(Position pos, BlockFace face, Entity entity) {
      this.pos = pos;
      this.face = face;
      this.entity = entity;
    }

    public String toApiResult() {
      StringBuilder sb = new StringBuilder();
      sb.append(positionToApiString(pos));
      sb.append(",");
      sb.append(RemoteSession.blockFaceToNotch(face));
      sb.append(",");
      sb.append(entity.getID());
      return sb.toString();
    }
  }
}