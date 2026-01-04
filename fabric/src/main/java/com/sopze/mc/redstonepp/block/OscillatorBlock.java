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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.NotNull;

public class OscillatorBlock extends ModDiodeBlock implements I_OverlayInfoProvider {
  public static final MapCodec<OscillatorBlock> CODEC = simpleCodec(OscillatorBlock::new);
  public static final IntegerProperty LEVEL;
  public static final IntegerProperty SIGNAL;

  private static final int[] RATES = new int[]{ 4, 5, 10, 15, 20, 40, 50, 100 };

  public @NotNull MapCodec<OscillatorBlock> codec() { return CODEC; }

  protected OscillatorBlock(Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LEVEL, 4).setValue(SIGNAL, 0).setValue(POWERED, false));
  }

  protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (!player.getAbilities().mayBuild) return InteractionResult.PASS;
    state= state.cycle(LEVEL);
    float f = state.getValue(LEVEL) > 0 ? 0.55F : 0.5F;
    level.playSound(player, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, f);
    level.setBlock(pos, state, 3);
    level.scheduleTick(pos, this, 1, TickPriority.EXTREMELY_HIGH);
    return InteractionResult.SUCCESS;
  }

  protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
    return !state.getValue(POWERED) ? 0 : state.getValue(FACING) == dir ? state.getValue(SIGNAL) : 0;
  }

  protected int getOutputSignal(BlockGetter level, BlockPos pos, BlockState state) { return state.getValue(SIGNAL); }

  protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    boolean wasPowered = state.getValue(POWERED);
    boolean powered = this.shouldTurnOn(level, pos, state);

    int signal = powered ? calculateOutputSignal(level, pos, state) : 0;

    if(wasPowered != powered || (powered && state.getValue(SIGNAL) != signal)){
      level.setBlock(pos, state.setValue(POWERED, powered).setValue(SIGNAL, signal), wasPowered != powered ? 3 : 1);
      level.scheduleTick(pos, this, this.getDelay(state), TickPriority.VERY_HIGH);
    }
    else level.scheduleTick(pos, this, this.getDelay(state), TickPriority.NORMAL);
  }

  protected void checkTickOnNeighbor(Level level, BlockPos pos, BlockState state) {
    boolean wasPowered = state.getValue(POWERED);
    boolean powered = this.shouldTurnOn(level, pos, state);

    if (wasPowered != powered && !level.getBlockTicks().willTickThisTick(pos, this)) {
      level.scheduleTick(pos, this, this.getDelay(state), this.shouldPrioritize(level, pos, state) ? TickPriority.EXTREMELY_HIGH : wasPowered ? TickPriority.VERY_HIGH : TickPriority.HIGH);
    }
  }

  private int calculateOutputSignal(Level level, BlockPos pos, BlockState state) {
    int i= this.getInputSignal(level, pos, state);
    if(i==0) return 0;
    return (int)Math.round(Math.clamp(i * ((1.0 + Math.sin(level.getGameTime() * (Math.PI * 2 / RATES[state.getValue(LEVEL)]))) * .5f), .0f, 15.0f));
  }

  protected int getDelay(BlockState state) { return 1; }

  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(new Property[]{FACING, LEVEL, SIGNAL, POWERED}); }

  static {
    LEVEL = IntegerProperty.create("level", 0, 7);
    SIGNAL = IntegerProperty.create("signal", 0, 15);
  }

  // ---------------------------------------------------------------- OVERLAY INFO

  public String[] getOverlayInfo(Level level, BlockPos pos, BlockState state) {
    int levelValue= state.getValue(LEVEL);
    return new String[]{
      "powered: " + state.getValue(POWERED),
      "facing: " + state.getValue(FACING),
      String.format("interval: %d (%d ticks)", levelValue, RATES[levelValue]),
      "input: " + this.getInputSignal(level, pos, state)
    };
  }

  public float getOverlayHeight(Level level, BlockPos pos, BlockState state) { return .334f; }
  public boolean shouldRenderInfoOverlay(Level level, BlockPos pos, BlockState state) { return true; }
}
