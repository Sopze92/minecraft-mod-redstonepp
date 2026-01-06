package com.sopze.mc.redstonepp.particles;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public class ModParticlesClient {

  public static void register(){
    ParticleFactoryRegistry.getInstance().register(ModParticles.AMETHYST_GLOW, AmethystGlowParticle.Provider::new);
  }

  public static void initialize(){}

}
