package com.sopze.mc.redstonepp.block;

import com.mojang.serialization.MapCodec;
import com.sopze.mc.redstonepp.block.entity.SignalOperatorBlockEntity;
import com.sopze.mc.redstonepp.particles.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class SignalOperatorBlock extends ModDiodeBlock implements EntityBlock, I_OverlayInfoProvider {
  public static final MapCodec<SignalOperatorBlock> CODEC = simpleCodec(SignalOperatorBlock::new);
  public static final IntegerProperty SIGNAL;
  public static final IntegerProperty VALUE;
  public static final EnumProperty<SignalTesterMode> MODE;

  public @NotNull MapCodec<SignalOperatorBlock> codec() { return CODEC; }

  protected SignalOperatorBlock(Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SIGNAL, 0).setValue(VALUE, 1).setValue(MODE, SignalTesterMode.EQUAL).setValue(POWERED, false));
  }

  protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
    if(player.getAbilities().mayBuild && stack.is(Items.REDSTONE) && hand == InteractionHand.MAIN_HAND){
      state= state.cycle(MODE);
      level.playSound(player, pos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 0.3F, .45f);
      level.setBlock(pos, state, 3);
      level.scheduleTick(pos, this, 0, TickPriority.VERY_HIGH);
      refreshBlockEntity(level, pos, state);
      animateOperator(state, level, pos, level.getRandom());
      return InteractionResult.SUCCESS;
    }
    return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
  }

  protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (!player.getAbilities().mayBuild) return InteractionResult.PASS;
    state= state.cycle(VALUE);
    float f = state.getValue(VALUE) > 0 ? 0.55F : 0.5F;
    level.playSound(player, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, f);
    level.setBlock(pos, state, 3);
    level.scheduleTick(pos, this, 0, TickPriority.VERY_HIGH);
    return InteractionResult.SUCCESS;
  }

  protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
    Direction facing= state.getValue(FACING);
    return facing == dir ? state.getValue(SIGNAL) : 0;
  }

  protected int getOutputSignal(BlockGetter level, BlockPos pos, BlockState state) { return state.getValue(SIGNAL); }

  protected int calculateOutputSignal(BlockGetter level, BlockPos pos, BlockState state) {
    Level _level= (Level)level;
    boolean wiredBack = this.isWiredOnBack(_level, pos, state, false);
    boolean wiredSide = this.isWiredOnSide(_level, pos, state, sideInputDiodesOnly());

    int input= wiredBack ? this.getInputSignal(_level, pos, state) : 0;
    int inputside= wiredSide ? this.getAlternateSignal(_level, pos, state) : 0;
    int value= state.getValue(VALUE);
    return (wiredBack || wiredSide) ? state.getValue(MODE).test(wiredBack ? input : value, wiredSide ? inputside : value) ? Math.max(1, wiredBack ? input : value) : 0 : 0;
  }

  protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    int signal= this.calculateOutputSignal(level, pos, state);

    boolean wasPowered= state.getValue(POWERED);
    boolean powered = this.shouldTurnOn(level, pos, state);

    if(wasPowered != powered || state.getValue(SIGNAL) != signal){
      level.setBlock(pos, state.setValue(POWERED, powered).setValue(SIGNAL, signal), 3);
    }
  }

  protected void checkTickOnNeighbor(Level level, BlockPos pos, BlockState state) {
    int signal= this.calculateOutputSignal(level, pos, state);

    boolean wasPowered= state.getValue(POWERED);
    boolean powered = this.shouldTurnOn(level, pos, state);

    if ((wasPowered != powered || state.getValue(SIGNAL) != signal) && !level.getBlockTicks().willTickThisTick(pos, this)) {
      level.scheduleTick(pos, this, this.getDelay(state), this.shouldPrioritize(level, pos, state) ? TickPriority.EXTREMELY_HIGH : TickPriority.VERY_HIGH);
    }
  }

  protected boolean shouldTurnOn(Level level, BlockPos pos, BlockState state) {
    return this.getInputSignal(level, pos, state) > 0 || this.getAlternateSignal(level, pos, state) > 0;
  }

  public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
    if (this.shouldTurnOn(level, pos, state)) level.scheduleTick(pos, this, 1);
  }

  protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
    this.updateNeighborsInFront(level, pos, state);
    if (this.shouldTurnOn(level, pos, state)) level.scheduleTick(pos, this, 1);
  }

  public ConnectionMode getConnectionMode() { return ConnectionMode.ALL; }

  protected boolean isSignalSource(BlockState state) { return true; }

  protected boolean sideInputDiodesOnly() { return false; }

  public float getYRotation(BlockState state) { return state.getValue(FACING).toYRot() + 180.0f; }

  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(new Property[]{FACING, SIGNAL, VALUE, MODE, POWERED}); }

  public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new SignalOperatorBlockEntity(pos, state, state.getValue(MODE));
  }

  public void refreshBlockEntity(Level level, BlockPos pos, BlockState state){
    BlockEntity blockEntity = level.getBlockEntity(pos);
    if (blockEntity instanceof SignalOperatorBlockEntity entity) entity.updateMode(state.getValue(MODE));
  }

  public void animateOperator(BlockState state, Level level, BlockPos pos, RandomSource random) {

    Direction direction = state.getValue(FACING);
    Direction direction2 = direction.getClockWise();

    float bx = pos.getX() + .5f;
    float by = pos.getY() + .4f;
    float bz = pos.getZ() + .5f;

    float sx= direction.getStepX();
    float sz= direction.getStepZ();

    for(int i=0; i< 4; i++){
      float s = random.nextFloat();
      float y = by - (s - .5f) * 0.2f;
      float f0 = (s+.15f) * 0.3f;
      float f1 = (random.nextFloat() - .5f) * 0.3375f;
      float xo = direction.getStepX() * f0 + direction2.getStepX() * f1;
      float zo = direction.getStepZ() * f0 + direction2.getStepZ() * f1;
      level.addParticle(DustParticleOptions.REDSTONE, bx + xo, y, bz + zo, sx, 1, sz);
    }
  }

  public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {

    if (state.getValue(POWERED)) {
      Direction direction = state.getValue(FACING);

      if(random.nextBoolean()){
        float x = pos.getX() + .5f + (random.nextFloat() - .5f) * .2f;
        float y = pos.getY() + .525f + (random.nextFloat() - .5f) * .2f;
        float z = pos.getZ() + .5f + (random.nextFloat() - .5f) * .2f;
        float xo = direction.getStepX() * -.3125f;
        float zo = direction.getStepZ() * -.3125f;
        level.addParticle(DustParticleOptions.REDSTONE, x + xo, y, z + zo, .0f, .0f, .0f);
      }
      else if(state.getValue(SIGNAL) > 0){
        Direction direction2 = direction.getClockWise();

        float s = random.nextFloat();
        float x = pos.getX() + .5f;
        float y = pos.getY() + .3f - (s - .5f) * 0.24f;
        float z = pos.getZ() + .5f;
        float f0 = (s - .35f) * 0.575f;
        float f1 = (random.nextFloat() - .5f) * 0.675f;
        float xo = direction.getStepX() * f0 + direction2.getStepX() * f1;
        float zo = direction.getStepZ() * f0 + direction2.getStepZ() * f1;
        level.addParticle(ModParticles.AMETHYST_GLOW, x + xo, y, z + zo, .0f, .0f, .0f);
      }
    }
  }

  static {
    SIGNAL = IntegerProperty.create("signal", 0, 15);
    VALUE = IntegerProperty.create("value", 0, 15);
    MODE = EnumProperty.create("mode", SignalTesterMode.class);
  }

  // ---------------------------------------------------------------- OVERLAY INFO

  public String[] getOverlayInfo(Level level, BlockPos pos, BlockState state) {
    int input= this.getInputSignal(level, pos, state);
    int inputside= this.getAlternateSignal(level, pos, state);
    return new String[]{
      "powered: " + state.getValue(POWERED),
      "facing: " + state.getValue(FACING),
      "operator: " + state.getValue(MODE).getInfoString(),
      String.format("value: %d%s", state.getValue(VALUE), input > 0 && inputside > 0 ? " (unused)":"" ),
      inputside == 0 ? ("input: " + input) : (String.format("input: %d, %d side", this.getInputSignal(level,pos,state), inputside)),
      "output: " + state.getValue(SIGNAL),
    };
  }

  public float getOverlayHeight(Level level, BlockPos pos, BlockState state) { return .334f; }
  public boolean shouldRenderInfoOverlay(Level level, BlockPos pos, BlockState state) { return true; }

  // ---------------------------------------------------------------- HELPERS

  @SuppressWarnings("UnnecessaryUnboxing")
  public enum SignalTesterMode implements StringRepresentable {
    EQUAL("equal", "==", (a,b)->a.intValue() == b.intValue()),
    NEQUAL("notequal", "!=", (a,b)->a.intValue() != b.intValue()),
    GREATER("greater", ">", (a,b)->a.intValue() > b.intValue()),
    GEQUAL("greaterequal", ">=", (a,b)->a.intValue() >= b.intValue()),
    LESS("less", "<", (a,b)->a.intValue() < b.intValue()),
    LEQUAL("lessequal", "<=", (a,b)->a.intValue() <= b.intValue());

    private final String name, operator;
    private final BiFunction<Integer, Integer, Boolean> function;

    private SignalTesterMode(String name, String operator, BiFunction<Integer, Integer, Boolean> function) { this.name = name; this.operator= operator; this.function= function; }

    public @NotNull String getSerializedName() { return this.name; }
    public @NotNull String getOperator() { return this.operator; }
    public @NotNull String getInfoString() { return String.format("%s (%s)", this.name.toUpperCase(), this.operator); }

    public boolean test(int a, int b) { return function.apply(a,b); }
  }
}
