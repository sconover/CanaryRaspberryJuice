package com.stuffaboutcode.canaryraspberryjuice.apis;

import com.stuffaboutcode.canaryraspberryjuice.CuboidReference;
import com.stuffaboutcode.canaryraspberryjuice.MinecraftRemoteCall;
import com.stuffaboutcode.canaryraspberryjuice.ServerHelper;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Location;
import net.canarymod.logger.Logman;

public class ExtendedWorldApi {
  // origin is the spawn location on the world
  private final Location origin;
  private final ServerHelper serverHelper;
  private final Logman logman;

  public ExtendedWorldApi(Location origin, ServerHelper serverHelper, Logman logman) {
    this.origin = origin;
    this.serverHelper = serverHelper;
    this.logman = logman;
  }

  @MinecraftRemoteCall("world.getBlocks")
  public BlockType[] worldGetBlocks(
      int x1, int y1, int z1,
      int x2, int y2, int z2) {

    //TODO: convert all of this stuff to position
    Location loc1 =
        serverHelper.parseRelativeBlockLocation(origin, x1, y1, z1);
    Location loc2 =
        serverHelper.parseRelativeBlockLocation(origin, x2, y2, z2);

    return CuboidReference.fromCorners(loc1, loc2)
        .fetchBlocks(serverHelper.getWorld())
        .blockTypeForEachBlock();
  }

  @MinecraftRemoteCall("world.getPlayerEntityId")
  public Player getPlayerEntityId(String playerName) {
    // TODO: what should the error policy be, in the case this is null?
    return serverHelper.getPlayerByName(playerName);
  }
}
