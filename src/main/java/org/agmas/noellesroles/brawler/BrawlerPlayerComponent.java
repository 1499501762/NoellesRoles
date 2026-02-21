package org.agmas.noellesroles.brawler;

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
 * 壮汉（Brawler）组件
 * 核心机制：可以使用技能向前冲刺一段距离并肘击撞到的玩家
 * 效果：被击中的玩家获得缓慢（Slowness）和恶心（Nausea）效果
 */
public class BrawlerPlayerComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<BrawlerPlayerComponent> KEY = 
        ComponentRegistry.getOrCreate(Identifier.of(Noellesroles.MOD_ID, "brawler"), BrawlerPlayerComponent.class);

    private final PlayerEntity player;
    
    // 冲刺状态与计时
    public boolean isCharging = false;                // 是否正在冲刺
    public int chargeTicksRemaining = 0;             // 冲刺剩余 tick
    public double chargeVelocity = 0.8;              // 冲刺速度（块/tick）
    public int chargeDuration = 10;                  // 冲刺持续时间（tick），约 0.5 秒
    // 本次冲锋是否已命中目标（用于只播放一次命中音效）
    public boolean hasHitThisCharge = false;
    
    // 冷却使用 AbilityPlayerComponent 统一管理
    public static final int CHARGE_COOLDOWN_TICKS = 600; // 冷却时间（tick），约 30 秒
    
    // 被击中目标效果
    public static final int SLOWNESS_DURATION = 100;     // 缓慢效果时长（tick），约 5 秒
    public static final int SLOWNESS_LEVEL = 200;          // 缓慢等级（I）
    public static final int NAUSEA_DURATION = 60;        // 恶心效果时长（tick），约 3 秒
    public static final int NAUSEA_LEVEL = 0;            // 恶心等级（I）
    public static final int DARKNESS_DURATION = 60;     // 黑暗效果时长（tick），约 3 秒
    public static final int DARKNESS_LEVEL = 0;         // 黑暗等级（I）
    // 击飞参数（用于冲刺命中时把目标击退）
    public static final double KNOCKBACK_STRENGTH = 1.2; // 水平击退强度
    public static final double KNOCKBACK_UPWARD = 0.35; // 垂直向上分量
    
    // 碰撞检测范围
    public static final double COLLISION_RADIUS = 1.0;   // 碰撞半径

    public BrawlerPlayerComponent(PlayerEntity player) {
        this.player = player;
        this.isCharging = false;
        this.chargeTicksRemaining = 0;
    }

    /**
     * 启动冲刺能力
     */
    public void startCharge() {
        if (isCharging) {
            return;  // 已在冲刺中
        }
        
        isCharging = true;
        // 重置本次冲锋命中标志
        hasHitThisCharge = false;
        chargeTicksRemaining = chargeDuration;
        this.sync();
    }

    /**
     * 停止冲刺
     */
    public void stopCharge() {
        isCharging = false;
        chargeTicksRemaining = 0;
        hasHitThisCharge = false;
        this.sync();
    }

    /**
     * 重置角色状态（游戏结束时调用）
     */
    public void reset() {
        this.isCharging = false;
        this.chargeTicksRemaining = 0;
        this.hasHitThisCharge = false;
        this.sync();
    }

    /**
     * 同步组件数据到客户端
     */
    public void sync() {
        KEY.sync(this.player);
    }

    /**
     * 客户端 Tick（用于本地渲染反馈）
     */
    @Override
    public void clientTick() {
        // 可选：在客户端显示冲刺粒子或动画效果
    }

    /**
     * 服务端 Tick（推进冲刺逻辑）
     */
    @Override
    public void serverTick() {
        // 冲刺倒计时
        if (isCharging) {
            chargeTicksRemaining--;
            if (chargeTicksRemaining <= 0) {
                stopCharge();
            }
        }
        
        // 定期同步
        sync();
    }

    /**
     * 将数据写入 NBT（用于保存/加载）
     */
    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putBoolean("isCharging", this.isCharging);
        tag.putInt("chargeTicksRemaining", this.chargeTicksRemaining);
        tag.putBoolean("hasHitThisCharge", this.hasHitThisCharge);
    }

    /**
     * 从 NBT 读取数据（用于加载）
     */
    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.isCharging = tag.getBoolean("isCharging");
        this.chargeTicksRemaining = tag.getInt("chargeTicksRemaining");
        this.hasHitThisCharge = tag.contains("hasHitThisCharge") && tag.getBoolean("hasHitThisCharge");
    }

    // 便捷 Getter

    public boolean isCharging() {
        return isCharging;
    }

    public int getChargeTicksRemaining() {
        return chargeTicksRemaining;
    }

    public PlayerEntity getPlayer() {
        return player;
    }
}
