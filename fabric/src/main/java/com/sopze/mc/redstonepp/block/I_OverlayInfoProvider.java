package com.sopze.mc.redstonepp.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface I_OverlayInfoProvider {

  default String[] getOverlayInfo(Level level, BlockPos pos, BlockState state) { return new String[]{"missing info"}; }
  default float getOverlayHeight(Level level, BlockPos pos, BlockState state) { return .5f; }
  default boolean shouldRenderInfoOverlay(Level level, BlockPos pos, BlockState state) { return false; }
}
