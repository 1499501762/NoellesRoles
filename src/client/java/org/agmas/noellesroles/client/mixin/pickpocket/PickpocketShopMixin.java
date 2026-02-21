package org.agmas.noellesroles.client.mixin.pickpocket;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.util.ShopEntry;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.agmas.noellesroles.Noellesroles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(LimitedInventoryScreen.class)
public abstract class PickpocketShopMixin extends LimitedHandledScreen<PlayerScreenHandler> {
    @Shadow
    @Final
    public ClientPlayerEntity player;

    public PickpocketShopMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    void pickpocketShopRenderer(CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        if (gameWorldComponent.isRole(player, Noellesroles.PICKPOCKET)) {
            // Desired items to show for Pickpocket (in display order)
            List<ShopEntry> entries = new ArrayList<>();
            entries.add(new ShopEntry(WatheItems.LOCKPICK.getDefaultStack(), 50, ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.CROWBAR.getDefaultStack(), 25, ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.BLACKOUT.getDefaultStack(), 200, ShopEntry.Type.TOOL));
            entries.add(new ShopEntry(WatheItems.REVOLVER.getDefaultStack(), 300, ShopEntry.Type.WEAPON));

            int apart = 36;
            int x = width / 2 - (entries.size()) * apart / 2 + 9;
            int shouldBeY = (((LimitedInventoryScreen)(Object)this).height - 32) / 2;
            int y = shouldBeY - 46;

            for(int i = 0; i < entries.size(); ++i) {
                ShopEntry desired = entries.get(i);
                // try to find the server index for this item in GameConstants.SHOP_ENTRIES
                int serverIndex = -1;
                for (int si = 0; si < GameConstants.SHOP_ENTRIES.size(); si++) {
                    ShopEntry se = GameConstants.SHOP_ENTRIES.get(si);
                    if (se.stack().getItem() == desired.stack().getItem()) {
                        serverIndex = si;
                        break;
                    }
                }
                // fallback to local index if not found (should be rare)
                int widgetIndex = serverIndex >= 0 ? serverIndex : i;
                addDrawableChild(new LimitedInventoryScreen.StoreItemWidget((LimitedInventoryScreen) (Object)this, x + apart * i, y, desired, widgetIndex));
            }
        }
    }

}
