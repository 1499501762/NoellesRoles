package org.agmas.noellesroles.gambler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

/**
 * Gambler component: 管理赌博能力的冷却
 */
public class GamblerPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    // 初始化由 NoellesRolesComponents 在 Mod 初始化阶段完成，避免类加载时访问 CCA 注册器
    public static ComponentKey<GamblerPlayerComponent> KEY = null;

    public static final int GAMBLE_COOLDOWN_TICKS = 15 * 20; // 15 seconds

    private final PlayerEntity player;

    public int lossStreak = 0; // 连续赌输次数，用于保底触发
    public int totalLosses = 0; // 累计赌输次数，用于触发全局保底（10 次）
    // 标记玩家是否已经使用过一次赌博能力（每局只能使用一次）
    public boolean hasUsedGamble = false;

    public GamblerPlayerComponent(PlayerEntity player) {
        this.player = player;
    }

    public void reset() {
        lossStreak = 0;
        totalLosses = 0;
        hasUsedGamble = false;
        sync();
    }

    public void sync() {
        if (KEY != null) KEY.sync(player);
    }

    @Override
    public void serverTick() {
        // if (cooldown > 0) {
        //     cooldown--;
        //     if (cooldown % 20 == 0) sync();
        // }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("gambler_loss_streak", lossStreak);
        tag.putInt("gambler_total_losses", totalLosses);
        tag.putBoolean("gambler_has_used", hasUsedGamble);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (tag.contains("gambler_loss_streak")) lossStreak = tag.getInt("gambler_loss_streak");
        if (tag.contains("gambler_total_losses")) totalLosses = tag.getInt("gambler_total_losses");
        if (tag.contains("gambler_has_used")) hasUsedGamble = tag.getBoolean("gambler_has_used");
    }
}

