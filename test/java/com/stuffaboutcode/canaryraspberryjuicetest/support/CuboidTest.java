package com.stuffaboutcode.canaryraspberryjuicetest.support;

import com.google.common.collect.Lists;
import com.stuffaboutcode.canaryraspberryjuice.Cuboid;
import com.stuffaboutcode.canaryraspberryjuice.CuboidReference;
import com.stuffaboutcode.canaryraspberryjuice.Relative;
import java.util.Arrays;
import java.util.List;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Position;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CuboidTest extends InWorldTestSupport {
  @Test
  public void testChangeType() {
    Position p = nextTestPosition("testChangeType");
    Cuboid cuboid = new CuboidReference(p, 10, 10, 10)
        .fetchBlocks(getServerWrapper().getWorld());

    cuboid.makeEmpty();
    assertEquals(BlockType.Air, getServerWrapper().getWorld().getBlockAt(p).getType());
    cuboid.changeBlocksToType(BlockType.GoldBlock);
    assertEquals(BlockType.GoldBlock, getServerWrapper().getWorld().getBlockAt(p).getType());
  }

  @Test
  public void testCuboid() {
    Position topOfWorld = new Position(1, 250, 1);

    CuboidReference topRef = new CuboidReference(topOfWorld, 1, 1, 1);
    Cuboid cuboid = topRef.fetchBlocks(getServerWrapper().getWorld());
    List<Relative<Block>> blockLocations = Lists.newArrayList(cuboid);
    assertEquals(1, blockLocations.size());
    assertEquals(new Position(1, 250, 1), blockLocations.get(0).object.getPosition());
    assertEquals(BlockType.Air, blockLocations.get(0).object.getType());

    CuboidReference topLargerRef = new CuboidReference(topOfWorld, 2, 3, 2);
    cuboid = topLargerRef.fetchBlocks(getServerWrapper().getWorld());
    blockLocations = Lists.newArrayList(cuboid);
    assertEquals(12, blockLocations.size());
    assertEquals(new Position(1, 250, 1), blockLocations.get(0).object.getPosition());
    assertEquals(new Position(1, 250, 2), blockLocations.get(1).object.getPosition());
    assertEquals(new Position(1, 251, 1), blockLocations.get(2).object.getPosition());
    assertEquals(new Position(1, 251, 2), blockLocations.get(3).object.getPosition());
    assertEquals(new Position(1, 252, 1), blockLocations.get(4).object.getPosition());
    assertEquals(new Position(1, 252, 2), blockLocations.get(5).object.getPosition());
    assertEquals(new Position(2, 250, 1), blockLocations.get(6).object.getPosition());
    assertEquals(new Position(2, 250, 2), blockLocations.get(7).object.getPosition());
    assertEquals(new Position(2, 251, 1), blockLocations.get(8).object.getPosition());
    assertEquals(new Position(2, 251, 2), blockLocations.get(9).object.getPosition());
    assertEquals(new Position(2, 252, 1), blockLocations.get(10).object.getPosition());
    assertEquals(new Position(2, 252, 2), blockLocations.get(11).object.getPosition());

    assertTrue(cuboid.isAir());

    Position bottomOfWorld = new Position(0, 0, 0);
    cuboid = new CuboidReference(bottomOfWorld, 2, 4, 2).fetchBlocks(getServerWrapper().getWorld());
    blockLocations = Lists.newArrayList(cuboid);
    assertEquals(16, blockLocations.size());

    assertFalse(cuboid.isAir());
  }

  @Test
  public void testCuboidCenter() {
    Position topOfWorld = new Position(1, 250, 1);

    assertEquals(
        Arrays.asList(
            new Position(1, 251, 1),
            new Position(1, 251, 2),
            new Position(2, 251, 1),
            new Position(2, 251, 2)),
        new CuboidReference(topOfWorld, 2, 3, 2)
            .center()
            .fetchBlocks(getServerWrapper().getWorld())
            .toPositions());

    assertEquals(
        Arrays.asList(new Position(1, 250, 1)),
        new CuboidReference(topOfWorld, 1, 1, 1)
            .center()
            .fetchBlocks(getServerWrapper().getWorld())
            .toPositions());
  }
}
