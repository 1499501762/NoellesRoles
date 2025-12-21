package org.agmas.noellesroles.mixin.host;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameFunctions.class)
public abstract class MasterKeyDropMixin {

    @Inject(method = "shouldDropOnDeath", at = @At("HEAD"), cancellable = true)
    private static void dropMasterKeyOnDeath(ItemStack stack, PlayerEntity victim, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isOf(ModItems.MASTER_KEY)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

}
