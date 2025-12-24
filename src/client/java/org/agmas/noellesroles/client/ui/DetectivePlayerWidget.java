package org.agmas.noellesroles.client.ui;

import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import dev.doctor4t.wathe.util.ShopEntry;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.detective.DetectivePlayerComponent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class DetectivePlayerWidget extends ButtonWidget {
    public final LimitedInventoryScreen screen;
    public final AbstractClientPlayerEntity targetPlayerEntry;

    public DetectivePlayerWidget(LimitedInventoryScreen screen, int x, int y, @NotNull AbstractClientPlayerEntity targetPlayerEntry, int index) {
        super(x, y, 16, 16, targetPlayerEntry.getName(), (a) -> {
            // 点击时发送侦探查询包，由服务端写入 DetectivePlayerComponent
            ClientPlayNetworking.send(new org.agmas.noellesroles.packet.DetectiveC2SPacket(targetPlayerEntry.getUuid()));
        }, DEFAULT_NARRATION_SUPPLIER);
        this.screen = screen;
        this.targetPlayerEntry = targetPlayerEntry;
    }

    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        // 背景边框
        context.drawGuiTexture(ShopEntry.Type.TOOL.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);

        // 绘制玩家头像；根据冷却/次数做明暗处理
        boolean inCooldown = AbilityPlayerComponent.KEY.get(MinecraftClient.getInstance().player).cooldown > 0;
        DetectivePlayerComponent compEarly = DetectivePlayerComponent.KEY.get(MinecraftClient.getInstance().player);
        boolean hasDetect = compEarly != null && compEarly.hasDetectRemaining();
        if (inCooldown || !hasDetect) {
            context.setShaderColor(0.25f, 0.25f, 0.25f, 1f);
        }
        PlayerSkinDrawer.draw(context, targetPlayerEntry.getSkinTextures().texture(), this.getX(), this.getY(), 16);
        context.setShaderColor(1f, 1f, 1f, 1f);

        // 悬停时显示玩家名与职业（身份）
        if (this.isHovered()) {
            this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);

            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            Text name = targetPlayerEntry.getName();
            context.drawTooltip(tr, name,
                    this.getX() - 4 - tr.getWidth(name) / 2,
                    this.getY() - 24);

            DetectivePlayerComponent comp = DetectivePlayerComponent.KEY.get(MinecraftClient.getInstance().player);
            String identityRaw = comp != null ? comp.getIdentityByUUID(targetPlayerEntry.getUuid()) : "???";

            Text identityText = Text.literal(identityRaw);

            // int color = "???".equals(identityRaw) ? new Color(180, 180, 180).getRGB() : new Color(140, 220, 120).getRGB();
            context.drawTooltip(tr, identityText,
                this.getX() - 4 - tr.getWidth(identityText) / 2,
                this.getY() -8);
            // 冷却数值显示
            if (inCooldown) {
                int cd = AbilityPlayerComponent.KEY.get(MinecraftClient.getInstance().player).cooldown / 20;
                context.drawText(tr, String.valueOf(cd), this.getX(), this.getY(), Color.RED.getRGB(), true);
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

    // HUD 辅助渲染：在屏幕右下角绘制一行文字
    public static void drawHudLine(DrawContext context, TextRenderer textRenderer, Text line, int color) {
        int drawY = context.getScaledWindowHeight()- textRenderer.getWrappedLinesHeight(line, Integer.MAX_VALUE);
        int drawX = context.getScaledWindowWidth() - textRenderer.getWidth(line);
        context.drawTextWithShadow(textRenderer, line, drawX, drawY, color);
    }
}
