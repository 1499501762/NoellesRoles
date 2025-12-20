package org.agmas.noellesroles.mixin.framing;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.bartender.BartenderPlayerComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerPoisonComponent.class)
public abstract class DoNotApplyFakePoisonMixin {

    @Shadow @Final private PlayerEntity player;

    @Shadow public int poisonTicks;


    @Shadow public abstract void reset();

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private void defenseVialApply(CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
        if (!gameWorldComponent.canUseKillerFeatures(player)) {
            if (gameWorldComponent.getRole(player) == null) return;
            if (gameWorldComponent.getRole(player).identifier().getNamespace().equals(Noellesroles.MOD_ID)) { // Don't interfere with any custom non-killer poisoning roles from other mods
                if (poisonTicks <= 5) {
                    reset();
                    ci.cancel();
                }
            }
        }
    }
}
