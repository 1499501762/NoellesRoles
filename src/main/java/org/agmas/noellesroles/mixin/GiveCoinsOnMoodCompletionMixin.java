package org.agmas.noellesroles.mixin;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.client.gui.StoreRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.agmas.noellesroles.Noellesroles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerMoodComponent.class)
public abstract class GiveCoinsOnMoodCompletionMixin {

    @Shadow public abstract float getMood();

    @Shadow @Final private PlayerEntity player;

    @Inject(method = "setMood", at = @At("HEAD"))
    void giveCoinsForMood(float mood, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld());
        // 仅在 mood 增加来源不是 DJ 时才发放金币（DJ 技能恢复不应触发奖励）
        String src = Noellesroles.PLAYER_MOOD_CHANGE_SOURCE.get(player.getUuid());
        if ("DJ".equals(src)) return;
        if (mood > getMood()) {
            Role role = gameWorldComponent.getRole(player);
            if (role != null && role.getMoodType().equals(Role.MoodType.REAL)) {
                if (gameWorldComponent.isRole(player, Noellesroles.MIMIC) || gameWorldComponent.isRole(player, Noellesroles.PICKPOCKET)) return;
                PlayerShopComponent shopComponent = PlayerShopComponent.KEY.get(player);
                shopComponent.addToBalance(50);
            }
        }
    }
}
