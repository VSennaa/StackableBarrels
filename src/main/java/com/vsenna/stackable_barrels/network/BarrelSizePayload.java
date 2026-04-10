package com.vsenna.stackable_barrels.network;

import com.vsenna.stackable_barrels.StackableBarrels;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record BarrelSizePayload(int size) implements CustomPayload {
    public static final CustomPayload.Id<BarrelSizePayload> ID = new CustomPayload.Id<>(new Identifier(StackableBarrels.MOD_ID, "barrel_size"));
    public static final PacketCodec<RegistryByteBuf, BarrelSizePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, BarrelSizePayload::size,
            BarrelSizePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
