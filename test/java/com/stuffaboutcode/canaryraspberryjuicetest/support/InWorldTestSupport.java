package com.stuffaboutcode.canaryraspberryjuicetest.support;

import com.stuffaboutcode.canaryraspberryjuice.CommandHandler;
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
import org.junit.BeforeClass;

/**
 * Intended to be extended by test classes. Provides convenience objects and methods
 * for use in in-world testing.
 */
public abstract class InWorldTestSupport {
  private ServerHelper serverHelper;
  private static int xOffset;
  private TestOut testOut;
  private CommandHandler commandHandler;

  public ServerHelper getServerHelper() {
    return serverHelper;
  }

  public TestOut getTestOut() {
    return testOut;
  }

  public CommandHandler getCommandHandler() {
    return commandHandler;
  }

  @BeforeClass
  public static void setUpOnce() {
    xOffset = 2;
  }

  @Before
  public void setUp() throws Exception {
    serverHelper = new ServerHelper(Canary.getServer());
    serverHelper.getWorld().setSpawnLocation(new Location(serverHelper.getWorld(), new Position(0,0,0)));

    testOut = new TestOut();
    commandHandler = new CommandHandler(
        Canary.getServer(),
        Logman.getLogman("Test-logman"),
        testOut);
  }

  public Position nextTestPosition(String name) {
    Position testPosition = new Position(xOffset, 100, 2);
    Position justBeforeTestPosition = new Position(xOffset-1, 99, 1);
    CuboidReference ref = new CuboidReference(justBeforeTestPosition, 31, 51, 31);
    ref.fetchBlocks(serverHelper.getWorld()).makeEmpty();

    Block block = serverHelper.getWorld().getBlockAt(justBeforeTestPosition);
    block.setType(BlockType.GoldBlock);
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

    if (serverHelper.hasPlayers()) {
      serverHelper.getFirstPlayer().teleportTo(
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
