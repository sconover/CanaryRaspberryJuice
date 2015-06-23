package com.stuffaboutcode.canaryraspberryjuicetest.support;

import com.stuffaboutcode.canaryraspberryjuice.Cuboid;
import com.stuffaboutcode.canaryraspberryjuice.CuboidReference;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Position;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SupportTest extends InWorldTestSupport {
  @Test
  public void testMovePlayer() throws Exception {
    if (getServerWrapper().hasPlayers()) {
      CuboidReference ref = new CuboidReference(new Position(1, 100, 1), 5, 5, 5);
      Cuboid goldCube = ref.fetchBlocks(getServerWrapper().getWorld());
      goldCube.changeBlocksToType(BlockType.GoldBlock);
      getServerWrapper().getFirstPlayer().teleportTo(
          LocationHelper.getLocationFacingPosition(new Position(1, 100, 1), 30, 10, 0));

      assertEquals(new Position(31, 110, 1), getServerWrapper().getFirstPlayer().getPosition());
    }
  }
}
