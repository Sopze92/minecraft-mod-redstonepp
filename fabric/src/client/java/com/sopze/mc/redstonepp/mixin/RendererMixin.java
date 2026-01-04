package com.sopze.mc.redstonepp.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

public class RendererMixin {

  @Mixin(GameRenderer.class)
  public interface I_GameRendererInvoker {
    @Invoker("bobView") void i_bobView(PoseStack poseStack, float partialTicks);
    @Invoker("bobHurt") void i_bobHurt(PoseStack poseStack, float partialTicks);
  }
}
