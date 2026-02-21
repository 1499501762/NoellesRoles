package org.agmas.noellesroles.client.mixin.dj;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.client.NoellesrolesClient;
import org.agmas.noellesroles.dj.DJPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class DJHudMixin {
    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void djHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        GameWorldComponent gwc = (GameWorldComponent) GameWorldComponent.KEY.get(mc.player.getWorld());
        if (!gwc.isRole(mc.player, Noellesroles.DJ)) return;

        AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(mc.player);
        PlayerShopComponent shop = PlayerShopComponent.KEY.get(mc.player);

        Text line;
        if (ability.cooldown > 0) {
            line = Text.translatable("tip.noellesroles.cooldown", ability.cooldown / 20);
        } else if (shop != null && shop.balance < 100) {
            line = Text.translatable("tip.dj.not_enough_money");
        } else {
            line = Text.translatable("tip.dj.play", NoellesrolesClient.abilityBind.getBoundKeyLocalizedText(), 100);
        }

        int drawY = context.getScaledWindowHeight() - getTextRenderer().getWrappedLinesHeight(line, Integer.MAX_VALUE);
        int drawX = context.getScaledWindowWidth() - getTextRenderer().getWidth(line);
        context.drawTextWithShadow(getTextRenderer(), line, drawX, drawY, Noellesroles.DJ.color());
    }
}
