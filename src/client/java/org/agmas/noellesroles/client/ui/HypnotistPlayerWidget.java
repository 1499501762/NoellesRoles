package org.agmas.noellesroles.client.ui;

import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.util.ShopEntry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import org.agmas.noellesroles.AbilityPlayerComponent;
import net.minecraft.text.Text;
import org.agmas.noellesroles.hypnotist.HypnotistPlayerComponent;
import org.agmas.noellesroles.packet.HypnotistC2SPacket;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class HypnotistPlayerWidget extends ButtonWidget {
    public final LimitedInventoryScreen screen;
    public final AbstractClientPlayerEntity disguiseTarget;

    public HypnotistPlayerWidget(LimitedInventoryScreen screen, int x, int y, @NotNull AbstractClientPlayerEntity disguiseTarget, int index) {
        super(x, y, 16, 16, disguiseTarget.getName(), (a) -> {
            AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(MinecraftClient.getInstance().player);
            HypnotistPlayerComponent hyp = HypnotistPlayerComponent.KEY.get(MinecraftClient.getInstance().player);
            boolean onCooldown = ability != null && ability.cooldown > 0;
            boolean noUses = hyp != null && hyp.usesRemaining <= 0;
            if (!onCooldown && !noUses) {
                if (MinecraftClient.getInstance().player.getWorld().getPlayerByUuid(disguiseTarget.getUuid()) == null) return;
                if (MinecraftClient.getInstance().player.getWorld().getPlayerByUuid(disguiseTarget.getUuid()).hasVehicle()) return;
                ClientPlayNetworking.send(new HypnotistC2SPacket(disguiseTarget.getUuid()));
            }
        }, DEFAULT_NARRATION_SUPPLIER);
        this.screen = screen;
        this.disguiseTarget = disguiseTarget;
    }

    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(MinecraftClient.getInstance().player);
        HypnotistPlayerComponent hyp = HypnotistPlayerComponent.KEY.get(MinecraftClient.getInstance().player);
        boolean onCooldown = ability != null && ability.cooldown > 0;
        boolean noUses = hyp != null && hyp.usesRemaining <= 0;
        boolean enabled = !onCooldown && !noUses;

        if (enabled) {
            context.drawGuiTexture(ShopEntry.Type.POISON.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
            PlayerSkinDrawer.draw(context, disguiseTarget.getSkinTextures().texture(), this.getX(), this.getY(), 16);
            if (this.isHovered()) {
                this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
                context.drawTooltip(MinecraftClient.getInstance().textRenderer, disguiseTarget.getName(), this.getX() - 4 - MinecraftClient.getInstance().textRenderer.getWidth(disguiseTarget.getName()) / 2, this.getY() - 9);
            }
        } else {
            context.setShaderColor(0.25f,0.25f,0.25f,0.5f);
            context.drawGuiTexture(ShopEntry.Type.POISON.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
            PlayerSkinDrawer.draw(context, disguiseTarget.getSkinTextures().texture(), this.getX(), this.getY(), 16);
            if (this.isHovered()) {
                this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
                if (noUses) {
                    context.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.translatable("hud.hypnotist.no_uses"), this.getX() - 4 - MinecraftClient.getInstance().textRenderer.getWidth(disguiseTarget.getName()) / 2, this.getY() - 9);
                } else {
                    context.drawTooltip(MinecraftClient.getInstance().textRenderer, disguiseTarget.getName(), this.getX() - 4 - MinecraftClient.getInstance().textRenderer.getWidth(disguiseTarget.getName()) / 2, this.getY() - 9);
                }
            }
            context.setShaderColor(1f,1f,1f,1f);
            if (onCooldown && ability != null) {
                context.drawText(MinecraftClient.getInstance().textRenderer, ability.cooldown/20+"",this.getX(),this.getY(), Color.RED.getRGB(),true);
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
