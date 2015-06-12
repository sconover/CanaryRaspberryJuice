package com.stuffaboutcode.canaryraspberryjuicetest;

import dagger.internal.Factory;
import javax.annotation.Generated;
import javax.inject.Provider;
import net.canarymod.api.Server;
import net.canarymod.api.world.World;

@Generated("dagger.internal.codegen.ComponentProcessor")
public final class FooModule_ProvideWorldFactory implements Factory<World> {
  private final FooModule module;
  private final Provider<Server> serverProvider;

  public FooModule_ProvideWorldFactory(FooModule module, Provider<Server> serverProvider) {  
    assert module != null;
    this.module = module;
    assert serverProvider != null;
    this.serverProvider = serverProvider;
  }

  @Override
  public World get() {  
    World provided = module.provideWorld(serverProvider.get());
    if (provided == null) {
      throw new NullPointerException("Cannot return null from a non-@Nullable @Provides method");
    }
    return provided;
  }

  public static Factory<World> create(FooModule module, Provider<Server> serverProvider) {  
    return new FooModule_ProvideWorldFactory(module, serverProvider);
  }
}

