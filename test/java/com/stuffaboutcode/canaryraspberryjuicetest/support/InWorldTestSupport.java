package com.stuffaboutcode.canaryraspberryjuicetest.support;

import com.google.common.collect.Lists;
import com.stuffaboutcode.canaryraspberryjuice.CanaryRaspberryJuiceListener;
import com.stuffaboutcode.canaryraspberryjuice.CommandHandler;
import com.stuffaboutcode.canaryraspberryjuice.CuboidReference;
import com.stuffaboutcode.canaryraspberryjuice.RemoteSession;
import com.stuffaboutcode.canaryraspberryjuice.RemoteSessionsHolder;
import com.stuffaboutcode.canaryraspberryjuice.ServerWrapper;
import java.io.IOException;
import java.util.ArrayDeque;
import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.inventory.ItemType;
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
  public static final int PLAYER_PLACEMENT_X_OFFSET = 0;
  public static final int PLAYER_PLACEMENT_Y_OFFSET = 10;
  public static final int PLAYER_PLACEMENT_Z_OFFSET = -30;

  private ServerWrapper serverWrapper;
  private static int xOffset = 2;
  private TestOut testOut;
  private CommandHandler commandHandler;
  private CanaryRaspberryJuiceListener pluginListener;

  public ServerWrapper getServerWrapper() {
    return serverWrapper;
  }

  public TestOut getTestOut() {
    return testOut;
  }

  public CommandHandler getCommandHandler() {
    return commandHandler;
  }

  public CanaryRaspberryJuiceListener getPluginListener() {
    return pluginListener;
  }

  @Before
  public void setUp() throws Exception {
    setUpAtPlayerOrigin(new Position(0, 0, 0));
  }

  public void setUpAtPlayerOrigin(Position position) throws IOException {
    serverWrapper = new ServerWrapper(Canary.getServer());
    serverWrapper.getWorld().setSpawnLocation(new Location(serverWrapper.getWorld(),
        position));

    testOut = new TestOut();
    Logman logman = Logman.getLogman("Test-logman");

    commandHandler = new CommandHandler(
        Canary.getServer(),
        new ServerWrapper(Canary.getServer()),
        logman,
        testOut);

    final RemoteSession session = new RemoteSession(
      logman,
        new RemoteSession.ToOutQueue(new ArrayDeque<>()),
        commandHandler,
        null);

    pluginListener = new CanaryRaspberryJuiceListener(new RemoteSessionsHolder() {
      @Override public Iterable<RemoteSession> get() {
        return Lists.newArrayList(session);
      }
    });
  }

  public void makeFirstPlayerWieldItem(Player player, ItemType itemType) {
    player.getInventory().setSlot(itemType.getId(), 0, 0);

    player.getInventory().setSlot(
        player.getInventory().getSelectedHotbarSlotId(),
        player.getInventory().getSlot(0));
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
          LocationHelper.getLocationFacingPosition(testPosition,
              PLAYER_PLACEMENT_X_OFFSET, PLAYER_PLACEMENT_Y_OFFSET, PLAYER_PLACEMENT_Z_OFFSET));
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
