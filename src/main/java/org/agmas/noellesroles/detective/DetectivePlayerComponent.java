package org.agmas.noellesroles.detective;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

// Identifier 类型不再直接作为字段或参数使用

import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DetectivePlayerComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<DetectivePlayerComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(Noellesroles.MOD_ID, "detective"), DetectivePlayerComponent.class);
    private final PlayerEntity player;

    /**
     * 警探身份记录：每条记录包含玩家UUID、身份字符串、以及是否已被猜测。
     */
    public static class GuessInfo {
        public final UUID uuid;
        public String identity; // e.g. "civilian"
        public boolean guessed; // 是否已经被猜测

        public GuessInfo(UUID uuid, String identity, boolean guessed) {
            this.uuid = uuid;
            this.identity = identity;
            this.guessed = guessed;
        }
    }

    /** 存储全局的身份列表（面向警探的记录） */
    private final List<GuessInfo> guessList = new ArrayList<>();

    /** 剩余查询次数 */
    public int detectRemaining;

    /** 最大查询次数（根据玩家人数计算，ratio=20%） */
    public int maxDetect;

    public DetectivePlayerComponent(PlayerEntity player) {
        this.player = player;
    }

    /** 重置状态，在回合开始或角色重置时调用 */
    public void reset(int playerCount) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        // 初始化/重置身份列表：记录自身身份为已知且已被猜测
        String nameString = gameWorldComponent.getRole(player.getUuid()).identifier().getPath();
        String selfIdentity = gameWorldComponent.getRole(player.getUuid()).identifier() != null
            ? Text.translatable("announcement.role." + nameString).getString()
            : "???";
        this.guessList.clear();
        this.guessList.add(new GuessInfo(this.player.getUuid(), selfIdentity, true));
        double ratio = NoellesRolesConfig.HANDLER.instance().detectiveAbilityRatio;
        this.maxDetect = Math.max(1, (int) Math.floor(playerCount * ratio));
        this.detectRemaining = this.maxDetect;
        this.sync();
    }

    /** 同步到客户端 */
    public void sync() {
        KEY.sync(this.player);
    }

    public int getDetectRemaining() {
        return this.detectRemaining;
    }

    public boolean hasDetectRemaining() {
        return this.detectRemaining > 0 ;
    }

    /**
     * 新增或更新身份记录。
     * @param uuid 玩家UUID
     * @param identity 身份字符串（如 "civilian"），为空则按"???"
     * @param guessed 是否已被猜测
    */
    public void addOrUpdateGuess(UUID uuid, String identity, boolean guessed) {
        String value = (identity == null || identity.isEmpty()) ? "???" : identity;
        for (GuessInfo info : this.guessList) {
            if (info.uuid.equals(uuid)) {
                info.identity = value;
                info.guessed = guessed;
                this.sync();
                return;
            }
        }
        this.guessList.add(new GuessInfo(uuid, value, guessed));
        this.sync();
    }

    /** 标记某UUID已被猜测 */
    public void markGuessed(UUID uuid) {
        for (GuessInfo info : this.guessList) {
            if (info.uuid.equals(uuid)) {
                info.guessed = true;
                this.sync();
                return;
            }
        }
    }

    /**
     * 通过UUID查询身份，不存在则返回 "???"。
     */
    public String getIdentityByUUID(UUID uuid) {
        for (GuessInfo info : this.guessList) {
            if (info.uuid.equals(uuid)) {
                return (info.identity == null || info.identity.isEmpty()) ? "???" : info.identity;
            }
        }
        return "???";
    }

    /** 查询某UUID是否已被猜测 */
    public boolean isGuessed(UUID uuid) {
        for (GuessInfo info : this.guessList) {
            if (info.uuid.equals(uuid)) {
                return info.guessed;
            }
        }
        return false;
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        // 仅序列化警探身份列表
        // 序列化警探身份列表
        NbtList list = new NbtList();
        for (GuessInfo info : this.guessList) {
            NbtCompound c = new NbtCompound();
            if (info.uuid != null) {
                c.putUuid("uuid", info.uuid);
            }
            c.putString("identity", (info.identity == null || info.identity.isEmpty()) ? "???" : info.identity);
            c.putBoolean("guessed", info.guessed);
            list.add(c);
        }
        tag.put("guessList", list);
        tag.putInt("detectRemaining", this.detectRemaining);
        tag.putInt("maxDetect", this.maxDetect);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        // 反序列化警探身份列表
        this.guessList.clear();
        if (tag.contains("guessList", NbtElement.LIST_TYPE)) {
            NbtList list = tag.getList("guessList", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                NbtCompound c = list.getCompound(i);
                UUID uuid = c.containsUuid("uuid") ? c.getUuid("uuid") : null;
                String identity = c.getString("identity");
                boolean guessed = c.getBoolean("guessed");
                if (uuid != null) {
                    this.guessList.add(new GuessInfo(uuid, (identity == null || identity.isEmpty()) ? "???" : identity, guessed));
                }
            }
        }
        this.detectRemaining = tag.getInt("detectRemaining");
        this.maxDetect = tag.getInt("maxDetect");
    }

    @Override
    public void serverTick() {
        this.sync();
    }

    @Override
    public void clientTick() {
    }

}
