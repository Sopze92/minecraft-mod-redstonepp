package com.sopze.mc.redstonepp.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class AmethystGlowParticle extends SingleQuadParticle {
  private final SpriteSet sprites;

  AmethystGlowParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
    super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites.first());
    this.friction = .96f;
    this.speedUpWhenYMotionIsBlocked = true;
    this.sprites = sprites;
    this.quadSize *= .75f;
    this.hasPhysics = false;
    this.setSpriteFromAge(sprites);
  }

  public SingleQuadParticle.@NotNull Layer getLayer() {
    return Layer.TRANSLUCENT;
  }

  public int getLightColor(float partialTick) {
    float f = ((float)this.age + partialTick) / (float)this.lifetime;
    f = Mth.clamp(f, .0f, 1.0f);
    int i = super.getLightColor(partialTick);
    int j = i & 255;
    int k = i >> 16 & 255;
    j += (int)(f * 15.0f * 16.0f);
    if (j > 240) {
      j = 240;
    }

    return j | k << 16;
  }

  public void tick() {
    double prevyd= this.yd;
    super.tick();
    this.setSpriteFromAge(this.sprites);
    this.yd= prevyd * 1.02;
  }

  @Environment(EnvType.CLIENT)
  public static class Provider implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet sprite;

    public Provider(SpriteSet sprites) {
      super();
      this.sprite = sprites;
    }

    public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
      AmethystGlowParticle particle = new AmethystGlowParticle(clientLevel, d, e, f, (.5 - randomSource.nextDouble()) * .4f, h, (.5 - randomSource.nextDouble()) * .4f, this.sprite);
      if (randomSource.nextBoolean()) particle.setColor(.85f, .8f, .95f);
      else particle.setColor(.7f, .4f, 1.0f);

      particle.yd *= .15;
      if (g == .0 && i == .0) {
        particle.xd *= .0125;
        particle.zd *= .0125;
      }

      particle.setLifetime((int)(8.0f / (randomSource.nextFloat() * .8f + .2f)));
      return particle;
    }
  }
}
