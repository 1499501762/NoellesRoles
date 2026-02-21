package org.agmas.noellesroles.dumb;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

/**
 * 哑巴（Dumb）组件
 * 核心机制：禁止玩家使用语音聊天
 */
public class DumbPlayerComponent implements AutoSyncedComponent {
    public static final ComponentKey<DumbPlayerComponent> KEY = 
        ComponentRegistry.getOrCreate(Identifier.of(Noellesroles.MOD_ID, "dumb"), DumbPlayerComponent.class);
    
    private final PlayerEntity player;
    public boolean isDumb = false;
    
    public DumbPlayerComponent(PlayerEntity player) {
        this.player = player;
    }
    
    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (tag.contains("isDumb")) {
            this.isDumb = tag.getBoolean("isDumb");
        }
    }
    
    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putBoolean("isDumb", this.isDumb);
    }
    
    public void setDumb(boolean dumb) {
        this.isDumb = dumb;
        this.sync();
    }
    
    public boolean isDumb() {
        return isDumb;
    }
    
    public void reset() {
        this.isDumb = false;
    }
    
    public void sync() {
        KEY.sync(this.player);
    }
}
