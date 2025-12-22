package org.agmas.noellesroles.packet;

import java.util.UUID;

import org.agmas.noellesroles.Noellesroles;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record DetectiveC2SPacket(UUID target) implements CustomPayload {
    public static final Identifier DETECTIVE_PAYLOAD_ID = Identifier.of(Noellesroles.MOD_ID, "detective_check");
    public static final Id<DetectiveC2SPacket> ID = new Id<>(DETECTIVE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, DetectiveC2SPacket> CODEC;

    public DetectiveC2SPacket(UUID target) {
        this.target = target;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(this.target);
    }

    public static DetectiveC2SPacket read(PacketByteBuf buf) {
        UUID target = buf.readUuid();
        return new DetectiveC2SPacket(target);
    }

    public UUID target() {
        return this.target;
    }

    static {
        CODEC = PacketCodec.of(DetectiveC2SPacket::write, DetectiveC2SPacket::read);
    }
}