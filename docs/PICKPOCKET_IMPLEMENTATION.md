# 窃贼（Pickpocket）实现文档

版本: v0.2

概要
- 名称: 窃贼（Pickpocket）
- 类型: Role（角色）
- 核心机制: 近距离偷钱（服务端判定，客户端显示 HUD/商店）

设计目标
- 能力: 近距离窃取一名玩家。窃贼固定获得 +25 金币，目标玩家的余额变为原来的 50%。一次只影响一个玩家。
- 冷却: 30 秒（600 ticks），使用全局 `AbilityPlayerComponent.cooldown` 字段管理。
- 商店: 窃贼可购买特定物品（开锁器、撬棍、黑暗模式、手枪/转轮等）；这些物品由父模组 `Wathe` 已经注册。

实现细节

1) 组件: `PickpocketPlayerComponent`
- 位置: `src/main/java/org/agmas/noellesroles/pickpocket/PickpocketPlayerComponent.java`
- 责任:
  - 暴露调参常量: `STEAL_AMOUNT` (25), `COOLDOWN_TICKS` (600), `STEAL_RANGE` (3.0)
  - 提供 `reset()` 与 `sync()` 接口以便在分配词条或重置时使用
  - 当前不保存运行时状态（留作未来扩展）

2) CCA 注册
- 在 `NoellesRolesComponents` 中注册 `PickpocketPlayerComponent.KEY`，并在 `fabric.mod.json` 的 `custom.cardinal-components` 列表中添加 `noellesroles:pickpocket`。

3) 角色注册
- 在 `Noellesroles.java` 中添加 `PICKPOCKET_ID` 与 `PICKPOCKET`（通过 `WatheRoles.registerRole` 注册为 `Role`）。
- 在 `initializePlayerRole(PlayerEntity, Role)` 中对被分配为 `PICKPOCKET` 的玩家调用 `PickpocketPlayerComponent.reset()` 并 `sync()`。

4) 能力逻辑（服务端）
- 触发：能力通过现有的 `AbilityC2SPacket` 触发（在 `Noellesroles.registerPackets()` 的 `ABILITY_PACKET` 接收器中）。
- 校验：使用 `GameWorldComponent.isRole(player, PICKPOCKET)` 判断玩家是否拥有 `PICKPOCKET` 角色；使用 `AbilityPlayerComponent.cooldown` 检查冷却。
- 目标选择：在 `STEAL_RANGE` 范围内选择最近的存活玩家作为目标（一次仅一个目标）。
- 盯视检测（防止偷窃）：判断目标是否在朝向窃贼方向（计算目标视线向量与到窃贼方向向量的点乘）；若点乘 >= 0.85（约 31° 内）视为“正在盯着”，此时窃取失败并给出失败反馈（短冷却）。
- 成功效果：目标余额变为原来的一半（整数除法），窃贼获得固定 `STEAL_AMOUNT`（+25），设置冷却 `COOLDOWN_TICKS` 并同步 `PlayerShopComponent` 与 `AbilityPlayerComponent`。
- 反馈：播放音效（当前使用 `SoundEvents.ENTITY_WITHER_DEATH` 作为提示），并在服务端生成粒子（成功：`HAPPY_VILLAGER` 给窃贼，`SMOKE` 给被窃者；失败：`CLOUD` 与 `ANGRY_VILLAGER`）。同时向双方发送短消息。

5) 商店物品与价格（参考父模组/全局商店表）
（这些物品由 `Wathe` 提供，不需要在本模组重复注册）

| 物品 | 价格 | 类型 |
|------|------|------|
| LOCKPICK (开锁器) | 50 | TOOL |
| CROWBAR (撬棍) | 25 | TOOL |
| BLACKOUT (黑暗模式) | 200 | TOOL |
| REVOLVER (左轮手枪) | 300 | WEAPON |

集成与测试要点
- 在多人服务器环境下测试：确保近距离能正确找到目标（3格以内），并且不会误伤自己。
- 验证 `PlayerShopComponent` 的同步：窃取后双方客户端显示的余额应立即更新。
- 验证冷却：窃取成功后 30 秒内再次触发应被拒绝。

扩展想法（非必须）
- 在成功窃取后播放独立音效或粒子效果以提示窃贼与被窃者。
- 添加拾取失败的反馈（例如目标在监视/眩晕状态下不可窃取）。

已做的改动清单
- 新文件: `src/main/java/org/agmas/noellesroles/pickpocket/PickpocketPlayerComponent.java`
- 更新: `src/main/java/org/agmas/noellesroles/Noellesroles.java`（注册 `PICKPOCKET` 角色、在 `ABILITY_PACKET` 处理器中实现窃取逻辑、在 `initializePlayerRole` 中初始化组件）
- 更新: `src/main/java/org/agmas/noellesroles/NoellesRolesComponents.java`（CCA 注册）
- 更新: `src/main/resources/fabric.mod.json`（添加 `noellesroles:pickpocket` 条目）
- 新增客户端 mixin: `src/client/java/org/agmas/noellesroles/client/mixin/pickpocket/PickpocketShopMixin.java`（注入 `LimitedInventoryScreen` 商店条目）
- 新增客户端 mixin: `src/client/java/org/agmas/noellesroles/client/mixin/pickpocket/PickpocketHudMixin.java`（HUD 显示冷却/就绪）
- 更新: `src/main/resources/assets/noellesroles/sounds.json`（新增 `pickpocket_success` / `pickpocket_fail` 条目，运行时改为使用 `ENTITY_WITHER_DEATH`）

下一步
- 在多人服务器环境下进行集成测试：运行 `./gradlew runClient` / `./gradlew runServer` 并验证以下项：
  - 能力触发（按绑定的能力键）能正确走到服务端并执行逻辑；
  - 被窃者在盯视窃贼时会导致窃取失败；
  - 成功/失败的粒子与音效能被附近玩家看到/听到；
  - `PlayerShopComponent` 的 `balance` 在客户端正确同步并在 UI（商店/余额显示）更新；
  - HUD（`PickpocketHudMixin`）在客户端显示冷却/就绪状态。

如果你希望，我可以继续：
- 添加本地化文本（`src/client/resources/assets/noellesroles/lang/zh_cn.json` 与 `en_us.json`）用于 HUD 与提示；
- 将成功/失败音效替换为自定义 .ogg 并发布到 `assets/noellesroles/sounds/`（当前使用凋零死亡音以便快速验证）。
