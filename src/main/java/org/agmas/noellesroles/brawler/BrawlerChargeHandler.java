package org.agmas.noellesroles.brawler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.agmas.noellesroles.util.Effects;
import dev.doctor4t.wathe.util.Scheduler;

import java.util.List;

/**
 * 壮汉冲刺效果处理工具
 * 负责碰撞检测、伤害计算、效果应用
 */
public class BrawlerChargeHandler {
    
    /**
     * 执行冲刺的单个 Tick 逻辑
     * - 根据朝向计算冲刺速度向量
     * - 应用速度到玩家
     * - 检测碰撞与击中目标
     */
    public static void tickCharge(ServerPlayerEntity player, BrawlerPlayerComponent component) {
        if (!component.isCharging()) {
            return;
        }
        
        // 获取玩家朝向并计算速度向量
        Vec3d direction = player.getRotationVector();
        // 水平方向（忽略Y）
        Vec3d horizontalDir = new Vec3d(direction.x, 0, direction.z).normalize();
        Vec3d velocity = horizontalDir.multiply(component.chargeVelocity);
        
        // 应用冲刺速度
        player.setVelocity(velocity.x, player.getVelocity().y, velocity.z);
        player.velocityModified = true;
        
        // 检测碰撞与目标
        detectAndHitTargets(player, component);
    }
    
    /**
     * 检测冲刺范围内的所有玩家，并对击中的玩家应用效果
     */
    private static void detectAndHitTargets(ServerPlayerEntity charger, BrawlerPlayerComponent component) {
        World world = charger.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }
        
        // 获取玩家当前位置与朝向
        Vec3d playerPos = charger.getPos();
        Vec3d direction = charger.getRotationVector();
        Vec3d horizontalDir = new Vec3d(direction.x, 0, direction.z).normalize();
        
        // 计算碰撞框：玩家前方一段距离的范围
        double searchRadius = BrawlerPlayerComponent.COLLISION_RADIUS + 0.5;  // 加上额外范围
        Vec3d searchCenter = playerPos.add(horizontalDir.multiply(searchRadius));
        
        Box searchBox = new Box(
            searchCenter.x - BrawlerPlayerComponent.COLLISION_RADIUS,
            searchCenter.y - 0.5,
            searchCenter.z - BrawlerPlayerComponent.COLLISION_RADIUS,
            searchCenter.x + BrawlerPlayerComponent.COLLISION_RADIUS,
            searchCenter.y + 2.0,
            searchCenter.z + BrawlerPlayerComponent.COLLISION_RADIUS
        );
        
        // 查找范围内的所有玩家
        List<Entity> entities = serverWorld.getOtherEntities(charger, searchBox);
        
        for (Entity entity : entities) {
            if (!(entity instanceof ServerPlayerEntity target) || target == charger) continue;


            // 如果本次冲锋已经命中过人，则仍然应用效果但不重复播放音效
            if (component.hasHitThisCharge) {
                applyChargeEffects(charger, target);
                continue;
            }

            // 首次命中：应用效果并播放一次音效，同时标记为已命中
            applyChargeEffects(charger, target);
            component.hasHitThisCharge = true;
            component.sync();
            
            // 延迟播放击中音效（约 700ms = 14 ticks）以配合表现
            Scheduler.schedule(() -> {
                Effects.playCustomSound(
                    charger,
                    net.minecraft.util.Identifier.of(org.agmas.noellesroles.Noellesroles.MOD_ID, "brawler_hit"),
                    1.0f
                );
            }, 14);
            // 如果设计为只对首人造成效果并停止处理，使用 break; 否则继续处理其他实体但不重复播放音效
        }
    }
    
    /**
     * 向击中的目标应用冲刺效果
    * - 缓慢效果（Slowness）
    * - 恶心效果（Nausea）
    * - 黑暗效果（Darkness）
     */
    private static void applyChargeEffects(ServerPlayerEntity charger, ServerPlayerEntity target) {
        // 应用缓慢效果（移动速度降低）
        target.addStatusEffect(
            new StatusEffectInstance(
                StatusEffects.SLOWNESS,
                BrawlerPlayerComponent.SLOWNESS_DURATION,
                BrawlerPlayerComponent.SLOWNESS_LEVEL,
                false,  // 不显示粒子
                false    // 不显示icon
            )
        );
        
        // 应用恶心效果（视野旋转/模糊）——开启粒子与图标以确保效果可见
        target.addStatusEffect(
            new StatusEffectInstance(
                StatusEffects.NAUSEA,
                BrawlerPlayerComponent.NAUSEA_DURATION,
                BrawlerPlayerComponent.NAUSEA_LEVEL,
                true,  // 显示粒子
                true    // 显示 icon
            )
        );

        // 应用黑暗效果（屏幕变暗）——原 BLINDNESS 改为 DARKNESS
        target.addStatusEffect(
            new StatusEffectInstance(
                StatusEffects.DARKNESS,
                BrawlerPlayerComponent.DARKNESS_DURATION,
                BrawlerPlayerComponent.DARKNESS_LEVEL,
                false,  // 不显示粒子
                false    // 不显示 icon
            )
        );
        
        // 击飞：将目标击退到远离冲锋者的方向并带一点向上分量
        Vec3d knockDir = target.getPos().subtract(charger.getPos());
        if (knockDir.lengthSquared() > 0.0001) {
            Vec3d norm = knockDir.normalize();
            double kbStrength = BrawlerPlayerComponent.KNOCKBACK_STRENGTH;
            double upward = BrawlerPlayerComponent.KNOCKBACK_UPWARD;
            Vec3d kb = new Vec3d(norm.x * kbStrength, upward, norm.z * kbStrength);
            target.setVelocity(kb.x, kb.y, kb.z);
            target.velocityModified = true;
        }
    }
}
