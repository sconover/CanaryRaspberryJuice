package com.stuffaboutcode.canaryraspberryjuice;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

  public Map<BlockType, Blocks> toBlockTypeToBlocks() {
    Map<BlockType, List<Block>> blockTypeToBlockList = new LinkedHashMap<BlockType, List<Block>>();
    for (Block block: blocks) {
      if (!blockTypeToBlockList.containsKey(block.getType())) {
        blockTypeToBlockList.put(block.getType(), new ArrayList<Block>());
      }
      blockTypeToBlockList.get(block.getType()).add(block);
    }

    Map<BlockType, Blocks> blockTypeToBlocks = new LinkedHashMap<BlockType, Blocks>();
    for (Map.Entry<BlockType, List<Block>> entry: blockTypeToBlockList.entrySet()) {
      blockTypeToBlocks.put(entry.getKey(), new Blocks(entry.getValue()));
    }
    return ImmutableMap.copyOf(blockTypeToBlocks);
  }

  public int size() {
    return blocks.size();
  }
}
