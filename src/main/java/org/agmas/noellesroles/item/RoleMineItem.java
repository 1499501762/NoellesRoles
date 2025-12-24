package org.agmas.noellesroles.item;

import dev.doctor4t.wathe.entity.FirecrackerEntity;
import dev.doctor4t.wathe.index.WatheEntities;
import dev.doctor4t.wathe.util.AdventureUsable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.agmas.noellesroles.NoellesRolesEntities;
import org.agmas.noellesroles.entities.RoleMineEntity;
import org.jetbrains.annotations.NotNull;

public class RoleMineItem extends Item implements AdventureUsable {
    public RoleMineItem(Settings settings) {
        super(settings);
    }

    public ActionResult useOnBlock(@NotNull ItemUsageContext context) {
        if (context.getSide().equals(Direction.UP) || context.getSide().equals(Direction.DOWN)) {
            PlayerEntity player = context.getPlayer();
            World world = player.getWorld();
            if (!world.isClient) {
                RoleMineEntity roleMineEntity = NoellesRolesEntities.ROLE_MINE_ENTITY_ENTITY_TYPE.create(world);
                Vec3d spawnPos = context.getHitPos();
                roleMineEntity.setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                roleMineEntity.setYaw(player.getHeadYaw());
                roleMineEntity.owner = player.getUuid();
                world.spawnEntity(roleMineEntity);
                if (!player.isCreative()) {
                    player.getStackInHand(context.getHand()).decrement(1);
                }
            }

            return ActionResult.SUCCESS;
        } else {
            return ActionResult.PASS;
        }
    }
}