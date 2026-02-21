package org.agmas.noellesroles.hypnotist;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.util.Identifier;

/**
 * Optional component to track whether a Hypnotist has used their hypnosis this game.
 */
public class HypnotistPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<HypnotistPlayerComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(Noellesroles.MOD_ID, "hypnotist"), HypnotistPlayerComponent.class);

    private final PlayerEntity player;
    /** 剩余可用次数（本局） */
    public int usesRemaining = 0;
    /** 最大可用次数（按玩家数 20% 计算，至少 1） */
    public int maxUses = 0;

    public HypnotistPlayerComponent(PlayerEntity player) {
        this.player = player;
    }

    /**
     * 在回合开始或角色初始化时调用。按玩家数 20% 计算最大次数并重置剩余次数。
     */
    public void reset(int playerCount) {
        this.maxUses = Math.max(1, (int) Math.floor(playerCount * 0.2f));
        this.usesRemaining = this.maxUses;
        this.sync();
    }

    public void sync() { KEY.sync(this.player); }

    @Override
    public void serverTick() {
        // no-op
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("usesRemaining", this.usesRemaining);
        tag.putInt("maxUses", this.maxUses);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.usesRemaining = tag.contains("usesRemaining") ? tag.getInt("usesRemaining") : 0;
        this.maxUses = tag.contains("maxUses") ? tag.getInt("maxUses") : this.maxUses;
    }

    public boolean hasUsesRemaining() { return this.usesRemaining > 0; }
    public void consumeUse() { if (this.usesRemaining > 0) this.usesRemaining--; }
}
