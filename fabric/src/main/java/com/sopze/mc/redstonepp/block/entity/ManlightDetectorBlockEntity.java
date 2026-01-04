package com.sopze.mc.redstonepp.block.entity;

import com.sopze.mc.redstonepp.block.SignalOperatorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ManlightDetectorBlockEntity extends BlockEntity {
  public ManlightDetectorBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntityTypes.MANLIGHT_DETECTOR, pos, state); }
}
