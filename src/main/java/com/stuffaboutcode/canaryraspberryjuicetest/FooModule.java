package com.stuffaboutcode.canaryraspberryjuicetest;

import dagger.Module;
import dagger.Provides;
import net.canarymod.Canary;
import net.canarymod.api.Server;
import net.canarymod.api.world.World;

@Module
public class FooModule {
  @Provides Server provideServer() {
    return Canary.getServer();
  }

  @Provides World provideWorld(Server server) {
    return server.getWorldManager().getAllWorlds().iterator().next();
  }
}
