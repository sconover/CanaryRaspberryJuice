package com.stuffaboutcode.canaryraspberryjuice;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Position;

/**
 * A "materialized" cuboid of blocks from a minecraft world.
 *
 * Intended to make mass block operations easy to perform.
 */
public class Cuboid implements Iterable<Relative<Block>> {
  private final Block[][][] blocks;

  public Cuboid(Block[][][] blocks) {
    this.blocks = blocks;
  }

  public Set<BlockType> blockTypes() {
    Set<BlockType> blockTypes = new LinkedHashSet<BlockType>();
    for (Relative<Block> blockLocation : this) {
      blockTypes.add(blockLocation.object.getType());
    }
    return blockTypes;
  }

  public BlockType[] blockTypeForEachBlock() {
    List<BlockType> blockTypes = new ArrayList<BlockType>();
    for (Relative<Block> blockLocation : this) {
      blockTypes.add(blockLocation.object.getType());
    }
    return blockTypes.toArray(new BlockType[blockTypes.size()]);
  }

  public Map<BlockType, List<Block>> blockTypeToBlocks() {
    Map<BlockType, List<Block>> blockTypeToBlockList = new LinkedHashMap<BlockType, List<Block>>();
    for (Relative<Block> blockLocation : this) {
      if (!blockTypeToBlockList.containsKey(blockLocation.object.getType())) {
        blockTypeToBlockList.put(blockLocation.object.getType(), new ArrayList<Block>());
      }
      blockTypeToBlockList.get(blockLocation.object.getType()).add(blockLocation.object);
    }
    return ImmutableMap.copyOf(blockTypeToBlockList);
  }

  public boolean isUniformType(BlockType blockType) {
    return blockTypes().equals(Sets.newHashSet(blockType));
  }

  public boolean isAir() {
    return isUniformType(BlockType.Air);
  }

  public Iterator<Relative<Block>> iterator() {
    return new RelativeBlockIterator(blocks);
  }

  public Cuboid changeBlocksToType(BlockType newType) {
    for (Relative<Block> relativeBlock: this) {
      relativeBlock.object.setType(newType);
      relativeBlock.object.update();
    }
    return this;
  }

  public Cuboid makeEmpty() {
    changeBlocksToType(BlockType.Air);
    return this;
  }

  public Block firstBlock() {
    return blocks[0][0][0];
  }

  public List<Position> toPositions() {
    List<Position> positions = new ArrayList<Position>();
    for (Relative<Block> relativeBlock: this) {
      positions.add(relativeBlock.object.getPosition());
    }
    return ImmutableList.copyOf(positions);
  }

  public static class RelativeBlockIterator implements Iterator<Relative<Block>> {
    private final Block[][][] blocks;
    private int x;
    private int y;
    private int z;
    private Relative next;

    public RelativeBlockIterator(Block[][][] blocks) {
      this.blocks = blocks;

      Preconditions.checkState(
          blocks.length >= 1 &&
              blocks[0].length >= 1 &&
              blocks[0][0].length >= 1,
          "3D block array must be at least 1x1x1");
      this.next = new Relative<Block>(blocks[0][0][0], 0, 0, 0);
    }

    @Override public boolean hasNext() {
      return next != null;
    }

    @Override public Relative<Block> next() {
      Relative<Block> result = next;
      advance();
      return result;
    }

    private void advance() {
      if (x == blocks.length - 1 &&
          y == blocks[0].length - 1 &&
          z == blocks[0][0].length - 1) {
        next = null;
        return;
      } else if (z < blocks[x][y].length - 1) {
        z += 1;
      } else if (y < blocks[x].length - 1) {
        y += 1;
        z = 0;
      } else if (x < blocks.length - 1) {
        x += 1;
        y = 0;
        z = 0;
      }
      next = new Relative<Block>(blocks[x][y][z], x, y, z);
    }
  }
}
