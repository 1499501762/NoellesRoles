package org.agmas.noellesroles;

import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.tag.WatheItemTags;
import dev.doctor4t.wathe.item.RevolverItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.item.FakeKnifeItem;
import org.agmas.noellesroles.item.RoleMineItem;

public class ModItems {
    public static void init() {
        GameConstants.ITEM_COOLDOWNS.put(FAKE_REVOLVER, GameConstants.getInTicks(0,8));
        // GameConstants.ITEM_COOLDOWNS.put(SNIPER_TRACKER, NoellesRolesConfig.HANDLER.instance().sniperCooldownTicks);
    }

    public static final Item FAKE_KNIFE = register(
            new FakeKnifeItem(new Item.Settings().maxCount(1)),
            "fake_knife"
    );
    public static final Item FAKE_REVOLVER = register(
            new RevolverItem(new Item.Settings().maxCount(1)),
            "fake_revolver"
    );
    public static final Item MASTER_KEY = register(
            new Item(new Item.Settings().maxCount(1)),
            "master_key"
    );
    public static final Item DELUSION_VIAL = register(
            new Item(new Item.Settings().maxCount(1)),
            "delusion_vial"
    );
    public static final Item DEFENSE_VIAL = register(
            new Item(new Item.Settings().maxCount(1)),
            "defense_vial"
    );
    public static final Item ROLE_MINE = register(
            new RoleMineItem(new Item.Settings().maxCount(1)),
            "role_mine"
    );
//     public static final Item SNIPER_TRACKER = register(
//             new Item(new Item.Settings().maxCount(1)),
//             "sniper_tracker"
//     );
//     public static final Item SNIPER_RIFLE = register(
//             new Item(new Item.Settings().maxCount(1)),
//             "sniper_rifle"
//     );
    public static Item register(Item item, String id) {
        // Create the identifier for the item.
        Identifier itemID = Identifier.of(Noellesroles.MOD_ID, id);

        // Register the item.
        Item registeredItem = Registry.register(Registries.ITEM, itemID, item);

        // Return the registered item!
        return registeredItem;
    }

}
