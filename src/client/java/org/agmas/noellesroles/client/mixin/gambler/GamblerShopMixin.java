package org.agmas.noellesroles.client.mixin.gambler;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.util.ShopEntry;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.ModItems;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(LimitedInventoryScreen.class)
public abstract class GamblerShopMixin extends LimitedHandledScreen<PlayerScreenHandler> {
    @Shadow
    @Final
    public ClientPlayerEntity player;

    public GamblerShopMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    void gamblerShopRenderer(CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        if (gameWorldComponent.isRole(player, Noellesroles.GAMBLER)) {
            List<ShopEntry> entries = new ArrayList<>();
            entries.add(new ShopEntry(ModItems.JAMMED_REVOLVER.getDefaultStack(), 800, ShopEntry.Type.WEAPON));

            int apart = 36;
            int x = width / 2 - (entries.size()) * apart / 2 + 9;
            int shouldBeY = (((LimitedInventoryScreen)(Object)this).height - 32) / 2;
            int y = shouldBeY - 46;

            for(int i = 0; i < entries.size(); ++i) {
                ShopEntry desired = entries.get(i);
                int serverIndex = -1;
                for (int si = 0; si < GameConstants.SHOP_ENTRIES.size(); si++) {
                    ShopEntry se = GameConstants.SHOP_ENTRIES.get(si);
                    if (se.stack().getItem() == desired.stack().getItem()) {
                        serverIndex = si;
                        break;
                    }
                }
                int widgetIndex = serverIndex >= 0 ? serverIndex : i;
                addDrawableChild(new LimitedInventoryScreen.StoreItemWidget((LimitedInventoryScreen) (Object)this, x + apart * i, y, desired, widgetIndex));
            }
        }
    }

}
