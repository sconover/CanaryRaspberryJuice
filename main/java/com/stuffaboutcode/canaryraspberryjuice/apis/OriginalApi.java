package com.stuffaboutcode.canaryraspberryjuice.apis;

import com.stuffaboutcode.canaryraspberryjuice.CuboidReference;
import com.stuffaboutcode.canaryraspberryjuice.MinecraftRemoteCall;
import com.stuffaboutcode.canaryraspberryjuice.RawArgString;
import com.stuffaboutcode.canaryraspberryjuice.ServerHelper;
import java.util.List;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Location;
import net.canarymod.logger.Logman;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class OriginalApi {
  // origin is the spawn location on the world
  private final Location origin;
  private final ServerHelper serverHelper;
  private final Logman logman;

  public OriginalApi(Location origin, ServerHelper serverHelper, Logman logman) {
    this.origin = origin;
    this.serverHelper = serverHelper;
    this.logman = logman;
  }

  @MinecraftRemoteCall("world.getBlock")
  public BlockType worldGetBlock(int x, int y, int z) {
    Location loc = serverHelper.parseRelativeBlockLocation(origin, x, y, z);
    return serverHelper.getWorld().getBlockAt(loc).getType();
  }

  @MinecraftRemoteCall("world.getBlockWithData")
  public Pair<BlockType, Short> worldGetBlockWithData(int x, int y, int z) {
    Location loc = serverHelper.parseRelativeBlockLocation(origin, x, y, z);
    return ImmutablePair.of(
        serverHelper.getWorld().getBlockAt(loc).getType(),
        serverHelper.getWorld().getBlockAt(loc).getType().getData());
  }

  @MinecraftRemoteCall("world.setBlock")
  public void setBlock(int x, int y, int z, short blockTypeId) {
    setBlock(x, y, z, blockTypeId, (short) 0);
  }

  @MinecraftRemoteCall("world.setBlock")
  public void setBlock(int x, int y, int z, short blockTypeId, short blockData) {
    setBlocks(
        x, y, z,
        x, y, z,
        blockTypeId, blockData);
  }

  @MinecraftRemoteCall("world.setBlocks")
  public void setBlocks(
      int x1, int y1, int z1,
      int x2, int y2, int z2,
      short blockTypeId) {
    setBlocks(
        x1, y1, z1,
        x2, y2, z2,
        blockTypeId, (short) 0);
  }

  @MinecraftRemoteCall("world.setBlocks")
  public void setBlocks(
      int x1, int y1, int z1,
      int x2, int y2, int z2,
      short blockTypeId, short blockData) {
    Location loc1 = serverHelper.parseRelativeBlockLocation(origin, x1, y1, z1);
    Location loc2 = serverHelper.parseRelativeBlockLocation(origin, x2, y2, z2);

    CuboidReference.fromCorners(loc1, loc2)
        .fetchBlocks(serverHelper.getWorld())
        .changeBlocksToType(BlockType.fromIdAndData(blockTypeId, blockData));
  }

  @MinecraftRemoteCall("world.getPlayerEntityIds")
  public Player[] getPlayerEntityIds() {
    List<Player> allPlayers = serverHelper.getPlayers();
    return allPlayers.toArray(new Player[allPlayers.size()]);
  }

  @MinecraftRemoteCall("chat.post")
  public void chatPost(@RawArgString String chatStr) {
    serverHelper.broadcastMessage(chatStr);
  }
}