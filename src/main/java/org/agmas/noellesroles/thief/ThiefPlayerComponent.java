package org.agmas.noellesroles.thief;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;

/**
 * THIEF角色的PlayerComponent
 * 用于追踪THIEF是否已经偷取了身份以及偷取的身份信息
 */
public class ThiefPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<ThiefPlayerComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(Noellesroles.MOD_ID, "thief"), ThiefPlayerComponent.class);
    
    private final PlayerEntity player;
    
    /** 是否已经偷取了身份 */
    public boolean hasStolen = false;
    
    /** 被偷取的身份ID */
    public Identifier stolenIdentity = null;

    public ThiefPlayerComponent(PlayerEntity player) {
        this.player = player;
    }

    /**
     * 偷取目标的身份
     * @param targetIdentity 目标的身份
     */
    public void stealIdentity(Identifier targetIdentity) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        
        // 根据ID查找对应的Role对象
        Role targetRole = null;
        for (Role role : WatheRoles.ROLES) {
            if (role.identifier().equals(targetIdentity)) {
                targetRole = role;
                break;
            }
        }
        
        // 如果找到了目标角色，添加该角色给THIEF
        if (targetRole != null) {
            gameWorldComponent.addRole(player, targetRole);
            this.hasStolen = true;
            this.stolenIdentity = targetIdentity;
            this.sync();
        }
    }

    /**
     * 重置状态
     */
    public void reset() {
        this.hasStolen = false;
        this.stolenIdentity = null;
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void serverTick() {
        // 服务端tick逻辑（如果需要）
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putBoolean("hasStolen", this.hasStolen);
        if (this.stolenIdentity != null) {
            tag.putString("stolenIdentity", this.stolenIdentity.toString());
        }
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.hasStolen = tag.contains("hasStolen") ? tag.getBoolean("hasStolen") : false;
        this.stolenIdentity = tag.contains("stolenIdentity") ? Identifier.of(tag.getString("stolenIdentity")) : null;
    }
}

