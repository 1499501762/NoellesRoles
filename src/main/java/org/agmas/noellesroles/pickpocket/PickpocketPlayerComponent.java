package org.agmas.noellesroles.pickpocket;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

/**
 * 窃贼（Pickpocket）组件
 * 核心机制：偷钱（固定 +25），但被窃者余额变为原来的一半（一次只偷一个人），技能冷却 30 秒
 */
public class PickpocketPlayerComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<PickpocketPlayerComponent> KEY =
        ComponentRegistry.getOrCreate(Identifier.of(Noellesroles.MOD_ID, "pickpocket"), PickpocketPlayerComponent.class);

    private final PlayerEntity player;

    // 偷窃相关常量与调参
    public static final int STEAL_AMOUNT = 25; // 窃取固定金额
    public static final int COOLDOWN_TICKS = 400; // 冷却 20 秒
    public static final double STEAL_RANGE = 3.0D; // 可窃取的距离（方块）

    public PickpocketPlayerComponent(PlayerEntity player) {
        this.player = player;
    }

    public void reset() {
        // 当前组件无需保存运行时状态；保留接口以便未来扩展
        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void clientTick() {
        // 可选：客户端展示提示/动画
    }

    @Override
    public void serverTick() {
        // 无需每 tick 的服务器逻辑；接口保留
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        // 无需持久化数据
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        // 无需持久化数据
    }

    public PlayerEntity getPlayer() {
        return player;
    }
}
