# THIEF (小偷) 角色实现文档

## 概述
THIEF是一个平民方角色，具有独特的**身份窃取能力**。他可以通过击杀其他玩家来窃取被击杀者的角色身份，以此迷惑其他玩家。

## 角色基本属性

| 属性 | 值 |
|------|-----|
| **颜色** | 灰色 (RGB: 100, 100, 100) |
| **是否可欺骗** | 否 |
| **是否会发光** | 否 |
| **阵营类型** | 真实阵营 (REAL) |
| **冲刺时间** | 与平民相同 |
| **角色ID** | `noellesroles:thief` |

## 核心机制

### 身份窃取 (Identity Stealing)

**触发条件:**
- THIEF必须击杀其他玩家
- THIEF在游戏中只能窃取**一次**身份

**效果:**
- 被击杀者的身份会被添加给THIEF
- 在其他玩家眼中，THIEF现在会显示为被击杀者的角色
- THIEF保留平民方的基本能力限制

**实现细节:**
```java
// ThiefPlayerComponent 追踪状态
public boolean hasStolen = false;        // 是否已窃取
public Identifier stolenIdentity = null; // 被窃取的身份ID
```

## 战略与玩法

### THIEF的优势 ✓

1. **伪装能力**
   - 成功窃取后可以完美伪装成其他角色
   - 可以误导杀手和其他平民

2. **获取信息**
   - 通过击杀和窃取身份了解游戏进展
   - 可以推断出有哪些特殊角色

3. **社交优势**
   - 作为"被窃取身份的角色"获得信任
   - 可以深入杀手阵营或其他群体

### THIEF的劣势 ✗

1. **有限的使用次数**
   - 整个游戏只能窃取一次身份
   - 需要谨慎选择目标

2. **高风险的触发条件**
   - 需要击杀其他玩家
   - 击杀本身会暴露位置和意图
   - 在火车上容易被发现

3. **身份限制**
   - 无法获得被窃取角色的特殊能力
   - 只是在其他人眼中显示为该角色

4. **时间窗口**
   - 被击杀者可能已经暴露了身份信息
   - 其他玩家可能已经知道该角色的真实身份

## 代码实现

### 文件结构

```
thief/
├── ThiefPlayerComponent.java    # THIEF数据组件
└── THIEF_README.md               # 本文档
```

### ThiefPlayerComponent 主要方法

```java
// 偷取目标身份
public void stealIdentity(Identifier targetIdentity) {
    // 查找对应的Role对象
    // 调用 gameWorldComponent.addRole() 添加该角色
    // 设置 hasStolen = true 防止再次窃取
}

// 重置状态（角色初始化时）
public void reset() {
    this.hasStolen = false;
    this.stolenIdentity = null;
}
```

### 事件集成

**AllowPlayerDeath 事件**
```java
// 检查击杀者是否为THIEF
if (playerEntityKiller != null && gameWorldComponent.isRole(playerEntityKiller, Noellesroles.THIEF)) {
    ThiefPlayerComponent thiefComponent = ThiefPlayerComponent.KEY.get(playerEntityKiller);
    if (!thiefComponent.hasStolen) {
        // 获取被击杀者的身份并窃取
        Identifier victimRole = gameWorldComponent.getRole(playerEntityVictim.getUuid()).identifier();
        thiefComponent.stealIdentity(victimRole);
    }
}
```

**ModdedRoleAssigned 事件**
```java
if (role.equals(THIEF)) {
    ThiefPlayerComponent thiefComponent = ThiefPlayerComponent.KEY.get(player);
    thiefComponent.reset();  // 初始化状态
}
```

## 数据持久化

THIEF的状态通过NBT数据保存和恢复：

```nbt
{
  hasStolen: 1b,
  stolenIdentity: "noellesroles:jester"
}
```

## 潜在的扩展方案

### 增强版本 (v2.0)
1. **多次窃取** - 允许窃取多个身份，但需要冷却时间
2. **身份融合** - 显示多个身份的混合特征
3. **能力传递** - 可选是否获得被窃取身份的部分能力
4. **反制机制** - 其他角色可以检测或追踪被盗身份

### 兼容性考虑
- 与Morphling(变身者)的交互规则
- 与Detective(侦探)的检测机制
- 与Coroner(验尸官)的死亡信息

## 平衡考虑

| 因素 | 说明 |
|------|------|
| **易用性** | 中等 - 需要击杀才能激活 |
| **强度** | 低-中等 - 仅获得伪装效果 |
| **对游戏的影响** | 中等 - 增加社交混乱和谍报价值 |
| **随机性** | 低 - 玩家选择，但被害者是随机的 |

## 调试建议

启用以下方式来测试THIEF功能：

1. **日志输出**
```java
System.out.println("[THIEF] " + player.getName().getString() + " stole identity: " + targetIdentity);
```

2. **状态检查**
```java
ThiefPlayerComponent thiefComp = ThiefPlayerComponent.KEY.get(player);
System.out.println("Has stolen: " + thiefComp.hasStolen);
System.out.println("Stolen identity: " + thiefComp.stolenIdentity);
```

3. **事件验证**
- 确保AllowPlayerDeath事件正确触发
- 验证击杀者的检查逻辑
- 检查addRole()是否成功执行

## 总结

THIEF角色为NoellesRoles模组增加了一个独特的社交型游戏玩法。通过身份窃取机制，玩家需要在击杀和伪装之间平衡，为游戏增加策略深度和欺骗元素。

