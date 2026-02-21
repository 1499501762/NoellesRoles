**哑巴（Dumb）实现说明**

- **概述**: 哑巴是一个词条（modifier），用于禁止玩家使用语音聊天功能。典型用途为商店道具或服务器管理对玩家短期禁言。

- **核心行为**: 当玩家被标记为“哑巴”时：
  - 服务器端记录状态并持久化，客户端会收到状态同步。
  - 由 `VoiceChatManager` 在接收语音包时拦截并阻止该玩家发送语音；同时向玩家显示提示信息。

- **主要代码位置**:
  - **组件**: [src/main/java/org/agmas/noellesroles/dumb/DumbPlayerComponent.java](src/main/java/org/agmas/noellesroles/dumb/DumbPlayerComponent.java#L1-L200)
    - 组件实现 `AutoSyncedComponent`，包含 `isDumb` 字段、NBT 读写、`setDumb(boolean)`、`reset()` 和 `sync()` 方法。
  - **语音管理**: [src/main/java/org/agmas/noellesroles/voice/VoiceChatManager.java](src/main/java/org/agmas/noellesroles/voice/VoiceChatManager.java#L1-L200)
    - `isMuted(...)` 与 `mutePlayer/unmutePlayer(...)` 用于配置和持久化静音（复用或配合哑巴词条）。
  - **语音插件接入点**: [src/main/java/org/agmas/noellesroles/voice/NoellesrolesVoiceChatPlugin.java](src/main/java/org/agmas/noellesroles/voice/NoellesrolesVoiceChatPlugin.java#L1-L200)
    - 在收到语音数据时检查玩家是否具有哑巴 modifier，并阻止发送。
  - **本地化/提示**: [src/main/resources/assets/noellesroles/lang/zh_cn.json](src/main/resources/assets/noellesroles/lang/zh_cn.json#L140-L160)
    - 包含 `message.noellesroles.voice.muted_blocked`、`message.noellesroles.voice.muted`、`message.noellesroles.voice.unmuted` 等键。

- **工作流与交互**:
  1. 触发方（如商店购买、管理员命令或能力效果）调用服务器逻辑，获取目标 `ServerPlayerEntity`。
  2. 在服务器上调用 `DumbPlayerComponent.KEY.get(target).setDumb(true)` 并持久化（组件会在 `writeToNbt` 中保存 `isDumb`）。
  3. 组件通过 CCA 的自动同步将状态同步到客户端，必要时服务器也会向目标发送 action-bar 提示：`Text.translatable("message.noellesroles.voice.muted", reason)`。
  4. `NoellesrolesVoiceChatPlugin` 在处理语音数据时会调用 `DumbPlayerComponent.KEY` 或 `VoiceChatManager.isMuted(uuid)` 判断并阻止语音转发，同时向发送者显示 `message.noellesroles.voice.muted_blocked`。

- **持久化与生命周期**:
  - `DumbPlayerComponent` 使用 NBT 字段 `isDumb` 存储在玩家数据中，会随玩家数据保存。组件 `reset()` 在必要时（如新回合）清除状态。
  - 若需要短期禁言（例如若干秒/分钟），推荐结合 `VoiceChatManager.mutePlayer(...)` 使用带时长的记录（`MutedPlayer.muteUntil`）。

- **配置与兼容性**:
  - 配置开关在 `run/config/noellesroles.json5` 中可见（例如 `muteIncompatibleWithThief` 等），需在加载时确保与 YetAnotherConfigLib 的字段一致以避免警告。
  - 与小偷（Thief）等词条冲突在角色/词条注册时通过 `RoleModifierRegistry` 进行检查与拒绝分配。

- **本地化**:
  - 主要提示键（示例）:
    - `message.noellesroles.voice.muted_blocked`：当被哑巴的玩家尝试发语音时显示短提示（action-bar）。
    - `message.noellesroles.voice.muted`：当玩家被静音（带原因）时通知。
    - `message.noellesroles.voice.unmuted`：解除静音通知。
  - 这些键已存在于 [src/main/resources/assets/noellesroles/lang/zh_cn.json](src/main/resources/assets/noellesroles/lang/zh_cn.json#L150-L156) 和对应的 `en_us.json`。

- **测试建议**:
  - 单元/集成测试：模拟 `ServerPlayerEntity`，设置 `DumbPlayerComponent` 为 `true`，并断言 `NoellesrolesVoiceChatPlugin` 拦截语音数据路径。
  - 运行时验证：在多人联机场景中由管理员给一名玩家设置哑巴（或通过商店购买道具触发），让该玩家尝试发语音，验证：
    - 语音未被广播给其他客户端；
    - 发送者收到 `message.noellesroles.voice.muted_blocked`；
    - 若使用带时长静音，时长到期后语音可恢复并收到 `message.noellesroles.voice.unmuted`。

- **扩展建议**:
  - 支持文本聊天与语音分开控制（目前哑巴用于语音；可扩展为同时拦截聊天或限制命令）。
  - 提供管理命令和 UI（例如 `/mute <player> <seconds> [reason]` 或服务器管理面板）。
  - 在玩家信息面板显示“已被禁言”图标，便于管理员快速查看。

---
如需我把这份文档合并到 `docs/角色与词条设计.md` 的相应章节或生成英文版本，我可以继续处理。
