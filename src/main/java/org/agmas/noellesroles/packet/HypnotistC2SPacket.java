package org.agmas.noellesroles.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;

import java.util.UUID;

public record HypnotistC2SPacket(UUID player) implements CustomPayload {
    public static final Identifier HYPNOTIST_PAYLOAD_ID = Identifier.of(Noellesroles.MOD_ID, "hypnotist_select");
    public static final Id<HypnotistC2SPacket> ID = new Id<>(HYPNOTIST_PAYLOAD_ID);
    public static final PacketCodec<PacketByteBuf, HypnotistC2SPacket> CODEC;

    public HypnotistC2SPacket(UUID player) { this.player = player; }

    public Id<? extends CustomPayload> getId() { return ID; }

    public void write(PacketByteBuf buf) { buf.writeUuid(this.player); }

    public static HypnotistC2SPacket read(PacketByteBuf buf) { return new HypnotistC2SPacket(buf.readUuid()); }

    public UUID player() { return this.player; }

    static { CODEC = PacketCodec.of(HypnotistC2SPacket::write, HypnotistC2SPacket::read); }
}
