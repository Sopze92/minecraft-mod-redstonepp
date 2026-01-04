package com.sopze.mc.redstonepp.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

public class HelperMixin {

  @Mixin(HopperBlock.class)
  public interface I_HopperBlockInvoker {
    @Invoker("getAnalogOutputSignal") int i_getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction);
  }

  @Mixin(CrafterBlock.class)
  public interface I_CrafterBlockInvoker {
    @Accessor("ORIENTATION") static EnumProperty<FrontAndTop> getOrientation() { throw new AssertionError(); }
    @Invoker("getAnalogOutputSignal") int i_getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction);
  }

  @Mixin(DiodeBlock.class)
  public interface I_DiodeBlockInvoker {
    @Invoker("getOutputSignal") int i_getOutputSignal(BlockGetter level, BlockPos pos, BlockState state);
    @Invoker("getInputSignal") int i_getInputSignal(Level level, BlockPos pos, BlockState state);
    @Invoker("getAlternateSignal") int i_getAlternateSignal(SignalGetter level, BlockPos pos, BlockState state);
  }

  @Mixin(ComparatorBlock.class)
  public interface I_ComparatorBlockInvoker {
    @Invoker("calculateOutputSignal") int i_calculateOutputSignal(Level level, BlockPos pos, BlockState state);
  }
}
