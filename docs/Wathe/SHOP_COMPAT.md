# Wathe 商店兼容性检查与修复指南

目的
- 确认本模组在客户端注入的商店条目与父模组 `Wathe` 在服务端的购买处理一致，避免客户端展现与服务器行为不一致导致发放真实武器或触发其他致死逻辑。

背景
- 客户端 UI（如 `LimitedInventoryScreen`）通常由各模组通过 mixin 注入额外 `ShopEntry`。但是实际的物品发放应由服务端在 `PlayerShopComponent.tryBuy(int index)` 中执行。
- 如果只有客户端添加条目而未在服务器端做相应处理，则玩家可能看到一个条目但实际购买时服务器按 Wathe 原有索引处理，可能发放不期望的物品（例如真实 `Revolver`）。

检查清单
1. 确认客户端与服务器对商店条目的索引/顺序一致：
   - 客户端 mixin 添加顺序（例如 `LOCKPICK`, `CROWBAR`, `BLACKOUT`, `REVOLVER`）必须与服务器端 `tryBuy` 判断所使用的 `index` 一致。
2. 确认 `PlayerShopComponent.tryBuy(int index)` 在服务器端确实执行发放：
   - 若父模组在服务器端实行集中处理，确认是否存在 `tryBuy` 的 mixin 或覆盖点，会把 `index` 映射到 `ShopEntry`。
3. 确保服务器端对 `index` 做权限/角色校验：
   - 仅在玩家拥有相应 `Role` 时才允许该 `index` 的购买。
4. 对易混淆物品（例如父模组的 `Revolver` 与本模组的 `FAKE_REVOLVER`）做明确替换：
   - 在服务器端 `tryBuy` 中，为窃贼（`PICKPOCKET`）的 `REVOLVER` 索引强制发放 `ModItems.FAKE_REVOLVER`，而非让父模组发放真实武器。

建议修复与缓解策略
- 最安全的做法：在本模组中为 `PlayerShopComponent.tryBuy` 添加服务器端拦截 mixin（见 `src/main/java/org/agmas/noellesroles/mixin/pickpocket/PickpocketPlayerShopComponentMixin.java`），在服务器端检测玩家角色与 `index` 并执行发放/扣费。这样即使客户端 UI 与父模组不同步，服务器也能保证发放安全的物品。

- 次优补救：如果无法拦截 `tryBuy`，则不要仅在客户端添加商店条目；改为通过父模组提供的服务器端注册 API（若存在）来注册条目，或者与父模组作者沟通加入显式钩子。

如何让我访问 `Wathe` 源码以做更深入分析（如果需要）
- 请将 `D:\Dev\Wathe` 加入当前 VS Code workspace，或把 `Wathe` 源码中 `PlayerShopComponent` / `LimitedInventoryScreen` / `ShopEntry` 的相关文件复制到本项目的临时目录并告知路径。当前环境的工具无法读取工作区外的文件夹。

附：本项目已经添加的保护措施
- 我们已在 `PickpocketPlayerShopComponentMixin` 中加入服务器端拦截（索引 3 -> 发放 `ModItems.FAKE_REVOLVER`），确保服务器端发放与预期一致。

如果你希望，我可以在你把 `Wathe` 源码加入 workspace 后：
- 扫描 `Wathe` 的 `PlayerShopComponent.tryBuy` 实现并定位可能的兼容性缺口；
- 提交补丁建议或 PR 到 `Wathe`，或根据 `Wathe` 的实现调整本模组的拦截策略。