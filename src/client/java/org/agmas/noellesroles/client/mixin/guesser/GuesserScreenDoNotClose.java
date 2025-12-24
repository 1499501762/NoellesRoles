package org.agmas.noellesroles.client.mixin.guesser;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import net.minecraft.client.option.KeyBinding;
import org.agmas.noellesroles.client.ui.guesser.GuesserRoleWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LimitedHandledScreen.class)
public class GuesserScreenDoNotClose {

    @WrapOperation(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;matchesKey(II)Z", ordinal = 0))
    boolean doNotCloseInventory(KeyBinding instance, int keyCode, int scanCode, Operation<Boolean> original) {
        if (GuesserRoleWidget.stopClosing) return false;
        return original.call(instance,keyCode,scanCode);
    }
    @Inject(method = "close", at = @At("HEAD"))
    void setStopClosingToFalse(CallbackInfo ci) {
        GuesserRoleWidget.stopClosing = false;
    }
}
