package com.stuffaboutcode.canaryraspberryjuicetest.support;

import com.stuffaboutcode.canaryraspberryjuice.CommandHandler;
import com.stuffaboutcode.canaryraspberryjuice.CuboidReference;
import com.stuffaboutcode.canaryraspberryjuice.ServerWrapper;
import net.canarymod.Canary;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.CanarySign;
import net.canarymod.api.world.blocks.properties.helpers.BlockProperties;
import net.canarymod.api.world.blocks.properties.helpers.StandingSignProperties;
import net.canarymod.api.world.position.Location;
import net.canarymod.api.world.position.Position;
import net.canarymod.logger.Logman;
import org.junit.Before;

/**
 * Intended to be extended by test classes. Provides convenience objects and methods
 * for use in in-world testing.
 */
public abstract class InWorldTestSupport {
  private ServerWrapper serverWrapper;
  private static int xOffset = 2;
  private TestOut testOut;
  private CommandHandler commandHandler;

  public ServerWrapper getServerWrapper() {
    return serverWrapper;
  }

  public TestOut getTestOut() {
    return testOut;
  }

  public CommandHandler getCommandHandler() {
    return commandHandler;
  }

  @Before
  public void setUp() throws Exception {
    serverWrapper = new ServerWrapper(Canary.getServer());
    serverWrapper.getWorld().setSpawnLocation(new Location(serverWrapper.getWorld(), new Position(0,0,0)));

    testOut = new TestOut();
    commandHandler = new CommandHandler(
        Canary.getServer(),
        new ServerWrapper(Canary.getServer()),
        Logman.getLogman("Test-logman"),
        testOut);
  }

  public Position nextTestPosition(String name) {
    Position testPosition = new Position(xOffset, 100, 2);
    Position justBeforeTestPosition = new Position(xOffset-1, 99, 1);
    CuboidReference ref = new CuboidReference(justBeforeTestPosition, 31, 51, 31);
    ref.fetchBlocks(serverWrapper.getWorld()).makeEmpty();

    Block block = serverWrapper.getWorld().getBlockAt(justBeforeTestPosition);
    block.setType(BlockType.SeaLantern);
    block.update();

    Block blockAbove = block.getRelative(0, 1, 0);
    blockAbove.setType(BlockType.StandingSign);
    StandingSignProperties.applyRotation(blockAbove, BlockProperties.Rotation.NORTH);
    blockAbove.update();

    CanarySign sign = (CanarySign)blockAbove.getTileEntity();
    sign.setTextOnLine(name, 0);
    //sign.setTextOnLine("bar", 1);
    //sign.setTextOnLine("zzz", 2);
    //sign.setTextOnLine("yyy", 3);

    if (serverWrapper.hasPlayers()) {
      serverWrapper.getFirstPlayer().teleportTo(
          LocationHelper.getLocationFacingPosition(testPosition, 0, 10, -30));
    }
    //try {
    //  Thread.sleep(500);
    //} catch (InterruptedException e) {
    //  throw new RuntimeException(e);
    //}
    xOffset += 30;

    return testPosition;
  }
}
