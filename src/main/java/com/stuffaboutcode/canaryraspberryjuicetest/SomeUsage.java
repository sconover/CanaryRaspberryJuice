package com.stuffaboutcode.canaryraspberryjuicetest;

import javax.inject.Inject;
import net.canarymod.api.Server;

public class SomeUsage {
  private final Server server;

  @Inject public SomeUsage(Server server) {
    this.server = server;
  }
}
