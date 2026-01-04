package com.sopze.mc.redstonepp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class Network {

  public static final ResourceLocation GREET_PACKET_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "0");

  public record GreetPayload(byte[] version) implements CustomPacketPayload {
    public static final Type<GreetPayload> PAYLOAD_ID = new Type<>(GREET_PACKET_ID);
    public static final StreamCodec<FriendlyByteBuf, GreetPayload> CODEC = StreamCodec.composite(ByteBufCodecs.BYTE_ARRAY, GreetPayload::version, GreetPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return PAYLOAD_ID; }

    public byte[] read() { return version; }
  }
}
