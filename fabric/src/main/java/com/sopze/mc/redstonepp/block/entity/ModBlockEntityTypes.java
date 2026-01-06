package com.sopze.mc.redstonepp.block.entity;

import com.sopze.mc.redstonepp.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import static com.sopze.mc.redstonepp.Constants.MOD_ID;

@SuppressWarnings("unchecked")
public class ModBlockEntityTypes {

  public static final BlockEntityType<SignalOperatorBlockEntity> SIGNAL_OPERATOR;
  public static final BlockEntityType<LogicOperatorBlockEntity> LOGIC_OPERATOR;
  public static final BlockEntityType<ManlightDetectorBlockEntity> MANLIGHT_DETECTOR;
  public static final BlockEntityType<LightDetectorBlockEntity> LIGHT_DETECTOR;

  static{
    SIGNAL_OPERATOR = register("signal_operator", SignalOperatorBlockEntity::new, ModBlocks.SIGNAL_OPERATOR);
    LOGIC_OPERATOR = register("logic_operator", LogicOperatorBlockEntity::new, ModBlocks.LOGIC_OPERATOR);
    MANLIGHT_DETECTOR= register("manlight_detector", ManlightDetectorBlockEntity::new, ModBlocks.MANLIGHT_DETECTOR);
    LIGHT_DETECTOR= register("light_detector", LightDetectorBlockEntity::new, ModBlocks.LIGHT_DETECTOR);
  }

  public static <T extends BlockEntityType<?>> T register(String path, FabricBlockEntityTypeBuilder.Factory<? extends BlockEntity> factory, Block... blocks) {
    return (T)Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, path), FabricBlockEntityTypeBuilder.create(factory, blocks).build());
  }

  public static void initialize() {}
}
