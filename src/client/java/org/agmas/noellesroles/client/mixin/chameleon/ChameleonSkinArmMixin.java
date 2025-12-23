package org.agmas.noellesroles.client.mixin.chameleon;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.agmas.noellesroles.chameleon.ChameleonPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(PlayerEntityRenderer.class)
public class ChameleonSkinArmMixin {

    @Inject(method = "renderArm", at = @At(value = "HEAD"))
    public void visibilityMixin(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {

        ChameleonPlayerComponent chameleonPlayerComponent = ChameleonPlayerComponent.KEY.get(player);
        if (chameleonPlayerComponent.hidingTicks > 0) {
            float f = MathHelper.clamp(MathHelper.lerp(chameleonPlayerComponent.hidingTicks/1000f, 1.0f, 0.0f), 0f, 1.0f);
            matrices.scale(f,f,f);
        }
    }
}
