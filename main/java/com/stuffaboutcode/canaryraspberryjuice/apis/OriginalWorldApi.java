package com.stuffaboutcode.canaryraspberryjuice.apis;

import com.stuffaboutcode.canaryraspberryjuice.MinecraftRemoteCall;
import com.stuffaboutcode.canaryraspberryjuice.ServerHelper;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Location;
import net.canarymod.logger.Logman;

public class OriginalWorldApi {
  // origin is the spawn location on the world
  private final Location origin;
  private final ServerHelper serverHelper;
  private final Logman logman;

  public OriginalWorldApi(Location origin, ServerHelper serverHelper, Logman logman) {
    this.origin = origin;
    this.serverHelper = serverHelper;
    this.logman = logman;
  }

  //TODO: consider simple type conversion / validation...
  @MinecraftRemoteCall("world.getBlock")
  public BlockType worldGetBlock(String xStr, String yStr, String zStr) {
    Location loc = serverHelper.parseRelativeBlockLocation(origin, xStr, yStr, zStr);
    return serverHelper.getWorld().getBlockAt(loc).getType();
  }
}
