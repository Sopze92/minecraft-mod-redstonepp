//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.sopze.mc.redstonepp.block;

import com.mojang.serialization.MapCodec;
import com.sopze.mc.redstonepp.block.entity.ManlightDetectorBlockEntity;
import com.sopze.mc.redstonepp.block.entity.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DaylightDetectorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ManlightDetectorBlock extends BaseEntityBlock implements I_OverlayInfoProvider {
  public static final MapCodec<ManlightDetectorBlock> CODEC = simpleCodec(ManlightDetectorBlock::new);
  public static final IntegerProperty POWER;
  public static final BooleanProperty INVERTED;
  private static final VoxelShape SHAPE;

  public @NotNull MapCodec<ManlightDetectorBlock> codec() {
    return CODEC;
  }

  public ManlightDetectorBlock(BlockBehaviour.Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(POWER, 0).setValue(INVERTED, false));
  }

  protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
    if (!player.mayBuild()) {
      return super.useWithoutItem(state, level, pos, player, hitResult);
    } else {
      if (!level.isClientSide()) {
        BlockState blockState = state.cycle(INVERTED);
        level.setBlock(pos, blockState, 2);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, Context.of(player, blockState));
        updateSignalStrength(blockState, level, pos);
      }

      return InteractionResult.SUCCESS;
    }
  }

  protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
    return state.getValue(POWER);
  }

  private static void updateSignalStrength(BlockState state, Level level, BlockPos pos) {
    int light = level.getBrightness(LightLayer.BLOCK, pos);
    if(state.getValue(INVERTED)) light= 15 - light;
    if (state.getValue(POWER) != light) level.setBlock(pos, state.setValue(POWER, light), 3);
  }

  protected boolean isSignalSource(BlockState state) {
    return true;
  }

  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new ManlightDetectorBlockEntity(pos, state);
  }

  @Nullable
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
    return !level.isClientSide() ? createTickerHelper(blockEntityType, ModBlockEntityTypes.MANLIGHT_DETECTOR, ManlightDetectorBlock::tickEntity) : null;
  }

  private static void tickEntity(Level level, BlockPos pos, BlockState state, ManlightDetectorBlockEntity blockEntity) {
    if (level.getGameTime() % 20L == 0L)  updateSignalStrength(state, level, pos);
  }

  protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
  protected boolean useShapeForLightOcclusion(BlockState state) { return true; }
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(new Property[]{POWER, INVERTED}); }

  static {
    POWER = BlockStateProperties.POWER;
    INVERTED = BlockStateProperties.INVERTED;
    SHAPE = Block.column(16.0, .0, 6.0);
  }

  // ---------------------------------------------------------------- OVERLAY INFO

  public String[] getOverlayInfo(Level level, BlockPos pos, BlockState state) {
    return new String[]{
      "inverted: " + state.getValue(INVERTED),
      "light: " + state.getValue(POWER)
    };
  }

  public float getOverlayHeight(Level level, BlockPos pos, BlockState state) { return .5f; }
  public boolean shouldRenderInfoOverlay(Level level, BlockPos pos, BlockState state) { return true; }
}
