package org.agmas.noellesroles.sniper;

import java.util.UUID;

import net.minecraft.util.Identifier;

import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

public class SniperPlayerComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<SniperPlayerComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(Noellesroles.MOD_ID, "sniper"), SniperPlayerComponent.class);
    private final PlayerEntity player;
    public UUID targetUUID;

    /** 剩余狙击次数 */
    public int shotsRemaining;

    /** 最大狙击次数（根据玩家人数计算） */
    public int maxShots;

    public SniperPlayerComponent(PlayerEntity player) {
        this.player = player;
    }

    /** 重置状态，在回合开始或角色重置时调用 */
    public void reset(int playerCount) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        this.targetUUID = player.getUuid();
        this.guessedIdentity = gameWorldComponent.getRole(player.getUuid()).identifier();
        double ratio = NoellesRolesConfig.HANDLER.instance().sniperShotRatio;

        // floor(playerCount * ratio)，至少保证 1 次
        this.maxShots = Math.max(1, (int) Math.floor(playerCount * ratio));
        this.shotsRemaining = this.maxShots;
        this.sync();
    }

    /** 同步到客户端 */
    public void sync() {
        KEY.sync(this.player);
    }

    /** 设置绑定目标 */
    public void setBinding(UUID targetUUID, Identifier guessedIdentity) {
        this.targetUUID = targetUUID;
        this.guessedIdentity = guessedIdentity;
    }

    public UUID getBoundTarget() {
        return this.targetUUID;
    }

    public int getShotsRemaining() {
        return this.shotsRemaining;
    }

    public boolean hasShotsRemaining() {
        return this.shotsRemaining > 0;
    }

    /** 狙击时的猜测身份，仅在下一次狙击生效后清空 */
    public Identifier guessedIdentity; // e.g. "civilian"

    public void setGuessRole(Identifier identifier) {
        this.guessedIdentity = identifier;
        this.sync();
    }

    public Identifier getGuessIdentity() {
        return this.guessedIdentity;
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (this.targetUUID != null) {
            tag.putUuid("targetUUID", this.targetUUID);
        }
        if (this.guessedIdentity != null) {
            tag.putString("guessedIdentity", this.guessedIdentity.toString());
        }
        tag.putInt("shotsRemaining", this.shotsRemaining);
        tag.putInt("maxShots", this.maxShots);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.targetUUID = tag.containsUuid("targetUUID") ? tag.getUuid("targetUUID") : player.getUuid();
        this.shotsRemaining = tag.getInt("shotsRemaining");
        this.maxShots = tag.getInt("maxShots");
        this.guessedIdentity = Identifier.of(tag.getString("guessedIdentity"));
    }

    @Override
    public void serverTick() {
        this.sync();
    }

    @Override
    public void clientTick() {
    }

}
