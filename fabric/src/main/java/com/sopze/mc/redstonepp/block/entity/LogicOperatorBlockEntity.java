package com.sopze.mc.redstonepp.block.entity;

import com.sopze.mc.redstonepp.block.LogicOperatorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LogicOperatorBlockEntity extends BlockEntity {

  private LogicOperatorBlock.LogicTesterMode mode;
  private FormattedCharSequence modeText;

  public LogicOperatorBlockEntity(BlockPos pos, BlockState state) { this(pos, state, LogicOperatorBlock.LogicTesterMode.AND); }

  public LogicOperatorBlockEntity(BlockPos pos, BlockState state, LogicOperatorBlock.LogicTesterMode mode) {
    super(ModBlockEntityTypes.LOGIC_OPERATOR, pos, state);
    updateMode(mode);
  }

  public void updateMode(LogicOperatorBlock.LogicTesterMode mode){
    this.mode= mode;
    modeText= FormattedCharSequence.forward(mode.getOperator(), Style.EMPTY);
  }
  public FormattedCharSequence getModeText() { return modeText; }

}
