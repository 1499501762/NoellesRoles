package org.agmas.noellesroles.packet;

import java.util.UUID;

import org.agmas.noellesroles.Noellesroles;

import dev.doctor4t.wathe.api.Role;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SniperC2SPacket(UUID target, Identifier guessedIdentifier) implements CustomPayload {
    public static final Identifier SNIPER_PAYLOAD_ID = Identifier.of(Noellesroles.MOD_ID, "sniper");
    public static final Id<SniperC2SPacket> ID = new Id<>(SNIPER_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, SniperC2SPacket> CODEC;

    public SniperC2SPacket(UUID target, Identifier guessedIdentifier) {
        this.target = target;
        this.guessedIdentifier = guessedIdentifier;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(this.target);
        buf.writeIdentifier(this.guessedIdentifier);
    }

    public static SniperC2SPacket read(PacketByteBuf buf) {
        UUID target = buf.readUuid();
        Identifier roleId = buf.readIdentifier();
        return new SniperC2SPacket(target, roleId);
    }

    public UUID target() {
        return this.target;
    }

    public Identifier guessedIdentifier() {
        return this.guessedIdentifier;
    }

    static {
        CODEC = PacketCodec.of(SniperC2SPacket::write, SniperC2SPacket::read);
    }
}
