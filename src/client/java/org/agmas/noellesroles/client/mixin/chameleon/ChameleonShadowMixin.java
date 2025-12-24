package org.agmas.noellesroles.client.mixin.chameleon;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.noellesroles.Noellesroles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class ChameleonShadowMixin {

    @Inject(method = "getShadowRadius(Lnet/minecraft/entity/LivingEntity;)F", at = @At("HEAD"), cancellable = true)
    public void phantomRatMixin(LivingEntity livingEntity, CallbackInfoReturnable<Float> cir) {
        if (livingEntity instanceof PlayerEntity) {
            WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(livingEntity.getWorld());
            if (worldModifierComponent.isRole(livingEntity.getUuid(), Noellesroles.CHAMELEON)) {
                cir.setReturnValue(0f);
                cir.cancel();
            }
        }
    }
}
