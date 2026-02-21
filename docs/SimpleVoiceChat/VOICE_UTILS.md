# 语音工具类使用说明

本说明介绍项目中新增的语音管理工具类：`VoiceChatManager` 与 `ModerationConfig`。

- **初始化**：在模组启动时调用

```java
File configDir = FabricLoader.getInstance().getConfigDir().toFile();
VoiceChatManager.init(configDir);
```

- **禁言 / 解除禁言**：在命令或管理逻辑中调用

```java
VoiceChatManager.mutePlayer(targetPlayer, durationMs, "管理员禁言");
VoiceChatManager.unmutePlayer(targetPlayer);
```

- **禁止收听 / 恢复收听**：

```java
VoiceChatManager.deafenPlayer(targetPlayer, durationMs, "管理员操作");
VoiceChatManager.undeafenPlayer(targetPlayer);
```

- **检查状态**：可用于权限判断或 UI 展示

```java
boolean muted = VoiceChatManager.isMuted(player.getUUID());
boolean deaf = VoiceChatManager.isDeafened(player.getUUID());
```

说明：该工具类封装了简单的持久化（使用 `ModerationConfig`，存放在 `config/voicechat_moderation.json`），并尝试在 VoiceChat 连接事件上显示提示信息。要在网络层彻底阻止语音数据转发，需要结合 VoiceChat 提供的 NetManager 或更底层的事件钩子进行实现。
