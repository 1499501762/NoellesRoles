package org.agmas.noellesroles.client.mixin.conductor;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.agmas.noellesroles.ConfigWorldComponent;
import org.agmas.noellesroles.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HeldItemFeatureRenderer.class)
public class MasterKeyInvisibilityMixin {
    @WrapOperation(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack view(LivingEntity instance, Operation<ItemStack> original) {

        ItemStack ret = original.call(instance);
        if (ret.isOf(ModItems.MASTER_KEY) && !ConfigWorldComponent.KEY.get(instance.getEntityWorld()).masterKeyIsVisible) {
            ret = WatheItems.LOCKPICK.getDefaultStack();
        }

        return ret;
    }
}
