package com.stuffaboutcode.canaryraspberryjuicetest;

import dagger.internal.Factory;
import javax.annotation.Generated;
import net.canarymod.api.Server;

@Generated("dagger.internal.codegen.ComponentProcessor")
public final class FooModule_ProvideServerFactory implements Factory<Server> {
  private final FooModule module;

  public FooModule_ProvideServerFactory(FooModule module) {  
    assert module != null;
    this.module = module;
  }

  @Override
  public Server get() {  
    Server provided = module.provideServer();
    if (provided == null) {
      throw new NullPointerException("Cannot return null from a non-@Nullable @Provides method");
    }
    return provided;
  }

  public static Factory<Server> create(FooModule module) {  
    return new FooModule_ProvideServerFactory(module);
  }
}

