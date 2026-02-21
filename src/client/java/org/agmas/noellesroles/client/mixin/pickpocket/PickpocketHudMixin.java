package org.agmas.noellesroles.client.mixin.pickpocket;

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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class PickpocketHudMixin {
    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void pickpocketHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(client.world);
        AbilityPlayerComponent abilityPlayerComponent = AbilityPlayerComponent.KEY.get(client.player);

        if (gameWorldComponent.isRole(client.player, Noellesroles.PICKPOCKET)) {
            int drawY = context.getScaledWindowHeight();
            Text line;

            if (abilityPlayerComponent.cooldown > 0) {
                line = Text.translatable("hud.pickpocket.cooldown", abilityPlayerComponent.cooldown / 20);
            } else {
                line = Text.translatable("hud.pickpocket.ready", NoellesrolesClient.abilityBind.getBoundKeyLocalizedText());
            }

            drawY -= getTextRenderer().getWrappedLinesHeight(line, 999999);
            context.drawTextWithShadow(getTextRenderer(), line,
                context.getScaledWindowWidth() - getTextRenderer().getWidth(line),
                drawY,
                Noellesroles.PICKPOCKET.color());
        }
    }
}
