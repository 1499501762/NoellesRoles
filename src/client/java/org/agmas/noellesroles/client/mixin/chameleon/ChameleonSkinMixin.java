package org.agmas.noellesroles.client.mixin.chameleon;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.agmas.noellesroles.chameleon.ChameleonPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.awt.*;

@Mixin(LivingEntityRenderer.class)
public class ChameleonSkinMixin {

    @ModifyArg(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"), index = 4)
    public int visibilityMixin(int par3, @Local(argsOnly = true) LivingEntity livingEntity) {

        if (livingEntity instanceof PlayerEntity player) {
            ChameleonPlayerComponent chameleonPlayerComponent = ChameleonPlayerComponent.KEY.get(player);
            if (chameleonPlayerComponent.hidingTicks > 0) {
                return new Color(1.0f, 1.0f, 1.0f, MathHelper.clamp(MathHelper.lerp(chameleonPlayerComponent.hidingTicks/400f, 1.0f, 0.0f), 0f, 1.0f)).getRGB();
            }
        }
        return par3;
    }
}
