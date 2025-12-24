package org.agmas.noellesroles.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.noellesroles.Noellesroles;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Entity.class, priority = 1005)
public abstract class DoNotCollideWithFeatherMixin {


    @WrapMethod(method = "collidesWith")
    boolean doNotCollideWithPlayers(Entity other, Operation<Boolean> original) {
        Entity self = (Entity)(Object)this;
        if (other instanceof PlayerEntity player && self instanceof PlayerEntity player2) {
            WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(player.getWorld());
            if (worldModifierComponent.isModifier(player, Noellesroles.FEATHER) || worldModifierComponent.isModifier(player2, Noellesroles.FEATHER)) {
                return false;
            }
        }
        return original.call(other);
    }
}
