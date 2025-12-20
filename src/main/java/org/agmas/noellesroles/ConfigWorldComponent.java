package org.agmas.noellesroles;

import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.UUID;

public class ConfigWorldComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<ConfigWorldComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(Noellesroles.MOD_ID, "config"), ConfigWorldComponent.class);
    public boolean insaneSeesMorphs = true;
    public boolean naturalVoodoosAllowed = false;
    public int masterKeyVisibleCount = 0;
    public boolean masterKeyIsVisible = false;
    private final World world;

    public void reset() {
        this.sync();
    }

    public ConfigWorldComponent(World world) {
        this.world = world;
    }

    public void sync() {
        KEY.sync(this.world);
    }

    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        insaneSeesMorphs = NoellesRolesConfig.HANDLER.instance().insanePlayersSeeMorphs;
        naturalVoodoosAllowed = NoellesRolesConfig.HANDLER.instance().voodooNonKillerDeaths;
        masterKeyVisibleCount = NoellesRolesConfig.HANDLER.instance().playerCountToMakeConducterKeyVisible;
        tag.putBoolean("insaneSeesMorphs", this.insaneSeesMorphs);
        tag.putBoolean("naturalVoodoosAllowed", this.naturalVoodoosAllowed);
        tag.putBoolean("masterKeyIsVisible", this.masterKeyIsVisible);
        tag.putInt("masterKeyVisibleCount", this.masterKeyVisibleCount);
    }



    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (tag.contains("insaneSeesMorphs"))   this.insaneSeesMorphs = tag.getBoolean("insaneSeesMorphs");
        if (tag.contains("naturalVoodoosAllowed"))   this.naturalVoodoosAllowed = tag.getBoolean("naturalVoodoosAllowed");
        if (tag.contains("masterKeyIsVisible"))   this.masterKeyIsVisible = tag.getBoolean("masterKeyIsVisible");
        if (tag.contains("masterKeyVisibleCount"))   this.masterKeyVisibleCount = tag.getInt("masterKeyVisibleCount");
    }

    @Override
    public void serverTick() {
        if (NoellesRolesConfig.HANDLER.instance().playerCountToMakeConducterKeyVisible == 0) {
            masterKeyIsVisible = false;
        } else {
            if (world.getServer() != null)
                masterKeyIsVisible =  world.getServer().getPlayerManager().getCurrentPlayerCount() >= NoellesRolesConfig.HANDLER.instance().playerCountToMakeConducterKeyVisible;
        }
        this.sync();
    }
}
