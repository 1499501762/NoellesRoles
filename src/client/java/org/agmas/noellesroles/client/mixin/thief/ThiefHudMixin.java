package org.agmas.noellesroles.client.mixin.thief;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.agmas.noellesroles.thief.ThiefPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(InGameHud.class)
public abstract class ThiefHudMixin {
    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    void renderThiefStolenRole(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        ThiefPlayerComponent thief = ThiefPlayerComponent.KEY.get(client.player);
        if (thief == null || !thief.hasStolen || thief.stolenIdentity == null) return;

        Role stolenRole = null;
        for (Role role : WatheRoles.ROLES) {
            if (role.identifier().equals(thief.stolenIdentity)) {
                stolenRole = role;
                break;
            }
        }
        if (stolenRole == null) return;

        GameWorldComponent gwc = (GameWorldComponent) GameWorldComponent.KEY.get(client.world);
        if (!gwc.isRole(client.player, stolenRole)) return;

        var id = thief.stolenIdentity;
        Text roleName = "noellesroles".equals(id.getNamespace())
                ? Text.translatable("announcement.role." + id.getNamespace() + "." + id.getPath())
                : Text.translatable("announcement.role." + id.getPath());

        Text thiefTip = Text.translatable("hud.thief.stolen_role").append(roleName);

        int x = (context.getScaledWindowWidth() - getTextRenderer().getWidth(thiefTip)) / 2;
        int y = (int) (context.getScaledWindowHeight() * 0.25);

        context.drawTextWithShadow(getTextRenderer(), thiefTip, x, y, Color.ORANGE.getRGB());
    }
}
