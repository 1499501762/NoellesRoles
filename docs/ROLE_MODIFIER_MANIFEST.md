# Role / Modifier 属性清单

版本: 2026-01-10

说明: 本清单由代码扫描生成，记录当前仓库中 `Role` 与 `Modifier` 的“财产”（Properties）：
- Type: Role / Modifier
- HasHUD: 是否存在客户端 HUD mixin（显示能力/状态）
- HasShop: 是否存在商店 mixin 或在服务端注册的 ShopEntry 列表（商店入口）
- HasAbility: 是否存在服务端能力处理（ABILITY_PACKET 或 专用 C2S 包）

注意：此清单用于接下来的注册系统重构（统一记录 UI/商店/技能 拥有情况并在注册时声明），以避免 Role 与 Modifier 同时提供重复 UI 导致冲突。

## Roles

- `JESTER`: Type=Role, HasHUD=false, HasShop=false, HasAbility=false
- `MORPHLING`: Type=Role, HasHUD=false, HasShop=false, HasAbility=yes (MorphC2SPacket)
- `CONDUCTOR`: Type=Role, HasHUD=true (MasterKeyHudMixin), HasShop=false, HasAbility=false
- `AWESOME_BINGLUS`: Type=Role, HasHUD=false, HasShop=false, HasAbility=false
- `BARTENDER`: Type=Role, HasHUD=false, HasShop=yes (BartenderShopMixin), HasAbility=false
- `NOISEMAKER`: Type=Role, HasHUD=false, HasShop=yes (NoisemakerShopMixin), HasAbility=false
- `SWAPPER`: Type=Role, HasHUD=false, HasShop=false, HasAbility=yes (Swap packet handling)
- `PHANTOM`: Type=Role, HasHUD=true (PhantomHudMixin), HasShop=false, HasAbility=yes
- `VOODOO`: Type=Role, HasHUD=false, HasShop=false, HasAbility=yes (Voodoo via Morph packet handling)
- `THE_INSANE_DAMNED_PARANOID_KILLER...`: Type=Role, HasHUD=false, HasShop=false, HasAbility=false
- `TRAPPER`: Type=Role, HasHUD=false, HasShop=yes (TrapperShopMixin), HasAbility=false
- `CORONER`: Type=Role, HasHUD=true (CoronerHudMixin), HasShop=false, HasAbility=false
- `EXECUTIONER`: Type=Role, HasHUD=true (ExecutionerHudMixin), HasShop=false, HasAbility=false
- `RECALLER`: Type=Role, HasHUD=true (RecallerHudMixin), HasShop=false, HasAbility=yes
- `VULTURE`: Type=Role, HasHUD=true (VultureHudMixin), HasShop=false, HasAbility=yes (Vulture packet)
- `BETTER_VIGILANTE`: Type=Role, HasHUD=false, HasShop=false, HasAbility=false
- `MIMIC`: Type=Role, HasHUD=false, HasShop=false, HasAbility=false
- `SNIPER`: Type=Role, HasHUD=false, HasShop=false, HasAbility=yes (SniperC2SPacket)
- `TROLL`: Type=Role, HasHUD=true (TrollHudMixin), HasShop=false, HasAbility=yes
- `DETECTIVE`: Type=Role, HasHUD=true (DetectiveHudMixin), HasShop=false, HasAbility=yes (DetectiveC2SPacket)
- `THIEF`: Type=Role, HasHUD=true (ThiefHudMixin), HasShop=false, HasAbility=false (death-based steal logic)
- `PICKPOCKET`: Type=Role, HasHUD=true (PickpocketHudMixin), HasShop=yes (PickpocketShopMixin), HasAbility=yes (ABILITY_PACKET handling)

## Modifiers

- `TINY`: Type=Modifier, HasHUD=false, HasShop=false, HasAbility=false
- `CHAMELEON`: Type=Modifier, HasHUD=false, HasShop=false, HasAbility=false
- `GUESSER`: Type=Modifier, HasHUD=false, HasShop=false, HasAbility=yes (Guess packet handling)
- `FEATHER`: Type=Modifier, HasHUD=false, HasShop=false, HasAbility=false
- `BRAWLER`: Type=Modifier, HasHUD=true (BrawlerHudMixin), HasShop=false, HasAbility=yes (ABILITY_PACKET handling)

## 结论与后续建议

- 当前存在多个同时可能提供 HUD/商店/技能的身份（Role 或 Modifier），会在玩家同时拥有 Role 与 Modifier 情况下产生 UI/商店的重复显示与冲突。
- 建议实施的最小改造步骤：
  1. 新增一个中心化注册器类 `RoleModifierRegistry`，用于在注册 Role/Modifier 时同时声明其 `hasHud/hasShop/hasAbility`（或自动探测）。
  2. 更新所有 `WatheRoles.registerRole(...)` 与 `HMLModifiers.registerModifier(...)` 的调用，改为使用 `RoleModifierRegistry.registerRole(...)` / `registerModifier(...)`，保存元数据。
  3. 在客户端 UI mixin（HUD / Shop mixin）加载时查询 `RoleModifierRegistry`，由注册器决定是否渲染该 UI（例如：优先 Role > Modifier，或按优先级规则）。
  4. 将 `Ability` 的处理逻辑保留在服务端，但也把 `hasAbility` 信息写入注册器以便其它系统查询（例如权限/提示）。