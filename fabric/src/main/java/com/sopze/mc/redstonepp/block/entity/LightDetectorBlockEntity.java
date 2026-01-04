package com.sopze.mc.redstonepp.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LightDetectorBlockEntity extends BlockEntity {
  public LightDetectorBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntityTypes.LIGHT_DETECTOR, pos, state); }
}
