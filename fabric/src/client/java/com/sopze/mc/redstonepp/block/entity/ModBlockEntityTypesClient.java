package com.sopze.mc.redstonepp.block.entity;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class ModBlockEntityTypesClient {

  public static void register(){
    BlockEntityRenderers.register(ModBlockEntityTypes.SIGNAL_OPERATOR, SignalOperatorBlockEntityRenderer::new);
    BlockEntityRenderers.register(ModBlockEntityTypes.LOGIC_OPERATOR, LogicOperatorBlockEntityRenderer::new);
  }
}
