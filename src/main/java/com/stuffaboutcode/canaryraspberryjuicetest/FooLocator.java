package com.stuffaboutcode.canaryraspberryjuicetest;

import dagger.Component;
import net.canarymod.api.Server;
import net.canarymod.api.world.World;

@Component(modules = FooModule.class)
public interface FooLocator {
  public Server server();
  public World world();
}
