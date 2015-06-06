package mctest.hello;

import net.canarymod.plugin.Plugin;

public class HelloPlugin extends Plugin {
  @Override public boolean enable() {
    getLogman().info("17 Enabling "+ getName() + " Version " + getVersion());
    getLogman().info("Authored by "+getAuthor());
    return true;
  }

  @Override public void disable() {

  }
}
