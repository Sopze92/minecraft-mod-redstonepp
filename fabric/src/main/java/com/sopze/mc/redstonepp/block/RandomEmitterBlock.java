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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.NotNull;

public class RandomEmitterBlock extends ModDiodeBlock implements I_OverlayInfoProvider {
  public static final MapCodec<RandomEmitterBlock> CODEC = simpleCodec(RandomEmitterBlock::new);
  public static final BooleanProperty RANDOM;
  public static final IntegerProperty SIGNAL, INTERVAL;

  public @NotNull MapCodec<RandomEmitterBlock> codec() { return CODEC; }

  protected RandomEmitterBlock(Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SIGNAL, 0).setValue(INTERVAL, 5).setValue(POWERED, false));
  }

  protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (!player.getAbilities().mayBuild || state.getValue(RANDOM)) return InteractionResult.PASS;
    state= state.cycle(INTERVAL);
    float f = state.getValue(INTERVAL) > 1 ? 0.55f : 0.5f;
    level.playSound(player, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, f);
    level.setBlock(pos, state, 3);
    level.scheduleTick(pos, this, 1, TickPriority.EXTREMELY_HIGH);
    return InteractionResult.SUCCESS;
  }

  protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
    Direction facing= state.getValue(FACING);
    return facing == dir ? state.getValue(SIGNAL) : 0;
  }

  protected int getOutputSignal(BlockGetter level, BlockPos pos, BlockState state) { return state.getValue(SIGNAL); }

  protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    if(!state.getValue(POWERED) && state.getValue(SIGNAL) == 0){
      level.setBlock(pos, state.setValue(SIGNAL, getRandomValue(random)), 3);
      level.scheduleTick(pos, this, this.getDelay(state) * 2, TickPriority.HIGH);
    }
  }

  protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

    boolean wasPowered = state.getValue(POWERED);
    boolean powered = this.shouldTurnOn(level, pos, state);

    boolean randomtick= !powered;

    if(wasPowered != powered){
      level.setBlock(pos, state.setValue(RANDOM, randomtick).setValue(POWERED, powered), 3);
      level.scheduleTick(pos, this, this.getDelay(state), TickPriority.VERY_HIGH);
      return;
    }

    if(state.getValue(POWERED)){
      int current= state.getValue(SIGNAL);
      int signal= current > 1 ? current/2 : current == 1 ? 0 : getRandomValue(random);

      if(current != signal){
        level.setBlock(pos, state.setValue(RANDOM, randomtick).setValue(SIGNAL, signal), 3);
        level.scheduleTick(pos, this, signal > 0 ? this.getDelay(state) * (signal== 1 ? 1 : 2) : getRandomDelay(state, random), TickPriority.HIGH);
      }
    }
    else if(state.getValue(SIGNAL) > 0){
      int current= state.getValue(SIGNAL);
      int signal= current > 1 ? current/2 : 0;

      if(current != signal){
        level.setBlock(pos, state.setValue(RANDOM, randomtick).setValue(SIGNAL, signal), 3);
        if(signal > 0) level.scheduleTick(pos, this, this.getDelay(state), TickPriority.VERY_HIGH);
      }
    }
    else if (randomtick != state.getValue(RANDOM)){
      level.setBlock(pos, state.setValue(RANDOM, randomtick), 3);
    }
  }

  protected void checkTickOnNeighbor(Level level, BlockPos pos, BlockState state) {
    boolean wasPowered = state.getValue(POWERED);
    boolean powered = this.shouldTurnOn(level, pos, state);

    if (wasPowered != powered && !level.getBlockTicks().willTickThisTick(pos, this)) {
      if(level instanceof ServerLevel server && server.getBlockTicks().hasScheduledTick(pos, this)) server.getBlockTicks().clearArea(BoundingBox.fromCorners(BlockUtil.minCorner(pos), BlockUtil.maxCorner(pos)));
      level.scheduleTick(pos, this, 1, TickPriority.EXTREMELY_HIGH);
    }
  }

  private int getRandomDelay(BlockState state, RandomSource random) {
    int interval= state.getValue(INTERVAL);
    return (int)Math.clamp(random.nextFloat() * (float)random.nextInt(16) * (interval*interval), 1, 800);
  }

  private int getRandomValue(RandomSource random) { return 1 + random.nextInt(15); }

  protected boolean isRandomlyTicking(BlockState state) { return state.getValue(RANDOM); }

  protected boolean isSignalSource(BlockState state) { return true; }

  protected int getDelay(BlockState state) { return 1; }

  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(new Property[]{FACING, SIGNAL, INTERVAL, RANDOM, POWERED}); }

  static {
    INTERVAL = IntegerProperty.create("interval", 1, 8);
    RANDOM = BooleanProperty.create("random");
    SIGNAL = IntegerProperty.create("signal", 0, 15);
  }

  // ---------------------------------------------------------------- OVERLAY INFO

  public String[] getOverlayInfo(Level level, BlockPos pos, BlockState state) {
    boolean random= state.getValue(RANDOM);

    return new String[]{
      "powered: " + state.getValue(POWERED),
      "facing: " + state.getValue(FACING),
      "interval: " + (random ? "random tick" : state.getValue(INTERVAL))
    };
  }

  public float getOverlayHeight(Level level, BlockPos pos, BlockState state) { return .334f; }
  public boolean shouldRenderInfoOverlay(Level level, BlockPos pos, BlockState state) { return true; }
}
