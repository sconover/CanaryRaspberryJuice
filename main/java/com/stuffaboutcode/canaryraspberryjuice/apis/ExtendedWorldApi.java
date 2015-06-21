package com.stuffaboutcode.canaryraspberryjuice.apis;

import com.stuffaboutcode.canaryraspberryjuice.MinecraftRemoteCall;
import com.stuffaboutcode.canaryraspberryjuice.ServerHelper;
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
      String loc1XStr, String loc1YStr, String loc1ZStr,
      String loc2XStr, String loc2YStr, String loc2ZStr) {

    //TODO: convert all of this stuff to position
    Location loc1 =
        serverHelper.parseRelativeBlockLocation(origin, loc1XStr, loc1YStr, loc1ZStr);
    Location loc2 =
        serverHelper.parseRelativeBlockLocation(origin, loc2XStr, loc2YStr, loc2ZStr);

    return serverHelper.getBlocks(loc1, loc2).toBlockTypeArray();
  }
}
