package com.sopze.mc.redstonepp.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sopze.mc.redstonepp.block.SignalOperatorBlock;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SignalOperatorBlockEntityRenderer implements BlockEntityRenderer<SignalOperatorBlockEntity, SignalOperatorBlockEntityRenderer.RenderState> {
  private final Font font;

  public SignalOperatorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    super();
    this.font = context.font();
  }

  public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
    BlockState state = renderState.blockState;
    SignalOperatorBlock block = (SignalOperatorBlock)state.getBlock();
    poseStack.pushPose();
    poseStack.translate(.5f, .5f, .5f);
    float r= block.getYRotation(state);
    poseStack.mulPose(Axis.YP.rotationDegrees(r%180 != 0 ? r : r+180.0f));
    poseStack.mulPose(Axis.XP.rotationDegrees(125.0f));
    poseStack.translate(.025f, .0375f, -.0375f);
    poseStack.scale(.05f, .05f, .05f);

    int color= 0xFFE8DED0;
    int light= renderState.lightCoords;

    FormattedCharSequence fchar= renderState.modeText;

    float x = -this.font.width(fchar) *.5f;
    nodeCollector.submitText(poseStack, x, .0f, fchar, false, Font.DisplayMode.POLYGON_OFFSET, light, color, 0, 0);

    poseStack.popPose();
  }

  public @NotNull SignalOperatorBlockEntityRenderer.RenderState createRenderState() { return new RenderState(); }

  public void extractRenderState(SignalOperatorBlockEntity blockEntity, SignalOperatorBlockEntityRenderer.RenderState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
    BlockEntityRenderState.extractBase(blockEntity, renderState, breakProgress);
    renderState.modeText= blockEntity.getModeText();
  }

  public static class RenderState extends BlockEntityRenderState {
    public FormattedCharSequence modeText;
    public RenderState() { super(); modeText= FormattedCharSequence.EMPTY; }
  }
}
