package com.stuffaboutcode.canaryraspberryjuice;

import com.google.common.base.Preconditions;
import net.canarymod.api.Server;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.World;

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
}