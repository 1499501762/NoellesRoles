package org.agmas.noellesroles.chameleon;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.UUID;

public class ChameleonPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<ChameleonPlayerComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(Noellesroles.MOD_ID, "chameleon"), ChameleonPlayerComponent.class);
    private final PlayerEntity player;
    public int hidingTicks;


    public void reset() {
        this.hidingTicks = 0;
        this.sync();
    }

    public ChameleonPlayerComponent(PlayerEntity player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("hidingTicks", hidingTicks);
    }

    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.hidingTicks = tag.contains("hidingTicks") ? tag.getInt("hidingTicks") : 0;
    }

    @Override
    public void serverTick() {
        WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(player.getWorld());
        if (worldModifierComponent.isRole(player,Noellesroles.CHAMELEON)) {
            if (player.getMovement().getX() != 0 || player.getMovement().getZ() != 0)  hidingTicks = 0;
            hidingTicks++;
        } else {
            hidingTicks = 0;
        }
        sync();
    }
}
