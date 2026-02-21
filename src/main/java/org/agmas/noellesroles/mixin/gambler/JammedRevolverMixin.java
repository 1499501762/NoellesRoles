package org.agmas.noellesroles.mixin.gambler;

import dev.doctor4t.wathe.util.GunShootPayload;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheSounds;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.World;
import org.agmas.noellesroles.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(GunShootPayload.Receiver.class)
public class JammedRevolverMixin {
    @Inject(method = "receive(Ldev/doctor4t/wathe/util/GunShootPayload;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V", at = @At("HEAD"), cancellable = true)
    private void onReceive(dev.doctor4t.wathe.util.GunShootPayload payload, ServerPlayNetworking.Context context, CallbackInfo ci) {
        ServerPlayerEntity player = context.player();
        ItemStack main = player.getMainHandStack();
        if (main.isOf(ModItems.JAMMED_REVOLVER)) {
            // 50% chance to backfire and kill the shooter
            boolean backfire = player.getRandom().nextFloat() < 0.5f;
            World world = player.getWorld();
            if (backfire) {
                // play explosion-like effects
                world.playSound(null, player.getBlockPos(), WatheSounds.ITEM_GRENADE_EXPLODE, SoundCategory.PLAYERS, 5.0F, 1.0F + player.getRandom().nextFloat() * 0.1F - 0.05F);
                // kill the shooter
                GameFunctions.killPlayer(player, true, player, GameConstants.DeathReasons.GUN);
                ci.cancel();
                return;
            }
            // else: allow standard handling (do nothing)
        }
    }
}
