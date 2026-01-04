package com.sopze.mc.redstonepp.mixin;

import com.sopze.mc.redstonepp.block.EmitterBlock;
import com.sopze.mc.redstonepp.block.ModBlocks;
import com.sopze.mc.redstonepp.block.ModDiodeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

public class RedstoneMixin {

  @Mixin(RepeaterBlock.class)
  public static class RepeaterBlockMixin {

    @Inject(method="useWithoutItem(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", at= @At("RETURN"), cancellable = true)
    private void h_useWithoutItem_r(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir){
      if(cir.getReturnValue() == InteractionResult.SUCCESS){
        float f = state.getValue(RepeaterBlock.DELAY) > 0 ? 0.55F : 0.5F;
        level.playSound(player, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, f);
      }
    }
  }

  @Mixin(DiodeBlock.class)
  public static class DiodeBlockMixin {

    @Inject(method="getAlternateSignal(Lnet/minecraft/world/level/SignalGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)I",
      at= @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void h_getAlternateSignal_r(SignalGetter level, BlockPos pos, BlockState state, CallbackInfoReturnable<Integer> cir, Direction direction, Direction direction2, Direction direction3, boolean bl){
      if(bl){
        BlockPos pos2= pos.relative(direction2);
        BlockPos pos3= pos.relative(direction3);
        if(level.getBlockState(pos2).getBlock() instanceof EmitterBlock || level.getBlockState(pos3).getBlock() instanceof EmitterBlock) {
          cir.setReturnValue(Math.max(level.getControlInputSignal(pos2, direction2, false), level.getControlInputSignal(pos3, direction3, false)));
        }
      }
    }
  }

  @Mixin(RedStoneWireBlock.class)
  public static class RedStoneWireBlockMixin {

    @Inject(method="shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z", at= @At(value="RETURN", ordinal=3), cancellable = true)
    private static void h_shouldConnectTo_r(BlockState state, Direction direction, CallbackInfoReturnable<Boolean> cir){ _shouldConnectTo(state, direction, cir); }

    @Inject(method="shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z", at= @At("TAIL"), cancellable = true)
    private static void h_shouldConnectTo_t(BlockState state, Direction direction, CallbackInfoReturnable<Boolean> cir){ _shouldConnectTo(state, direction, cir); }

    @Unique
    private static void _shouldConnectTo(BlockState state, Direction direction, CallbackInfoReturnable<Boolean> cir){
      if (state.getBlock() instanceof ModDiodeBlock block){

        // doing a switch() makes mixin fail due mixin class direct-reference, prolly switch implementations are made jumping out of then back to this class

        ModDiodeBlock.ConnectionMode mode= block.getConnectionMode();

        if(mode == ModDiodeBlock.ConnectionMode.FRONT){
          cir.setReturnValue(direction == state.getValue(ModDiodeBlock.FACING));
        }
        else if(mode == ModDiodeBlock.ConnectionMode.LINE){
          Direction direction2 = state.getValue(ModDiodeBlock.FACING);
          cir.setReturnValue(direction2 == direction || direction2.getOpposite() == direction);
        }
        else if(mode == ModDiodeBlock.ConnectionMode.ALL){
          cir.setReturnValue(true);
        }
      }
      else if (state.is(ModBlocks.EMITTER)) {
        cir.setReturnValue(direction == state.getValue(EmitterBlock.FACING));
      }
    }
  }
}
