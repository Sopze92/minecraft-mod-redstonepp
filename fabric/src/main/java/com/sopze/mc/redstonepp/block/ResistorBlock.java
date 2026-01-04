package com.sopze.mc.redstonepp.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.NotNull;

public class ResistorBlock extends ModLockableDiodeBlock implements I_OverlayInfoProvider {
  public static final MapCodec<ResistorBlock> CODEC = simpleCodec(ResistorBlock::new);
  public static final IntegerProperty LEVEL;
  public static final IntegerProperty SIGNAL;

  public @NotNull MapCodec<ResistorBlock> codec() { return CODEC; }

  protected ResistorBlock(Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LEVEL, 0).setValue(SIGNAL, 0).setValue(LOCKED, false).setValue(POWERED, false));
  }

  protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (!player.getAbilities().mayBuild) return InteractionResult.PASS;
    state= state.cycle(LEVEL);
    float f = state.getValue(LEVEL) > 0 ? 0.55F : 0.5F;
    level.playSound(player, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, f);
    level.setBlock(pos, state, 3);
    level.scheduleTick(pos, this, 0, TickPriority.VERY_HIGH);
    return InteractionResult.SUCCESS;
  }

  protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
    if(!state.getValue(POWERED)) return 0;
    Direction facing= state.getValue(FACING);
    return facing == direction ? state.getValue(SIGNAL) : 0;
  }

  protected int getOutputSignal(BlockGetter level, BlockPos pos, BlockState state) { return state.getValue(SIGNAL); }

  protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
    if(!state.getValue(POWERED)) return 0;
    Direction facing= state.getValue(FACING);
    int i= this.getInputSignal(level, pos, state);
    return facing == direction ? state.getValue(SIGNAL) : facing != direction.getOpposite() ? Math.min(i, i - state.getValue(SIGNAL)) : 0;
  }

  protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    if (!this.isLocked(level, pos, state)) {
      boolean wasPowered = state.getValue(POWERED);
      boolean powered = this.shouldTurnOn(level, pos, state);

      int input= powered ? this.getInputSignal(level, pos, state) : 0;
      int signal= input != 0 ? calculateOutputSignal(input, level, pos, state) : 0;

      if(wasPowered != powered || state.getValue(SIGNAL) != signal){
        level.setBlock(pos, state.setValue(POWERED, powered).setValue(SIGNAL, signal), 3);
        level.scheduleTick(pos, this, this.getDelay(state), TickPriority.VERY_HIGH);
      }
    }
  }

  protected void checkTickOnNeighbor(Level level, BlockPos pos, BlockState state) {
    if (!this.isLocked(level, pos, state)) {
      boolean wasPowered = state.getValue(POWERED);
      boolean powered = this.shouldTurnOn(level, pos, state);

      int input= powered ? this.getInputSignal(level, pos, state) : 0;
      int signal= input != 0 ? calculateOutputSignal(input, level, pos, state) : 0;

      if ((wasPowered != powered || state.getValue(SIGNAL) != signal) && !level.getBlockTicks().willTickThisTick(pos, this)) {
        level.scheduleTick(pos, this, this.getDelay(state), this.shouldPrioritize(level, pos, state) ? TickPriority.EXTREMELY_HIGH : wasPowered ? TickPriority.VERY_HIGH : TickPriority.HIGH);
      }
    }
  }

  private int calculateOutputSignal(int input, Level level, BlockPos pos, BlockState state) {
    return Math.max(0, input - state.getValue(LEVEL));
  }

  protected boolean hasAnalogOutputSignal(BlockState state) { return true; }

  protected boolean hasSideOutput(BlockState state) { return true; }

  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(new Property[]{FACING, LEVEL, SIGNAL, LOCKED, POWERED}); }

  static {
    LEVEL = IntegerProperty.create("level", 0, 15);
    SIGNAL = IntegerProperty.create("signal", 0, 15);
  }

  // ---------------------------------------------------------------- OVERLAY INFO

  public String[] getOverlayInfo(Level level, BlockPos pos, BlockState state) {
    Direction direction = state.getValue(FACING);
    return new String[]{
      "powered: " + state.getValue(POWERED),
      "facing: " + direction,
      "locked: " + state.getValue(LOCKED),
      "resistance: " + state.getValue(LEVEL),
      "input: " + this.getInputSignal(level, pos, state),
      "output: " + state.getValue(SIGNAL),
      "analog: " + this.getAnalogOutputSignal(state, level, pos, direction)
    };
  }

  public float getOverlayHeight(Level level, BlockPos pos, BlockState state) { return .334f; }
  public boolean shouldRenderInfoOverlay(Level level, BlockPos pos, BlockState state) { return true; }
}
