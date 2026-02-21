# DJ 角色实现文档

概述
- 角色名：DJ
-- 作用：向附近玩家播放音乐（可选通过 NetMusic 播放），同时为范围内玩家批量恢复冲刺耐力（sprint/stamina）与理智（SAN）。使用 DJ 能力会在播放成功后从触发者扣除 100 金币（需要 `PlayerShopComponent`）：播放失败或余额不足则拒绝。

设计要点
- 配置驱动：歌曲由 `NoellesRolesConfig.djSongIds`（网易云 songId 列表）提供；效果范围、回复量与冷却可通过配置调整。
- 持久化组件：`DJPlayerComponent`（CCA AutoSyncedComponent）负责保存当前 `currentSongId` 与 `isPlaying` 状态，并通过 NBT 同步到客户端。组件 id：`noellesroles:dj`（已在 `fabric.mod.json` 注册）。
- 无强制依赖 NetMusic：服务端尝试启动 NetMusic 播放应为可选（见集成选项），以避免在缺少 NetMusic 时构建失败。

关键配置字段（位于 `NoellesRolesConfig`）
- `djCooldownTicks`：DJ 能力冷却（ticks），默认 1200（60 秒）。
- `djEffectRange`：效果半径（方块），默认 16。
- `djStaminaRestoreTicks`：每次对目标增加的冲刺耐力（以 ticks 为单位，默认 2）。
- `djSongIds`：网易云 songId 列表（Long），示例：[210255, 284578]。
- `djSanRestoreAmount`：每个目标恢复的理智（0..1），默认 0.015。

服务器行为（当前实现）
1. 玩家按能力键触发（通过通用 `ABILITY_PACKET`，服务器端接收器已注册）：
   - 校验玩家当前角色为 DJ 并且处于可用冷却。
   - 从 `djSongIds` 随机选择一个 `songId`，写入 `DJPlayerComponent.currentSongId` 并将 `isPlaying` 置为 true（并同步）。
   - 遍历同一世界在线玩家，筛选出与 DJ 玩家位置距离在 `djEffectRange` 以内的玩家：
    - 对每名符合条件的玩家恢复冲刺耐力：服务端通过 Mixin 访问并增加 `sprintingTicks`（默认每次增加 `djStaminaRestoreTicks`，默认 2）。
    - 通过 `PlayerMoodComponent` 增加理智值（调用 `mood.setMood(min(1.0, cur + djSanRestoreAmount))` 并同步；默认每次 +0.015）。
  - 将触发者的能力冷却设置为 `djCooldownTicks` 并同步。
  - 向触发者发送 `message.dj.started` 本地化提示；若配置无歌曲则发送 `message.dj.fail.no_songs`。
  - 注意：当前实现要求存在 `PlayerShopComponent` 且会在播放成功后从触发者扣除固定 100 金币；若玩家余额不足会拒绝播放并发送 `tip.dj.not_enough_money`。

NetMusic 集成（可选，实施说明）
- 推荐方式（若服务器已安装 NetMusic 且可在编译期访问 API）：调用
  `EntityMusicPlayerManager.playEntityBySongId(entity, songId)` 在目标实体上注册虚拟播放会话。
- 更稳妥的方式（兼容且不增加编译期依赖）：在运行时使用反射尝试寻找 `EntityMusicPlayerManager` 并调用 `playEntityBySongId`；若失败则回退到只写入 `DJPlayerComponent` 并仅施加效果（不播放音乐）。
- 另一种做法是使用 `TileEntityMusicPlayer`：创建/写入对应 BE 的 NBT（`setPlayToClient`）并视需要将播放绑定到实体或虚拟会话。

- 本次实现说明与限制
- 当前代码已创建并注册 `DJPlayerComponent`，并在服务器端实现了：
  - 触发时调用 NetMusic 播放（若可用），在成功后写入 `DJPlayerComponent.currentSongId` 与 `isPlaying=true` 并同步；同时从触发者扣除 100 金币（`PlayerShopComponent` 必须存在，用于支付）。
  - 注册每个正在播放的 DJ 的剩余播放时长（若 NetMusic 提供虚拟会话则使用其 songTime 转为 ticks），并在服务端每 tick 递减。每 100 tick（约 5 秒）进行一次播放状态检查：
    - 若 `TileEntityMusicPlayer` 存在且 `isPlay()` 返回 true，则在检查时再次为范围内玩家施加恢复（周期性恢复）；
    - 若 `TileEntityMusicPlayer` 为 null 但虚拟会话存在（`getVirtualEntitySession` 返回非空），实现也会将其视为“播放中”并持续施加效果（这解决了 NetMusic 在某些情况下没有创建实体播放器但存在虚拟会话的问题）；
    - 若两者均不可用，则将 `DJPlayerComponent.isPlaying` 置 false 并清理跟踪。
  - 在对局重置时调用 `NetMusicCommand.stopFollowForEntity(entity)` 停止 NetMusic 跟随并清理残留实体播放器。

本地测试步骤
1. 编辑配置文件（`run/config/noellesroles.json5` 或服务端配置路径），确保 `djSongIds` 非空并设置合理参数。
2. 构建并运行服务端/客户端：

```powershell
./gradlew runServer
./gradlew runClient
```

3. 在游戏里分配玩家为 `DJ`（可在测试中直接修改游戏内角色或使用测试命令），站到待测试位置，按能力键使用 DJ 能力。
4. 验证：范围内玩家体力上升、理智增加；触发者冷却生效；若 NetMusic 可用并已集成则听到随身播放或 BE/实体播放。

- 开发者注意事项与下一步
- 若要加入 NetMusic 的实际播放：我建议采用运行时反射方式以保证兼容性（实现会在 `Noellesroles` 的接收器里尝试调用），或在 `build.gradle` 中把 NetMusic 的 API 添加为 `compileOnly` 依赖并直接调用。
- 可选地增加客户端 HUD/音效提示，显示当前播放曲目与进度（可通过读取 `DJPlayerComponent` 或监听 NetMusic 提供的回调实现）。

本文件由自动化脚本生成，若需要我可以继续：
- 立刻实现 NetMusic 反射调用并在成功时注册实体播放（运行时回退），或
- 直接把 NetMusic 列为 `compileOnly` 依赖并加入静态调用实现。

调试与日志（示例）
- 当播放启动时（示例日志）：

```
[Server thread/INFO]: DJ playback requested for BLRINK317 song=1470564858 started=true
[Server thread/INFO]: DJ session registered for BLRINK317: seconds=267 ticks=5340
[Server thread/INFO]: DJ stamina restored to BLRINK317 (d942a750...): addedTicks=2
[Server thread/INFO]: DJ mood updated for BLRINK317 (d942a750...): cur=0.6647 add=0.015 next=0.6797
```

- 周期检查（每 ~5s）会输出检查与决策信息，包含 TileEntity 与虚拟会话状态：

```
[Server thread/INFO]: DJ check: player=BLRINK317 uuid=d942a750... playing=true reason=te te=present rec.seconds=267 remaining=5239
[Server thread/INFO]: DJ periodic effects for player=BLRINK317 uuid=d942a750... healed=3 mood=3
```

- 当 TileEntity 为 null 但存在虚拟会话时，会记录 `reason=rec`，并仍视为播放以继续效果：

```
[Server thread/INFO]: DJ check: player=BLRINK317 uuid=d942a750... playing=true reason=rec te=null rec.seconds=267 remaining=5239
```

- 当组件缺失或播放结束时会有相应清理日志：

```
[Server thread/INFO]: DJ component missing for d942a750... during check; te=null no_virtual_session; will retry later
[Server thread/INFO]: DJ stopped according to entity player=BLRINK317 uuid=d942a750...
```
