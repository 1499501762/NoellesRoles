package org.agmas.noellesroles.client.mixin.brawler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import org.agmas.noellesroles.brawler.BrawlerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 客户端粒子效果 Mixin
 * 在玩家冲刺时显示粒子效果
 */
@Mixin(ClientWorld.class)
public abstract class BrawlerParticlesMixin {

    @Inject(method = "tickEntities", at = @At("HEAD"))
    private void tickBrawlerParticles(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        BrawlerPlayerComponent brawlerComp = BrawlerPlayerComponent.KEY.get(client.player);
        if (brawlerComp != null && brawlerComp.isCharging()) {
            // 在玩家周围生成粒子效果
            spawnChargeParticles(client.player);
        }
    }

    /**
     * 在玩家冲刺时生成旋风粒子效果
     */
    private void spawnChargeParticles(PlayerEntity player) {
        ClientWorld world = (ClientWorld) player.getWorld();
        Vec3d pos = player.getPos();
        
        // 在玩家周围生成多个粒子，形成旋风效果
        for (int i = 0; i < 5; i++) {
            double angle = (player.age + i) * 0.3;
            double radius = 0.8;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            
            // 烟雾粒子
            world.addParticle(
                ParticleTypes.CLOUD,
                pos.x + offsetX,
                pos.y + 0.5,
                pos.z + offsetZ,
                (Math.random() - 0.5) * 0.2,
                0.1,
                (Math.random() - 0.5) * 0.2
            );
            
            // 速度线粒子（白色）
            world.addParticle(
                ParticleTypes.END_ROD,
                pos.x + offsetX,
                pos.y + 1.0,
                pos.z + offsetZ,
                -Math.cos(angle) * 0.3,
                0,
                -Math.sin(angle) * 0.3
            );
        }
    }
}
