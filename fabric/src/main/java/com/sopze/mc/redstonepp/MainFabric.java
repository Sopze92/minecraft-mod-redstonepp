package com.sopze.mc.redstonepp;

import com.sopze.mc.redstonepp.block.ModBlocks;
import com.sopze.mc.redstonepp.block.entity.ModBlockEntityTypes;
import com.sopze.mc.redstonepp.item.ModItems;
import com.sopze.mc.redstonepp.state.ModSettings;
import com.sopze.mc.redstonepp.wrapper.I_LoaderWrapper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import static com.sopze.mc.redstonepp.Constants.*;

public class MainFabric implements ModInitializer, I_LoaderWrapper {

  @Override
	public void onInitialize() {
    MainCommon.initialize(this);

    PayloadTypeRegistry.playS2C().register(Network.GreetPayload.PAYLOAD_ID, Network.GreetPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(Network.GreetPayload.PAYLOAD_ID, Network.GreetPayload.CODEC);

    ServerPlayConnectionEvents.JOIN.register(MainFabric::_onUserConnected_Server);

    ModBlocks.initialize();
    ModBlockEntityTypes.initialize();
    ModItems.initialize();
	}

  private static void _onUserConnected_Server(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server){
    if(!server.isDedicatedServer()){ return; }
    ServerPlayer player= handler.player;
    Logger.slog(CHECKING_CLIENT_MOD, player.getName().getString());
    player.connection.send(ServerPlayNetworking.createS2CPacket(new Network.GreetPayload(MainCommon.getLocalVersion())));
  }

  // wrapper

  public boolean isModLoaded(String modid) { return FabricLoader.getInstance().isModLoaded(modid); }

  public String computeVersionString(){
    return FabricLoader.getInstance().getModContainer(MOD_ID)
      .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
      .orElse("0.0.0X-MissingVersion");
  }
}