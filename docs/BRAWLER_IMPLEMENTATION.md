
### 注册点 2：词条事件处理（ModifierAssigned & ResetPlayerEvent）

```java
ModifierAssigned.EVENT.register(((playerEntity, modifier) -> {
    if (modifier.equals(BRAWLER)) {
        BrawlerPlayerComponent brawlerComp = BrawlerPlayerComponent.KEY.get(playerEntity);
        brawlerComp.reset();
        brawlerComp.sync();
    }
}));

ResetPlayerEvent.EVENT.register(((playerEntity) -> {
    BrawlerPlayerComponent brawlerComp = BrawlerPlayerComponent.KEY.get(playerEntity);
    brawlerComp.reset();
    brawlerComp.sync();
}));
```

### 注册点 3：能力触发（ABILITY_PACKET 处理）

```javaModifier(context.player(), BRAWLER) && abilityPlayerComponent.cooldown <= 0) {
        BrawlerPlayerComponent brawlerComp = BrawlerPlayerComponent.KEY.get(context.player());
        brawlerComp.startCharge();
        abilityPlayerComponent.cooldown = BrawlerPlayerComponent.CHARGE_COOLDOWN_TICKS;
        abilityPlayerComponent.sync();
        
        // 播放冲刺音效
        context.player().getServerWorld().playSound(...);orldModifierComponent.isRole(context.player(), BRAWLER) && abilityPlayerComponent.cooldown <= 0) {
        BrawlerPlayerComponent brawlerComp = BrawlerPlayerComponent.KEY.get(context.player());
        if (!brawlerComp.isOnCooldown()) {
            brawlerComp.startCharge();
            abilityPlayerComponent.cooldown = BrawlerPlayerComponent.CHARGE_COOLDOWN_TICKS;
            abilityPlayerComponent.sync();
        }
    }
});
```

### 注册点 4：服务端 Tick 逻辑

```java
ServerTickEvents.END_SERVER_TICK.register(((server) -> {
    // 壮汉冲刺逻辑处理
    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
        BrawlerPlayerComponent brawlerComp = BrawlerPlayerComponent.KEY.get(player);
        if (brawlerComp.isCharging()) {
            // 每个 Tick 处理冲刺逻辑（碰撞检测、效果应用）
            BrawlerChargeHandler.tickCharge(player, brawlerComp);
        }
    }
    // ... 其他逻辑 ...
}));
```

## 使用流程

1. **玩家获得壮汉词条** → `ModifierAssigned` 事件调用 `BrawlerPlayerComponent.reset()`
2. **玩家按下能力键（默认已绑定）** → 客户端发送 `AbilityC2SPacket`
3. **服务端接收包** → 检测词条 + 验证冷却后调用 `BrawlerPlayerComponent.startCharge()`
4. **每个 Tick** → `BrawlerChargeHandler.tickCharge()` 更新速度与碰撞
5. **冲刺结束** → 自动清零状态，进入冷却

## 客户端集成（已完成）

### 按键绑定
✅ **已完成** - 使用现有的 ABILITY_PACKET 系统，客户端已有能力键绑定。

### 待实现项（记录在 TODO）

1. **HUD 显示**
   - 冷却进度条
   - 能力状态提示文本

2. **粒子效果**
   - 冲刺时播放粒子
   - 击中目标时的视觉反馈

3. **动画状态**
   - 冲刺开始/结束的动画过渡

## 参数调整指南

所有可调参数均在 `BrawlerPlayerComponent` 中以常量形式定义：

| 参数 | 当前值 | 说明 | 调整范围 |
|------|-------|------|---------|
| `chargeVelocity` | 0.8 | 冲刺速度（块/tick） | 0.5-1.5 |
| `chargeDuration` | 20 | 冲刺持续时间（tick） | 10-40 |
| `CHARGE_COOLDOWN_TICKS` | 200 | 冷却时间（tick） | 100-400 |
| `COLLISION_RADIUS` | 1.0 | 碰撞范围（块） | 0.8-1.5 |
| `SLOWNESS_DURATION` | 100 | 缓慢效果时长（tick） | 60-200 |
| `SLOWNESS_LEVEL` | 20 | 缓慢等级（0=I, 1=II） | 20-40 |
| `NAUSEA_DURATION` | 60 | 恶心效果时长（tick） | 30-120 |
| `NAUSEA_LEVEL` | 0 | 恶心等级（0=I, 1=II） | 0-1 |
| `DARKNESS_DURATION` | 60 | 黑暗效果时长（tick） | 30-120 |
| `DARKNESS_LEVEL` | 0 | 黑暗等级（0=I, 1=II） | 0-1 |

## 测试检查清单

- [ ] 词条能够正常分配给玩家
- [ ] 按下能力键时触发冲刺（使用默认能力键）
- [ ] 冷却倒计时正确推进
- [ ] 冲刺速度向量正确应用
- [ ] 碰撞检测能够正确识别附近玩家
- [ ] 缓慢与恶心效果正确应用到目标
- [ ] 游戏重新加载后状态正确恢复（NBT 序列化）
- [ ] 与其他词条/角色无逻辑冲突
- [ ] 词条移除/重置时状态正确清理

## 文件清单

```
src/main/java/org/agmas/noellesroles/
├── brawler/
│   ├── BrawlerPlayerComponent.java       (核心组件)
│   └── BrawlerChargeHandler.java         (效果处理，含黑暗效果)
├── ModSounds.java                        (音效注册)
└── Noellesroles.java                     (主入口，包含词条注册与集成，播放冲刺音效)

src/client/java/org/agmas/noellesroles/client/mixin/brawler/
├── BrawlerHudMixin.java                  (HUD 显示)
└── BrawlerParticlesMixin.java            (客户端粒子效果)

src/client/resources/
└── noellesroles.client.mixins.json       (客户端 Mixin 注册)

src/main/resources/
├── fabric.mod.json                       (组件声明)
├── assets/noellesroles/
│   ├── sounds.json                       (音效声明)
│   ├── sounds/brawler_ability.ogg        (冲刺音效文件)
│   └── lang/
│       ├── zh_cn.json                    (中文翻译，含音效字幕)
│       └── en_us.json                    (英文翻译，含音效字幕)

docs/
└── BRAWLER_IMPLEMENTATION.md             (本文档)
```

## HUD 实现

### 显示逻辑

**文件**: `src/client/java/org/agmas/noellesroles/client/mixin/brawler/BrawlerHudMixin.java`

HUD 会在屏幕右下角显示壮汉词条的状态信息：

1. **冲刺中**: "冲刺中... (X.Xs)" - 显示剩余冲刺时间
2. **冷却中**: "冷却中：X 秒" - 显示冷却倒计时
3. **就绪**: "按 [键位] 冲刺！" - 提示可以使用技能

**显示颜色**: 使用词条的棕色主题 (139, 69, 19)

### 翻译文本

| 键名 | 中文 | 英文 |
|------|------|------|
| `announcement.modifier.noellesroles.brawler` | 壮汉 | Brawler |
| `announcement.modifier_description.noellesroles.brawler` | 可以向前冲刺并肘击撞到的玩家，使其眩晕。 | Can charge forward and elbow strike players to stun them. |
| `hud.brawler.ready` | 按 %s 冲刺！ | Press %s to charge! |
| `hud.brawler.charging` | 冲刺中... (%.1fs) | Charging... (%.1fs) |
| `hud.brawler.cooldown` | 冷却中：%s 秒 | Cooldown: %s seconds |

---

**下一步**: 
1. ✅ 实现 HUD 显示
2. ✅ 添加粒子效果与音效反馈
3. 编译并测试壮汉词条的完整功能
4. 如需要，添加冲刺动画状态（角色旋转、武器摆动等）
