package com.sopze.mc.redstonepp;

import com.sopze.mc.redstonepp.block.ModBlocks;
import com.sopze.mc.redstonepp.block.entity.ModBlockEntityTypesClient;
import com.sopze.mc.redstonepp.gui.DebugOverlay;
import com.sopze.mc.redstonepp.particles.ModParticlesClient;
import com.sopze.mc.redstonepp.state.ModOptions;
import com.sopze.mc.redstonepp.state.ModSettings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import java.io.File;

import static com.sopze.mc.redstonepp.Constants.*;

public class MainClient implements ClientModInitializer {

  private static boolean _SERVER_HAS_MOD = false;
  private static byte[] _SERVER_VERSION;

  public static ModOptions options;
  public static DebugOverlay debugOverlay;

  @Override
	public void onInitializeClient() {

    BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT, ModBlocks.CUTOUT_BLOCKS);
    ClientPlayConnectionEvents.DISCONNECT.register(MainClient::_onUserDisconnected_Client);
    ClientPlayConnectionEvents.JOIN.register(MainClient::_onUserConnected_Client);
    ClientPlayNetworking.registerGlobalReceiver(Network.GreetPayload.PAYLOAD_ID, MainClient::_onReceivedGreetPayload_Client);

    ModBlockEntityTypesClient.register();
    ModParticlesClient.register();

    Minecraft client= Minecraft.getInstance();
    options= new ModOptions(client, new File(client.gameDirectory, "config"));

    debugOverlay = new DebugOverlay();
	}

  private static void _onUserDisconnected_Client(ClientPacketListener handler, Minecraft client) {
    _SERVER_HAS_MOD = false;
    _SERVER_VERSION = new byte[] {0,0,0};
  }

  private static void _onUserConnected_Client(ClientPacketListener handler, PacketSender sender, Minecraft client) {
    boolean local= handler.getConnection().isMemoryConnection();
    _SERVER_HAS_MOD = local;
    _SERVER_VERSION = local ? MainCommon.getLocalVersion() : new byte[] {0,0,0};
    MainCommon.setEnabledLocally(local);
  }

  private static void _onReceivedGreetPayload_Client(Network.GreetPayload payload, ClientPlayNetworking.Context context) {
    _SERVER_HAS_MOD = true;
    _SERVER_VERSION = payload.read();
    final byte[] localVersion= MainCommon.getLocalVersion();

    boolean valid= Util.isCompatibleVersion(_SERVER_VERSION);
    MainCommon.setEnabledLocally(valid);

    Logger.slog(VERSION_CHECK_INFO, _SERVER_VERSION[0], _SERVER_VERSION[1], _SERVER_VERSION[2], localVersion[0], localVersion[1], localVersion[2]);
    Logger.slog(VERSION_CHECK_MESSAGE, valid ? VERSION_CHECK_SUCCEED : VERSION_CHECK_FAIL);
  }

  public static void onGlobalStateChanged(boolean state) {
    debugOverlay.refreshGlobalState(state);
  }

  public static boolean isAnyConfigAccessorInstalled(){ return FabricLoader.getInstance().isModLoaded("modmenu"); }
  public static boolean isSodiumInstalled(){ return FabricLoader.getInstance().isModLoaded("sodium"); }

  public static boolean getServerHasMod() { return _SERVER_HAS_MOD; }
  public static byte[] getServerVersion() { return _SERVER_VERSION; }
}