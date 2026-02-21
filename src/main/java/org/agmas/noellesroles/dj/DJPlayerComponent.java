package org.agmas.noellesroles.dj;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.util.Identifier;

/**
 * Component to track DJ's current inserted/playing song (网易云 songId) and play state.
 */
public class DJPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<DJPlayerComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(Noellesroles.MOD_ID, "dj"), DJPlayerComponent.class);

    private final PlayerEntity player;
    public long currentSongId = 0L; // 0 == none
    public boolean isPlaying = false;

    public DJPlayerComponent(PlayerEntity player) {
        this.player = player;
    }

    public void reset() {
        this.currentSongId = 0L;
        this.isPlaying = false;
        this.sync();
    }

    public void sync() { KEY.sync(this.player); }

    @Override
    public void serverTick() {
        // no periodic behaviour here; state changes are handled by receivers
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putLong("currentSongId", this.currentSongId);
        tag.putBoolean("isPlaying", this.isPlaying);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.currentSongId = tag.contains("currentSongId") ? tag.getLong("currentSongId") : 0L;
        this.isPlaying = tag.contains("isPlaying") ? tag.getBoolean("isPlaying") : false;
    }
}
