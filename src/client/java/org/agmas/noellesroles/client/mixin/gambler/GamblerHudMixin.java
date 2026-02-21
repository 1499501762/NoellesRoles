package org.agmas.noellesroles.client.mixin.gambler;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.client.NoellesrolesClient;
import org.agmas.noellesroles.gambler.GamblerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class GamblerHudMixin {
    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void gamblerHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(client.world);
        AbilityPlayerComponent abilityPlayerComponent = AbilityPlayerComponent.KEY.get(client.player);

        if (gameWorldComponent.isRole(client.player, Noellesroles.GAMBLER)) {
            GamblerPlayerComponent gamblerComp = GamblerPlayerComponent.KEY.get(client.player);
            int drawY = context.getScaledWindowHeight();
            Text line;

            if (abilityPlayerComponent.cooldown > 0) {
                line = Text.translatable("hud.gambler.cooldown", abilityPlayerComponent.cooldown / 20);
            } else {
                line = Text.translatable("hud.gambler.ready", NoellesrolesClient.abilityBind.getBoundKeyLocalizedText());
            }

            drawY -= getTextRenderer().getWrappedLinesHeight(line, 999999);
            context.drawTextWithShadow(getTextRenderer(), line,
                context.getScaledWindowWidth() - getTextRenderer().getWidth(line),
                drawY,
                Noellesroles.GAMBLER.color());

            // loss streak
            Text streak = Text.translatable("hud.gambler.loss_streak", gamblerComp.lossStreak);
            drawY -= getTextRenderer().getWrappedLinesHeight(streak, 999999);
            context.drawTextWithShadow(getTextRenderer(), streak,
                context.getScaledWindowWidth() - getTextRenderer().getWidth(streak),
                drawY,
                0xFFAAAA);
        }
    }
}
