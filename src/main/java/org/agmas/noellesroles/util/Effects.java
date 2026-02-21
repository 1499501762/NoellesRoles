package org.agmas.noellesroles.util;

import org.jetbrains.annotations.Nullable;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;

import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import java.util.UUID;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class Effects {
    private Effects() {}
    /**
     * 在实体位置播放默认刀声效（服务器端播放，广播给附近玩家）。
     */
    public static void playKnifeStabSound(PlayerEntity player, float soundVolume) {
        player.getWorld().playSound(
            /* player */ null,
            /* x */ player.getX(),
            /* y */ player.getEyeY(),
            /* z */ player.getZ(),
            /* sound */ WatheSounds.ITEM_KNIFE_STAB,
            /* category */ SoundCategory.PLAYERS,
            /* volume */ soundVolume,
            /* pitch */ 1f + player.getRandom().nextFloat() * 0.1f - 0.05f
        );
    }

    public static void playKnifePrepareSound(PlayerEntity player, float soundVolume) {
        player.getWorld().playSound(
            /* player */ null,
            /* x */ player.getX(),
            /* y */ player.getEyeY(),
            /* z */ player.getZ(),
            /* sound */ WatheSounds.ITEM_KNIFE_PREPARE,
            /* category */ SoundCategory.PLAYERS,
            /* volume */ soundVolume,
            /* pitch */ 1f + player.getRandom().nextFloat() * 0.1f - 0.05f
        );
    }

    public static void playGrenadeThrowSound(PlayerEntity player, float soundVolume) {
        player.getWorld().playSound(
            /* player */ null,
            /* x */ player.getX(),
            /* y */ player.getEyeY(),
            /* z */ player.getZ(),
            /* sound */ WatheSounds.ITEM_GRENADE_THROW,
            /* category */ SoundCategory.PLAYERS,
            /* volume */ soundVolume,
            /* pitch */ 1f + player.getRandom().nextFloat() * 0.1f - 0.05f
        );
    }

    public static void playGrenadeExplodeSound(PlayerEntity player, float soundVolume) {
        player.getWorld().playSound(
            /* player */ null,
            /* x */ player.getX(),
            /* y */ player.getEyeY(),
            /* z */ player.getZ(),
            /* sound */ WatheSounds.ITEM_GRENADE_EXPLODE,
            /* category */ SoundCategory.PLAYERS,
            /* volume */ soundVolume,
            /* pitch */ 1f + player.getRandom().nextFloat() * 0.1f - 0.05f
        );
    }
    /**
     * 播放上膛/扳机点击音效（服务器端播放，广播给附近玩家）。
     */
    public static void playCockingSound(PlayerEntity player, float soundVolume) {
        player.getWorld().playSound(
            /* player */ null,
            /* x */ player.getX(),
            /* y */ player.getEyeY(),
            /* z */ player.getZ(),
            /* sound */ WatheSounds.ITEM_REVOLVER_CLICK,
            /* category */ SoundCategory.PLAYERS,
            /* volume */ soundVolume,
            /* pitch */ 1f + player.getRandom().nextFloat() * 0.1f - 0.05f
        );
    }

    /**
     * 播放开枪音效并在玩家视线前方生成枪口粒子（烟/火焰）作为 muzzle flash。
     * - 在非服务器世界上仍会播放音效，但不会产生粒子（粒子需 ServerWorld）。
     */
    public static void playShootingEffects(PlayerEntity player, @Nullable Float soundVolume) {
        // 播放开枪声音（广播）
        soundVolume = (soundVolume == null) ? 1.0f : soundVolume;
        player.getWorld().playSound(
            /* player */ null,
            player.getX(),
            player.getEyeY(),
            player.getZ(),
            WatheSounds.ITEM_REVOLVER_SHOOT,
            SoundCategory.PLAYERS,
            /* volume */ soundVolume,
            /* pitch */ 1f + player.getRandom().nextFloat() * 0.1f - 0.05f
        );

        // 粒子效果（仅在服务器世界上创建并广播）
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            // 枪口位置：玩家眼睛位置向前偏移一段距离
            Vec3d look = player.getRotationVec(1.0F); // 单位朝向向量
            Vec3d eyePos = new Vec3d(player.getX(), player.getEyeY(), player.getZ());
            Vec3d muzzlePos = eyePos.add(look.multiply(0.5)); // 根据需要调整偏移（0.3-0.8）

            // 少量烟雾 + 少量火花作为 muzzle flash
            serverWorld.spawnParticles(
                ParticleTypes.SMOKE,
                muzzlePos.x, muzzlePos.y, muzzlePos.z,
                /* count */ 6,
                /* offsetX */ 0.02, /* offsetY */ 0.02, /* offsetZ */ 0.02,
                /* speed */ 0.02
            );

            serverWorld.spawnParticles(
                ParticleTypes.FLAME,
                muzzlePos.x, muzzlePos.y, muzzlePos.z,
                /* count */ 3,
                /* offsetX */ 0.01, /* offsetY */ 0.01, /* offsetZ */ 0.01,
                /* speed */ 0.05
            );

            // 可选：沿朝向发射一些带方向性的粒子（让闪光偏向枪口方向）
            // 将粒子的 motion 通过 offsets 以视觉上更有方向感（简单近似）
            serverWorld.spawnParticles(
                ParticleTypes.CAMPFIRE_COSY_SMOKE, // 任选更合适的粒子
                muzzlePos.x, muzzlePos.y, muzzlePos.z,
                4,
                look.x * 0.02, look.y * 0.02, look.z * 0.02,
                0.01
            );
        }
    }

    /**
     * 播放自定义音效（通过 Identifier 动态创建 SoundEvent）。
     * 用于非预定义的音效，如壮汉冲刺音。
     */
    public static void playCustomSound(PlayerEntity player, Identifier soundId, float soundVolume) {
        player.getWorld().playSound(
            /* player */ null,
            /* x */ player.getX(),
            /* y */ player.getEyeY(),
            /* z */ player.getZ(),
            /* sound */ SoundEvent.of(soundId),
            /* category */ SoundCategory.PLAYERS,
            /* volume */ soundVolume,
            /* pitch */ 1f + player.getRandom().nextFloat() * 0.1f - 0.05f
        );
    }
    /**
     * Apply a Slowness status effect and a strong movement-speed attribute modifier
     * that forces effective movement speed to 0. The modifier is identified by the
     * provided UUID so it can be removed later.
     */
    private static final java.util.Map<UUID, Double> SAVED_BASE_MOVEMENT = new java.util.concurrent.ConcurrentHashMap<>();

    public static void applyZeroMovementSlowness(ServerPlayerEntity player, int durationTicks, UUID modifierId) {
        try {
            UUID key = player.getUuid();
            // Apply slowness effect (high amplifier to be obvious)
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, durationTicks, 255, false, true, true));
            var inst = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (inst != null) {
                // Save base value if not already saved
                double before = inst.getBaseValue();
                Double prev = SAVED_BASE_MOVEMENT.get(key);
                if (prev == null) {
                    SAVED_BASE_MOVEMENT.put(key, before);
                    Log.info(LogCategory.GENERAL, "Effects: saved base movement for %s".formatted(key));
                }
                try { inst.setBaseValue(0.0d); } catch (Throwable t) { Log.info(LogCategory.GENERAL, "Effects: failed to set base value to 0 for %s: %s".formatted(key, t.getMessage())); }
                // Immediately enforce zero velocity on the server to reduce client-side movement prediction
                try {
                    player.setVelocity(0.0, player.getVelocity().y, 0.0);
                    player.setSprinting(false);
                } catch (Throwable t) { Log.info(LogCategory.GENERAL, "Effects: failed to zero velocity for %s: %s".formatted(key, t.getMessage())); }
            }
        } catch (Throwable t) {
            String msg = t.getMessage() == null ? "" : t.getMessage();
            Log.info(LogCategory.GENERAL, "Effects: applyZeroMovementSlowness error: %s".formatted(msg));
        }
    }

    /** Remove zero-movement effect: restore saved base movement speed if present. */
    public static void removeZeroMovementSlowness(ServerPlayerEntity player, UUID modifierId) {
        try {
            var inst = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (inst != null) {
                UUID key = player.getUuid();
                Double orig = SAVED_BASE_MOVEMENT.remove(key);
                if (orig != null) {
                    try { inst.setBaseValue(orig); Log.info(LogCategory.GENERAL, "Effects: restored base movement for %s".formatted(key)); } catch (Throwable t) { Log.info(LogCategory.GENERAL, "Effects: failed to restore base for %s: %s".formatted(key, t.getMessage())); }
                }
            }
        } catch (Throwable ignored) {}
    }
    
}