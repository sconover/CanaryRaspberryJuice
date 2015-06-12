package com.stuffaboutcode.canaryraspberryjuicetest;

import javax.annotation.Generated;
import javax.inject.Provider;
import net.canarymod.api.Server;
import net.canarymod.api.world.World;

@Generated("dagger.internal.codegen.ComponentProcessor")
public final class DaggerFooLocator implements FooLocator {
  private Provider<Server> provideServerProvider;
  private Provider<World> provideWorldProvider;

  private DaggerFooLocator(Builder builder) {  
    assert builder != null;
    initialize(builder);
  }

  public static Builder builder() {  
    return new Builder();
  }

  public static FooLocator create() {  
    return builder().build();
  }

  private void initialize(final Builder builder) {  
    this.provideServerProvider = FooModule_ProvideServerFactory.create(builder.fooModule);
    this.provideWorldProvider = FooModule_ProvideWorldFactory.create(builder.fooModule, provideServerProvider);
  }

  @Override
  public Server server() {  
    return provideServerProvider.get();
  }

  @Override
  public World world() {  
    return provideWorldProvider.get();
  }

  public static final class Builder {
    private FooModule fooModule;
  
    private Builder() {  
    }
  
    public FooLocator build() {  
      if (fooModule == null) {
        this.fooModule = new FooModule();
      }
      return new DaggerFooLocator(this);
    }
  
    public Builder fooModule(FooModule fooModule) {  
      if (fooModule == null) {
        throw new NullPointerException("fooModule");
      }
      this.fooModule = fooModule;
      return this;
    }
  }
}

