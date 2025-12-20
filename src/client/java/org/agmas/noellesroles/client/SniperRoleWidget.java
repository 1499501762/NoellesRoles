package org.agmas.noellesroles.client;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.util.GunShootPayload;
import dev.doctor4t.wathe.util.ShopEntry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.packet.SniperC2SPacket;
import org.agmas.noellesroles.sniper.SniperPlayerComponent;
import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.systems.RenderSystem;

import java.awt.*;
public class SniperRoleWidget extends ButtonWidget {
    public final LimitedInventoryScreen screen;
    public final Role role;
    public final MutableText roleNameString;
    private final int idx;
    public SniperRoleWidget(LimitedInventoryScreen screen, int x, int y, @NotNull Role role, int idx) {
        super(x, y, 16, 16, Text.translatable("announcement.role." + role.identifier().getPath()), (a) -> {
            ClientPlayNetworking.send(new SniperC2SPacket(MinecraftClient.getInstance().player.getUuid(), role.identifier()));
            
            //模拟枪响
            // SniperPlayerComponent sniperComp = (SniperPlayerComponent) SniperPlayerComponent.KEY.get(MinecraftClient.getInstance().player);
            // if (!sniperComp.targetUUID.equals(MinecraftClient.getInstance().player.getUuid()) && !sniperComp.guessedIdentity.equals(GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld()).getRole(MinecraftClient.getInstance().player).identifier())) {
            //     PlayerEntity target = MinecraftClient.getInstance().player.getWorld().getPlayerByUuid(sniperComp.targetUUID);
            //     ClientPlayNetworking.send(new GunShootPayload(target.getId()));
            // }
        }, DEFAULT_NARRATION_SUPPLIER);
        this.screen = screen;
        this.role = role;
        this.roleNameString = Text.translatable("announcement.role." + role.identifier().getPath());
        this.idx = idx;
    }
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        if (SniperPlayerComponent.KEY.get(MinecraftClient.getInstance().player).shotsRemaining == 0) {
            // 无子弹状态显示
            context.setShaderColor(0f, 0f, 0f, 1f);
            context.drawGuiTexture(ShopEntry.Type.POISON.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
            
            // 无子弹悬停提示使用角色名
            if (this.isHovered()) {
                this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
                context.drawTooltip(MinecraftClient.getInstance().textRenderer, 
                    roleNameString, 
                    this.getX() - 4 - MinecraftClient.getInstance().textRenderer.getWidth(roleNameString) / 2,
                    this.getY() - 9);
            }
            
        }else if (AbilityPlayerComponent.KEY.get(MinecraftClient.getInstance().player).cooldown == 0) {
            context.setShaderColor(1f, 1f, 1f, 1f);
            context.drawText(MinecraftClient.getInstance().textRenderer, "?", 
                this.getX() + (this.getWidth() - MinecraftClient.getInstance().textRenderer.getWidth("?")) / 2,
                this.getY() + (this.getHeight() - MinecraftClient.getInstance().textRenderer.fontHeight) / 2,
                Color.WHITE.getRGB(), true);
            
            context.drawGuiTexture(ShopEntry.Type.POISON.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
            
            // 悬停提示使用角色名
            if (this.isHovered()) {
                this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
                context.drawTooltip(MinecraftClient.getInstance().textRenderer, 
                    roleNameString, 
                    this.getX() - 4 - MinecraftClient.getInstance().textRenderer.getWidth(roleNameString) / 2,
                    this.getY() - 9);
            }
        } else {
            // 冷却状态显示
            context.setShaderColor(0.25f,0.25f,0.25f,1f);
            context.drawText(MinecraftClient.getInstance().textRenderer, "?", 
                this.getX() + (this.getWidth() - MinecraftClient.getInstance().textRenderer.getWidth("?")) / 2,
                this.getY() + (this.getHeight() - MinecraftClient.getInstance().textRenderer.fontHeight) / 2,
                Color.WHITE.getRGB(), true);
            
            context.drawGuiTexture(ShopEntry.Type.POISON.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
            if (this.isHovered()) {
                this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
                // TextRenderer tr = MinecraftClient.getInstance().textRenderer;
                // int textW = tr.getWidth(roleNameString);
                // int tooltipX = this.getX() + this.getWidth()/2 - textW/2;
                // int tooltipY = this.getY() - 9;

                // // 手工画背景（简单矩形）
                // context.fill(tooltipX - 3, tooltipY - 3, tooltipX + textW + 3, tooltipY + 10, 0xF0100010); // 背景色
                // context.fill(tooltipX - 2, tooltipY - 2, tooltipX + textW + 2, tooltipY + 9, 0x505000ff); // 内层

                // // 手工画文字（白色）
                // context.drawText(tr, roleNameString, tooltipX, tooltipY, 0xFFFFFF, true);
                // context.getMatrices().push();

                context.drawTooltip(MinecraftClient.getInstance().textRenderer, 
                    roleNameString, 
                    this.getX() - 4 - MinecraftClient.getInstance().textRenderer.getWidth(roleNameString) / 2,
                    this.getY() - 9);
            }
            // 冷却时间显示在左上角
            context.setShaderColor(1f,1f,1f,1f);
            context.drawText(MinecraftClient.getInstance().textRenderer, AbilityPlayerComponent.KEY.get(MinecraftClient.getInstance().player).cooldown/20+"",this.getX(),this.getY(), Color.RED.getRGB(),true);
        }
    }
    // 保留原有的高亮绘制方法
    private void drawShopSlotHighlight(DrawContext context, int x, int y, int z) {
        int color = -1862287543;
        context.fillGradient(RenderLayer.getGuiOverlay(), x, y, x + 16, y + 14, color, color, z);
        context.fillGradient(RenderLayer.getGuiOverlay(), x, y + 14, x + 15, y + 15, color, color, z);
        context.fillGradient(RenderLayer.getGuiOverlay(), x, y + 15, x + 14, y + 16, color, color, z);
    }

    public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
    }

}