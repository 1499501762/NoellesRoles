package org.agmas.noellesroles.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;

import java.util.UUID;

public record ScarecrowC2SPacket(UUID player) implements CustomPayload {
    public static final Identifier SCARECROW_PAYLOAD_ID = Identifier.of(Noellesroles.MOD_ID, "scarecrow_select");
    public static final Id<ScarecrowC2SPacket> ID = new Id<>(SCARECROW_PAYLOAD_ID);
    public static final PacketCodec<PacketByteBuf, ScarecrowC2SPacket> CODEC;

    public ScarecrowC2SPacket(UUID player) {
        this.player = player;
    }

    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(this.player);
    }

    public static ScarecrowC2SPacket read(PacketByteBuf buf) {
        return new ScarecrowC2SPacket(buf.readUuid());
    }

    public UUID player() { return this.player; }

    static {
        CODEC = PacketCodec.of(ScarecrowC2SPacket::write, ScarecrowC2SPacket::read);
    }
}
