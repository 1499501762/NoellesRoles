# Wathe 游戏系统集成指南

**项目**: Wathe Minecraft Fabric 模组 - 社交推理游戏  
**版本**: 基于当前 main 分支  
**语言**: Java 21+  
**框架**: Minecraft Fabric, CCA (Cardinal Components Architecture)

---

## 目录

1. [概述](#概述)
2. [玩家组件系统](#玩家组件系统)
3. [理智值（心情）系统](#理智值心情系统)
4. [经济系统](#经济系统)
5. [狂暴（Berserk）系统](#狂暴berserk系统)
6. [游戏世界组件](#游戏世界组件)
7. [常用常数](#常用常数)
8. [集成代码示例](#集成代码示例)
9. [与玩家组件交互](#与玩家组件交互)
10. [常见问题](#常见问题)

---

## 概述

Wathe 的游戏系统基于 **CCA (Cardinal Components Architecture)** 进行组件化设计。每个玩家都有多个组件来管理其游戏状态：

| 组件 | 功能 | 类 |
|------|------|-----|
| 心情组件 | 管理理智值和心理状态 | `PlayerMoodComponent` |
| 商店组件 | 管理金钱和物品购买 | `PlayerShopComponent` |
| 狂暴组件 | 管理狂暴状态和护甲等级 | `PlayerPsychoComponent` |
| 游戏世界组件 | 管理游戏全局状态 | `GameWorldComponent` |

### 核心架构

```
玩家 (ServerPlayerEntity)
 ├─ PlayerMoodComponent (理智值系统)
 ├─ PlayerShopComponent (经济系统)
 ├─ PlayerPsychoComponent (狂暴系统)
 └─ PlayerNoteComponent, PlayerPoisonComponent, ...

世界 (ServerWorld)
 ├─ GameWorldComponent (游戏状态)
 ├─ GameTimeComponent (计时器)
 └─ TrainWorldComponent (地图组件)
```

---

## 玩家组件系统

### 访问玩家组件

所有玩家组件都通过 **ComponentKey** 访问：

```java
import dev.doctor4t.wathe.cca.*;
import net.minecraft.server.network.ServerPlayerEntity;

ServerPlayerEntity player = ...;

// 获取各个组件
PlayerMoodComponent moodComponent = PlayerMoodComponent.KEY.get(player);
PlayerShopComponent shopComponent = PlayerShopComponent.KEY.get(player);
PlayerPsychoComponent psychoComponent = PlayerPsychoComponent.KEY.get(player);

// 要同步到客户端，需要调用 sync()
moodComponent.sync();
```

### 组件生命周期

所有组件都实现了以下接口：

- **AutoSyncedComponent** - 自动与客户端同步数据
- **ServerTickingComponent** - 服务端每个游戏 tick 执行
- **ClientTickingComponent** - 客户端每个游戏 tick 执行

---

## 理智值（心情）系统

### 概述

理智值（Mood）是一个 0 到 1 之间的浮点数，反映玩家的心理状态。

- **1.0** = 完全理智
- **0.55** = 中等疯狂（开始出现幻觉，需要完成任务）
- **0.2** = 严重疯狂（可能看到虚假物品）
- **0.0** = 完全疯狂

### PlayerMoodComponent

**位置**: [src/main/java/dev/doctor4t/wathe/cca/PlayerMoodComponent.java](src/main/java/dev/doctor4t/wathe/cca/PlayerMoodComponent.java)

```java
public class PlayerMoodComponent implements AutoSyncedComponent, 
    ServerTickingComponent, ClientTickingComponent {
    
    // 获取当前理智值（0-1）
    public float getMood()
    
    // 设置理智值
    public void setMood(float mood)
    
    // 检查是否低于中等疯狂阈值
    public boolean isLowerThanMid()
    
    // 检查是否低于严重疯狂阈值
    public boolean isLowerThanDepressed()
    
    // 完成食物任务
    public void eatFood()
    
    // 完成饮料任务
    public void drinkCocktail()
    
    // 获取心理错乱物品映射
    public HashMap<UUID, ItemStack> getPsychosisItems()
    
    // 重置所有任务和理智值
    public void reset()
}
```

### 理智值机制

#### 理智值下降
- 每个 tick 都会因未完成的任务而下降
- 下降速度：`MOOD_DRAIN = 1 / 4800` (每个未完成任务)
- 4 个任务同时进行时会快速下降

#### 理智值恢复
- 完成一个任务恢复：`MOOD_GAIN = 0.5f`
- 每次同步完成恢复 50%

#### 任务系统

玩家会收到 4 种类型的任务：

```java
public enum Task {
    SLEEP,    // 睡眠任务
    OUTSIDE,  // 去室外任务
    EAT,      // 进食任务
    DRINK     // 饮酒任务
}
```

**任务机制**：
- 首次任务延迟：30 秒
- 任务间隔：30-60 秒随机
- 任务权重：越经常出现的任务越少见
- 一次只能有 1 个活动任务

### 使用理智值

```java
PlayerMoodComponent moodComp = PlayerMoodComponent.KEY.get(player);

// 获取理智值
float currentMood = moodComp.getMood();
System.out.println("理智值: " + (currentMood * 100) + "%");

// 降低理智值（例如因为死亡）
moodComp.setMood(0f);  // 完全疯狂

// 恢复理智值
moodComp.setMood(1f);  // 完全理智

// 逐步变化
moodComp.setMood(moodComp.getMood() - 0.1f);

// 检查心理状态
if (moodComp.isLowerThanMid()) {
    // 开始出现幻觉
    System.out.println("玩家陷入中度疯狂!");
}

if (moodComp.isLowerThanDepressed()) {
    // 严重心理问题
    System.out.println("玩家陷入严重疯狂!");
}

// 重置
moodComp.reset();
moodComp.sync();
```

### 理智值阈值常数

```java
// 在 GameConstants 中定义
float MOOD_GAIN = 0.5f;                    // 任务恢复量
float MOOD_DRAIN = 1f / 4800;              // 每 tick 每任务下降速度
int TIME_TO_FIRST_TASK = 600;              // 首任务延迟（30秒）
int MIN_TASK_COOLDOWN = 600;               // 最小任务间隔（30秒）
int MAX_TASK_COOLDOWN = 1200;              // 最大任务间隔（60秒）
int SLEEP_TASK_DURATION = 160;             // 睡眠任务时长（8秒）
int OUTSIDE_TASK_DURATION = 160;           // 室外任务时长（8秒）
float MID_MOOD_THRESHOLD = 0.55f;          // 中等疯狂阈值
float DEPRESSIVE_MOOD_THRESHOLD = 0.2f;    // 严重疯狂阈值
float ITEM_PSYCHOSIS_CHANCE = 0.5f;        // 幻觉物品概率
int ITEM_PSYCHOSIS_REROLL_TIME = 200;      // 幻觉物品刷新时间
```

### 幻觉物品系统

当理智值低于 0.55 时，玩家会看到虚假物品：

```java
// 获取幻觉物品映射
HashMap<UUID, ItemStack> psychosisItems = moodComp.getPsychosisItems();

for (UUID playerUuid : psychosisItems.keySet()) {
    ItemStack hallucination = psychosisItems.get(playerUuid);
    // 这个物品可能不是真实的
    System.out.println("玩家看到: " + hallucination.getItem().getName());
}
```

---

## 经济系统

### 概述

经济系统允许玩家使用金钱购买物品。金钱通过杀死对手或被动收入获得。

### PlayerShopComponent

**位置**: [src/main/java/dev/doctor4t/wathe/cca/PlayerShopComponent.java](src/main/java/dev/doctor4t/wathe/cca/PlayerShopComponent.java)

```java
public class PlayerShopComponent implements AutoSyncedComponent, 
    ServerTickingComponent, ClientTickingComponent {
    
    // 获取当前余额
    public int balance
    
    // 添加金钱
    public void addToBalance(int amount)
    
    // 设置余额
    public void setBalance(int amount)
    
    // 尝试购买（按索引）
    public void tryBuy(int index)
    
    // 使用黑暗模式
    public static boolean useBlackout(PlayerEntity player)
    
    // 使用狂暴模式
    public static boolean usePsychoMode(PlayerEntity player)
    
    // 重置余额
    public void reset()
}
```

### 金钱来源

#### 初始金钱
```java
int MONEY_START = 100;  // 游戏开始时的初始金钱
```

#### 被动收入
```java
Function<Long, Integer> PASSIVE_MONEY_TICKER = time -> {
    if (time % 200 == 0) {  // 每 10 秒
        return 5;             // 获得 5 元
    }
    return 0;
};
```

#### 杀敌金钱
```java
int MONEY_PER_KILL = 100;  // 每次杀敌获得 100 元
```

### 商店系统

商店由 ShopEntry 列表组成，每项包括物品、价格和类型：

```java
public class ShopEntry {
    public enum Type {
        WEAPON,   // 武器
        POISON,   // 毒药
        TOOL      // 工具
    }
    
    public ItemStack stack();     // 物品
    public int price();           // 价格
    public Type type();           // 类型
    public boolean onBuy(PlayerEntity player);  // 购买回调
}
```

### 完整商店列表

| 物品 | 价格 | 类型 | 描述 |
|------|------|------|------|
| KNIFE | 100 | 武器 | 近战武器 |
| REVOLVER | 300 | 武器 | 远程武器 |
| GRENADE | 350 | 武器 | 爆炸物 |
| PSYCHO_MODE | 300 | 武器 | 狂暴模式 |
| POISON_VIAL | 100 | 毒药 | 食物毒药 |
| SCORPION | 50 | 毒药 | 弱毒 |
| FIRECRACKER | 10 | 工具 | 爆竹 |
| LOCKPICK | 50 | 工具 | 开锁工具 |
| CROWBAR | 25 | 工具 | 撬棍 |
| BODY_BAG | 200 | 工具 | 尸体袋 |
| BLACKOUT | 200 | 工具 | 黑暗模式 |
| NOTE | 10 | 工具 | 便条（×4） |

### 使用经济系统

```java
PlayerShopComponent shopComp = PlayerShopComponent.KEY.get(player);

// 获取余额
int money = shopComp.balance;
System.out.println("玩家金钱: " + money);

// 添加金钱
shopComp.addToBalance(50);

// 设置余额
shopComp.setBalance(0);

// 尝试购买第 0 项（刀）
shopComp.tryBuy(0);

// 使用狂暴模式
boolean success = PlayerShopComponent.usePsychoMode(player);
if (success) {
    System.out.println("激活狂暴模式!");
} else {
    System.out.println("激活失败 - 没有库存空间");
}

// 使用黑暗模式
boolean darkSuccess = PlayerShopComponent.useBlackout(player);
if (darkSuccess) {
    System.out.println("激活黑暗!");
}

// 重置
shopComp.reset();
shopComp.sync();
```

---

## 狂暴（Berserk）系统

### 概述

狂暴（Berserk）是一个临时状态，允许玩家短时间内获得强大的战斗能力。狂暴期间，玩家会获得护甲保护。

- 给予木棒（BBat）作为武器
- 获得护甲等级保护（护甲吸收伤害）
- 持续 30 秒（600 ticks）

### PlayerPsychoComponent

**位置**: [src/main/java/dev/doctor4t/wathe/cca/PlayerPsychoComponent.java](src/main/java/dev/doctor4t/wathe/cca/PlayerPsychoComponent.java)

**功能**: 管理玩家的狂暴状态和护甲防御机制。

```java
public class PlayerPsychoComponent implements AutoSyncedComponent, 
    ServerTickingComponent, ClientTickingComponent {
    
    // 狂暴剩余时间（ticks）
    public int psychoTicks;
    
    // 护甲等级 - 狂暴期间的防御层数，每层吸收一次伤害
    public int armour;
    
    // 启动狂暴模式
    public boolean startPsycho()
    
    // 停止狂暴模式
    public void stopPsycho()
    
    // 获取装甲等级
    public int getArmour()
    
    // 设置装甲等级
    public void setArmour(int armour)
    
    // 获取剩余时间
    public int getPsychoTicks()
    
    // 设置剩余时间
    public void setPsychoTicks(int ticks)
    
    // 重置状态
    public void reset()
}
```

### 狂暴机制

#### 启动条件
- 在库存中有空的槽位
- 不在狂暴状态中

#### 持续时间和初始状态
```java
int PSYCHO_TIMER = 600;      // 30 秒 (600 ticks / 20)
int PSYCHO_MODE_ARMOUR = 1;  // 初始护甲等级
```

#### 狂暴期间的效果
- 给予木棒（WatheItems.BAT）作为主要武器
- 自动切换到木棒
- 设置初始护甲等级（默认为 1）
- 跟踪全局活跃狂暴计数（GameWorldComponent.getPsychosActive()）
- 护甲机制：每当玩家在狂暴期间受到伤害时，护甲值减 1（而不是受伤害）

#### 结束
- 时间耗尽自动停止
- 护甲降到 0 且受伤时停止
- 从库存中移除木棒
- 全局狂暴计数减 1

### 使用狂暴系统

```java
PlayerPsychoComponent psychoComp = PlayerPsychoComponent.KEY.get(player);

// 启动狂暴
boolean started = psychoComp.startPsycho();
if (started) {
    System.out.println("狂暴已激活!");
} else {
    System.out.println("激活失败 - 库存已满");
}

// 获取状态
int ticksLeft = psychoComp.getPsychoTicks();
System.out.println("剩余时间: " + (ticksLeft / 20) + " 秒");

// 设置剩余时间
psychoComp.setPsychoTicks(300);  // 15 秒

// 获取装甲
int armor = psychoComp.getArmour();
System.out.println("装甲等级: " + armor);

// 设置装甲
psychoComp.setArmour(2);

// 手动停止
psychoComp.stopPsycho();

// 重置
psychoComp.reset();
psychoComp.sync();
```

### 全局狂暴计数

```java
GameWorldComponent gameComp = GameWorldComponent.KEY.get(world);

// 获取活跃狂暴玩家数
int activeBerserk = gameComp.getPsychosActive();
System.out.println("当前狂暴的玩家数: " + activeBerserk);

// 检查是否有活跃狂暴
if (gameComp.isPsychoActive()) {
    System.out.println("有玩家处于狂暴状态!");
}
```

### 护甲（Armor）交互机制详解

#### 护甲的用途

护甲是狂暴状态下的防御机制。当玩家在狂暴期间受到伤害时：

1. **有护甲保护** ($armour > 0$)：护甲值减 1，玩家**不受伤害**
2. **护甲耗尽** ($armour = 0$)：玩家恢复正常，狂暴效果取消

#### 护甲初始化

```java
PlayerPsychoComponent psychoComp = PlayerPsychoComponent.KEY.get(player);

// 启动狂暴时自动设置护甲
psychoComp.startPsycho();  // 内部会设置 armour = PSYCHO_MODE_ARMOUR (默认为 1)
System.out.println("当前护甲: " + psychoComp.getArmour());  // 输出: 1
```

#### 读取护甲状态

```java
PlayerPsychoComponent psychoComp = PlayerPsychoComponent.KEY.get(player);

// 获取当前护甲等级
int currentArmour = psychoComp.getArmour();
System.out.println("护甲等级: " + currentArmour);

// 检查是否仍在狂暴状态
int psychoTicks = psychoComp.getPsychoTicks();
if (psychoTicks > 0) {
    System.out.println("狂暴进行中...");
    System.out.println("剩余: " + (psychoTicks / 20.0) + " 秒");
    System.out.println("护甲保护层数: " + currentArmour);
}
```

#### 修改护甲值

```java
PlayerPsychoComponent psychoComp = PlayerPsychoComponent.KEY.get(player);

// 增加护甲 - 例如通过护甲强化道具
psychoComp.setArmour(psychoComp.getArmour() + 1);
psychoComp.sync();  // 必须同步到客户端

// 设置特定护甲值
psychoComp.setArmour(3);  // 设置为 3 层防护
psychoComp.sync();

// 移除所有护甲（强制结束狂暴）
psychoComp.setArmour(0);
psychoComp.sync();
```

#### 护甲伤害事件处理

在处理伤害时，游戏会自动检查护甲：

```java
// 这是在 GameFunctions.killPlayer() 中的实现逻辑：
PlayerPsychoComponent component = PlayerPsychoComponent.KEY.get(victim);

if (component.getPsychoTicks() > 0) {  // 检查是否在狂暴状态
    if (component.getArmour() > 0) {   // 检查是否有护甲
        // 护甲吸收伤害
        component.setArmour(component.getArmour() - 1);
        component.sync();
        // 播放护甲被击中的音效
        victim.playSoundToPlayer(WatheSounds.ITEM_PSYCHO_ARMOUR, 
                                SoundCategory.MASTER, 5F, 1F);
        return;  // 玩家不受伤害，函数返回
    } else {
        // 护甲耗尽，停止狂暴
        component.stopPsycho();
    }
}

// 没有狂暴或狂暴已结束，继续正常伤害处理
// ...继续杀死玩家的逻辑
```

#### 与其他系统的护甲交互

**与游戏规则的交互**：

```java
// 在游戏事件中处理狂暴玩家的护甲
public void handlePlayerDamage(ServerPlayerEntity player, DamageSource source) {
    PlayerPsychoComponent psychoComp = PlayerPsychoComponent.KEY.get(player);
    
    if (psychoComp.getPsychoTicks() > 0) {
        if (psychoComp.getArmour() > 0) {
            // 可以添加自定义逻辑
            System.out.println("伤害被护甲挡住! 剩余: " + (psychoComp.getArmour() - 1));
            return;  // 防止继续处理伤害
        }
    }
}

// 与全局狂暴计数同步
public void checkBerserkStatus(ServerWorld world, ServerPlayerEntity player) {
    GameWorldComponent gameComp = GameWorldComponent.KEY.get(world);
    PlayerPsychoComponent psychoComp = PlayerPsychoComponent.KEY.get(player);
    
    int totalBerserk = gameComp.getPsychosActive();
    int myArmour = psychoComp.getArmour();
    
    if (totalBerserk > 0 && myArmour > 0) {
        System.out.println("当前有" + totalBerserk + "位狂暴玩家");
        System.out.println("我的护甲: " + myArmour);
    }
}
```

#### 护甲完整生命周期

```
启动狂暴
  ↓
护甲初始化为 1
  ↓
玩家受伤→护甲减 1→受伤被阻挡 (重复)
  ↓
护甲 = 0 且受伤→狂暴终止
  ↓
或 时间耗尽 → 狂暴终止
  ↓
恢复正常状态
```

---

## 游戏世界组件

### 概述

游戏世界组件管理整个游戏的全局状态，包括游戏模式、角色分配、计时器等。

### GameWorldComponent

**位置**: [src/main/java/dev/doctor4t/wathe/cca/GameWorldComponent.java](src/main/java/dev/doctor4t/wathe/cca/GameWorldComponent.java)

```java
public class GameWorldComponent implements AutoSyncedComponent, 
    ServerTickingComponent, ClientTickingComponent {
    
    public enum GameStatus {
        INACTIVE,    // 游戏未进行
        INITIALIZING, // 初始化中
        ACTIVE,      // 游戏进行中
        STOPPING     // 停止中
    }
    
    // 游戏状态
    public GameStatus getGameStatus()
    public boolean isRunning()  // 返回 ACTIVE 或 STOPPING
    
    // 游戏模式
    public GameMode getGameMode()
    public void setGameMode(GameMode gameMode)
    
    // 地图效果
    public MapEffect getMapEffect()
    public void setMapEffect(MapEffect mapEffect)
    
    // 角色管理
    public void addRole(UUID player, Role role)
    public Role getRole(UUID uuid)
    public HashMap<UUID, Role> getRoles()
    public void setRoles(List<UUID> players, Role role)
    
    // 狂暴状态
    public int getPsychosActive()        // 返回当前狂暴的玩家数
    public boolean isPsychoActive()     // 返回是否有玩家处于狂暴状态
    public void setPsychosActive(int count)  // 设置狂暴玩家计数
    
    // 分红
    public int getKillerDividend()
    public int getVigilanteDividend()
    public void setKillerDividend(int amount)
    public void setVigilanteDividend(int amount)
    
    // 计时器
    public int getTimeRemaining()
    public void setTimeRemaining(int ticks)
    
    // 其他
    public void reset()
}
```

### 游戏状态流程

```
INACTIVE -> INITIALIZING -> ACTIVE -> STOPPING -> INACTIVE
```

### 使用游戏世界组件

```java
GameWorldComponent gameComp = GameWorldComponent.KEY.get(world);

// 检查游戏是否运行
if (gameComp.isRunning()) {
    System.out.println("游戏进行中!");
}

// 获取当前游戏模式
GameMode mode = gameComp.getGameMode();
System.out.println("游戏模式: " + mode.identifier);

// 获取当前地图效果
MapEffect effect = gameComp.getMapEffect();
System.out.println("地图效果: " + effect.identifier);

// 分配角色
gameComp.addRole(playerUuid, WatheRoles.KILLER);

// 获取玩家角色
Role playerRole = gameComp.getRole(playerUuid);
System.out.println("玩家角色: " + playerRole.identifier());

// 批量分配角色
List<UUID> innocentPlayers = Arrays.asList(...);
gameComp.setRoles(innocentPlayers, WatheRoles.CIVILIAN);

// 获取所有杀手
List<UUID> killers = gameComp.getAllKillerTeamPlayers();
System.out.println("杀手数量: " + killers.size());

// 获取所有角色
HashMap<UUID, Role> allRoles = gameComp.getRoles();
for (UUID uuid : allRoles.keySet()) {
    Role role = allRoles.get(uuid);
    System.out.println(uuid + " 是 " + role.identifier());
}

// 获取剩余时间
int timeLeft = gameComp.getTimeRemaining();
System.out.println("剩余时间: " + (timeLeft / 20) + " 秒");

// 设置剩余时间
gameComp.setTimeRemaining(600);  // 30 秒

// 获取活跃狂暴数
int psychoCount = gameComp.getPsychosActive();

// 设置分红（例如杀手额外奖励）
gameComp.setKillerDividend(100);
gameComp.setVigilanteDividend(50);

// 重置游戏状态
gameComp.reset();
gameComp.sync();
```

---

## 常用常数

### 时间常数

```java
// 转换为 ticks (20 ticks = 1 秒)
int getInTicks(int minutes, int seconds)
// 例如: getInTicks(1, 30) = 1800 ticks = 90 秒
```

### 死亡原因

```java
public interface DeathReasons {
    Identifier GENERIC = Wathe.id("generic");
    Identifier KNIFE = Wathe.id("knife_stab");
    Identifier GUN = Wathe.id("gun_shot");
    Identifier BAT = Wathe.id("bat_hit");
    Identifier GRENADE = Wathe.id("grenade");
    Identifier POISON = Wathe.id("poison");
    Identifier FELL_OUT_OF_TRAIN = Wathe.id("fell_out_of_train");
}
```

### 物品冷却时间

```java
Map<Item, Integer> ITEM_COOLDOWNS

KNIFE: 1200 ticks (60 秒)
REVOLVER: 200 ticks (10 秒)
DERRINGER: 20 ticks (1 秒)
GRENADE: 5000 ticks (5 分钟)
LOCKPICK: 3000 ticks (3 分钟)
CROWBAR: 200 ticks (10 秒)
BODY_BAG: 5000 ticks (5 分钟)
PSYCHO_MODE: 5000 ticks (5 分钟)
BLACKOUT: 3000 ticks (3 分钟)
```

### 黑暗和心理状态持续时间

```java
int PSYCHO_TIMER = 600;           // 30 秒
int BLACKOUT_MIN_DURATION = 300;  // 15 秒
int BLACKOUT_MAX_DURATION = 400;  // 20 秒
int FIRECRACKER_TIMER = 300;      // 15 秒
```

---

## 集成代码示例

### 示例 1: 监听并修改玩家理智值

```java
// 在事件监听或命令中

PlayerMoodComponent moodComp = PlayerMoodComponent.KEY.get(targetPlayer);

// 当玩家杀死无辜者时降低理智值
float newMood = moodComp.getMood() - 0.2f;
moodComp.setMood(newMood);
moodComp.sync();

// 通知玩家
targetPlayer.sendMessage(
    Text.literal("你的良知受到谴责... 理智值下降!")
        .styled(s -> s.withColor(Formatting.RED)),
    false
);

// 如果严重疯狂，给予特殊效果
if (moodComp.isLowerThanDepressed()) {
    targetPlayer.addStatusEffect(
        new StatusEffectInstance(
            StatusEffects.BLINDNESS,
            100,
            0
        )
    );
}
```

### 示例 2: 经济交互和购买系统

```java
PlayerShopComponent shopComp = PlayerShopComponent.KEY.get(player);
GameWorldComponent gameComp = GameWorldComponent.KEY.get(world);

// 杀死对手后获得金钱
if (gameComp.getRole(killerUuid).canUseKiller()) {
    int killerMoney = GameConstants.MONEY_PER_KILL;  // 100
    shopComp.addToBalance(killerMoney);
    
    // 额外的杀手分红
    shopComp.addToBalance(gameComp.getKillerDividend());
}

// 购买物品
boolean purchaseSuccess = false;
if (shopComp.balance >= 100) {
    shopComp.tryBuy(0);  // 购买刀
    purchaseSuccess = true;
}

if (purchaseSuccess) {
    player.sendMessage(
        Text.literal("✓ 购买成功!"),
        true
    );
}
```

### 示例 3: 检查角色和分配权限

```java
GameWorldComponent gameComp = GameWorldComponent.KEY.get(world);
PlayerMoodComponent moodComp = PlayerMoodComponent.KEY.get(player);

// 检查玩家角色
Role playerRole = gameComp.getRole(player);
if (playerRole == null) {
    return;  // 玩家未分配角色
}

// 检查是否是杀手
if (playerRole.canUseKiller()) {
    System.out.println("这个玩家是杀手!");
    
    // 杀手特殊逻辑
    // ...
}

// 检查是否无辜
if (playerRole.isInnocent()) {
    System.out.println("这个玩家是无辜者!");
    
    // 无辜者特殊逻辑
    // ...
}

// 检查能否看到计时器
if (playerRole.canSeeTime()) {
    // 显示计时器
}

// 检查心情类型
switch (playerRole.getMoodType()) {
    case REAL -> {
        // 真实心情 - 影响理智值
        System.out.println("使用真实心情系统");
    }
    case FAKE -> {
        // 伪造心情 - 始终显示 1.0
        System.out.println("心情伪造");
    }
    case NONE -> {
        // 无心情 - 不使用理智值系统
        System.out.println("不使用理智值");
    }
}

// 检查冲刺能力
int maxSprintTicks = playerRole.getMaxSprintTime();
if (maxSprintTicks > 0) {
    System.out.println("最大冲刺时间: " + (maxSprintTicks / 20) + " 秒");
} else {
    System.out.println("无法冲刺");
}
```

### 示例 4: 狂暴模式管理

```java
PlayerPsychoComponent psychoComp = PlayerPsychoComponent.KEY.get(player);
GameWorldComponent gameComp = GameWorldComponent.KEY.get(player.getWorld());

// 尝试激活狂暴
if (psychoComp.startPsycho()) {
    // 成功激活
    gameComp.setPsychosActive(gameComp.getPsychosActive() + 1);
    
    player.sendMessage(
        Text.literal("╔════════════════════╗")
            .styled(s -> s.withColor(0xFF0000)),
        false
    );
    player.sendMessage(
        Text.literal("║  狂暴已激活!  ║"),
        false
    );
    player.sendMessage(
        Text.literal("╚════════════════════╝")
            .styled(s -> s.withColor(0xFF0000)),
        false
    );
} else {
    player.sendMessage(
        Text.literal("✗ 激活失败 - 背包已满!"),
        true
    );
}

// 监控狂暴时间
if (psychoComp.getPsychoTicks() > 0) {
    int secondsLeft = psychoComp.getPsychoTicks() / 20;
    player.sendMessage(
        Text.literal("狂暴: " + secondsLeft + "s"),
        true
    );
}

// 检查全局状态
if (gameComp.isPsychoActive()) {
    System.out.println("场上有 " + gameComp.getPsychosActive() + " 个疯狂的玩家!");
    
    // 可能的不同游戏规则
    // ...
}
```

### 示例 5: 完整游戏初始化

```java
public void initializeGame(
    ServerWorld serverWorld,
    GameWorldComponent gameComp,
    List<ServerPlayerEntity> players
) {
    // 分配角色
    List<UUID> killersUuids = Arrays.asList(
        players.get(0).getUuid(),
        players.get(1).getUuid()
    );
    gameComp.setRoles(killersUuids, WatheRoles.KILLER);
    
    // 其余玩家为平民
    List<UUID> civiliansUuids = players.stream()
        .map(ServerPlayerEntity::getUuid)
        .filter(uuid -> !killersUuids.contains(uuid))
        .toList();
    gameComp.setRoles(civiliansUuids, WatheRoles.CIVILIAN);
    
    // 初始化所有玩家
    for (ServerPlayerEntity player : players) {
        // 重置理智值系统
        PlayerMoodComponent.KEY.get(player).reset();
        
        // 重置经济系统
        PlayerShopComponent shopComp = PlayerShopComponent.KEY.get(player);
        shopComp.setBalance(GameConstants.MONEY_START);
        
        // 重置狂暴系统
        PlayerPsychoComponent.KEY.get(player).reset();
        
        // 同步所有组件
        PlayerMoodComponent.KEY.sync(player);
        shopComp.sync();
        PlayerPsychoComponent.KEY.sync(player);
        
        // 通知玩家
        Role role = gameComp.getRole(player);
        player.sendMessage(
            Text.literal("你的角色是: " + role.identifier().getPath())
                .styled(s -> s.withColor(role.color())),
            false
        );
    }
    
    gameComp.sync();
}
```

---

## 与玩家组件交互

### 模式 1: 获取组件并修改

```java
// 最常见的模式

PlayerEntity player = ...;

// 获取组件
PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(player);

// 修改数据
mood.setMood(mood.getMood() - 0.1f);

// 同步到客户端
mood.sync();
```

### 模式 2: 服务端和客户端交互

```java
// 服务端修改
if (player instanceof ServerPlayerEntity serverPlayer) {
    PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(serverPlayer);
    mood.setMood(0.5f);
    mood.sync();  // 发送到客户端
}

// 客户端会在下一个 tick 看到更新
// 因为组件实现了 AutoSyncedComponent
```

### 模式 3: 事件监听与响应

```java
public void onPlayerEvent(PlayerEntity player) {
    // 获取所有相关组件
    PlayerMoodComponent moodComp = PlayerMoodComponent.KEY.get(player);
    PlayerShopComponent shopComp = PlayerShopComponent.KEY.get(player);
    PlayerPsychoComponent psychoComp = PlayerPsychoComponent.KEY.get(player);
    GameWorldComponent gameComp = GameWorldComponent.KEY.get(player.getWorld());
    
    // 在一个原子操作中修改
    float newMood = moodComp.getMood() - 0.1f;
    moodComp.setMood(newMood);
    
    int newBalance = shopComp.balance + 50;
    shopComp.setBalance(newBalance);
    
    // 同步所有改变
    moodComp.sync();
    shopComp.sync();
}
```

### 模式 4: 批量操作

```java
// 对所有玩家执行操作

List<? extends PlayerEntity> players = world.getPlayers(p -> true);

for (PlayerEntity player : players) {
    PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(player);
    PlayerShopComponent shop = PlayerShopComponent.KEY.get(player);
    
    // 对每个玩家进行修改
    mood.setMood(Math.min(mood.getMood() + 0.1f, 1.0f));
    shop.addToBalance(10);
    
    mood.sync();
    shop.sync();
}
```

### 模式 5: 条件读取

```java
// 只在满足条件时修改

PlayerEntity player = ...;
GameWorldComponent gameComp = GameWorldComponent.KEY.get(player.getWorld());

// 只在游戏运行时修改
if (gameComp.isRunning()) {
    PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(player);
    
    // 只对有真实心情的玩家
    Role role = gameComp.getRole(player);
    if (role != null && role.getMoodType() == Role.MoodType.REAL) {
        mood.setMood(mood.getMood() - 0.05f);
        mood.sync();
    }
}
```

---

## 常见问题

### Q1: 如何在 tick 中持续修改玩家状态？

```java
// 使用事件监听或 mixin

@Mixin(PlayerEntity.class)
public class MyPlayerMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void myCustomTick(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        
        if (self instanceof ServerPlayerEntity player) {
            PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(player);
            
            // 每个 tick 修改
            mood.setMood(mood.getMood() - 0.001f);
            mood.sync();
        }
    }
}
```

### Q2: 如何避免重复同步导致的性能问题？

```java
// 只在有实际改变时同步

PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(player);
float oldMood = mood.getMood();
float newMood = oldMood - 0.05f;

if (oldMood != newMood) {  // 只在改变时
    mood.setMood(newMood);
    mood.sync();
}
```

### Q3: 如何检查玩家是否在游戏中？

```java
GameWorldComponent gameComp = GameWorldComponent.KEY.get(world);

// 检查游戏是否运行
if (!gameComp.isRunning()) {
    return;  // 游戏未进行
}

// 检查玩家是否有分配角色
Role playerRole = gameComp.getRole(player);
if (playerRole == null) {
    return;  // 玩家未在游戏中
}
```

### Q4: 如何在游戏模式间传递数据？

```java
// 使用 GameWorldComponent 来保存数据

GameWorldComponent gameComp = GameWorldComponent.KEY.get(world);

// 在游戏开始时保存
gameComp.setGameMode(newMode);
gameComp.setMapEffect(newEffect);

// 在游戏结束时读取
GameMode finishingMode = gameComp.getGameMode();
```

### Q5: 理智值下降太快怎么办？

```java
// 调整这些常数（在自己的 mod 中）

// 降低下降速度
// float MOOD_DRAIN = 0.5f / 4800;  // 原来是 1.0f / 4800

// 增加任务恢复量
// float MOOD_GAIN = 1.0f;  // 原来是 0.5f

// 增加任务间隔（减少任务压力）
// int MAX_TASK_COOLDOWN = 2400;  // 原来是 1200（60秒）
```

### Q6: 如何自定义经济系统的金钱来源？

```java
// 监听玩家特定事件，手动添加金钱

public void onCustomEvent(PlayerEntity player) {
    PlayerShopComponent shop = PlayerShopComponent.KEY.get(player);
    
    // 自定义条件下获得金钱
    if (someCondition) {
        shop.addToBalance(customAmount);
        shop.sync();
    }
}
```

### Q7: 狂暴模式结束后怎样处理玩家？

```java
PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(player);

// 在 tick 中检查
if (psycho.getPsychoTicks() == 0 && previousWasActive) {
    // 狂暴刚结束
    
    player.sendMessage(
        Text.literal("狂暴效果已消退"),
        false
    );
    
    // 恢复玩家状态
    // ...
    
    previousWasActive = false;
}
```

### Q8: 如何在狂暴期间监听护甲变化？

```java
// 方法 1：使用 Mixin 监听伤害事件

@Mixin(LivingEntity.class)
public class DamageListenerMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayerEntity player) {
            PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(player);
            
            if (psycho.getPsychoTicks() > 0 && psycho.getArmour() > 0) {
                // 护甲拦截了伤害
                System.out.println("护甲吸收伤害! 剩余: " + (psycho.getArmour() - 1));
            }
        }
    }
}

// 方法 2：主动检查护甲状态

public void checkArmourStatus(ServerPlayerEntity player) {
    PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(player);
    
    if (psycho.getPsychoTicks() > 0) {
        int armour = psycho.getArmour();
        int timeLeft = psycho.getPsychoTicks();
        
        if (armour == 0) {
            System.out.println("警告: 护甲已耗尽，下次伤害会取消狂暴!");
        } else {
            System.out.println("护甲剩余: " + armour + "层，时间: " + (timeLeft / 20) + "秒");
        }
    }
}
```

### Q9: 如何通过护甲值来确定玩家的危险程度？

```java
public String getBerserkThreatLevel(ServerPlayerEntity player) {
    PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(player);
    
    if (psycho.getPsychoTicks() <= 0) {
        return "无威胁 - 不在狂暴状态";
    }
    
    int armour = psycho.getArmour();
    int time = psycho.getPsychoTicks() / 20;  // 转换为秒
    
    if (armour >= 3) {
        return "致命威胁 - 有" + armour + "层护甲，还有" + time + "秒";
    } else if (armour == 2) {
        return "高威胁 - 有" + armour + "层护甲，还有" + time + "秒";
    } else if (armour == 1) {
        return "中等威胁 - 有" + armour + "层护甲，还有" + time + "秒";
    } else {
        return "低威胁 - 护甲已耗尽，狂暴即将结束";
    }
}
```

### Q10: 如何为其他 mod 提供护甲增强道具？

```java
// 创建护甲增强物品的回调

public class ArmorEnhancementItem extends Item {
    public ArmorEnhancementItem(Item.Settings settings) {
        super(settings);
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, 
            PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player && !world.isClient) {
            PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(player);
            
            if (psycho.getPsychoTicks() > 0) {
                // 增加护甲
                int currentArmour = psycho.getArmour();
                if (currentArmour < 5) {  // 最多 5 层
                    psycho.setArmour(currentArmour + 1);
                    psycho.sync();
                    
                    user.sendMessage(
                        Text.literal("护甲强化! 当前: " + (currentArmour + 1)),
                        false
                    );
                    
                    ItemStack itemStack = user.getStackInHand(hand);
                    itemStack.decrementUnlessCreative(1, player);
                    
                    return TypedActionResult.success(itemStack);
                }
            }
        }
        
        return TypedActionResult.pass(user.getStackInHand(hand));
    }
}
```

### Q8: 如何跨服务器保存玩家组件数据？

```java
// 组件使用 NBT 自动保存

@Override
public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
    tag.putInt("Balance", this.balance);
    // 添加更多属性...
}

@Override
public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.@NotNull WrapperLookup registryLookup) {
    this.balance = tag.getInt("Balance");
    // 读取更多属性...
}
```

---

## 总结

Wathe 的游戏系统设计清晰而模块化：

1. **理智值系统** - 追踪玩家心理状态
2. **经济系统** - 管理游戏内货币
3. **狂暴系统** - 提供临时能力威胁和护甲防御机制
4. **全局状态** - 通过 GameWorldComponent 管理

通过这些组件的有机结合，可以创建丰富的游戏体验。集成时遵循组件模式，正确使用 `sync()` 方法确保客户端和服务端数据同步。

---

**文档版本**: 2.1  
**最后更新**: 2026-01-09  
**维护者**: doctor4t
**护甲系统**: 已完整文档化
