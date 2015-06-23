package com.stuffaboutcode.canaryraspberryjuice.apis;

import com.stuffaboutcode.canaryraspberryjuice.CuboidReference;
import com.stuffaboutcode.canaryraspberryjuice.RPC;
import com.stuffaboutcode.canaryraspberryjuice.ServerHelper;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Location;
import net.canarymod.api.world.position.Position;
import net.canarymod.logger.Logman;

public class ExtendedApi {
  // origin is the spawn location on the world
  private final Location origin;
  private final ServerHelper serverHelper;
  private final Logman logman;

  public ExtendedApi(Location origin, ServerHelper serverHelper, Logman logman) {
    this.origin = origin;
    this.serverHelper = serverHelper;
    this.logman = logman;
  }

  @RPC("world.getBlocks")
  public BlockType[] worldGetBlocks(
      int x1, int y1, int z1,
      int x2, int y2, int z2) {
    return CuboidReference.relativeTo(origin,
        new Position(x1, y1, z1),
        new Position(x2, y2, z2))
        .fetchBlocks(serverHelper.getWorld())
        .blockTypeForEachBlock();
  }

  @RPC("world.getPlayerEntityId")
  public Player getPlayerEntityId(String playerName) {
    // TODO: what should the error policy be, in the case this is null?
    return serverHelper.getPlayerByName(playerName);
  }
}
