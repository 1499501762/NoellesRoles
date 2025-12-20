package org.agmas.noellesroles.client;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.trainmurdermystery.util.GunShootPayload;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.morphling.MorphlingPlayerComponent;
import org.agmas.noellesroles.packet.MorphC2SPacket;
import org.agmas.noellesroles.packet.SniperC2SPacket;
import org.agmas.noellesroles.sniper.SniperPlayerComponent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.UUID;

public class SniperPlayerWidget extends ButtonWidget{
    public final LimitedInventoryScreen screen;
    public final UUID targetUUID=null;
    public final AbstractClientPlayerEntity targetPlayerEntry;


    public SniperPlayerWidget(LimitedInventoryScreen screen, int x, int y, @NotNull AbstractClientPlayerEntity targetPlayerEntry, int index) {
        super(x, y, 16, 16, targetPlayerEntry.getName(), (a) -> {
            ClientPlayNetworking.send(new SniperC2SPacket(targetPlayerEntry.getUuid(), GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld()).getRole(MinecraftClient.getInstance().player).identifier()));
            
            //模拟枪响
            // SniperPlayerComponent sniperComp = (SniperPlayerComponent) SniperPlayerComponent.KEY.get(MinecraftClient.getInstance().player);
            // if (!sniperComp.targetUUID.equals(MinecraftClient.getInstance().player.getUuid()) && !sniperComp.guessedIdentity.equals(GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld()).getRole(MinecraftClient.getInstance().player).identifier())) {
            //     PlayerEntity target = MinecraftClient.getInstance().player.getWorld().getPlayerByUuid(sniperComp.targetUUID);
            //     ClientPlayNetworking.send(new GunShootPayload(target.getId()));
            // }
        }, DEFAULT_NARRATION_SUPPLIER);
        this.screen = screen;
        this.targetPlayerEntry = targetPlayerEntry;
        // this.targetUUID = targetPlayerEntry.getUuid();
    }

    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        // System.out.println("[Sniper Debug] Player String: " + targetPlayerEntry.getName().getString());
        SniperPlayerComponent sniperPlayerComponent = (SniperPlayerComponent) SniperPlayerComponent.KEY.get(MinecraftClient.getInstance().player);
        if (SniperPlayerComponent.KEY.get(MinecraftClient.getInstance().player).shotsRemaining == 0) {
            // 无子弹状态显示
            context.setShaderColor(0f, 0f, 0f, 1f);
            context.drawGuiTexture(ShopEntry.Type.POISON.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
            PlayerSkinDrawer.draw(context, targetPlayerEntry.getSkinTextures().texture(), this.getX(), this.getY(), 16);
            // 无子弹悬停提示使用玩家名
            if (this.isHovered()) {
                this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
                context.drawTooltip(MinecraftClient.getInstance().textRenderer, targetPlayerEntry.getName(), this.getX() - 4 - MinecraftClient.getInstance().textRenderer.getWidth(targetPlayerEntry.getName()) / 2, this.getY() - 9);
            }
        }else{
            if ((AbilityPlayerComponent.KEY.get(MinecraftClient.getInstance().player)).cooldown == 0) {
                context.drawGuiTexture(ShopEntry.Type.POISON.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
                PlayerSkinDrawer.draw(context, targetPlayerEntry.getSkinTextures().texture(), this.getX(), this.getY(), 16);
                if (this.isHovered()) {
                    this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
                    context.drawTooltip(MinecraftClient.getInstance().textRenderer, targetPlayerEntry.getName(), this.getX() - 4 - MinecraftClient.getInstance().textRenderer.getWidth(targetPlayerEntry.getName()) / 2, this.getY() - 9);
                    if (sniperPlayerComponent.targetUUID.equals(targetUUID)) {
                        context.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.translatable("hud.sniper.selected"), this.getX() - 4 - MinecraftClient.getInstance().textRenderer.getWidth(Text.translatable("hud.sniper.selected")) / 2, this.getY() - 9);
                    }
                }
            }
            if ((AbilityPlayerComponent.KEY.get(MinecraftClient.getInstance().player)).cooldown > 0) {
                context.setShaderColor(0.25f,0.25f,0.25f,1f);
                context.drawGuiTexture(ShopEntry.Type.POISON.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
                PlayerSkinDrawer.draw(context, targetPlayerEntry.getSkinTextures().texture(), this.getX(), this.getY(), 16);
                if (this.isHovered()) {
                    this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
                    context.drawTooltip(MinecraftClient.getInstance().textRenderer, targetPlayerEntry.getName(), this.getX() - 4 - MinecraftClient.getInstance().textRenderer.getWidth(targetPlayerEntry.getName()) / 2, this.getY() - 9);
                    if (sniperPlayerComponent.targetUUID.equals(targetUUID)) {
                        context.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.translatable("hud.sniper.selected"), this.getX() - 4 - MinecraftClient.getInstance().textRenderer.getWidth(Text.translatable("hud.sniper.selected")) / 2, this.getY() - 9);
                    }
                }

                context.setShaderColor(1f,1f,1f,1f);
                context.drawText(MinecraftClient.getInstance().textRenderer, AbilityPlayerComponent.KEY.get(MinecraftClient.getInstance().player).cooldown/20+"",this.getX(),this.getY(), Color.RED.getRGB(),true);

            }
        }
    }

    private void drawShopSlotHighlight(DrawContext context, int x, int y, int z) {
        int color = -1862287543;
        context.fillGradient(RenderLayer.getGuiOverlay(), x, y, x + 16, y + 14, color, color, z);
        context.fillGradient(RenderLayer.getGuiOverlay(), x, y + 14, x + 15, y + 15, color, color, z);
        context.fillGradient(RenderLayer.getGuiOverlay(), x, y + 15, x + 14, y + 16, color, color, z);
    }

    public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
    }

}
