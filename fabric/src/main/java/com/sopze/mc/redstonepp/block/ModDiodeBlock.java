package com.sopze.mc.redstonepp.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class ModDiodeBlock extends DiodeBlock {

  protected ModDiodeBlock(Properties properties) { super(properties); }

  public boolean isWiredOnBack(Level level, BlockPos pos, BlockState state, boolean onlyComponents){
    Direction facing= state.getValue(FACING);
    return isWired(level.getBlockState(pos.relative(facing)), facing, onlyComponents);
  }

  public boolean isWiredOnSide(Level level, BlockPos pos, BlockState state, boolean onlyComponents){
    Direction facing= state.getValue(FACING), side0= facing.getClockWise(), side1= facing.getCounterClockWise();
    return isWired(level.getBlockState(pos.relative(side0)), facing, onlyComponents) || isWired(level.getBlockState(pos.relative(side1)), facing, onlyComponents);
  }

  public boolean isWired(BlockState state, Direction direction, boolean onlyComponents){
    if(state.is(Blocks.REDSTONE_WIRE)) return true;
    if(onlyComponents) return false;
    Block inblock= state.getBlock();
    if(inblock instanceof ModDiodeBlock mdblock) {
      Direction infacing= state.getValue(FACING);
      return infacing == direction || (infacing != direction.getOpposite() && mdblock.hasSideOutput(state));
    }
    if(inblock instanceof DiodeBlock || inblock instanceof EmitterBlock) return state.getValue(FACING) == direction;
    return state.isSignalSource();
  }

  protected @NotNull BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
    if (direction == Direction.DOWN && !this.canSurviveOn(level, neighborPos, neighborState)) {
      return Blocks.AIR.defaultBlockState();
    } else {
      return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }
  }

  public ConnectionMode getConnectionMode() { return ConnectionMode.LINE; }

  protected boolean isSignalSource(BlockState state) { return false; }

  protected boolean hasSideOutput(BlockState state) { return false; }

  protected boolean hasAnalogOutputSignal(BlockState state) { return false; }

  protected boolean sideInputDiodesOnly() { return true; }

  public boolean isLocked(LevelReader level, BlockPos pos, BlockState state) { return false; }

  protected int getDelay(BlockState state) { return 0; }

  public enum ConnectionMode { FRONT, LINE, ALL }

}
