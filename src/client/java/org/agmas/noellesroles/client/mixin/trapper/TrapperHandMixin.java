package org.agmas.noellesroles.client.mixin.trapper;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntityRenderer.class)
public class TrapperHandMixin {
    @WrapOperation(method = "getArmPose", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack view(AbstractClientPlayerEntity instance, Hand hand, Operation<ItemStack> original) {

        ItemStack ret = original.call(instance, hand);
        if ((ret.isOf(Items.PAPER) && GameWorldComponent.KEY.get(instance.getWorld()).isRole(instance, Noellesroles.TRAPPER)) || ret.isOf(ModItems.ROLE_MINE)) {
            ret = ItemStack.EMPTY;
        }

        return ret;
    }
}
