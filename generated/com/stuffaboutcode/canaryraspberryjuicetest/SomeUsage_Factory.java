package com.stuffaboutcode.canaryraspberryjuicetest;

import dagger.internal.Factory;
import javax.annotation.Generated;
import javax.inject.Provider;
import net.canarymod.api.Server;

@Generated("dagger.internal.codegen.ComponentProcessor")
public final class SomeUsage_Factory implements Factory<SomeUsage> {
  private final Provider<Server> serverProvider;

  public SomeUsage_Factory(Provider<Server> serverProvider) {  
    assert serverProvider != null;
    this.serverProvider = serverProvider;
  }

  @Override
  public SomeUsage get() {  
    return new SomeUsage(serverProvider.get());
  }

  public static Factory<SomeUsage> create(Provider<Server> serverProvider) {  
    return new SomeUsage_Factory(serverProvider);
  }
}

