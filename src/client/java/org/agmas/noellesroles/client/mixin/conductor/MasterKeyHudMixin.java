package org.agmas.noellesroles.client.mixin.conductor;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.agmas.harpymodloader.client.HarpymodloaderClient;
import org.agmas.noellesroles.ConfigWorldComponent;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.client.NoellesrolesClient;
import org.agmas.noellesroles.executioner.ExecutionerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RoleNameRenderer.class)
public class MasterKeyHudMixin {
    @Inject(method = "renderHud", at = @At("HEAD"))
    private static void executionerHudRenderer(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        if (player.getMainHandStack().isOf(ModItems.MASTER_KEY) && !ConfigWorldComponent.KEY.get(player.getWorld()).masterKeyIsVisible) {
            context.getMatrices().push();
            context.getMatrices().translate((float) context.getScaledWindowWidth() / 2.0F, (float) context.getScaledWindowHeight() / 2.0F + 6.0F, 0.0F);
            context.getMatrices().scale(0.6F, 0.6F, 1.0F);
            context.setShaderColor(1,1,1,0.5f);
            Text name = Text.translatable("tip.master_key_invisible");
            if (ConfigWorldComponent.KEY.get(player.getWorld()).masterKeyVisibleCount != 0) {
                name = Text.translatable("tip.master_key_invisible_count", ConfigWorldComponent.KEY.get(player.getWorld()).masterKeyVisibleCount);
            }
            context.setShaderColor(1,1,1,1);
            context.drawTextWithShadow(renderer, name, -renderer.getWidth(name) / 2, 32, Colors.GRAY);

            context.getMatrices().pop();
        }
    }
}
