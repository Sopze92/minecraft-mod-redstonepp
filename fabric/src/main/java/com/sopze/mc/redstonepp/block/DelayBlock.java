package com.sopze.mc.redstonepp.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class DelayBlock extends ModLockableDiodeBlock implements I_OverlayInfoProvider {

  public static final MapCodec<DelayBlock> CODEC = simpleCodec(DelayBlock::new);
  public static final IntegerProperty LEVEL;

  private static final int[] DELAY_VALUES= new int[]{ 5, 10, 15, 20, 30, 40, 80 };

  public @NotNull MapCodec<DelayBlock> codec() {
    return CODEC;
  }

  public DelayBlock(BlockBehaviour.Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LEVEL, 1).setValue(LOCKED, false).setValue(POWERED, false));
  }

  protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
    if (!player.getAbilities().mayBuild) return InteractionResult.PASS;
    state= state.cycle(LEVEL);
    float f = state.getValue(LEVEL) > 0 ? 0.55F : 0.5F;
    level.playSound(player, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, f);
    level.setBlock(pos, state, 3);
    return InteractionResult.SUCCESS;
  }

  public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
    if (state.getValue(POWERED)) {
      Direction direction = state.getValue(FACING);
      float x = pos.getX() + 0.5f + (random.nextFloat() - 0.5f) * 0.2f;
      float y = pos.getY() + 0.525f + (random.nextFloat() - 0.5f) * 0.2f;
      float z = pos.getZ() + 0.5f + (random.nextFloat() - 0.5f) * 0.2f;
      float f = (random.nextBoolean() ? state.getValue(LEVEL) : -5.0F) * .0625f;

      float xo = direction.getStepX() * f;
      float zo = direction.getStepZ() * f;
      level.addParticle(DustParticleOptions.REDSTONE, x + xo, y, z + zo, .0f, .0f, .0f);
    }
  }

  protected int getDelay(BlockState state) { return DELAY_VALUES[state.getValue(LEVEL)]; }

  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(new Property[]{FACING, LEVEL, LOCKED, POWERED});
  }

  static {
    LEVEL = IntegerProperty.create("level", 0, 6);
  }

  // ---------------------------------------------------------------- OVERLAY INFO

  public String[] getOverlayInfo(Level level, BlockPos pos, BlockState state) {
    int levelValue= state.getValue(LEVEL);
    return new String[]{
      "powered: " + state.getValue(POWERED),
      "facing: " + state.getValue(FACING),
      "locked: " + state.getValue(LOCKED),
      String.format("delay: %d (%d ticks)", levelValue, DELAY_VALUES[levelValue]),
      "input: " + this.getInputSignal(level, pos, state),
    };
  }

  public float getOverlayHeight(Level level, BlockPos pos, BlockState state) { return .334f; }
  public boolean shouldRenderInfoOverlay(Level level, BlockPos pos, BlockState state) { return true; }
}
