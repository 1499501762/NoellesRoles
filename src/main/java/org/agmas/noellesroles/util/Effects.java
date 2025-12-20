package org.agmas.noellesroles.util;

import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
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
            /* sound */ TMMSounds.ITEM_KNIFE_STAB,
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
            /* sound */ TMMSounds.ITEM_KNIFE_PREPARE,
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
            /* sound */ TMMSounds.ITEM_GRENADE_THROW,
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
            /* sound */ TMMSounds.ITEM_GRENADE_EXPLODE,
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
            /* sound */ TMMSounds.ITEM_REVOLVER_CLICK,
            /* category */ SoundCategory.PLAYERS,
            /* volume */ soundVolume,
            /* pitch */ 1f + player.getRandom().nextFloat() * 0.1f - 0.05f
        );
    }

    /**
     * 播放开枪音效并在玩家视线前方生成枪口粒子（烟/火焰）作为 muzzle flash。
     * - 在非服务器世界上仍会播放音效，但不会产生粒子（粒子需 ServerWorld）。
     */
    public static void playShootingEffects(PlayerEntity player, float soundVolume) {
        // 播放开枪声音（广播）
        player.getWorld().playSound(
            /* player */ null,
            player.getX(),
            player.getEyeY(),
            player.getZ(),
            TMMSounds.ITEM_REVOLVER_SHOOT,
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

    
}