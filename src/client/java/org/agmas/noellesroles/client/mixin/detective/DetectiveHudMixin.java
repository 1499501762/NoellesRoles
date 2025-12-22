package org.agmas.noellesroles.client.mixin.detective;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.agmas.noellesroles.client.DetectivePlayerWidget;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.detective.DetectivePlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class DetectiveHudMixin {
    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void detectiveHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        GameWorldComponent gwc = (GameWorldComponent) GameWorldComponent.KEY.get(mc.player.getWorld());
        if (!gwc.isRole(mc.player, Noellesroles.DETECTIVE)) return;

        AbilityPlayerComponent ability = AbilityPlayerComponent.KEY.get(mc.player);
        DetectivePlayerComponent comp = DetectivePlayerComponent.KEY.get(mc.player);

        // 绘制右下角状态文本（冷却或剩余次数）
        Text line;
        if (ability.cooldown > 0) {
            line = Text.translatable("tip.noellesroles.cooldown", ability.cooldown / 20);
        } else {
            line = Text.translatable("hud.noellesroles.detective", comp.getDetectRemaining(), comp.maxDetect);
        }
        DetectivePlayerWidget.drawHudLine(context, getTextRenderer(), line, Noellesroles.DETECTIVE.color());
    }
}