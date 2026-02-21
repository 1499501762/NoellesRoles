# 催眠师（Hypnotist）实现说明

概述
- 角色：催眠师（Killer-style modded role）
- 核心机制：对一名当前理智（SAN）低于一半（< 0.5）的玩家施放“催眠”；若目标在 30 秒（600 ticks）内理智仍然低于一半，则将该玩家随机转换为一名杀手角色并公告全局。技能每局可用次数限制为当前对局玩家数的 20%（向下取整，至少 1 次）。

触发与网络
- 客户端通过 `HypnotistPlayerWidget` 在界面上点击目标头像，立即发送 `HypnotistC2SPacket(UUID target)` 到服务端。
- 包格式与注册：参见 `src/main/java/org/agmas/noellesroles/packet/HypnotistC2SPacket.java`，在 `Noellesroles.onInitialize()` 中通过 `PayloadTypeRegistry.playC2S().register(...)` 注册 codec。

服务端校验流程（必须全部在服务端完成）
1. 接收包时验证触发者身份：`GameWorldComponent.KEY.get(player).isRole(player, Noellesroles.HYPNOTIST)`。
2. 验证触发者处于存活且可行动状态：`GameFunctions.isPlayerAliveAndSurvival(context.player())`。
3. 验证目标存在且在线：使用 `context.player().getWorld().getPlayerByUuid(payload.player())` 并强制转换为 `ServerPlayerEntity`。
4. 验证目标当前心情组件存在且理智低于一半：`PlayerMoodComponent.KEY.get(target).getMood() < 0.5f`。若不满足，给触发者 `message.hypnotist.fail.not_zero` 提示并返回。
5. 可选：检查 `HypnotistPlayerComponent.KEY` 的 `usesRemaining`/`maxUses` 字段（若启用组件）。在角色初始化或回合重置时通过 `reset(playerCount)` 计算本局上限 `max(1, floor(playerCount * 0.2))` 并重置 `usesRemaining`；触发时先判断 `hasUsesRemaining()`，再 `consumeUse()` 扣减（不退款）。

延迟转换与随机选择
- 服务端会使用调度器（项目内 `Scheduler.schedule(Runnable, ticks)`）在 600 ticks（30s）后执行验证回调：
  - 再次验证目标仍然存活且理智未回升（`PlayerMoodComponent.getMood() < 0.5f`）。
  - 若满足，则从允许的“杀手类角色”池中随机挑选一名（过滤掉不允许作为 killer 的角色与被禁用的角色），调用 `GameWorldComponent.addRole(target, chosenRole)` 完成转换并触发 `ModdedRoleAssigned.EVENT`。
  - 向全服发送 `message.hypnotist.announcement`（包含被催眠玩家名字），并给目标与催眠者分别发送提示与音效/粒子（实现处可扩展）。
  - 若目标在等待期间死亡或理智回升，取消转换并向催眠者发送 `message.hypnotist.fail.recovered`。

冷却与记录
- 使用现成的 `AbilityPlayerComponent.cooldown` 字段为催眠者设置冷却（当前实现为 `GameConstants.getInTicks(2,0)`，即 2 分钟）。
-- 可选持久化：通过新增的 `HypnotistPlayerComponent`（`usesUsed`）记录本局已使用次数；组件实现了 CCA 的自动同步与 NBT 序列化。使用限制逻辑在服务端执行，计算方式为 `max(1, floor(playerCount * 0.2))`。

安全性与边界条件
- 所有角色修改必须在服务端执行，且不能信任客户端传来的任何额外数据。
- 在延迟回调时需再次验证目标的存活性与理智状态，确保并发场景（目标被其他逻辑影响或死亡）能安全回滚。
- 可以根据设计要求额外限制为不可转换某些特殊角色（例如：已标记为不可替换的内置角色），目前实现通过过滤 `HarpyModLoaderConfig.HANDLER.instance().disabled` 与 `Harpymodloader.VANNILA_ROLES`。

本地化键
- HUD 文本：`hud.hypnotist.player_selection`（客户端 mixin 渲染）
- 目标提示：`message.hypnotist.notice`（目标被转换时的 action-bar/聊天提示）
- 失败/成功提示：`message.hypnotist.fail.not_zero`、`message.hypnotist.fail.already_used`、`message.hypnotist.fail.recovered`、`message.hypnotist.success`
- 公告：`message.hypnotist.announcement`（向全体玩家显示）

相关文件参考
- `src/main/java/org/agmas/noellesroles/packet/HypnotistC2SPacket.java`
- `src/client/java/org/agmas/noellesroles/client/ui/HypnotistPlayerWidget.java`
- `src/client/java/org/agmas/noellesroles/client/mixin/hypnotist/HypnotistScreenMixin.java`
- `src/main/java/org/agmas/noellesroles/hypnotist/HypnotistPlayerComponent.java`（可选）
- `src/main/java/org/agmas/noellesroles/Noellesroles.java`（注册与服务端接收器逻辑）

测试建议
1. 运行本地客户端/服务器：
```powershell
./gradlew runServer
./gradlew runClient
```
2. 在单局中分配 `Hypnotist` 角色给测试玩家，确保目标玩家的 `PlayerMoodComponent` 理智值为 0。
3. 打开背包界面（或触发 mixin 的目标选择界面），点击目标头像，观察服务端日志与双方消息：确认在 30 秒后目标转换为杀手并在全局公告。

可扩展点
- 将延迟时长、冷却、是否启用 `HypnotistPlayerComponent` 等参数提取到 `NoellesRolesConfig` 配置中以便调整。
- 为转换事件增加粒子/声音效果（服务器侧播放），并在客户端显示新身份的短暂提示（仅给触发者或仅给目标）。

—— 完 ——
