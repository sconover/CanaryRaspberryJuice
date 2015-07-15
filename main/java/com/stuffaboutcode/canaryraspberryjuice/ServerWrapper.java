package com.stuffaboutcode.canaryraspberryjuice;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import net.canarymod.api.Server;
import net.canarymod.api.entity.Entity;
import net.canarymod.api.entity.EntityItem;
import net.canarymod.api.entity.living.EntityLiving;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.entity.vehicle.Boat;
import net.canarymod.api.entity.vehicle.Minecart;
import net.canarymod.api.entity.vehicle.Vehicle;
import net.canarymod.api.world.World;
import net.canarymod.api.world.position.Position;

/**
 * Wrapper around a canary server. Provides convenience methods for accessing objects and
 * information about the world. Tests and other code should not use a Canary Server object
 * directly.
 */
public class ServerWrapper {
  private final Server server;
  private final World firstWorld;

  public ServerWrapper(Server server) {
    this.server = server;

    Preconditions.checkState(
        server.getWorldManager().getAllWorlds().size() == 1,
        "only supports single-world servers");
    this.firstWorld = server.getWorldManager().getAllWorlds().iterator().next();
  }

  public World getWorld() {
    return firstWorld;
  }

  public Position getSpawnPosition() {
    return getWorld().getSpawnLocation();
  }

  // get the host player, i.e. the first player on the server
  public Player getFirstPlayer() {
    Preconditions.checkState(
        !server.getPlayerList().isEmpty(),
        "must have logged-in players in order for this to work");

    return server.getPlayerList().get(0);
  }

  public List<Player> getPlayers() {
    return ImmutableList.copyOf(server.getPlayerList());
  }

  public Player getPlayerByName(String playerName) {
    return server.getPlayer(playerName);
  }

  // get the host player, i.e. the first player on the server
  public boolean hasPlayers() {
    return !server.getPlayerList().isEmpty();
  }

  public void broadcastMessage(String message) {
    server.broadcastMessage(message);
  }

  public Entity getEntityById(int entityId) {
    // TODO: yep, this is how it's done in mc-land. will want to wrap a metric around this
    // Will probably want to dig into mc server and figure out if there's an indexed lookup somewhere
    // ex of mc tradition: https://github.com/bergerkiller/BKCommonLib/blob/master/src/main/java/com/bergerkiller/bukkit/common/utils/EntityUtil.java#L36

    List<EntityLiving> entityLivingList = getWorld().getEntityLivingList();
    List<Player> playerList = getWorld().getPlayerList();
    List<Boat> boatList = getWorld().getBoatList();
    List<Vehicle> vehicleList = getWorld().getVehicleList();
    List<Minecart> minecartList = getWorld().getMinecartList();
    List<EntityItem> itemList = getWorld().getItemList();
    List<Entity> allEntities =
        new ArrayList<Entity>(
            entityLivingList.size() +
                playerList.size() +
                boatList.size() +
                vehicleList.size() +
                minecartList.size() +
                itemList.size());
    allEntities.addAll(entityLivingList);
    allEntities.addAll(playerList);
    allEntities.addAll(boatList);
    allEntities.addAll(vehicleList);
    allEntities.addAll(minecartList);
    allEntities.addAll(itemList);

    for (Entity entity : allEntities) {
      if (entity.getID() == entityId) {
        return entity;
      }
    }
    throw new RuntimeException(String.format("No entity found for id=%d", entityId));
  }
}
