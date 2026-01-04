package com.sopze.mc.redstonepp.particles;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import static com.sopze.mc.redstonepp.Constants.MOD_ID;

public class ModParticles {

  public static final SimpleParticleType AMETHYST_GLOW;

  static {
    AMETHYST_GLOW = register("amethyst_glow");
  }

  public static SimpleParticleType register(String id){ return Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, id), FabricParticleTypes.simple()); }

  public static void initialize(){}

}
