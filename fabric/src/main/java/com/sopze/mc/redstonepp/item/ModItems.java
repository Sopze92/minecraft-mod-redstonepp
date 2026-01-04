package com.sopze.mc.redstonepp.item;

import com.sopze.mc.redstonepp.MainCommon;
import com.sopze.mc.redstonepp.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Function;

import static com.sopze.mc.redstonepp.Constants.MOD_ID;

public class ModItems {

  private static final Item
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

  static{
    // ITEM BLOCKS
    EMITTER = _register(ModBlocks.EMITTER);
    RANDOM_EMITTER = _register(ModBlocks.RANDOM_EMITTER);
    INVERTER = _register(ModBlocks.INVERTER);
    PULSE = _register(ModBlocks.PULSE);
    OSCILLATOR = _register(ModBlocks.OSCILLATOR);

    DELAY = _register(ModBlocks.DELAY);
    REGULATOR = _register(ModBlocks.REGULATOR);
    RESISTOR = _register(ModBlocks.RESISTOR);
    COIL = _register(ModBlocks.COIL);

    SIGNAL_OPERATOR = _register(ModBlocks.SIGNAL_OPERATOR);
    LOGIC_OPERATOR = _register(ModBlocks.LOGIC_OPERATOR);

    MANLIGHT_DETECTOR = _register(ModBlocks.MANLIGHT_DETECTOR);
    LIGHT_DETECTOR = _register(ModBlocks.LIGHT_DETECTOR);
  }

  private static Item _register(String id, Function<Item.Properties, Item> factory) { return Items.registerItem(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, id)), factory); }
  private static Item _register(Block block){
    return Items.registerBlock(block);
  }

  public static void initialize(){
    ItemGroupEvents.MODIFY_ENTRIES_ALL.register(ModItems::_onModifyItemGroupEntriesEvent);
  }

  private static void _onModifyItemGroupEntriesEvent(CreativeModeTab group, FabricItemGroupEntries entries){
    if(!MainCommon.isEnabledLocally() || !(BuiltInRegistries.CREATIVE_MODE_TAB.wrapAsHolder(group).unwrapKey().orElse(null) instanceof ResourceKey<CreativeModeTab> groupKey)) return;

    else if(groupKey == CreativeModeTabs.REDSTONE_BLOCKS) {
      entries.addAfter(Blocks.REDSTONE_BLOCK, new Item[]{
        EMITTER
      });
      entries.addAfter(Blocks.COMPARATOR, new Item[]{
        RANDOM_EMITTER,
        INVERTER,
        PULSE,
        OSCILLATOR,

        DELAY,
        REGULATOR,
        RESISTOR,
        COIL,

        SIGNAL_OPERATOR,
        LOGIC_OPERATOR
      });
      entries.addAfter(Blocks.DAYLIGHT_DETECTOR, new Item[]{
        MANLIGHT_DETECTOR,
        LIGHT_DETECTOR
      });
    }
  }
}
