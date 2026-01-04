package com.sopze.mc.redstonepp.particles;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.particle.GlowParticle;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import static com.sopze.mc.redstonepp.Constants.MOD_ID;

public class ModParticlesClient {

  public static void register(){
    ParticleFactoryRegistry.getInstance().register(ModParticles.AMETHYST_GLOW, AmethystGlowParticle.Provider::new);
  }

  public static void initialize(){}

}
