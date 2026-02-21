# Scarecrow（稻草人）实现说明

## 概述
稻草人（Scarecrow）是一名杀手角色（Killer）：通过客户端的单目标选择界面选择一名玩家作为目标，服务端接收并执行效果。触发后服务端会将目标玩家的理智值（SAN）清空，赋予黑暗效果（20 秒），并在黑暗持续期间每秒循环播放心跳声，同时向目标显示 action-bar 提示。

实现要点为“客户端发起选择 → 服务器校验并执行”；客户端界面参考并复用了 Swapper 的头像选择风格，但点击即发送选择（非两阶段确认）。所有状态修改均在服务端完成，客户端仅负责显示与发送选择包。

## 设计要点
- 直接调用 `PlayerMoodComponent.setMood(0f)` 清空理智值并 `sync()`。
- 直接调用 `PlayerMoodComponent.setMood(0f)` 清空理智值并 `sync()`。
- 在目标上添加 `StatusEffects.DARKNESS`（30 秒s）。
- 在黑暗持续期间，服务器每秒（每 18 tick）循环播放心跳音 `SoundEvents.ENTITY_WARDEN_HEARTBEAT` 直到黑暗结束；客户端仅负责播放/显示效果。
- 能力由客户端选择界面发起（`ScarecrowC2SPacket`），服务端在接收后进行权限与存活性校验并执行效果（服务端为权威）。
- 能力冷却为 2 分钟（`GameConstants.getInTicks(2,0)`）。

## 代码位置
- 角色注册：`src/main/java/org/agmas/noellesroles/Noellesroles.java`
  - 添加了 `SCARECROW_ID` 与 `SCARECROW` 的注册。
  - 在 `ABILITY_PACKET` 能力分发区块中加入了稻草人能力逻辑（通过 `PlayerMoodComponent.KEY` 操作）。

- 本地化：
  - `src/main/resources/assets/noellesroles/lang/zh_cn.json`：`message.scarecrow.notice` 已添加。
  - `src/main/resources/assets/noellesroles/lang/en_us.json`：`message.scarecrow.notice` 已添加。

## 触发示例（已实现）
- 玩家为稻草人并按下能力键时（客户端打开选择界面并通过 `ScarecrowC2SPacket` 上报所选目标，服务端校验）：
  - 服务端确认触发者为 `SCARECROW` 角色并且触发合法（存活、非旁观等）；
  - 验证目标 UUID 对应的玩家存在且存活；
  - 对目标执行（代码位置见下）：
    - `PlayerMoodComponent.KEY.get(target).setMood(0f);` 并 `sync()`；
    - `target.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 400, 0));`（20 秒）；
    - `target.sendMessage(Text.translatable("message.scarecrow.notice"), true);`（action-bar 提示）；
    - 将目标加入服务器心跳跟踪表，使服务器每 20 tick 向该玩家播放一次心跳音直至黑暗结束；
  - 对触发玩家设置能力冷却 `GameConstants.getInTicks(2,0)`（默认 2 分钟）。

## 可选扩展
- 保持单目标选择，也可实现被动触发（例如稻草人接近时触发或在分配时触发）。
- 根据稻草人的等级或物品增强持续时间或效果强度（例如更长的黑暗或额外盲目反馈）。
- 为目标添加短时视野/粒子或 UI 提示以增强反馈。注意：作为杀手角色，设计时要避免与团队目标产生不平衡互动（例如允许队友误伤）。

## 测试步骤
1. 编译并运行客户端/服务端。
2. 在游戏中把某玩家分配为 `SCARECROW`。
3. 在稻草人附近让其他玩家保持正常理智值并按下能力键。
4. 验证：附近玩家的理智值被置为 0、屏幕进入黑暗、听到心跳声，并收到提示（action-bar）。

## 注意事项
- 所有状态修改在服务端完成，客户端仅负责显示与发送选择包。
- 当前实现为单目标选择触发（非半径/附近触发）。
- `PlayerMoodComponent.setMood` 可能会在其他逻辑中被使用，请确保同步与其它恢复逻辑不会冲突。

---
文档自动生成：实现位于 `Noellesroles` 主类；如需我把能力改为被动触发或调整参数（范围/冷却/持续时间），请告诉我你想要的具体数值。