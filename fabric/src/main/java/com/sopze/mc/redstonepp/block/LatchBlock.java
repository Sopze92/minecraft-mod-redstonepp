package com.sopze.mc.redstonepp.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.NotNull;

public class LatchBlock extends ModLockableDiodeBlock implements I_OverlayInfoProvider {
  public static final MapCodec<LatchBlock> CODEC = simpleCodec(LatchBlock::new);
  public static final IntegerProperty SIGNAL;

  public @NotNull MapCodec<LatchBlock> codec() { return CODEC; }

  protected LatchBlock(Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SIGNAL, 0).setValue(LOCKED, false).setValue(POWERED, false));
  }

  protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
    Direction facing= state.getValue(FACING);
    return facing == dir ? state.getValue(SIGNAL) : 0;
  }

  protected int getOutputSignal(BlockGetter level, BlockPos pos, BlockState state) { return state.getValue(SIGNAL); }

  protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    if (!this.isLocked(level, pos, state)) {
      boolean wasPowered = state.getValue(POWERED);
      boolean powered = this.shouldTurnOn(level, pos, state);

      if(wasPowered != powered){
        if(powered) {
          int signal= state.getValue(SIGNAL) == 0 ? this.getInputSignal(level, pos, state) : 0;
          level.setBlock(pos, state.setValue(POWERED, powered).setValue(SIGNAL, signal), 3);
        }
        else level.setBlock(pos, state.setValue(POWERED, powered), 3);
      }
    }
  }

  protected void checkTickOnNeighbor(Level level, BlockPos pos, BlockState state) {
    if (!this.isLocked(level, pos, state)) {
      boolean wasPowered = state.getValue(POWERED);
      boolean powered = this.shouldTurnOn(level, pos, state);

      if(wasPowered != powered && !level.getBlockTicks().willTickThisTick(pos, this)){
        level.scheduleTick(pos, this, this.getDelay(state), this.shouldPrioritize(level, pos, state) ? TickPriority.EXTREMELY_HIGH : TickPriority.VERY_HIGH);
      }
    }
  }

  protected boolean isSignalSource(BlockState state) { return true; }

  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(new Property[]{FACING, SIGNAL, LOCKED, POWERED}); }

  static {
    SIGNAL = IntegerProperty.create("signal", 0, 15);
  }

  // ---------------------------------------------------------------- OVERLAY INFO

  public String[] getOverlayInfo(Level level, BlockPos pos, BlockState state) {
    int signal= state.getValue(SIGNAL);
    return new String[]{
      "facing: " + state.getValue(FACING),
      "state: " + (signal > 0),
      "output: " + signal,
    };
  }

  public float getOverlayHeight(Level level, BlockPos pos, BlockState state) { return .334f; }
  public boolean shouldRenderInfoOverlay(Level level, BlockPos pos, BlockState state) { return true; }
}
