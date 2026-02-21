package org.agmas.noellesroles.client.mixin.hypnotist;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.hypnotist.HypnotistPlayerComponent;
import org.agmas.noellesroles.client.ui.HypnotistPlayerWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.List;

@Mixin(LimitedInventoryScreen.class)
public abstract class HypnotistScreenMixin extends LimitedHandledScreen<PlayerScreenHandler> {
    @Shadow @Final public ClientPlayerEntity player;

    public HypnotistScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "render", at = @At("HEAD"))
    void renderHypnotistText(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        if (gameWorldComponent.isRole(player, Noellesroles.HYPNOTIST)) {
            int y = (height- 32) / 2;
            int x = width / 2;
            Text name = Text.translatable("hud.hypnotist.player_selection");
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, name, x - (MinecraftClient.getInstance().textRenderer.getWidth(name)/2), y + 40, Color.MAGENTA.getRGB());
            // 显示剩余使用次数
            try {
                HypnotistPlayerComponent hyp = HypnotistPlayerComponent.KEY.get(player);
                if (hyp != null) {
                    Text uses = Text.translatable("hud.hypnotist.uses_remaining", hyp.usesRemaining, hyp.maxUses);
                    context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, uses, x - (MinecraftClient.getInstance().textRenderer.getWidth(uses)/2), y + 52, Color.MAGENTA.getRGB());
                }
            } catch (Exception ignored) {}
        }
    }

    @Inject(method = "init", at = @At("HEAD"))
    void renderHypnotistHeads(CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        if (gameWorldComponent.isRole(player, Noellesroles.HYPNOTIST)) {
            List<AbstractClientPlayerEntity> entries = MinecraftClient.getInstance().world.getPlayers();
            if (!entries.contains(player)) entries.add(player);
            int apart = 36;
            int x = width / 2 - (entries.size()) * apart / 2 + 9;
            int shouldBeY = (height - 32) / 2;
            int y = shouldBeY + 80;

            for(int i = 0; i < entries.size(); ++i) {
                HypnotistPlayerWidget child = new HypnotistPlayerWidget(
                    ((LimitedInventoryScreen)(Object)this),
                    x + apart * i,
                    y,
                    entries.get(i),
                    i
                );
                addDrawableChild(child);
            }
        }
    }

}
