package org.agmas.noellesroles.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerEntity.class)
public interface SprintingTicksAccessor {
    @Accessor("sprintingTicks")
    float getSprintingTicks();

    @Accessor("sprintingTicks")
    void setSprintingTicks(float value);
}
