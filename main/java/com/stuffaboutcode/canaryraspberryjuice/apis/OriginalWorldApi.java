package com.stuffaboutcode.canaryraspberryjuice.apis;

import com.stuffaboutcode.canaryraspberryjuice.MinecraftRemoteCall;
import com.stuffaboutcode.canaryraspberryjuice.ServerHelper;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Location;
import net.canarymod.logger.Logman;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

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

  @MinecraftRemoteCall("world.getBlock")
  public BlockType worldGetBlock(int x, int y, int z) {
    Location loc = serverHelper.parseRelativeBlockLocation(origin, x, y, z);
    return serverHelper.getWorld().getBlockAt(loc).getType();
  }

  @MinecraftRemoteCall("world.getBlockWithData")
  public Pair<BlockType,Short> worldGetBlockWithData(int x, int y, int z) {
    Location loc = serverHelper.parseRelativeBlockLocation(origin, x, y, z);
    return ImmutablePair.of(
        serverHelper.getWorld().getBlockAt(loc).getType(),
        serverHelper.getWorld().getBlockAt(loc).getType().getData());
  }


}
