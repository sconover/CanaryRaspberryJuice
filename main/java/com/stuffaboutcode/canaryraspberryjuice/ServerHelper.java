package com.stuffaboutcode.canaryraspberryjuice;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import net.canarymod.api.Server;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.position.Position;

/**
 * Wrapper around a canary server. Provides convenience methods for accessing objects and
 * information about the world. Tests and other code should not use a Canary Server object
 * directly.
 */
public class ServerHelper {
  private final Server server;
  private final World firstWorld;

  public ServerHelper(Server server) {
    this.server = server;

    Preconditions.checkState(
        server.getWorldManager().getAllWorlds().size() == 1,
        "only supports single-world servers");
    this.firstWorld = server.getWorldManager().getAllWorlds().iterator().next();
  }

  public World getWorld() {
    return firstWorld;
  }

  // get the host player, i.e. the first player on the server
  public Player getFirstPlayer() {
    Preconditions.checkState(
        !server.getPlayerList().isEmpty(),
        "must have logged-in players in order for this to work");

    return server.getPlayerList().get(0);
  }

  // get the host player, i.e. the first player on the server
  public boolean hasPlayers() {
    return !server.getPlayerList().isEmpty();
  }

  // get a cuboid of lots of blocks
  public Blocks getBlocks(Position loc1, Position loc2) {
    List<Block> blockList = new ArrayList<Block>();

    int minX, maxX, minY, maxY, minZ, maxZ;
    minX = loc1.getBlockX() < loc2.getBlockX() ? loc1.getBlockX() : loc2.getBlockX();
    maxX = loc1.getBlockX() >= loc2.getBlockX() ? loc1.getBlockX() : loc2.getBlockX();
    minY = loc1.getBlockY() < loc2.getBlockY() ? loc1.getBlockY() : loc2.getBlockY();
    maxY = loc1.getBlockY() >= loc2.getBlockY() ? loc1.getBlockY() : loc2.getBlockY();
    minZ = loc1.getBlockZ() < loc2.getBlockZ() ? loc1.getBlockZ() : loc2.getBlockZ();
    maxZ = loc1.getBlockZ() >= loc2.getBlockZ() ? loc1.getBlockZ() : loc2.getBlockZ();

    for (int y = minY; y <= maxY; ++y) {
      for (int x = minX; x <= maxX; ++x) {
        for (int z = minZ; z <= maxZ; ++z) {
          blockList.add(getWorld().getBlockAt(x, y, z));
        }
      }
    }

    return new Blocks(blockList);
  }

}
