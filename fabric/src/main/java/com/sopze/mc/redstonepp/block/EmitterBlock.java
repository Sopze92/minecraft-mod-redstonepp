package com.sopze.mc.redstonepp.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class EmitterBlock extends HorizontalDirectionalBlock implements I_OverlayInfoProvider {
  public static final MapCodec<EmitterBlock> CODEC = simpleCodec(EmitterBlock::new);
  public static final IntegerProperty SIGNAL;
  private static final VoxelShape SHAPE;

  public @NotNull MapCodec<EmitterBlock> codec() { return CODEC; }

  protected EmitterBlock(Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(SIGNAL, 15));
  }

  protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (!player.getAbilities().mayBuild) return InteractionResult.PASS;
    state= state.cycle(SIGNAL);
    float f = state.getValue(SIGNAL) > 0 ? 0.55F : 0.5F;
    level.playSound(player, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, f);
    level.setBlock(pos, state, 3);
    return InteractionResult.SUCCESS;
  }

  protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) { return state.getSignal(level, pos, dir); }

  protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
    return state.getValue(FACING) == dir ? state.getValue(SIGNAL) : 0;
  }

  protected int getOutputSignal(BlockGetter level, BlockPos pos, BlockState state) { return state.getValue(SIGNAL); }

  protected boolean isSignalSource(BlockState p_52572_) { return true; }

  public BlockState getStateForPlacement(BlockPlaceContext context) { return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }

  protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }

  protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
    this.updateNeighborsInFront(level, pos, state);
  }

  protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
    if (!movedByPiston) {
      this.updateNeighborsInFront(level, pos, state);
    }
  }

  protected void updateNeighborsInFront(Level level, BlockPos pos, BlockState state) {
    Direction direction = state.getValue(FACING);
    BlockPos blockPos = pos.relative(direction.getOpposite());
    Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, direction.getOpposite(), Direction.UP);
    level.neighborChanged(blockPos, this, orientation);
    level.updateNeighborsAtExceptFromFacing(blockPos, this, direction, orientation);
  }

  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(new Property[]{FACING, SIGNAL});
  }

  public static int getLightLevel(BlockState state){ return state.getValue(SIGNAL) / 2; }

  static {
    SIGNAL = IntegerProperty.create("signal", 0, 15);
    SHAPE = Block.column(16.0, 0.0, 10.0);
  }

  // ---------------------------------------------------------------- OVERLAY INFO

  public String[] getOverlayInfo(Level level, BlockPos pos, BlockState state) {
    return new String[]{
      "facing: " + state.getValue(FACING),
      "output: " + state.getValue(SIGNAL),
    };
  }

  public float getOverlayHeight(Level level, BlockPos pos, BlockState state) { return .334f; }
  public boolean shouldRenderInfoOverlay(Level level, BlockPos pos, BlockState state) { return true; }
}
