package com.sopze.mc.redstonepp.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Function;

import static com.sopze.mc.redstonepp.Constants.MOD_ID;

public class ModBlocks {

  public static final Block[] CUTOUT_BLOCKS;

  public static final Block
    //WIRESTONE,
    EMITTER,
    RANDOM_EMITTER,
    INVERTER,
    PULSE,
    OSCILLATOR,
    DELAY,
    REGULATOR,
    RESISTOR,
    COIL,
    SIGNAL_OPERATOR,
    LOGIC_OPERATOR,
    MANLIGHT_DETECTOR,
    LIGHT_DETECTOR;

  static {
    //WIRESTONE = _register("wirestone", RedstoneWireStoneBlock::new, BlockBehaviour.Properties.of().instabreak().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY));
    EMITTER = _register("emitter", EmitterBlock::new, Properties.of().instabreak().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY).lightLevel(EmitterBlock::getLightLevel));
    RANDOM_EMITTER = _register("random_emitter", RandomEmitterBlock::new, Properties.of().instabreak().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY).randomTicks());
    INVERTER = _register("inverter", InverterBlock::new);

    PULSE = _register("pulse", PulseBlock::new);
    OSCILLATOR = _register("oscillator", OscillatorBlock::new);
    DELAY = _register("delay", DelayBlock::new);
    REGULATOR = _register("regulator", RegulatorBlock::new);
    RESISTOR = _register("resistor", ResistorBlock::new);
    COIL = _register("coil", CoilBlock::new);

    SIGNAL_OPERATOR = _register("signal_operator", SignalOperatorBlock::new);
    LOGIC_OPERATOR = _register("logic_operator", LogicOperatorBlock::new);

    MANLIGHT_DETECTOR = _register("manlight_detector", ManlightDetectorBlock::new);
    LIGHT_DETECTOR = _register("light_detector", LightDetectorBlock::new);

    CUTOUT_BLOCKS= new Block[]{ EMITTER, PULSE, OSCILLATOR, REGULATOR, RESISTOR, COIL, RANDOM_EMITTER, SIGNAL_OPERATOR, LOGIC_OPERATOR };
  }

  private static Block _register(String id, Function<Properties, Block> factory, Properties settings) { return Blocks.register(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, id)), factory, settings); }
  private static Block _register(String id, Properties settings) { return _register(id, Block::new, settings); }

  private static Block _register(String id, Function<Properties, Block> factory){ return _register(id, factory, Properties.of().instabreak().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY)); }

  public static void initialize(){}
}
