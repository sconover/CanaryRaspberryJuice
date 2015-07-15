package com.stuffaboutcode.canaryraspberryjuice;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import net.canarymod.Canary;
import net.canarymod.api.Server;
import net.canarymod.api.world.World;
import net.canarymod.plugin.Plugin;

public class CanaryRaspberryJuicePlugin extends Plugin {

  public ServerListenerThread serverThread;

  public List<RemoteSession> sessions;

  public CanaryRaspberryJuiceConfiguration config;

  @Override
  public boolean enable() {
    getLogman().info("Enabling " + getName() + " Version " + getVersion());
    getLogman().info("Authored by " + getAuthor());

    //load configuration
    config = new CanaryRaspberryJuiceConfiguration(this);
    //get server port
    int port = config.getPort();

    //register the listener
    Canary.hooks().registerListener(
        new CanaryRaspberryJuiceListener(new PluginSessionsHolder(this)),
        this);

    //setup sessions array
    sessions = new ArrayList<RemoteSession>();

    //create new tcp listener thread
    try {
      serverThread = new ServerListenerThread(this, new InetSocketAddress(port));
      new Thread(serverThread).start();
      getLogman().info("ThreadListener Started");
    } catch (Exception e) {
      e.printStackTrace();
      getLogman().warn("Failed to start ThreadListener");
      return false;
    }

    return true;
  }

  @Override
  public void disable() {
    Canary.hooks().unregisterPluginListeners(this);
    for (RemoteSession session : sessions) {
      try {
        session.close();
      } catch (Exception e) {
        getLogman().warn("Failed to close RemoteSession");
        e.printStackTrace();
      }
    }
    serverThread.running = false;
    try {
      serverThread.serverSocket.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    sessions = null;
    serverThread = null;
    getLogman().info("Raspberry Juice Stopped");
  }

  /** called when a new session is established. */
  public void handleConnection(RemoteSession newSession) {

    if (checkBanned(newSession)) {
      getLogman().warn("Kicking "
          + newSession.getSocket().getRemoteSocketAddress()
          + " because the IP address has been banned.");
      newSession.kick("You've been banned from this server!");
      return;
    }
    synchronized (sessions) {
      sessions.add(newSession);
    }
  }

  public boolean checkBanned(RemoteSession session) {
    String sessionIp = session.getSocket().getInetAddress().getHostAddress();
    return Canary.bans().isIpBanned(sessionIp);
  }

  public Server getServer() {
    return Canary.getServer();
  }

  public World getWorld() {
    return getServer().getWorldManager().getAllWorlds().iterator().next();
  }

  public static class PluginSessionsHolder implements RemoteSessionsHolder {
    private final CanaryRaspberryJuicePlugin plugin;

    public PluginSessionsHolder(
        CanaryRaspberryJuicePlugin plugin) {
      this.plugin = plugin;
    }

    @Override public Iterable<RemoteSession> get() {
      return plugin.sessions;
    }
  }
}
