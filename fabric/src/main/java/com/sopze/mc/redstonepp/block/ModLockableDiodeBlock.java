package com.sopze.mc.redstonepp.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

public abstract class ModLockableDiodeBlock extends ModDiodeBlock {

  public static final BooleanProperty LOCKED;

  protected ModLockableDiodeBlock(Properties properties) { super(properties); }

  public @NotNull BlockState getStateForPlacement(BlockPlaceContext context) {
    BlockState blockState = super.getStateForPlacement(context);
    return blockState.setValue(LOCKED, this.isLocked(context.getLevel(), context.getClickedPos(), blockState));
  }

  protected @NotNull BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
    if (direction == Direction.DOWN && !this.canSurviveOn(level, neighborPos, neighborState)) {
      return Blocks.AIR.defaultBlockState();
    } else {
      return !level.isClientSide() && direction.getAxis() != state.getValue(FACING).getAxis() ? state.setValue(LOCKED, this.isLocked(level, pos, state)) : super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }
  }

  public boolean isLocked(LevelReader level, BlockPos pos, BlockState state) {
    return this.getAlternateSignal(level, pos, state) > 0;
  }

  static {
    LOCKED = BlockStateProperties.LOCKED;
  }
}
