package com.sopze.mc.redstonepp.block;

import com.google.common.collect.Lists;
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
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class InverterBlock extends ModLockableDiodeBlock implements I_OverlayInfoProvider {
  public static final MapCodec<InverterBlock> CODEC = simpleCodec(InverterBlock::new);
  public static final IntegerProperty SIGNAL;
  private static final Map<BlockGetter, List<Toggle>> TOGGLES;

  public @NotNull MapCodec<InverterBlock> codec() { return CODEC; }

  protected InverterBlock(Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SIGNAL, 0).setValue(LOCKED, false).setValue(POWERED, false));
  }

  protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
    Direction facing= state.getValue(FACING);
    return facing == dir ? state.getValue(SIGNAL) : 0;
  }

  protected int getOutputSignal(BlockGetter level, BlockPos pos, BlockState state) { return state.getValue(SIGNAL); }

  protected int calculateOutputSignal(BlockGetter level, BlockPos pos, BlockState state) {
    return 15 - this.getInputSignal((Level)level, pos, state);
  }

  protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    if (!this.isLocked(level, pos, state)) {
      boolean wasPowered = state.getValue(POWERED);
      boolean powered = this.shouldTurnOn(level, pos, state);

      int current= state.getValue(SIGNAL);
      int signal= this.calculateOutputSignal(level, pos, state);

      if(wasPowered != powered || current != signal){

        List<Toggle> list = TOGGLES.get(level);
        if(list != null) while(!list.isEmpty() && level.getGameTime() - list.getFirst().timestamp > 24L) list.removeFirst();

        if(isToggledTooFrequently(level, pos, (!wasPowered && powered) || signal > current)) {
          if(!wasPowered && current == 0) return;
          if(level.getBlockTicks().hasScheduledTick(pos, this)) level.getBlockTicks().clearArea(BoundingBox.fromCorners(BlockUtil.minCorner(pos), BlockUtil.maxCorner(pos)));
          level.setBlock(pos, state.setValue(POWERED, false).setValue(SIGNAL, 0), 3);
          level.levelEvent(1502, pos, 0);
        }
        else {
          level.setBlock(pos, state.setValue(POWERED, powered).setValue(SIGNAL, signal), 3);
        }
      }
    }
  }

  protected void checkTickOnNeighbor(Level level, BlockPos pos, BlockState state) {
    if (!this.isLocked(level, pos, state)) {
      boolean wasPowered = state.getValue(POWERED);
      boolean powered = this.shouldTurnOn(level, pos, state);

      int current= state.getValue(SIGNAL);
      int signal= this.calculateOutputSignal(level, pos, state);

      if ((wasPowered != powered || current != signal) && !level.getBlockTicks().willTickThisTick(pos, this)){

        List<Toggle> list = TOGGLES.get(level);
        if(list != null) while(!list.isEmpty() && level.getGameTime() - list.getFirst().timestamp > 24L) list.removeFirst();

        if(!isToggledTooFrequently(level, pos, false)) {
          level.scheduleTick(pos, this, this.getDelay(state), this.shouldPrioritize(level, pos, state) ? TickPriority.EXTREMELY_HIGH : TickPriority.VERY_HIGH);
        }
      }
    }
  }

  protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
    this.updateNeighborsInFront(level, pos, state);
    if(!isLocked(level, pos, state) && (this.shouldTurnOn(level, pos, state) || this.calculateOutputSignal(level, pos, state) > 0)) level.scheduleTick(pos, this, 1);
  }

  protected boolean isSignalSource(BlockState state) { return true; }

  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(new Property[]{FACING, SIGNAL, LOCKED, POWERED}); }

  private static boolean isToggledTooFrequently(Level level, BlockPos pos, boolean logToggle) {
    List<Toggle> list = TOGGLES.computeIfAbsent(level, (blockGetter) -> Lists.newArrayList());
    if (logToggle) list.add(new Toggle(pos.immutable(), level.getGameTime()));
    int i = 0;
    for(Toggle toggle : list) {
      if (toggle.pos.equals(pos) && ++i >= 8) return true;
    }
    return false;
  }

  static {
    SIGNAL = IntegerProperty.create("signal", 0, 15);
    TOGGLES = new WeakHashMap<>();
  }

  // ---------------------------------------------------------------- HELPERS

  public record Toggle(BlockPos pos, long timestamp) {}

  // ---------------------------------------------------------------- OVERLAY INFO

  public String[] getOverlayInfo(Level level, BlockPos pos, BlockState state) {
    return new String[]{
      "facing: " + state.getValue(FACING),
      "input: " + this.getInputSignal(level, pos, state),
      "output: " + state.getValue(SIGNAL),
    };
  }

  public float getOverlayHeight(Level level, BlockPos pos, BlockState state) { return .334f; }
  public boolean shouldRenderInfoOverlay(Level level, BlockPos pos, BlockState state) { return true; }
}
