package org.agmas.noellesroles.mixin.gambler;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.util.ShopEntry;
import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerShopComponent.class)
public abstract class GamblerPlayerShopComponentMixin {
    @Shadow public int balance;

    @Shadow @Final private PlayerEntity player;

    @Shadow public abstract void sync();

    @Inject(method = "tryBuy", at = @At("HEAD"), cancellable = true)
    void gamblerTryBuy(int index, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
        if (gameWorldComponent.isRole(player, Noellesroles.GAMBLER)) {
            if (index < 0 || index >= GameConstants.SHOP_ENTRIES.size()) return;
            ShopEntry serverEntry = GameConstants.SHOP_ENTRIES.get(index);
            if (serverEntry == null) return;

            // Handle Jammed Revolver purchase safely on server
            if (serverEntry.stack().getItem() == ModItems.JAMMED_REVOLVER) {
                if (this.balance >= serverEntry.price()) {
                    if (ShopEntry.insertStackInFreeSlot(player, serverEntry.stack().copy())) {
                        this.balance -= serverEntry.price();
                        sync();
                        if (this.player instanceof ServerPlayerEntity serverPlayer) {
                            serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(WatheSounds.UI_SHOP_BUY), SoundCategory.PLAYERS, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), 1.0F, 0.9F + this.player.getRandom().nextFloat() * 0.2F, serverPlayer.getRandom().nextLong()));
                        }
                    } else {
                        this.player.sendMessage(Text.literal("Purchase Failed").formatted(Formatting.DARK_RED), true);
                        if (this.player instanceof ServerPlayerEntity serverPlayer) {
                            serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(WatheSounds.UI_SHOP_BUY_FAIL), SoundCategory.PLAYERS, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), 1.0F, 0.9F + this.player.getRandom().nextFloat() * 0.2F, serverPlayer.getRandom().nextLong()));
                        }
                    }
                } else {
                    this.player.sendMessage(Text.literal("Purchase Failed").formatted(Formatting.DARK_RED), true);
                    if (this.player instanceof ServerPlayerEntity serverPlayer) {
                        serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(WatheSounds.UI_SHOP_BUY_FAIL), SoundCategory.PLAYERS, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), 1.0F, 0.9F + this.player.getRandom().nextFloat() * 0.2F, serverPlayer.getRandom().nextLong()));
                    }
                }
                ci.cancel();
                return;
            }
        }
    }

}
