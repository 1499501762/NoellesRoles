package org.agmas.noellesroles.client.mixin.brawler;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.brawler.BrawlerPlayerComponent;
import org.agmas.noellesroles.client.NoellesrolesClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class BrawlerHudMixin {
    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void brawlerHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        
        WorldModifierComponent modifierComponent = WorldModifierComponent.KEY.get(client.world);
        AbilityPlayerComponent abilityPlayerComponent = AbilityPlayerComponent.KEY.get(client.player);
        BrawlerPlayerComponent brawlerPlayerComponent = BrawlerPlayerComponent.KEY.get(client.player);
        
        if (modifierComponent.isModifier(client.player, Noellesroles.BRAWLER)) {
            int drawY = context.getScaledWindowHeight();
            Text line;

            // 如果正在冲刺，显示冲刺状态
            if (brawlerPlayerComponent.isCharging()) {
                line = Text.translatable("hud.brawler.charging", brawlerPlayerComponent.chargeTicksRemaining / 20.0);
            }
            // 如果在冷却中，显示冷却时间（使用 AbilityPlayerComponent）
            else if (abilityPlayerComponent.cooldown > 0) {
                line = Text.translatable("hud.brawler.cooldown", abilityPlayerComponent.cooldown / 20);
            }
            // 否则显示可以使用技能
            else {
                line = Text.translatable("hud.brawler.ready", NoellesrolesClient.abilityBind.getBoundKeyLocalizedText());
            }

            drawY -= getTextRenderer().getWrappedLinesHeight(line, 999999);
            context.drawTextWithShadow(getTextRenderer(), line, 
                context.getScaledWindowWidth() - getTextRenderer().getWidth(line), 
                drawY, 
                Noellesroles.BRAWLER.color());
        }
    }
}
