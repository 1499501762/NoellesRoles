package org.agmas.noellesroles.client.mixin.sniper;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.client.ui.sniper.SniperPlayerWidget;
import org.agmas.noellesroles.client.ui.sniper.SniperRoleWidget;
import org.agmas.noellesroles.util.RoleUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.List;
import java.util.Map;

@Mixin(LimitedInventoryScreen.class)
public abstract class SniperScreenMixin extends LimitedHandledScreen<PlayerScreenHandler> {
    @Shadow @Final public ClientPlayerEntity player;

    public SniperScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "render", at = @At("HEAD"))
    void renderSniperText(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        GameWorldComponent gwc = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        if (gwc.isRole(player, Noellesroles.SNIPER)) {
            int centerY = (height - 32) / 2 - 5;
            int centerX = width / 2;

            Text pickPlayer = Text.translatable("hud.sniper.choose_player");
            // Text pickRole   = Text.translatable("hud.sniper.choose_role");

            var tr = MinecraftClient.getInstance().textRenderer;
            context.drawTextWithShadow(tr, pickPlayer,
                    centerX - tr.getWidth(pickPlayer) / 2, centerY + 45, Color.CYAN.getRGB());
            // context.drawTextWithShadow(tr, pickRole,
            //         centerX - tr.getWidth(pickRole) / 2, centerY + 115, Color.YELLOW.getRGB());
        }
    }

    @Inject(method = "init", at = @At("HEAD"))
    void renderSniperEntries(CallbackInfo ci) {
        GameWorldComponent gwc = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        if (!gwc.isRole(player, Noellesroles.SNIPER)) return;

        // 1) 玩家列表（头像行）
        List<AbstractClientPlayerEntity> players = MinecraftClient.getInstance().world.getPlayers();
        if (!players.contains(player)) players.add(player);

        int apartPlayers = 36;
        int baseXPlayers = width / 2 - (players.size()) * apartPlayers / 2 + 9;
        int centerY = (height - 32) / 2;
        int yPlayers = centerY + 70;

        for (int i = 0; i < players.size(); i++) {
            SniperPlayerWidget pWidget = new SniperPlayerWidget(
                    ((LimitedInventoryScreen)(Object)this),
                    baseXPlayers + apartPlayers * i,
                    yPlayers,
                    players.get(i),
                    i
            );
            addDrawableChild(pWidget);
        }

        // 2) 职业列表
        Map<String, dev.doctor4t.wathe.api.Role> roleMap = RoleUtils.getEnabledInnocentRoleMap();

        int apartRoles = 36;
        int baseXRoles = width / 2 - (roleMap.size() * apartRoles) / 2 + 9;
        int yRoles = centerY + 120;

        int i = 0;
        for (Map.Entry<String, dev.doctor4t.wathe.api.Role> entry : roleMap.entrySet()) {
            dev.doctor4t.wathe.api.Role roleObj = entry.getValue();

            // 新构造函数只需要 Role 对象
            SniperRoleWidget rWidget = new SniperRoleWidget(
                    ((LimitedInventoryScreen)(Object)this),
                    baseXRoles + apartRoles * i,
                    yRoles,
                    roleObj,
                    i
            );
            // System.out.println("[ROLE_INIT] i=" + i + " name=" + roleObj.identifier() + " x=" + (baseXRoles + apartRoles * i) + " y=" + yRoles);
            addDrawableChild(rWidget);
            i++;
        }
    }
}
