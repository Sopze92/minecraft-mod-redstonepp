package com.sopze.mc.redstonepp.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sopze.mc.redstonepp.block.LogicOperatorBlock;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LogicOperatorBlockEntityRenderer implements BlockEntityRenderer<LogicOperatorBlockEntity, LogicOperatorBlockEntityRenderer.RenderState> {
  private final Font font;

  public LogicOperatorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    super();
    this.font = context.font();
  }

  public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
    BlockState state = renderState.blockState;
    LogicOperatorBlock block = (LogicOperatorBlock)state.getBlock();
    poseStack.pushPose();
    poseStack.translate(.5f, .5f, .5f);
    float r= block.getYRotation(state);
    poseStack.mulPose(Axis.YP.rotationDegrees(r%180 != 0 ? r : r+180.0f));
    poseStack.mulPose(Axis.XP.rotationDegrees(125.0f));

    int color= 0xFFB38EF3;
    int light= renderState.lightCoords;

    FormattedCharSequence fchar= renderState.modeText;

    float w = this.font.width(fchar);
    float x = -w *.5f;

    float f= Math.max(.0f, (w-12.0f) * 0.00375f);
    float s= .05f - f;

    poseStack.translate(.025f, .0375f + f*3, -.0375f);
    poseStack.scale(s, s, s);

    nodeCollector.submitText(poseStack, x, .0f, fchar, false, Font.DisplayMode.POLYGON_OFFSET, light, color, 0, 0);

    /*
    BlockState state = renderState.blockState;
    SignalTesterBlock block = (SignalTesterBlock)state.getBlock();
    poseStack.pushPose();
    poseStack.mulPose(Axis.YP.rotationDegrees(-block.getYRotation(state)));
    poseStack.mulPose(Axis.XP.rotationDegrees(125.0f));
    poseStack.translate(.5125f, -.7325f, -.1625f);
    poseStack.scale(.0334f, .0334f, .0334f);

    int color= 0xFFFF0707;
    int light= renderState.lightCoords;


    String[] text= new String[]{state.getValue(SignalTesterBlock.MODE).getOperator(), state.getValue(SignalTesterBlock.VALUE).toString()};

    for(int i=0; i< text.length; i++){

      FormattedCharSequence fchar= FormattedCharSequence.forward(text[i], Style.EMPTY);

      float x = -this.font.width(fchar) *.5f;
      nodeCollector.submitText(poseStack, x, (float)(i * (font.lineHeight-1)), fchar, false, Font.DisplayMode.SEE_THROUGH, light, color, 0, 0);
    }
    */

    poseStack.popPose();
  }

  public @NotNull LogicOperatorBlockEntityRenderer.RenderState createRenderState() { return new RenderState(); }

  public void extractRenderState(LogicOperatorBlockEntity blockEntity, LogicOperatorBlockEntityRenderer.RenderState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
    BlockEntityRenderState.extractBase(blockEntity, renderState, breakProgress);
    //renderState.modeText= FormattedCharSequence.forward("op", Style.EMPTY);
    renderState.modeText= blockEntity.getModeText();
  }

  public static class RenderState extends BlockEntityRenderState {
    public FormattedCharSequence modeText;
    public RenderState() { super(); modeText= FormattedCharSequence.EMPTY; }
  }
}
