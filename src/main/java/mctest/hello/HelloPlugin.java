package mctest.hello;

import net.canarymod.Canary;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.system.ServerTickHook;
import net.canarymod.plugin.Plugin;
import net.canarymod.plugin.PluginListener;

public class HelloPlugin extends Plugin {
  @Override public boolean enable() {
    getLogman().info("Enabling "+ getName() + " Version " + getVersion());
    getLogman().info("Authored by "+getAuthor());

    Canary.hooks().registerListener(new Listener(), this);

    Canary.getServer().broadcastMessage("ENABLE PLUGIN");
    System.out.println(String.valueOf(Canary.getServer().getTicksPerSecond()));


    return true;
  }

  @Override public void disable() {

  }

  public static class Listener implements PluginListener {
    @HookHandler
    public void onTick(ServerTickHook tickHook) {
      // System.out.println("tick");
    }
  }
}
