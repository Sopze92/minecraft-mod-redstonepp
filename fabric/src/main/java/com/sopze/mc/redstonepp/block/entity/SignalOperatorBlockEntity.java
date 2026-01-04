package com.sopze.mc.redstonepp.block.entity;

import com.sopze.mc.redstonepp.block.SignalOperatorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SignalOperatorBlockEntity extends BlockEntity {

  private SignalOperatorBlock.SignalTesterMode mode;
  private FormattedCharSequence modeText;

  public SignalOperatorBlockEntity(BlockPos pos, BlockState state) { this(pos, state, SignalOperatorBlock.SignalTesterMode.EQUAL); }

  public SignalOperatorBlockEntity(BlockPos pos, BlockState state, SignalOperatorBlock.SignalTesterMode mode) {
    super(ModBlockEntityTypes.SIGNAL_OPERATOR, pos, state);
    updateMode(mode);
  }

  public void updateMode(SignalOperatorBlock.SignalTesterMode mode){
    this.mode= mode;
    modeText= FormattedCharSequence.forward(mode.getOperator(), Style.EMPTY);
  }
  public FormattedCharSequence getModeText() { return modeText; }
}
