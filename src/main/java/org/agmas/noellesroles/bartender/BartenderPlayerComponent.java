package org.agmas.noellesroles.bartender;

import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.UUID;

public class BartenderPlayerComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<BartenderPlayerComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(Noellesroles.MOD_ID, "bartender"), BartenderPlayerComponent.class);
    private final PlayerEntity player;
    public int glowTicks = 0;
    public int armor = 0;

    public void reset() {
        this.glowTicks = 0;
        this.armor = 0;
        this.sync();
    }

    public BartenderPlayerComponent(PlayerEntity player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void clientTick() {
    }

    public void serverTick() {
        if (this.glowTicks > 0) {
            --this.glowTicks;

        }
        this.sync();
    }

    public boolean giveArmor() {
        armor = 1;
        this.sync();
        return true;
    }


    public boolean startGlow() {
        setGlowTicks(GameConstants.getInTicks(0,40));
        this.sync();
        return true;
    }


    public void setGlowTicks(int ticks) {
        this.glowTicks = ticks;
        this.sync();
    }

    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("glowTicks", this.glowTicks);
        tag.putInt("armor", this.armor);
    }

    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.glowTicks = tag.contains("glowTicks") ? tag.getInt("glowTicks") : 0;
        this.armor = tag.contains("armor") ? tag.getInt("armor") : 0;
    }
}
