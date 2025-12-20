package org.agmas.noellesroles.util;

import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.UUID;

/**
 * PlayerBody 工厂：以 PlayerEntity 为入口，从 player 获取 server/world。
 */
public final class PlayerBodyCreater {
    private PlayerBodyCreater() {}

    private static volatile net.minecraft.entity.EntityType<PlayerBodyEntity> ENTITY_TYPE = null;

    public static void setEntityType(net.minecraft.entity.EntityType<PlayerBodyEntity> type) {
        ENTITY_TYPE = type;
    }

    private static net.minecraft.entity.EntityType<PlayerBodyEntity> requireType() {
        if (ENTITY_TYPE == null) {
            throw new IllegalStateException("PlayerBodyEntity type not set. Call PlayerBodyFactory.setEntityType(...) first.");
        }
        return ENTITY_TYPE;
    }

    /**
     * 在不需要手动传入 world/server 的情况下，基于 player 创建但不 spawn 到世界（返回未加入世界的实体）。
     */
    public static PlayerBodyEntity createFromPlayer(PlayerEntity player, double x, double y, double z, UUID playerUuid) {
        if (player == null) return null;
        if (player.getWorld().isClient) return null;
        ServerWorld world = (ServerWorld) player.getWorld();
        net.minecraft.entity.EntityType<PlayerBodyEntity> type = requireType();
        PlayerBodyEntity body = type.create(world);
        if (body == null) return null;

        body.refreshPositionAndAngles(x, y, z, 0f, 0f);
        if (playerUuid != null) body.setPlayerUuid(playerUuid);
        body.setInvulnerable(true);
        return body;
    }

    /**
     * 以 player 为入口创建并 spawn 到 player 所在世界（主线程直接调用）。
     */
    public static PlayerBodyEntity spawnFromPlayer(PlayerEntity player, double x, double y, double z, UUID playerUuid) {
        PlayerBodyEntity body = createFromPlayer(player, x, y, z, playerUuid);
        if (body == null) return null;
        ServerWorld world = (ServerWorld) player.getWorld();
        world.spawnEntity(body);
        return body;
    }

    /**
     * 以 player 为模板创建并 spawn 到 player 所在世界。
     * copyRotation/copyCustomName/copyEquipment 可为 null（按 false 处理）。
     */
    public static PlayerBodyEntity spawnFromPlayerTemplate(PlayerEntity player,
                                                           Boolean copyRotation, Boolean copyCustomName, Boolean copyEquipment) {
        if (player == null) return null;
        if (player.getWorld().isClient) return null;

        boolean rot = !Boolean.FALSE.equals(copyRotation);
        boolean name = !Boolean.FALSE.equals(copyCustomName);
        boolean equip = Boolean.TRUE.equals(copyEquipment);

        ServerWorld world = (ServerWorld) player.getWorld();
        PlayerBodyEntity body = requireType().create(world);
        if (body == null) return null;

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        float yaw = rot ? player.getYaw() : 0f;
        float pitch = rot ? player.getPitch() : 0f;
        body.refreshPositionAndAngles(x, y, z, yaw, pitch);

        body.setPlayerUuid(player.getUuid());

        if (name) {
            if (player.hasCustomName()) {
                body.setCustomName(player.getCustomName().copy());
                body.setCustomNameVisible(player.isCustomNameVisible());
            } else {
                body.setCustomName(Text.of(player.getName().getString()));
            }
        }

        if (equip) {
            try {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ItemStack stack = player.getEquippedStack(slot);
                    if (stack != null && !stack.isEmpty()) {
                        body.equipStack(slot, stack.copy());
                    }
                }
            } catch (Throwable ignored) {
                // 容错：避免因不同版本/实现导致崩服
            }
        }

        body.setInvulnerable(true);
        world.spawnEntity(body);
        return body;
    }

    /**
     * 线程安全版本：若当前不在主线程，会通过 player.getServer() 排队到主线程执行（无返回值）。
     */
    public static void spawnFromPlayerTemplateSafe(PlayerEntity player,
                                                   Boolean copyRotation, Boolean copyCustomName, Boolean copyEquipment) {
        if (player == null) return;
        if (player.getWorld().isClient) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;
        server.execute(() -> spawnFromPlayerTemplate(player, copyRotation, copyCustomName, copyEquipment));
    }

    /**
     * 线程安全并带回调版本：回调在主线程被调用并接收生成的实体（若为 null 则表示失败）。
     */
    public static void spawnFromPlayerTemplateSafeWithCallback(PlayerEntity player,
                                                               Boolean copyRotation, Boolean copyCustomName, Boolean copyEquipment,
                                                               Consumer<PlayerBodyEntity> callback) {
        if (player == null) {
            if (callback != null) callback.accept(null);
            return;
        }
        if (player.getWorld().isClient) {
            if (callback != null) callback.accept(null);
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            if (callback != null) callback.accept(null);
            return;
        }
        server.execute(() -> {
            PlayerBodyEntity body = spawnFromPlayerTemplate(player, copyRotation, copyCustomName, copyEquipment);
            if (callback != null) callback.accept(body);
        });
    }
}