package org.agmas.noellesroles.client.mixin.detective;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.client.DetectivePlayerWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LimitedInventoryScreen.class)
public abstract class DetectiveScreenMixin extends LimitedHandledScreen<PlayerScreenHandler> {
    @Shadow @Final public ClientPlayerEntity player;

    public DetectiveScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    void renderDetectiveEntries(CallbackInfo ci) {
        GameWorldComponent gwc = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        if (!gwc.isRole(player, Noellesroles.DETECTIVE)) return;

        // 玩家列表（头像行）
        List<AbstractClientPlayerEntity> players = MinecraftClient.getInstance().world.getPlayers();
        if (!players.contains(player)) players.add(player);

        int apartPlayers = 36;
        int centerY = (height - 32) / 2;
        int baseXPlayers = width / 2 - (players.size()) * apartPlayers / 2 + 9;
        int yPlayers = centerY + 70;

        for (int i = 0; i < players.size(); i++) {
            DetectivePlayerWidget pWidget = new DetectivePlayerWidget(
                    ((LimitedInventoryScreen)(Object)this),
                    baseXPlayers + apartPlayers * i,
                    yPlayers,
                    players.get(i),
                    i
            );
            addDrawableChild(pWidget);
        }
    }
}
