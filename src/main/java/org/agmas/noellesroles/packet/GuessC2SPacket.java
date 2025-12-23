package org.agmas.noellesroles.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;

import java.util.UUID;

public record GuessC2SPacket(UUID player, String guess) implements CustomPayload {
    public static final Identifier GUESS_PAYLOAD_ID = Identifier.of(Noellesroles.MOD_ID, "guess");
    public static final Id<GuessC2SPacket> ID = new Id<>(GUESS_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, GuessC2SPacket> CODEC;

    public GuessC2SPacket(UUID player, String guess) {
        this.player = player;
        this.guess = guess;
    }

    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(this.player);
        buf.writeString(this.guess);
    }

    public static GuessC2SPacket read(PacketByteBuf buf) {
        return new GuessC2SPacket(buf.readUuid(), buf.readString());
    }


    public UUID player() {
        return this.player;
    }


    static {
        CODEC = PacketCodec.of(GuessC2SPacket::write, GuessC2SPacket::read);
    }
}