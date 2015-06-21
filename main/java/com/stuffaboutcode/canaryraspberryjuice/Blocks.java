package com.stuffaboutcode.canaryraspberryjuice;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;

public class Blocks {
  private final List<Block> blocks;

  public Blocks(List<Block> blocks) {
    this.blocks = ImmutableList.copyOf(blocks);
  }

  public BlockType[] toBlockTypeArray() {
    List<BlockType> blockTypes = new ArrayList<BlockType>();
    for (Block block : blocks) {
      blockTypes.add(block.getType());
    }
    return blockTypes.toArray(new BlockType[blockTypes.size()]);
  }
}
