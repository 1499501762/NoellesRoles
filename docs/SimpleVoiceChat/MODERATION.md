# Simple Voice Chat — 管理与审核指南

本文档面向服务器管理员与模组开发者，介绍如何在 Simple Voice Chat 中实现禁言、禁止收听、解除禁用等管理功能。

**目录**
- [概念说明](#概念说明)
- [权限系统](#权限系统)
- [禁言与禁止收听](#禁言与禁止收听)
- [开发者集成指南](#开发者集成指南)
- [示例实现](#示例实现)

---

## 概念说明

Simple Voice Chat 中涉及的主要管理概念：

| 概念 | 说明 | 实现方式 |
|------|------|---------|
| **禁言（Mute）** | 禁止玩家发送语音数据，其他玩家仍能听到其他声音 | 服务端控制，拒绝处理该玩家的音频包 |
| **禁止收听（Deafen）** | 禁止玩家接收他人的语音数据，但仍能发送 | 客户端过滤或服务端不转发该玩家接收的音频 |
| **禁用语音（Disable Voice）** | 完全禁用玩家的语音功能，禁言+禁收 | 服务端拒绝语音连接或断开已有连接 |
| **临时禁言** | 按时间限制的禁言 | 由管理模组或权限系统支持 |
| **解除禁用** | 恢复玩家的语音功能 | 服务端允许语音连接并恢复音频流 |

---

## 权限系统

### 权限节点

VoiceChat 定义了以下权限节点（通过 Fabric Permissions API）：

```
voicechat.*                       # VoiceChat 的全部权限
voicechat.create_group           # 创建语音群组
voicechat.set_group              # 设置或修改群组配置
voicechat.leave_group            # 离开语音群组
voicechat.add_group              # 添加群组（管理员级别）
voicechat.remove_group           # 删除群组（管理员级别）
voicechat.joined_group           # 列出已加入的群组
```

### 权限检查示例

如果需要在自定义命令或事件处理中检查权限，可使用 `PermissionManager`：

```java
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import net.minecraft.server.level.ServerPlayer;

PermissionManager pm = CommonCompatibilityManager.INSTANCE.createPermissionManager();

// 检查玩家是否有权限创建群组
boolean canCreateGroup = pm.check(player, "voicechat.create_group", 4);
// 权限等级：0（所有人）到 4（仅 OP）
```

---

## 禁言与禁止收听

### 方法 1：通过权限系统（推荐用于集成）

使用权限管理系统（如 LuckPerms、PermissionsEx 等）可以批量管理用户的语音权限。Fabric 的权限 API 允许其他模组检查这些权限。

**示例：自定义权限检查模组**

```java
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import net.minecraft.server.level.ServerPlayer;

// 在模组初始化或命令处理中调用
public class VoiceModerationIntegration {
    
    public static boolean canSpeak(ServerPlayer player) {
        PermissionManager pm = CommonCompatibilityManager.INSTANCE.createPermissionManager();
        // 定义自定义权限节点
        return pm.check(player, "mymod.voicechat.speak", 0);
    }
    
    public static boolean canHear(ServerPlayer player) {
        PermissionManager pm = CommonCompatibilityManager.INSTANCE.createPermissionManager();
        return pm.check(player, "mymod.voicechat.hear", 0);
    }
}
```

### 方法 2：通过服务端事件拦截（进阶）

在语音连接或数据包处理阶段拦截，可实现更细粒度的控制：

**监听玩家语音连接事件**

```java
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VoiceModerationManager {
    
    private static final Set<UUID> mutedPlayers = new HashSet<>();
    private static final Set<UUID> deafenedPlayers = new HashSet<>();
    
    public static void setupEvents() {
        // 监听语音连接
        CommonCompatibilityManager.INSTANCE.onServerVoiceChatConnected((ServerPlayer player) -> {
            if (isMuted(player.getUUID())) {
                // 如果玩家被禁言，可以断开连接或标记为禁音状态
                player.displayClientMessage(
                    Component.literal("§c你已被禁言，无法使用语音聊天"),
                    false
                );
                // 实际禁言需要在网络层或模组层面实现
            }
        });
    }
    
    public static void mutePlayer(UUID playerUUID) {
        mutedPlayers.add(playerUUID);
    }
    
    public static void unmutePlayer(UUID playerUUID) {
        mutedPlayers.remove(playerUUID);
    }
    
    public static void deafenPlayer(UUID playerUUID) {
        deafenedPlayers.add(playerUUID);
    }
    
    public static void undeafen(UUID playerUUID) {
        deafenedPlayers.remove(playerUUID);
    }
    
    public static boolean isMuted(UUID playerUUID) {
        return mutedPlayers.contains(playerUUID);
    }
    
    public static boolean isDeafened(UUID playerUUID) {
        return deafenedPlayers.contains(playerUUID);
    }
}
```

### 方法 3：通过数据包过滤（仅限 Fabric 网络管理）

对于需要访问网络层的高级集成，可通过 `NetManager` 接口进行包过滤：

```java
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.FabricNetManager;
import net.minecraft.resources.Identifier;

NetManager net = CommonCompatibilityManager.INSTANCE.getNetManager();
if (net instanceof FabricNetManager) {
    FabricNetManager fnet = (FabricNetManager) net;
    // 获取 VoiceChat 相关的数据包标识符
    java.util.Set<Identifier> packets = fnet.getPackets();
    // 可在此基础上实现自定义数据包拦截逻辑
}
```

---

## 开发者集成指南

### 创建禁言管理 Mod

如果需要为 VoiceChat 创建完整的禁言/管理模组，建议按以下步骤：

#### 1. 定义配置与存储

```java
import java.util.*;
import java.io.*;
import com.google.gson.*;

public class ModerationConfig {
    
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private File configFile;
    
    public ModerationConfig(File configFile) {
        this.configFile = configFile;
    }
    
    public static class MutedPlayer {
        public UUID uuid;
        public String name;
        public long muteUntil; // 0 = 永久; > 0 = 解除时间戳
        public String reason;
    }
    
    private List<MutedPlayer> mutedList = new ArrayList<>();
    
    public void save() throws IOException {
        String json = gson.toJson(mutedList);
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(json);
        }
    }
    
    public void load() throws IOException {
        if (!configFile.exists()) return;
        try (FileReader reader = new FileReader(configFile)) {
            mutedList = gson.fromJson(reader, 
                new com.google.gson.reflect.TypeToken<List<MutedPlayer>>(){}.getType());
        }
    }
    
    public boolean isMuted(UUID playerUUID) {
        long now = System.currentTimeMillis();
        for (MutedPlayer mp : mutedList) {
            if (mp.uuid.equals(playerUUID)) {
                if (mp.muteUntil == 0 || mp.muteUntil > now) {
                    return true;
                } else {
                    // 禁言已过期，删除
                    mutedList.remove(mp);
                    return false;
                }
            }
        }
        return false;
    }
}
```

#### 2. 集成到 VoiceChat 事件系统

```java
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.events.ServerVoiceChatEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class ModerationModSetup {
    
    private static ModerationConfig config;
    
    public static void init(File configDir) throws IOException {
        config = new ModerationConfig(new File(configDir, "voicechat_moderation.json"));
        config.load();
        
        // 监听玩家语音连接
        CommonCompatibilityManager.INSTANCE.onServerVoiceChatConnected((ServerPlayer player) -> {
            if (config.isMuted(player.getUUID())) {
                player.displayClientMessage(
                    Component.literal("§c你已被禁言"),
                    false
                );
                // 此处可添加逻辑断开语音连接或标记为受限状态
            }
        });
        
        // 定期保存配置（例如每 5 分钟）
        // 由主模组的定时任务调用 config.save()
    }
    
    public static void mutePlayer(ServerPlayer player, long durationMs, String reason) throws IOException {
        ModerationConfig.MutedPlayer mp = new ModerationConfig.MutedPlayer();
        mp.uuid = player.getUUID();
        mp.name = player.getName().getString();
        mp.muteUntil = durationMs == 0 ? 0 : System.currentTimeMillis() + durationMs;
        mp.reason = reason;
        
        config.mutedList.add(mp);
        config.save();
        
        player.displayClientMessage(
            Component.literal("§c你已被禁言：" + reason),
            false
        );
    }
}
```

#### 3. 注册管理命令

```java
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class ModerationCommands {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /voicemute <玩家> <时长(秒)> [原因]
        dispatcher.register(
            Commands.literal("voicemute")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("duration", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                            String duration = StringArgumentType.getString(ctx, "duration");
                            long ms = Long.parseLong(duration) * 1000L;
                            
                            try {
                                ModerationModSetup.mutePlayer(target, ms, "管理员禁言");
                                ctx.getSource().sendSuccess(
                                    () -> Component.literal("§a已禁言 " + target.getName().getString()),
                                    true
                                );
                            } catch (IOException e) {
                                ctx.getSource().sendFailure(Component.literal("§c保存失败：" + e.getMessage()));
                            }
                            return 1;
                        })
                    )
                )
        );
        
        // /voiceunmute <玩家>
        dispatcher.register(
            Commands.literal("voiceunmute")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> {
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        // 从禁言列表移除
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("§a已解除禁言：" + target.getName().getString()),
                            true
                        );
                        return 1;
                    })
                )
        );
    }
}
```

#### 4. 在主模组中注册命令

```java
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public class MyModerationMod {
    
    public static void init() {
        // 注册命令
        CommonCompatibilityManager.INSTANCE.onRegisterServerCommands(
            (CommandDispatcher<CommandSourceStack> dispatcher) -> {
                ModerationCommands.register(dispatcher);
            }
        );
    }
}
```

---

## 示例实现

### 完整的禁言模组示例（fabric.mod.json）

```json
{
  "schemaVersion": 1,
  "id": "voicechat-moderation",
  "version": "1.0.0",
  "name": "Voice Chat Moderation",
  "description": "VoiceChat 的禁言与审核管理模组",
  "authors": ["Your Name"],
  "contact": {
    "sources": "https://github.com/yourname/voicechat-moderation"
  },
  "license": "MIT",
  "environment": "*",
  "entrypoints": {
    "main": ["com.example.voicechat_moderation.VoiceChatModerationMod"]
  },
  "mixins": [],
  "depends": {
    "fabricloader": ">=0.14.0",
    "minecraft": "1.20",
    "voicechat": "*"
  }
}
```

### 主入口类

```java
package com.example.voicechat_moderation;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;

public class VoiceChatModerationMod implements ModInitializer {
    
    @Override
    public void onInitialize() {
        try {
            ModerationModSetup.init(FabricLoader.getInstance().getConfigDir().toFile());
            MyModerationMod.init();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Voice Chat Moderation", e);
        }
    }
}
```

---

## 故障排查与最佳实践

### 常见问题

**Q: 禁言后玩家仍能发送音频**
- A: 需要在服务端网络处理层面拦截，或通过权限系统禁用权限后由 VoiceChat 检查。检查事件监听是否正确挂接。

**Q: 禁言配置没有持久化**
- A: 确保在每次修改后调用 `config.save()`，且文件路径正确。可添加日志输出调试。

**Q: 命令无法执行**
- A: 检查权限等级（`hasPermission(2)` 表示需要管理员权限），或改为 `hasPermission(0)` 以允许所有玩家使用。

### 最佳实践

1. **使用权限系统**：优先使用 Fabric Permissions API 以与其他权限系统兼容
2. **持久化存储**：对禁言列表进行序列化与定期保存
3. **时间管理**：实现临时禁言与自动过期机制
4. **日志记录**：记录所有禁言、解禁操作，便于审计
5. **配置文件**：支持外部配置文件以便管理员调整

---

## 参考资源

- [Simple Voice Chat 集成指南](INTEGRATION.md)
- [Fabric Permissions API](https://github.com/lucko/fabric-permissions-api)
- [Brigadier 命令系统](https://github.com/Mojang/brigadier)
- [Fabric Wiki - Events](https://fabricmc.net/wiki/tutorial:events)

---

如有关于特定禁言场景的问题，欢迎反馈。我们可以补充更多的实现示例或适配流行的权限管理系统。
