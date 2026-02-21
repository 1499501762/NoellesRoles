# Gambler 实现说明

本文档总结了为 `Gambler` 角色及其特殊武器“卡壳左轮”所做的设计与实现细节，包含关键代码位置、交互流程、测试步骤与后续待办。

**摘要**

- 目标：为 `Gambler` 角色实现一项主动能力（15s 冷却）：触发时有 50% 概率将玩家金币翻倍，否则减半（减半结果向下取整到 5 的倍数）。此外提供一把可在商店以 1000 价格购买的“卡壳左轮”，每次开枪有 50% 概率走火自伤（致死）。

**主要改动文件**

- `src/main/java/org/agmas/noellesroles/gambler/GamblerPlayerComponent.java`：玩家组件，保存冷却并负责同步/持久化。
- `src/main/java/org/agmas/noellesroles/Noellesroles.java`：导入组件，向 `ABILITY_PACKET` 的处理器中加入赌博能力逻辑；在 `onInitialize()` 将 `JAMMED_REVOLVER` 商店条目加入 `GameConstants.SHOP_ENTRIES`。
- `src/main/java/org/agmas/noellesroles/ModItems.java`：注册 `JAMMED_REVOLVER` 并在 `init()` 中设置物品冷却。
- `src/main/java/org/agmas/noellesroles/mixin/gambler/JammedRevolverMixin.java`：拦截服务器端的 `GunShootPayload` 接收器，实现卡壳左轮 50% 走火逻辑（走火时直接调用杀死玩家并播放爆炸音效）。
- `src/main/resources/assets/noellesroles/lang/zh_cn.json` / `en_us.json`：添加了 `message.gambler.win` / `message.gambler.lose` 和 `item.noellesroles.jammed_revolver` 本地化键。

**Gambler 组件细节**

- 文件：`GamblerPlayerComponent.java`。
- 关键常量：`GAMBLE_COOLDOWN_TICKS = 15 * 20`（15 秒）。
- 字段：`int cooldown`，`sync()` 使用 CCA 的 `KEY.sync(player)`；`serverTick()` 递减并定期同步。
- 序列化：实现 `writeToNbt` / `readFromNbt` 保存冷却。

**能力实现（服务器端保障）**

- 触发点：`Noellesroles.registerPackets()` 中的 `ABILITY_PACKET` 全局接收器。
- 行为：当玩家所属角色为 `GAMBLER` 且通用 `AbilityPlayerComponent.cooldown <= 0` 时，检查 `GamblerPlayerComponent.cooldown`。
  - 随机判定：50% 胜利 → `PlayerShopComponent.balance *= 2`；50% 失败 → `balance = floor((balance/2) / 5) * 5`（向下取整至 5 的倍数）。
  - 同步：对 `PlayerShopComponent` 和 `GamblerPlayerComponent` 调用 `sync()`；同时把 `AbilityPlayerComponent.cooldown` 设为同样冷却以避免重复触发。
  - 反馈：成功播放 `ENTITY_PLAYER_LEVELUP`，失败播放 `ENTITY_PLAYER_HURT`（可在 `Noellesroles` 中调整）；并通过 action-bar 发送本地化消息键 `message.gambler.win` / `message.gambler.lose`，参数为新的余额值。

**卡壳左轮（Jammed Revolver）**

- 注册：在 `ModItems` 中新增 `JAMMED_REVOLVER`，使用与 `FAKE_REVOLVER` 相同的 `RevolverItem` 类型并对其在 `ModItems.init()` 中设置冷却（与 `FAKE_REVOLVER` 相同或可单独调整）。
- 商店条目：在 `Noellesroles.onInitialize()` 向 `GameConstants.SHOP_ENTRIES` 添加 `new ShopEntry(ModItems.JAMMED_REVOLVER.getDefaultStack(), 800, ShopEntry.Type.WEAPON)`，因此服务器端商店上线并按索引可被客户端映射（客户端逻辑已支持基于服务端 `GameConstants.SHOP_ENTRIES` 的索引匹配）。
- 走火实现：新增 `JammedRevolverMixin`（混入到 `GunShootPayload.Receiver` 的 `receive` 方法头部）：当玩家主手物品为 `ModItems.JAMMED_REVOLVER`，以 50% 概率判定走火；走火时直接调用 `GameFunctions.killPlayer(player, true, player, GameConstants.DeathReasons.GUN)` 并播放爆炸音效，取消后续普通枪击处理；否则允许原有逻辑继续处理。

**本地化**

- 新增键：
  - `message.gambler.win`：赌博成功的 action-bar 文本（参数：余额）。
  - `message.gambler.lose`：赌博失败的 action-bar 文本（参数：余额）。
  - `item.noellesroles.jammed_revolver`：物品名。
- 文件位置：`src/main/resources/assets/noellesroles/lang/zh_cn.json` 和 `en_us.json`。

**注册/兼容性注意点**

- 所有关键行为均在服务器端执行（尤其是对金钱和杀戮的判定），避免客户端索引不一致或客户端伪造造成的误触发（之前的经验：客户端商店索引与服务器不一致会导致误触发 `PSYCHO_MODE`）。
- `GamblerPlayerComponent` 已在 `NoellesRolesComponents.registerEntityComponentFactories` 注册（与其他玩家组件保持相同 respawn 策略）。
- 新增的 mixin `gambler.JammedRevolverMixin` 已添加到 `src/main/resources/noellesroles.mixins.json` 的 mixins 列表中。

**测试步骤（本地）**

1. 编译并运行客户端：

```powershell
.
# Windows
.
.
# or run via Gradle wrapper
.
.
```

（注：在你的工作区使用 `.
` 替换为你常用的 `.
` 构建/运行命令；主要是 `./gradlew runClient` 或在 Windows 下 `.
`）

更简单的命令：

```powershell
.
# 在项目根目录运行
.
```

2. 在游戏内验证：
- 给测试玩家 `GAMBLER` 角色，确保 `GamblerPlayerComponent` 同步并冷却为 0。可在重连/重置后检查组件数据。
- 触发能力（按绑定的按键 → 触发 `ABILITY_PACKET`），观察 action-bar 提示与音效：
  - 成功时余额翻倍并看到 `message.gambler.win`（含新余额）。
  - 失败时余额减半并向下取整到 5 的倍数，看到 `message.gambler.lose`。
- 进入商店购买 `卡壳左轮`（价格 800），拿到物品后开枪测试：
  - 有约 50% 概率会立刻走火并死亡（播放爆炸音效）；否则按原有 `RevolverItem` 职责执行普通射击逻辑。

**已知限制与后续任务**

- 目前 `JAMMED_REVOLVER` 使用与普通 `RevolverItem` 相同的 `Item` 类型，若需要自定义子弹计数、弹匣或耐久表现，建议新增专门的 `JammedRevolverItem` 类并在服务器端处理其额外状态字段。
- 推荐完成额外的客户端显示（HUD/提示）以告知玩家其 `Gambler` 能力的冷却剩余；目前已有 `AbilityPlayerComponent` 的通用 HUD，若需更显著表现可在 `src/client` 中拓展。
- 建议在集成测试中运行多轮游戏以统计走火概率与冷却同步稳定性，观察网络延迟下组件同步行为是否正常。

---

**客户端 UI 与商店实现（补充）**

我已为 Gambler 增加了客户端 HUD 与商店显示支持：

- HUD（文件）：`src/client/java/org/agmas/noellesroles/client/mixin/gambler/GamblerHudMixin.java`。
  - 在 `InGameHud.render` 末尾绘制：显示能力冷却（使用 `AbilityPlayerComponent.cooldown`）和 `GamblerPlayerComponent.lossStreak` 连输计数。
  - 使用本地化键：`hud.gambler.cooldown`、`hud.gambler.ready`、`hud.gambler.loss_streak`（已添加到客户端语言文件）。

- 商店（文件）：`src/client/java/org/agmas/noellesroles/client/mixin/gambler/GamblerShopMixin.java`。
  - 在打开 `LimitedInventoryScreen` 时，如果玩家为 `GAMBLER`，在屏幕顶部添加 `JAMMED_REVOLVER` 的 `StoreItemWidget`。
  - 复用了现有的服务端索引映射逻辑：先在 `GameConstants.SHOP_ENTRIES` 中查找服务端索引，若未找到则回退到本地索引，保证客户端显示与服务端一致，避免索引错位导致误触发 `PSYCHO_MODE` 的问题。

已更新的客户端本地化键位于：
- `src/client/resources/assets/noellesroles/lang/en_us.json`（`hud.gambler.*`）
- `src/client/resources/assets/noellesroles/lang/zh_cn.json`（`hud.gambler.*`）

这些变更已同步到仓库；如果你希望 HUD 风格、位置或文字有不同表现，我可以进一步微调并添加单元/视觉测试截图说明。

如需我把这份文档调整为更技术化的 API 参考（包含方法签名、字段表格、示例请求/响应包），或现在运行一次本地构建并提供运行日志，我可以继续执行。