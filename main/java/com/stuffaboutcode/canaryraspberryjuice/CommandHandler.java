package com.stuffaboutcode.canaryraspberryjuice;

import java.util.ArrayDeque;
import java.util.List;
import net.canarymod.api.Server;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.position.Location;
import net.canarymod.api.world.position.Vector3D;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.hook.player.ChatHook;
import net.canarymod.logger.Logman;

public class CommandHandler {
  // origin is the spawn location on the world
  private Location origin;

  private final Server server;
  private final Logman logman;
  private final RemoteSession.Out out;

  private final ArrayDeque<BlockRightClickHook> blockHitQueue =
			new ArrayDeque<BlockRightClickHook>();
  private final ArrayDeque<ChatHook> chatPostedQueue = new ArrayDeque<ChatHook>();

  private Player attachedPlayer;

  public CommandHandler(Server server, Logman logman, RemoteSession.Out out) {
    this.server = server;
    this.logman = logman;
    this.out = out;
  }

  public void handleLine(String line) {
    //System.out.println(line);
    String methodName = line.substring(0, line.indexOf("("));
    //split string into args, handles , inside " i.e. ","
    String[] args = line.substring(line.indexOf("(") + 1, line.length() - 1).split(",");
    //System.out.println(methodName + ":" + Arrays.toString(args));
    handleCommand(methodName, args);
  }

  protected void handleCommand(String c, String[] args) {

    if (origin == null) this.origin = getSpawnLocation();

    try {
      // get the server
      Server server = getServer();

      // get the world
      World world = getWorld();

      // world.getBlock
      if (c.equals("world.getBlock")) {
        Location loc = parseRelativeBlockLocation(args[0], args[1], args[2]);
        send(world.getBlockAt(loc).getTypeId());

        // world.getBlocks
      } else if (c.equals("world.getBlocks")) {
        Location loc1 = parseRelativeBlockLocation(args[0], args[1], args[2]);
        Location loc2 = parseRelativeBlockLocation(args[3], args[4], args[5]);
        send(getBlocks(loc1, loc2));

        // world.getBlockWithData
      } else if (c.equals("world.getBlockWithData")) {
        Location loc = parseRelativeBlockLocation(args[0], args[1], args[2]);
        send(world.getBlockAt(loc).getTypeId() + "," + world.getBlockAt(loc).getData());

        // world.setBlock
      } else if (c.equals("world.setBlock")) {
        Location loc = parseRelativeBlockLocation(args[0], args[1], args[2]);
        updateBlock(world, loc, Short.parseShort(args[3]),
						args.length > 4 ? Short.parseShort(args[4]) : (short) 0);

        // world.setBlocks
      } else if (c.equals("world.setBlocks")) {
        Location loc1 = parseRelativeBlockLocation(args[0], args[1], args[2]);
        Location loc2 = parseRelativeBlockLocation(args[3], args[4], args[5]);
        short blockType = Short.parseShort(args[6]);
        short data = args.length > 7 ? Short.parseShort(args[7]) : (short) 0;
        setCuboid(loc1, loc2, blockType, data);

        // world.getPlayerIds
      } else if (c.equals("world.getPlayerIds")) {
        StringBuilder bdr = new StringBuilder();
        for (Player p : server.getPlayerList()) {
          bdr.append(p.getID());
          bdr.append("|");
        }
        bdr.deleteCharAt(bdr.length() - 1);
        send(bdr.toString());

        // world.getPlayerId
      } else if (c.equals("world.getPlayerId")) {
        Player p = getNamedPlayer(args[0]);
        if (p != null) {
          send(p.getID());
        } else {
          logman.info("Player [" + args[0] + "] not found.");
          send("Fail");
        }

        // chat.post
      } else if (c.equals("chat.post")) {
        //create chat message from args as it was split by ,
        String chatMessage = "";
        int count;
        for (count = 0; count < args.length; count++) {
          chatMessage = chatMessage + args[count] + ",";
        }
        chatMessage = chatMessage.substring(0, chatMessage.length() - 1);
        server.broadcastMessage(chatMessage);

        // events.clear
      } else if (c.equals("events.clear")) {
        blockHitQueue.clear();
        chatPostedQueue.clear();

        // events.block.hits
      } else if (c.equals("events.block.hits")) {
        // this doesn't work with multiplayer! need to think about how this should work
        StringBuilder b = new StringBuilder();
        BlockRightClickHook event;
        while ((event = blockHitQueue.poll()) != null) {
          Block block = event.getBlockClicked();
          Location loc = block.getLocation();
          b.append(blockLocationToRelative(loc));
          b.append(",");
          b.append(RemoteSession.blockFaceToNotch(block.getFaceClicked()));
          b.append(",");
          b.append(event.getPlayer().getID());
          if (blockHitQueue.size() > 0) {
            b.append("|");
          }
        }
        //DEBUG
        //logman.info(b.toString());
        send(b.toString());

        // events.chat.posts
      } else if (c.equals("events.chat.posts")) {
        StringBuilder b = new StringBuilder();
        ChatHook event;
        while ((event = chatPostedQueue.poll()) != null) {
          b.append(event.getPlayer().getID());
          b.append(",");
          b.append(event.getMessage());
          if (chatPostedQueue.size() > 0) {
            b.append("|");
          }
        }
        //DEBUG
        //logman.info(b.toString());
        send(b.toString());

        // player.getTile
      } else if (c.equals("player.getTile")) {
        String name = null;
        if (args.length > 0) {
          name = args[0];
        }
        Player currentPlayer = getCurrentPlayer(name);
        send(blockLocationToRelative(currentPlayer.getLocation()));

        // player.setTile
      } else if (c.equals("player.setTile")) {
        String name = null, x = args[0], y = args[1], z = args[2];
        if (args.length > 3) {
          name = args[0];
					x = args[1];
					y = args[2];
					z = args[3];
        }
        Player currentPlayer = getCurrentPlayer(name);
        //get players current location, so when they are moved we will use the same pitch and yaw (rotation)
        Location loc = currentPlayer.getLocation();
        currentPlayer.teleportTo(
						parseRelativeBlockLocation(x, y, z, loc.getPitch(), loc.getRotation()));

        // player.getPos
      } else if (c.equals("player.getPos")) {
        String name = null;
        if (args.length > 0) {
          name = args[0];
        }
        Player currentPlayer = getCurrentPlayer(name);
        send(locationToRelative(currentPlayer.getLocation()));

        // player.setPos
      } else if (c.equals("player.setPos")) {
        String name = null, x = args[0], y = args[1], z = args[2];
        if (args.length > 3) {
          name = args[0];
					x = args[1];
					y = args[2];
					z = args[3];
        }
        Player currentPlayer = getCurrentPlayer(name);
        //get players current location, so when they are moved we will use the same pitch and yaw (rotation)
        Location loc = currentPlayer.getLocation();
        currentPlayer.teleportTo(
						parseRelativeLocation(x, y, z, loc.getPitch(), loc.getRotation()));

        // player.getDirection
      } else if (c.equals("player.getDirection")) {
        String name = null;
        if (args.length > 0) {
          name = args[0];
        }
        Player currentPlayer = getCurrentPlayer(name);
        Vector3D direction = getDirection(currentPlayer);
        send(direction.getX() + "," + direction.getY() + "," + direction.getZ());

        // player.getRotation
      } else if (c.equals("player.getRotation")) {
        String name = null;
        if (args.length > 0) {
          name = args[0];
        }
        Player currentPlayer = getCurrentPlayer(name);
        send(currentPlayer.getLocation().getRotation());

        // player.getPitch
      } else if (c.equals("player.getPitch")) {
        String name = null;
        if (args.length > 0) {
          name = args[0];
        }
        Player currentPlayer = getCurrentPlayer(name);
        send(currentPlayer.getLocation().getPitch());

        // world.getHeight
      } else if (c.equals("world.getHeight")) {
        Location loc = parseRelativeBlockLocation(args[0], "0", args[1]);
        send(world.getHighestBlockAt(loc.getBlockX(), loc.getBlockZ()) - origin.getBlockY());

        // entity.getTile
      } else if (c.equals("entity.getTile")) {
        //get entity based on id
        //EntityLiving entity = getEntityLiving(Integer.parseInt(args[0]));
        Player entity = getEntity(Integer.parseInt(args[0]));
        if (entity != null) {
          send(blockLocationToRelative(entity.getLocation()));
        } else {
          logman.info("Entity [" + args[0] + "] not found.");
          send("Fail");
        }
        // entity.setTile
      } else if (c.equals("entity.setTile")) {
        String x = args[1], y = args[2], z = args[3];
        //get entity based on id
        //EntityLiving entity = getEntityLiving(Integer.parseInt(args[0]));
        Player entity = getEntity(Integer.parseInt(args[0]));
        if (entity != null) {
          //get entity's current location, so when they are moved we will use the same pitch and yaw (rotation)
          Location loc = entity.getLocation();
          entity.teleportTo(
							parseRelativeBlockLocation(x, y, z, loc.getPitch(), loc.getRotation()));
        } else {
          logman.info("Entity [" + args[0] + "] not found.");
          send("Fail");
        }

        // entity.getPos
      } else if (c.equals("entity.getPos")) {
        //get entity based on id
        //EntityLiving entity = getEntityLiving(Integer.parseInt(args[0]));
        Player entity = getEntity(Integer.parseInt(args[0]));
        if (entity != null) {
          send(locationToRelative(entity.getLocation()));
        } else {
          logman.info("Entity [" + args[0] + "] not found.");
          send("Fail");
        }

        // entity.setPos
      } else if (c.equals("entity.setPos")) {
        String x = args[1], y = args[2], z = args[3];
        //get entity based on id
        //EntityLiving entity = getEntityLiving(Integer.parseInt(args[0]));
        Player entity = getEntity(Integer.parseInt(args[0]));
        if (entity != null) {
          //get entity's current location, so when they are moved we will use the same pitch and yaw (rotation)
          Location loc = entity.getLocation();
          entity.teleportTo(parseRelativeLocation(x, y, z, loc.getPitch(), loc.getRotation()));
        } else {
          logman.info("Entity [" + args[0] + "] not found.");
          send("Fail");
        }

        // entity.getDirection
      } else if (c.equals("entity.getDirection")) {
        //get entity based on id
        //EntityLiving entity = getEntityLiving(Integer.parseInt(args[0]));
        Player entity = getEntity(Integer.parseInt(args[0]));
        if (entity != null) {
          Vector3D direction = getDirection(entity);
          send(direction.getX() + "," + direction.getY() + "," + direction.getZ());
        } else {
          logman.info("Entity [" + args[0] + "] not found.");
          send("Fail");
        }

        // entity.getRotation
      } else if (c.equals("entity.getRotation")) {
        //get entity based on id
        //EntityLiving entity = getEntityLiving(Integer.parseInt(args[0]));
        Player entity = getEntity(Integer.parseInt(args[0]));
        if (entity != null) {
          send(entity.getLocation().getRotation());
        } else {
          logman.info("Entity [" + args[0] + "] not found.");
          send("Fail");
        }

        // entity.getPitch
      } else if (c.equals("entity.getPitch")) {
        //get entity based on id
        //EntityLiving entity = getEntityLiving(Integer.parseInt(args[0]));
        Player entity = getEntity(Integer.parseInt(args[0]));
        if (entity != null) {
          send(entity.getLocation().getPitch());
        } else {
          logman.info("Entity [" + args[0] + "] not found.");
          send("Fail");
        }

        // not a command which is supported
      } else {
        logman.warn(c + " is not supported.");
        send("Fail");
      }
    } catch (Exception e) {

      logman.warn("Error occured handling command");
      e.printStackTrace();
      send("Fail");
		}
  }

  public void send(Object a) {
    out.send(a.toString());
  }

  public Server getServer() {
    return server;
  }

  public World getWorld() {
    return getServer().getWorldManager().getAllWorlds().iterator().next();
  }

  public Location getSpawnLocation() {
    return getWorld().getSpawnLocation();
  }

  // add a block hit to the queue to be processed
  public void queueBlockHit(BlockRightClickHook hitHook) {
    blockHitQueue.add(hitHook);
  }

  // add a chat posted to the queue to be processed
  public void queueChatPost(ChatHook chatHook) {
    chatPostedQueue.add(chatHook);
  }

  // get the host player, i.e. the first player on the server
  public Player getHostPlayer() {
    List<Player> allPlayers = getServer().getPlayerList();
    if (allPlayers.size() >= 1) {
			return allPlayers.iterator().next();
		}
    return null;
  }

  // gets a named player, as opposed to the host player
  public Player getNamedPlayer(String name) {
    if (name == null) return null;
    // TODO - change this to use getPlayer(name)
    List<Player> allPlayers = getServer().getPlayerList();
    for (int i = 0; i < allPlayers.size(); ++i) {
      if (name.equals(allPlayers.get(i).getName())) {
        return allPlayers.get(i);
      }
    }
    return null;
  }

  //get entity by id - TODO to be compatible with the pi it should be changed to return an entity not a player...
  public Player getEntity(int id) {
    List<Player> allPlayers = getServer().getPlayerList();
    for (int i = 0; i < allPlayers.size(); ++i) {
      if (allPlayers.get(i).getID() == id) {
        return allPlayers.get(i);
      }
    }
    return null;
  }

  // create a cuboid of lots of blocks
  private void setCuboid(Location pos1, Location pos2, short blockType, short data) {
    int minX, maxX, minY, maxY, minZ, maxZ;
    World world = pos1.getWorld();
    minX = pos1.getBlockX() < pos2.getBlockX() ? pos1.getBlockX() : pos2.getBlockX();
    maxX = pos1.getBlockX() >= pos2.getBlockX() ? pos1.getBlockX() : pos2.getBlockX();
    minY = pos1.getBlockY() < pos2.getBlockY() ? pos1.getBlockY() : pos2.getBlockY();
    maxY = pos1.getBlockY() >= pos2.getBlockY() ? pos1.getBlockY() : pos2.getBlockY();
    minZ = pos1.getBlockZ() < pos2.getBlockZ() ? pos1.getBlockZ() : pos2.getBlockZ();
    maxZ = pos1.getBlockZ() >= pos2.getBlockZ() ? pos1.getBlockZ() : pos2.getBlockZ();

    for (int x = minX; x <= maxX; ++x) {
      for (int z = minZ; z <= maxZ; ++z) {
        for (int y = minY; y <= maxY; ++y) {
          updateBlock(world, x, y, z, blockType, data);
        }
      }
    }
  }

  // get a cuboid of lots of blocks
  private String getBlocks(Location pos1, Location pos2) {
    StringBuilder blockData = new StringBuilder();

    int minX, maxX, minY, maxY, minZ, maxZ;
    World world = pos1.getWorld();
    minX = pos1.getBlockX() < pos2.getBlockX() ? pos1.getBlockX() : pos2.getBlockX();
    maxX = pos1.getBlockX() >= pos2.getBlockX() ? pos1.getBlockX() : pos2.getBlockX();
    minY = pos1.getBlockY() < pos2.getBlockY() ? pos1.getBlockY() : pos2.getBlockY();
    maxY = pos1.getBlockY() >= pos2.getBlockY() ? pos1.getBlockY() : pos2.getBlockY();
    minZ = pos1.getBlockZ() < pos2.getBlockZ() ? pos1.getBlockZ() : pos2.getBlockZ();
    maxZ = pos1.getBlockZ() >= pos2.getBlockZ() ? pos1.getBlockZ() : pos2.getBlockZ();

    for (int y = minY; y <= maxY; ++y) {
      for (int x = minX; x <= maxX; ++x) {
        for (int z = minZ; z <= maxZ; ++z) {
          blockData.append(new Integer(world.getBlockAt(x, y, z).getTypeId()).toString() + ",");
        }
      }
    }

    return blockData.substring(0,
				blockData.length() > 0 ? blockData.length() - 1 : 0);  // We don't want last comma
  }

  // updates a block
  private void updateBlock(World world, Location loc, short blockType, short blockData) {
    Block thisBlock = world.getBlockAt(loc);
    updateBlock(thisBlock, blockType, blockData);
  }

  private void updateBlock(World world, int x, int y, int z, short blockType, short blockData) {
    Block thisBlock = world.getBlockAt(x, y, z);
    updateBlock(thisBlock, blockType, blockData);
  }

  private void updateBlock(Block thisBlock, short blockType, short blockData) {
    // check to see if the block is different - otherwise leave it
    if ((thisBlock.getTypeId() != blockType) || (thisBlock.getData() != blockData)) {
      thisBlock.setTypeId(blockType);
      thisBlock.setData(blockData);
      thisBlock.update();
    }
  }

  // gets the current player
  public Player getCurrentPlayer(String name) {
    // if a named player is returned use that
    Player player = getNamedPlayer(name);
    // otherwise if there is an attached player for this session use that
    if (player == null) {
      player = attachedPlayer;
      // otherwise go and get the host player and make that the attached player
      if (player == null) {
        player = getHostPlayer();
        attachedPlayer = player;
      }
    }
    return player;
  }

  public Location parseRelativeBlockLocation(String xstr, String ystr, String zstr) {
    int x = (int) Double.parseDouble(xstr);
    int y = (int) Double.parseDouble(ystr);
    int z = (int) Double.parseDouble(zstr);
    return new Location(getWorld(), origin.getBlockX() + x, origin.getBlockY() + y,
				origin.getBlockZ() + z, 0f, 0f);
  }

  public Location parseRelativeLocation(String xstr, String ystr, String zstr) {
    double x = Double.parseDouble(xstr);
    double y = Double.parseDouble(ystr);
    double z = Double.parseDouble(zstr);
    return new Location(getWorld(), origin.getBlockX() + x, origin.getBlockY() + y,
				origin.getBlockZ() + z, 0f, 0f);
  }

  public Location parseRelativeBlockLocation(String xstr, String ystr, String zstr, float pitch,
			float yaw) {
    Location loc = parseRelativeBlockLocation(xstr, ystr, zstr);
    loc.setPitch(pitch);
    loc.setRotation(yaw);
    return loc;
  }

  public Location parseRelativeLocation(String xstr, String ystr, String zstr, float pitch,
			float yaw) {
    Location loc = parseRelativeLocation(xstr, ystr, zstr);
    loc.setPitch(pitch);
    loc.setRotation(yaw);
    return loc;
  }

  public String blockLocationToRelative(Location loc) {
    return (loc.getBlockX() - origin.getBlockX())
				+ ","
				+ (loc.getBlockY() - origin.getBlockY())
				+ ","
				+
        (loc.getBlockZ() - origin.getBlockZ());
  }

  public String locationToRelative(Location loc) {
    return (loc.getX() - origin.getX()) + "," + (loc.getY() - origin.getY()) + "," +
        (loc.getZ() - origin.getZ());
  }

  // creates a unit vector from rotation and pitch
  public Vector3D getDirection(Player player) {
    double rotation = Math.toRadians(player.getLocation().getRotation());
    double pitch = Math.toRadians(player.getLocation().getPitch());
    double x = (Math.sin(rotation) * Math.cos(pitch)) * -1;
    double y = Math.sin(pitch) * -1;
    double z = Math.cos(rotation) * Math.cos(pitch);
    return new Vector3D(x, y, z);
  }
}
