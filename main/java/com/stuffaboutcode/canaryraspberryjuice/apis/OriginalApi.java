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
import net.canarymod.api.world.position.Vector3D;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.logger.Logman;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import static com.stuffaboutcode.canaryraspberryjuice.Util.calculateDirection;
import static com.stuffaboutcode.canaryraspberryjuice.Util.positionRelativeTo;

public class OriginalApi {
  // origin is the spawn location on the world
  private final Position origin;
  private final ServerWrapper serverWrapper;
  private final ArrayDeque<BlockRightClickHook> blockHitQueue;
  private final Logman logman;

  public OriginalApi(
      Position origin,
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

  @RPC("world.getHeight")
  public int world_getHeight(int x, int z) {
    int relativeX = origin.getBlockX() + x;
    int relativeZ = origin.getBlockZ() + z;
    int absoluteHeight = serverWrapper.getWorld().getHighestBlockAt(relativeX, relativeZ);
    return absoluteHeight - origin.getBlockY();
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
  public BlockPosition player_getTile() {
    //TODO: what do we do here if there's no player logged in?
    return player_getTile(serverWrapper.getFirstPlayer().getName());
  }

  @RPC("player.getTile")
  public BlockPosition player_getTile(String playerName) {
    return getEntityTile(serverWrapper.getPlayerByName(playerName));
  }

  @RPC("player.setTile")
  public void player_setTile(int relativeX, int relativeY, int relativeZ) {
    player_setTile(serverWrapper.getFirstPlayer().getName(), relativeX, relativeY, relativeZ);
  }

  //TODO: convert all (int)p.getX to p.getBlockX, etc

  @RPC("player.setTile")
  public void player_setTile(String playerName, int relativeX, int relativeY, int relativeZ) {
    teleportEntityTo(serverWrapper.getPlayerByName(playerName), relativeX, relativeY, relativeZ);
  }

  @RPC("player.getPos")
  public Position player_getPos() {
    //TODO: what do we do here if there's no player logged in?
    return player_getPos(serverWrapper.getFirstPlayer().getName());
  }

  @RPC("player.getPos")
  public Position player_getPos(String playerName) {
    return getEntityPosition(serverWrapper.getPlayerByName(playerName));
  }

  @RPC("player.setPos")
  public void player_setPos(float relativeX, float relativeY, float relativeZ) {
    player_setPos(serverWrapper.getFirstPlayer().getName(), relativeX, relativeY, relativeZ);
  }

  @RPC("player.setPos")
  public void player_setPos(String playerName, float relativeX, float relativeY, float relativeZ) {
    teleportEntityTo(serverWrapper.getPlayerByName(playerName), relativeX, relativeY, relativeZ);
  }

  // TODO: all of these need javadoc

  @RPC("player.getDirection")
  public Vector3D player_getDirection() {
    //TODO: what do we do here if there's no player logged in?
    return player_getDirection(serverWrapper.getFirstPlayer().getName());
  }

  @RPC("player.getDirection")
  public Vector3D player_getDirection(String playerName) {
    return getEntityDirection(serverWrapper.getPlayerByName(playerName));
  }

  @RPC("player.getPitch")
  public float player_getPitch() {
    //TODO: what do we do here if there's no player logged in?
    return player_getPitch(serverWrapper.getFirstPlayer().getName());
  }

  @RPC("player.getPitch")
  public float player_getPitch(String playerName) {
    return serverWrapper.getPlayerByName(playerName).getPitch();
  }

  @RPC("player.getRotation")
  public float player_getRotation() {
    //TODO: what do we do here if there's no player logged in?
    return player_getRotation(serverWrapper.getFirstPlayer().getName());
  }

  @RPC("player.getRotation")
  public float player_getRotation(String playerName) {
    return serverWrapper.getPlayerByName(playerName).getRotation();
  }

  @RPC("entity.getTile")
  public BlockPosition entity_getTile(int entityId) {
    return getEntityTile(serverWrapper.getEntityById(entityId));
  }

  @RPC("entity.setTile")
  public void entity_setTile(int entityId, int relativeX, int relativeY, int relativeZ) {
    teleportEntityTo(serverWrapper.getEntityById(entityId), relativeX, relativeY, relativeZ);
  }

  @RPC("entity.getPos")
  public Position entity_getPos(int entityId) {
    return getEntityPosition(serverWrapper.getEntityById(entityId));
  }

  @RPC("entity.setPos")
  public void entity_setPos(int entityId, float relativeX, float relativeY, float relativeZ) {
    teleportEntityTo(serverWrapper.getEntityById(entityId), relativeX, relativeY, relativeZ);
  }

  @RPC("entity.getDirection")
  public Vector3D entity_getDirection(int entityId) {
    return getEntityDirection(serverWrapper.getEntityById(entityId));
  }

  @RPC("entity.getPitch")
  public float entity_getPitch(int entityId) {
    return serverWrapper.getEntityById(entityId).getPitch();
  }

  @RPC("entity.getRotation")
  public float entity_getRotation(int entityId) {
    return serverWrapper.getEntityById(entityId).getRotation();
  }

  private Vector3D getEntityDirection(Entity entity) {
    return calculateDirection(entity.getPitch(), entity.getRotation());
  }

  private Position getEntityPosition(Entity entity) {
    return positionRelativeTo(entity.getLocation(), origin);
  }

  private BlockPosition getEntityTile(Entity entity) {
    return BlockPosition.fromPosition(positionRelativeTo(entity.getLocation(), origin));
  }

  private void teleportEntityTo(
      Entity entity,
      double relativeX, double relativeY, double relativeZ) {
    Position newPosition =
        new Position(
            origin.getX() + relativeX,
            origin.getY() + relativeY,
            origin.getZ() + relativeZ);

    // maintain existing entity pitch/yaw
    Location newLocation = new Location(serverWrapper.getWorld(), newPosition);
    newLocation.setPitch(entity.getPitch());
    newLocation.setRotation(entity.getRotation());
    entity.teleportTo(newLocation);
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
      sb.append(BlockPosition.fromPosition(pos).toApiResult());
      sb.append(",");
      sb.append(RemoteSession.blockFaceToNotch(face));
      sb.append(",");
      sb.append(entity.getID());
      return sb.toString();
    }
  }

  public static class BlockPosition {
    public static BlockPosition fromPosition(Position p) {
      return new BlockPosition(p.getBlockX(), p.getBlockY(), p.getBlockZ());
    }

    private final int x;
    private final int y;
    private final int z;

    public BlockPosition(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    public String toApiResult() {
      return String.format("%d,%d,%d", x, y, z);
    }
  }
}