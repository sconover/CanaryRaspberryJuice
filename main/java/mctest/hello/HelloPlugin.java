package mctest.hello;

import net.canarymod.Canary;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.ChatHook;
import net.canarymod.plugin.Plugin;
import net.canarymod.plugin.PluginListener;

public class HelloPlugin extends Plugin {
  @Override public boolean enable() {
    getLogman().info("Enabling " + getName() + " Version " + getVersion());
    getLogman().info("Authored by "+ getAuthor());

    Canary.hooks().registerListener(new Listener(), this);

    Canary.getServer().broadcastMessage("ENABLE PLUGIN 4");

    return true;
  }

  @Override public void disable() {

  }

  public static class Listener implements PluginListener {
    @HookHandler
    public void onChat(ChatHook hook) {
      System.out.println("CHAT HOOK FIRED: " + hook.getMessage());
    }
  }
}
