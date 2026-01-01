package org.agmas.noellesroles.mixin.thief;

import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import org.agmas.noellesroles.thief.ThiefPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class ThiefReseterMixin {

    @Inject(method = "resetPlayer", at = @At("TAIL"))
    private static void resetThief(ServerPlayerEntity player, CallbackInfo ci) {
        ThiefPlayerComponent.KEY.get(player).reset();
    }
}
