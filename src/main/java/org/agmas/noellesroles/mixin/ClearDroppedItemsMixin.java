package org.agmas.noellesroles.mixin;

import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 在每局游戏初始化时清空所有凋落物（ItemEntity）
 */
@Mixin(GameFunctions.class)
public abstract class ClearDroppedItemsMixin {
    
    @Unique
    private static final Map<ServerWorld, Integer> resetCountMap = new HashMap<>();

    /**
     * 在 resetPlayer 方法开始时注入，清空凋落物
     */
    @Inject(method = "resetPlayer", at = @At("HEAD"))
    private static void clearDroppedItems(ServerPlayerEntity player, CallbackInfo ci) {
        ServerWorld world = player.getServerWorld();
        int count = resetCountMap.getOrDefault(world, 0);
        
        // 只在第一次重置时清空凋落物
        if (count == 0) {
            // 获取世界中所有的物品实体并移除
            List<? extends ItemEntity> itemEntities = world.getEntitiesByType(
                net.minecraft.entity.EntityType.ITEM,
                entity -> true
            );
            
            // 移除所有物品实体
            for (ItemEntity itemEntity : itemEntities) {
                itemEntity.discard();
            }
        }
        
        // 增加重置计数
        resetCountMap.put(world, count + 1);
    }
    
    /**
     * 在玩家重置完成后，减少计数器
     */
    @Inject(method = "resetPlayer", at = @At("TAIL"))
    private static void decreaseCounter(ServerPlayerEntity player, CallbackInfo ci) {
        ServerWorld world = player.getServerWorld();
        int count = resetCountMap.getOrDefault(world, 0);
        
        // 减少计数，当所有玩家都处理完后，重置为0
        count--;
        if (count <= 0) {
            resetCountMap.remove(world);
        } else {
            resetCountMap.put(world, count);
        }
    }
}
