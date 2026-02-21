# Wathe 冲刺耐力系统集成指南

**项目**: Wathe Minecraft Fabric 模组 - 社交推理游戏  
**版本**: 基于当前 main 分支  
**语言**: Java 21+  
**框架**: Minecraft Fabric 1.20.5+

---

## 目录

1. [概述](#概述)
2. [系统架构](#系统架构)
3. [冲刺耐力机制](#冲刺耐力机制)
4. [角色配置](#角色配置)
5. [检测和修改耐力](#检测和修改耐力)
6. [与其他系统的交互](#与其他系统的交互)
7. [集成代码示例](#集成代码示例)
8. [常见问题](#常见问题)

---

## 概述

Wathe 的**冲刺耐力系统**是一个基于角色的移动限制机制。它控制玩家在游戏期间能够冲刺（奔跑）多久，为游戏增加了战术深度和平衡性。

### 核心特性

| 特性 | 描述 |
|------|------|
| **基于角色** | 耐力由玩家的角色决定，不同角色有不同的冲刺时间限制 |
| **时间限制** | 每个角色有一个最大冲刺时长（单位：ticks） |
| **恢复机制** | 停止冲刺时每 tick 恢复 0.25 点耐力 |
| **消耗速度** | 冲刺时每 tick 消耗 1 点耐力 |
| **无限冲刺** | 某些角色可设置为无限冲刺（特殊值 -1） |
| **移动速度限制** | 冲刺期间的移动速度被限制在特定值 |

### 游戏平衡

- **平民（无限制）**：无冲刺限制（maxSprintTime = -1）
- **杀手（无限制）**：无冲刺限制（maxSprintTime = -1），但移动速度较低
- **其他角色**：通常有 200-400 ticks 的冲刺限制

---

## 系统架构

### 涉及的类

**1. Role.java**（角色定义）
```java
public final class Role {
    private final int maxSprintTime;  // 单位：ticks，-1 表示无限制
    
    public int getMaxSprintTime() {
        return maxSprintTime;
    }
}
```

**2. PlayerEntityMixin.java**（玩家冲刺逻辑）
```java
@Unique
private float sprintingTicks;  // 当前耐力值（浮点数）

@Inject(method = "tickMovement", at = @At("HEAD"))
public void wathe$limitSprint(CallbackInfo ci) {
    // 每 tick 调用，管理冲刺耐力
}

@ModifyReturnValue(method = "getMovementSpeed", at = @At("RETURN"))
public float wathe$overrideMovementSpeed(float original) {
    // 修改移动速度
}
```

**3. GameWorldComponent.java**（游戏状态）
```java
public Role getRole(UUID playerUuid) {
    // 获取玩家的角色
}

public boolean isRunning() {
    // 检查游戏是否进行中
}
```

### 数据流向

```
游戏 Tick
  ↓
PlayerEntityMixin.tickMovement()
  ├─ 检查游戏是否运行
  ├─ 获取玩家角色
  ├─ 获取角色的 maxSprintTime
  ├─ 更新 sprintingTicks：
  │  ├─ 冲刺中：sprintingTicks -= 1
  │  └─ 停止冲刺：sprintingTicks += 0.25
  └─ 耐力用尽时：setSprinting(false)
```

---

## 冲刺耐力机制

### 耐力的生命周期

```
初始状态：sprintingTicks = maxSprintTime
  ↓
玩家开始冲刺（按住 Shift 键）
  ├─ sprintingTicks -= 1（每 tick）
  ├─ 移动速度修改为 0.1f（冲刺速度）
  └─ 继续直到 sprintingTicks ≤ 0
  
玩家停止冲刺（释放 Shift 键）
  ├─ sprintingTicks += 0.25（每 tick）
  ├─ 移动速度修改为 0.07f（正常速度）
  └─ 继续恢复直到达到 maxSprintTime

特殊情况：maxSprintTime = -1（无限制）
  ├─ sprintingTicks 检查被跳过
  └─ 玩家可以无限冲刺
```

### 具体数值

```java
// 冲刺消耗
float SPRINT_DRAIN_PER_TICK = 1.0f;

// 耐力恢复
float SPRINT_RECOVER_PER_TICK = 0.25f;

// 移动速度
float SPRINT_SPEED = 0.1f;      // 冲刺速度（更快）
float NORMAL_SPEED = 0.07f;     // 正常速度（较慢）

// 特殊值
int INFINITE_SPRINT = -1;       // 表示无限冲刺
```

### 时间换算

```
1 second = 20 ticks
1 minute = 1200 ticks

示例：
- 200 ticks = 10 秒冲刺时间
- 400 ticks = 20 秒冲刺时间
- 600 ticks = 30 秒冲刺时间
- -1 = 无限冲刺
```

---

## 角色配置

### Role 类的耐力参数

```java
public Role(
    Identifier identifier,        // 角色标识符
    int color,                    // 颜色
    boolean isInnocent,           // 是否是平民
    boolean canUseKiller,         // 是否能使用杀手功能
    MoodType moodType,            // 心情类型
    int maxSprintTime,            // ← 耐力值（关键参数）
    boolean canSeeTime            // 是否能看到时间
)
```

### 常见的耐力配置

```java
// 示例 1：无限冲刺的角色（平民/杀手）
new Role(
    identifier,
    0xFF0000,
    true,
    false,
    MoodType.REAL,
    -1,  // 无限冲刺
    true
)

// 示例 2：有限冲刺的角色（20 秒）
new Role(
    identifier,
    0x0000FF,
    true,
    false,
    MoodType.REAL,
    400,  // 20 秒 (400 ticks / 20)
    true
)

// 示例 3：短冲刺角色（5 秒）
new Role(
    identifier,
    0x00FF00,
    false,
    true,
    MoodType.FAKE,
    100,  // 5 秒 (100 ticks / 20)
    false
)
```

---

## 检测和修改耐力

### 获取玩家的耐力限制

```java
// 获取玩家角色的最大冲刺时间
GameWorldComponent gameComp = GameWorldComponent.KEY.get(world);
Role playerRole = gameComp.getRole(player);

if (playerRole != null) {
    int maxSprintTicks = playerRole.getMaxSprintTime();
    
    if (maxSprintTicks == -1) {
        System.out.println("无限冲刺");
    } else {
        float maxSprintSeconds = maxSprintTicks / 20.0f;
        System.out.println("最大冲刺: " + maxSprintSeconds + " 秒");
    }
}
```

### 检查玩家当前的耐力状态

```java
// 注意：sprintingTicks 是私有字段，无法直接访问
// 但可以通过以下方式推断：

public boolean canSprintNow(ServerPlayerEntity player) {
    GameWorldComponent gameComp = GameWorldComponent.KEY.get(player.getWorld());
    Role role = gameComp.getRole(player);
    
    // 无限冲刺的角色总能冲刺
    if (role != null && role.getMaxSprintTime() == -1) {
        return true;
    }
    
    // 有限制的角色需要检查当前耐力
    // 耐力用尽时，player.isSprinting() 会被设置为 false
    return true;  // 如果能进到这里说明还有耐力
}

// 检查冲刺是否活跃
public boolean isCurrentlySprinting(ServerPlayerEntity player) {
    return player.isSprinting();
}
```

### 通过 Mixin 访问和修改耐力

```java
// 如果需要访问或修改 sprintingTicks，需要使用 Mixin

@Mixin(PlayerEntity.class)
public class SprintingTicksAccessorMixin {
    @Accessor("sprintingTicks")  // 需要为字段提供访问器
    public abstract float getSprintingTicks();
    
    @Accessor("sprintingTicks")
    public abstract void setSprintingTicks(float value);
}

// 使用方式
public void resetPlayerSprinting(ServerPlayerEntity player) {
    SprintingTicksAccessor accessor = (SprintingTicksAccessor) player;
    accessor.setSprintingTicks(0);  // 重置耐力
}

public void giveFullSprinting(ServerPlayerEntity player) {
    SprintingTicksAccessor accessor = (SprintingTicksAccessor) player;
    GameWorldComponent gameComp = GameWorldComponent.KEY.get(player.getWorld());
    Role role = gameComp.getRole(player);
    
    if (role != null && role.getMaxSprintTime() > 0) {
        accessor.setSprintingTicks(role.getMaxSprintTime());
    }
}
```

---

## 与其他系统的交互

### 与移动速度的关系

```java
// PlayerEntityMixin.getMovementSpeed() 被修改：

@ModifyReturnValue(method = "getMovementSpeed", at = @At("RETURN"))
public float wathe$overrideMovementSpeed(float original) {
    if (GameFunctions.isPlayerAliveAndSurvival((PlayerEntity) (Object) this)) {
        // 如果冲刺中，返回更快的速度 (0.1f)
        // 否则返回正常速度 (0.07f)
        return this.isSprinting() ? 0.1f : 0.07f;
    } else {
        return original;  // 死亡或创意模式时不修改
    }
}

// 速度对比：
// - 冲刺速度：0.1f（比正常速度快约 43%）
// - 正常速度：0.07f（Wathe 自定义的较慢移动速度）
// - Vanilla 速度：0.0f（默认）
```

### 与攻击机制的关系

```java
// 在 PlayerEntityMixin.attack() 中，某些攻击可能会影响冲刺
// 例如：刀刺攻击时可能中断冲刺

@WrapMethod(method = "attack")
public void attack(Entity target, Operation<Void> original) {
    PlayerEntity self = (PlayerEntity) (Object) this;
    // 攻击逻辑可能重置或消耗冲刺耐力
    original.call(target);
}
```

### 与游戏状态的关系

```java
// 冲刺耐力只在游戏运行时生效

public void wathe$limitSprint(CallbackInfo ci) {
    GameWorldComponent gameComponent = GameWorldComponent.KEY.get(this.getWorld());
    
    // 条件 1：玩家活着且在生存模式
    if (!GameFunctions.isPlayerAliveAndSurvival((PlayerEntity) (Object) this)) {
        return;
    }
    
    // 条件 2：游戏正在运行
    if (gameComponent == null || !gameComponent.isRunning()) {
        return;
    }
    
    // 条件 3：玩家有有效的角色
    Role role = gameComponent.getRole((PlayerEntity) (Object) this);
    if (role == null || role.getMaxSprintTime() < -1) {
        return;
    }
    
    // 满足所有条件时才应用耐力限制
}
```

---

## 集成代码示例

### 示例 1：创建自定义角色与冲刺限制

```java
public class CustomRoles {
    
    // 创建一个只能冲刺 10 秒的角色
    public static Role createLimitedSprintRole() {
        return new Role(
            Identifier.of("mymod", "limited_runner"),
            0xFFAA00,
            true,
            false,
            Role.MoodType.REAL,
            200,  // 10 秒 = 200 ticks / 20
            true
        );
    }
    
    // 创建一个有 30 秒冲刺的强大角色
    public static Role createPowerRole() {
        return new Role(
            Identifier.of("mymod", "power_user"),
            0xFF0000,
            false,
            true,
            Role.MoodType.NONE,
            600,  // 30 秒
            false
        );
    }
    
    // 创建无限冲刺的角色
    public static Role createUnlimitedSprintRole() {
        return new Role(
            Identifier.of("mymod", "unlimited"),
            0x00FF00,
            true,
            false,
            Role.MoodType.REAL,
            -1,  // 无限制
            true
        );
    }
}
```

### 示例 2：监听和反应冲刺状态变化

```java
public class SprintListener {
    
    private final Map<UUID, Boolean> lastSprintState = new HashMap<>();
    
    public void onServerTick(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            UUID uuid = player.getUuid();
            boolean currentlySprinting = player.isSprinting();
            boolean wasLastSprinting = lastSprintState.getOrDefault(uuid, false);
            
            // 检测冲刺开始
            if (currentlySprinting && !wasLastSprinting) {
                onSprintStart(player);
            }
            
            // 检测冲刺停止
            if (!currentlySprinting && wasLastSprinting) {
                onSprintEnd(player);
            }
            
            lastSprintState.put(uuid, currentlySprinting);
        }
    }
    
    private void onSprintStart(ServerPlayerEntity player) {
        System.out.println(player.getName().getString() + " 开始冲刺");
        // 可以添加声音、粒子等效果
    }
    
    private void onSprintEnd(ServerPlayerEntity player) {
        System.out.println(player.getName().getString() + " 停止冲刺");
        // 可以添加疲劳效果
    }
}
```

### 示例 3：基于角色动态调整冲刺能力

```java
public class AdaptiveSprintSystem {
    
    public void updatePlayerSprintAbility(ServerPlayerEntity player, Role role) {
        GameWorldComponent gameComp = GameWorldComponent.KEY.get(player.getWorld());
        
        if (role == null || !gameComp.isRunning()) {
            return;
        }
        
        int maxSprint = role.getMaxSprintTime();
        
        // 根据不同角色进行特殊处理
        if (maxSprint == -1) {
            // 无限冲刺角色
            enableInfiniteSprint(player);
        } else if (maxSprint > 400) {
            // 长耐力角色（>20 秒）
            applyLongDistanceRunner(player);
        } else if (maxSprint > 200) {
            // 中等耐力角色（10-20 秒）
            applyMediumRunner(player);
        } else {
            // 低耐力角色（<10 秒）
            applySprintLimit(player);
        }
    }
    
    private void enableInfiniteSprint(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("无限冲刺已启用"), false);
    }
    
    private void applyLongDistanceRunner(ServerPlayerEntity player) {
        // 给予长距离跑步的额外好处
        player.addStatusEffect(new StatusEffectInstance(
            StatusEffects.SPEED, 
            Integer.MAX_VALUE, 
            0, 
            false, 
            false
        ));
    }
    
    private void applyMediumRunner(ServerPlayerEntity player) {
        // 正常冲刺能力
    }
    
    private void applySprintLimit(ServerPlayerEntity player) {
        // 应用冲刺限制
        player.sendMessage(
            Text.literal("⚠ 冲刺能力有限"), 
            true
        );
    }
}
```

### 示例 4：冲刺耐力的 HUD 显示

```java
// 客户端侧代码（需要与服务端同步数据）

public class SprintHUDRenderer {
    
    private float currentSprintValue = 0;
    private float maxSprintValue = 200;
    
    public void render(GuiGraphicsContext context, float tickDelta) {
        // 获取玩家
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // 显示冲刺条
        int width = 182;
        int height = 5;
        int x = 10;
        int y = 10;
        
        // 背景（黑色）
        context.fill(x, y, x + width, y + height, 0xFF000000);
        
        // 耐力条（绿色）
        float percentage = currentSprintValue / maxSprintValue;
        int filledWidth = (int) (width * Math.min(percentage, 1));
        context.fill(x, y, x + filledWidth, y + height, 0xFF00FF00);
        
        // 文本
        context.drawText(
            client.textRenderer,
            String.format("冲刺: %.1f%%", percentage * 100),
            x,
            y + height + 5,
            0xFFFFFF,
            false
        );
    }
}
```

---

## 常见问题

### Q1: 如何检查玩家是否能够冲刺？

```java
public boolean canPlayerSprint(ServerPlayerEntity player) {
    // 条件 1：玩家活着
    if (!player.isAlive() || player.isSpectator() || player.isCreative()) {
        return false;
    }
    
    // 条件 2：游戏运行中
    GameWorldComponent gameComp = GameWorldComponent.KEY.get(player.getWorld());
    if (gameComp == null || !gameComp.isRunning()) {
        return false;
    }
    
    // 条件 3：玩家有角色
    Role role = gameComp.getRole(player);
    return role != null;
}
```

### Q2: 如何区分"无限冲刺"和"有限冲刺"角色？

```java
public void checkSprintType(Role role) {
    if (role.getMaxSprintTime() == -1) {
        System.out.println("无限冲刺角色");
    } else if (role.getMaxSprintTime() <= 0) {
        System.out.println("无法冲刺的角色（最大时间为 0）");
    } else {
        float seconds = role.getMaxSprintTime() / 20.0f;
        System.out.println("有限冲刺角色，最长: " + seconds + " 秒");
    }
}
```

### Q3: 冲刺耐力与移动速度有什么关系？

```java
// 冲刺时：
// - 调用 setSprinting(true)
// - getMovementSpeed() 返回 0.1f
// - 玩家快速移动

// 不冲刺时：
// - 调用 setSprinting(false)
// - getMovementSpeed() 返回 0.07f
// - 玩家正常（较慢）移动

// 注意：这是 Wathe 自定义的速度，不是 Vanilla Minecraft
```

### Q4: 如何强制重置玩家的冲刺耐力？

```java
// 方法 1：强制停止冲刺（客户端会自动恢复）
player.setSprinting(false);

// 方法 2：使用 Mixin 访问器直接重置
@Mixin(PlayerEntity.class)
public interface SprintingTicksAccessor {
    @Accessor("sprintingTicks")
    void setSprintingTicks(float value);
}

// 调用方式
SprintingTicksAccessor accessor = (SprintingTicksAccessor) player;
accessor.setSprintingTicks(0);  // 完全耗尽
// 或
GameWorldComponent gameComp = GameWorldComponent.KEY.get(player.getWorld());
Role role = gameComp.getRole(player);
if (role != null && role.getMaxSprintTime() > 0) {
    accessor.setSprintingTicks(role.getMaxSprintTime());  // 完全恢复
}
```

### Q5: 如何为角色添加冲刺奖励或惩罚？

```java
public void modifySprintAbility(ServerPlayerEntity player, float multiplier) {
    // 不能直接修改 maxSprintTime（它在 Role 中是不可变的）
    // 但可以通过以下方式实现：
    
    // 方法 1：使用状态效果增加移动速度
    if (multiplier > 1.0f) {
        player.addStatusEffect(new StatusEffectInstance(
            StatusEffects.SPEED,
            Integer.MAX_VALUE,
            (int)(multiplier * 10) - 10,  // 放大器
            false,
            false
        ));
    }
    
    // 方法 2：使用 Mixin 修改当前冲刺值
    // SprintingTicksAccessor accessor = (SprintingTicksAccessor) player;
    // float current = accessor.getSprintingTicks();
    // accessor.setSprintingTicks(current * multiplier);
}
```

### Q6: 冲刺耐力在哪些情况下会被重置？

```java
// 冲刺耐力会被重置的情况：

// 1. 游戏重新开始
//    - 新游戏 → sprintingTicks = 0（从 0 开始恢复）

// 2. 玩家角色改变
//    - 如果新角色有不同的 maxSprintTime → sprintingTicks 限制改变

// 3. 玩家死亡并复活
//    - 死亡时保存（通过 NBT）
//    - 复活时恢复

// 4. 玩家离线再上线
//    - NBT 数据用于保存 sprintingTicks
//    - 下次登录时恢复
```

### Q7: 如何防止某个玩家冲刺（临时禁用）？

```java
// 方法 1：使用速度效果和限制
public void disableSprintTemporarily(ServerPlayerEntity player, int durationTicks) {
    // 强制停止冲刺
    player.setSprinting(false);
    
    // 添加缓慢效果来防止再次冲刺
    player.addStatusEffect(new StatusEffectInstance(
        StatusEffects.SLOWNESS,
        durationTicks,
        127,  // 最大等级：完全冻结
        false,
        false
    ));
}

// 方法 2：直接重置耐力
public void exhaustSprint(ServerPlayerEntity player) {
    SprintingTicksAccessor accessor = (SprintingTicksAccessor) player;
    accessor.setSprintingTicks(0);  // 耐力耗尽
}
```

### Q8: 不同玩家的冲刺耐力独立吗？

```java
// 是的，完全独立

// 每个玩家（PlayerEntity）都有自己的 sprintingTicks 字段
// @Unique
// private float sprintingTicks;  // 每个实例独立

// 因此：
// - 玩家 A 冲刺不会影响玩家 B 的耐力
// - 每个玩家根据自己的角色有不同的 maxSprintTime 限制
// - NBT 保存和加载时完全独立
```

---

## 最佳实践

### 1. 尊重角色配置

```java
// ✅ 正确：检查角色的 maxSprintTime
Role role = gameComp.getRole(player);
if (role != null && role.getMaxSprintTime() > 0) {
    // 应用冲刺限制
}

// ❌ 错误：硬编码冲刺时间
// 这会忽略角色的配置
```

### 2. 检查游戏状态

```java
// ✅ 正确：只在游戏运行时应用耐力限制
if (gameComp.isRunning()) {
    limitSprint();
}

// ❌ 错误：无条件应用限制
// 这会在游戏未运行时也限制冲刺
```

### 3. 持久化数据

```java
// ✅ 正确：使用 NBT 保存和加载
@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
private void saveSprint(NbtCompound nbt, CallbackInfo ci) {
    nbt.putFloat("sprintingTicks", this.sprintingTicks);
}

// 在服务器重启时耐力不会重置
```

### 4. 避免硬编码值

```java
// ✅ 正确：使用常数或配置
float RECOVER_PER_TICK = 0.25f;

// ❌ 错误：硬编码数字
sprintingTicks = Math.min(sprintingTicks + 0.25f, maxSprintTime);
```

---

## 总结

Wathe 的冲刺耐力系统是一个简洁而有效的机制，它：

1. **基于角色** - 不同角色有不同的冲刺限制
2. **时间限制** - 通过 ticks 单位控制冲刺时长
3. **自动恢复** - 停止冲刺后逐渐恢复耐力
4. **游戏平衡** - 为不同角色提供不同的战术选择
5. **可配置** - 通过 maxSprintTime 参数轻松调整

其他 Mod 可以通过以下方式与此系统交互：

- 创建具有特定冲刺限制的自定义角色
- 监听玩家的冲刺状态变化
- 添加视觉反馈（HUD、粒子效果等）
- 在特定事件发生时修改冲刺能力

---

**文档版本**: 1.0  
**最后更新**: 2026-01-11  
**维护者**: doctor4t
