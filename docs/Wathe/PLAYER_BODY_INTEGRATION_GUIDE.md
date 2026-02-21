# Wathe 玩家尸体系统集成指南

**项目**: Wathe Minecraft Fabric 模组 - 社交推理游戏  
**版本**: 基于当前 main 分支  
**语言**: Java 21+  
**框架**: Minecraft Fabric 1.20.5+

---

## 目录

1. [概述](#概述)
2. [PlayerBodyEntity](#playerbodyentity)
3. [尸体生命周期](#尸体生命周期)
4. [与尸体交互](#与尸体交互)
5. [尸体渲染系统](#尸体渲染系统)
6. [事件和回调](#事件和回调)
7. [集成代码示例](#集成代码示例)
8. [常见问题](#常见问题)

---

## 概述

当玩家在游戏中死亡时，会在死亡位置生成一个 **PlayerBodyEntity**（玩家尸体实体）。尸体是一个特殊的 LivingEntity，具有以下特性：

- **无敌性**: 尸体对大多数伤害免疫（除了虚空伤害和通用杀死伤害）
- **分解机制**: 尸体会随时间推移逐渐分解和消失
- **视觉变化**: 从完整的玩家身体逐渐变成骨架
- **交互性**: 可以使用尸体袋（BodyBag）与尸体交互
- **身份追踪**: 每个尸体关联一个玩家 UUID，用于识别死亡的玩家

### 关键特性

| 特性 | 描述 |
|------|------|
| 生成位置 | 玩家死亡时在死亡位置前方 |
| 尺寸 | 1.0 × 0.25（宽 × 高） |
| 追踪范围 | 128 格块 |
| 体力值 | 999999.0（实际上无法伤害） |
| 初始状态 | 完整尸体 |
| 最终状态 | 骨架 → 消失 |

---

## PlayerBodyEntity

### 类定义

**位置**: [src/main/java/dev/doctor4t/wathe/entity/PlayerBodyEntity.java](src/main/java/dev/doctor4t/wathe/entity/PlayerBodyEntity.java)

```java
public class PlayerBodyEntity extends LivingEntity {
    // 玩家 UUID 的追踪数据
    private static final TrackedData<Optional<UUID>> PLAYER = 
        DataTracker.registerData(PlayerBodyEntity.class, 
                                 TrackedDataHandlerRegistry.OPTIONAL_UUID);
    
    // 构造函数
    public PlayerBodyEntity(EntityType<? extends LivingEntity> entityType, World world)
    
    // 获取关联的玩家 UUID
    public UUID getPlayerUuid()
    
    // 设置关联的玩家 UUID
    public void setPlayerUuid(UUID playerUuid)
    
    // 持久化数据（NBT）
    public void writeCustomDataToNbt(NbtCompound nbt)
    public void readCustomDataFromNbt(NbtCompound nbt)
}
```

### 关键方法

#### 获取玩家身份

```java
PlayerBodyEntity body = ...;  // 世界中的尸体实体

// 获取死亡玩家的 UUID
UUID playerUuid = body.getPlayerUuid();

// 转换为玩家对象（如果玩家在线）
ServerPlayerEntity originalPlayer = server.getPlayerManager().getPlayer(playerUuid);
if (originalPlayer != null) {
    System.out.println("尸体属于: " + originalPlayer.getName().getString());
} else {
    System.out.println("玩家已离线，UUID: " + playerUuid);
}
```

#### 检查尸体有效性

```java
PlayerBodyEntity body = ...;

// 尸体的年龄（Tick 数）
int age = body.age;

// 尸体的位置
Vec3d pos = body.getPos();

// 尸体的世界
World world = body.getWorld();

// 检查是否在客户端
if (body.getWorld().isClient) {
    // 客户端逻辑
} else {
    // 服务端逻辑
}
```

### 尸体属性

```java
// 不返回装备物品（尸体不穿装备）
@Override
public Iterable<ItemStack> getArmorItems() {
    return null;
}

// 不允许装备
@Override
public ItemStack getEquippedStack(EquipmentSlot slot) {
    return ItemStack.EMPTY;
}

// 始终无敌
@Override
public boolean isInvulnerable() {
    return true;
}

// 对除虚空伤害和通用杀死伤害外的所有伤害免疫
@Override
public boolean isInvulnerableTo(DamageSource damageSource) {
    return !damageSource.isOf(DamageTypes.GENERIC_KILL) && 
           !damageSource.isOf(DamageTypes.OUT_OF_WORLD);
}
```

---

## 尸体生命周期

### 时间常数

```java
// 尸体在开始分解前的存在时间
int TIME_TO_DECOMPOSITION = 60 ticks;  // 3 秒

// 尸体分解的持续时间
int DECOMPOSING_TIME = 80 ticks;  // 4 秒

// 总存在时间
int TOTAL_LIFETIME = TIME_TO_DECOMPOSITION + DECOMPOSING_TIME;  // 140 ticks (7 秒)
```

### 生命周期阶段

```
生成 (age = 0)
  ↓
完整阶段 (0 ~ 60 ticks)
  - 显示完整的玩家尸体模型
  - 可以使用尸体袋移除
  ↓
分解阶段 (60 ~ 140 ticks)
  - 尸体逐渐变透明和下沉
  - 从玩家身体模型过渡到骨架模型
  - 依然可以使用尸体袋移除
  ↓
消失 (age > 140 ticks)
  - 尸体自动从世界移除
```

### 视觉变化

尸体的渲染会根据 age 值变化：

```java
// PlayerBodyEntityRenderer.java 中的实现
float clamp = MathHelper.clamp(
    (float) (playerBodyEntity.age - GameConstants.TIME_TO_DECOMPOSITION) / 
    GameConstants.DECOMPOSING_TIME, 
    0, 
    GameConstants.TIME_TO_DECOMPOSITION + GameConstants.DECOMPOSING_TIME
);
float easeDown = Easing.CUBIC_IN.ease(clamp, 0, -1, 1);

// age 值与视觉效果的对应关系：
// 0-60:    完整尸体，Y轴无变化
// 60-140:  逐渐下沉（Y轴负向移动）并变透明
// 140+:    尸体消失
```

---

## 与尸体交互

### 获取世界中的所有尸体

```java
public List<PlayerBodyEntity> getAllBodies(ServerWorld world) {
    return world.getEntitiesByType(
        WatheEntities.PLAYER_BODY,
        PlayerBodyEntity::new
    );
}

// 使用示例
List<PlayerBodyEntity> bodies = getAllBodies(world);
for (PlayerBodyEntity body : bodies) {
    System.out.println("尸体年龄: " + body.age + " ticks");
    System.out.println("尸体位置: " + body.getPos());
    System.out.println("原玩家: " + body.getPlayerUuid());
}
```

### 移除尸体

```java
// 方法 1：直接移除
PlayerBodyEntity body = ...;
body.discard();  // 立即从世界移除

// 方法 2：使用尸体袋（游戏内方式）
PlayerEntity user = ...;
ItemStack bodyBag = new ItemStack(WatheItems.BODY_BAG);

// 这会调用 BodyBagItem.useOnEntity()
if (body.isAlive()) {
    body.discard();
    // 播放声音效果
    world.playSound(null, body.getX(), body.getY() + 0.1, body.getZ(),
                    SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 0.5f, 1f);
    // 消耗物品
    bodyBag.decrement(1);
}
```

### 获取尸体信息

```java
PlayerBodyEntity body = ...;
ServerPlayerManager playerManager = server.getPlayerManager();

// 获取死亡玩家的信息
UUID playerUuid = body.getPlayerUuid();
ServerPlayerEntity deadPlayer = playerManager.getPlayer(playerUuid);

if (deadPlayer != null) {
    String playerName = deadPlayer.getName().getString();
    System.out.println("死亡玩家: " + playerName);
} else {
    // 玩家已离线，需要从UUID获取名字
    // 可能需要查询数据库或缓存
    System.out.println("玩家已离线，UUID: " + playerUuid);
}

// 获取死亡位置
Vec3d deathPos = body.getPos();
System.out.println("死亡位置: X=" + deathPos.x + ", Y=" + deathPos.y + ", Z=" + deathPos.z);

// 获取分解进度（0 = 新鲜, 1 = 完全分解）
float decompositionProgress = Math.min(1.0f, 
    (float) (body.age - GameConstants.TIME_TO_DECOMPOSITION) / 
    GameConstants.DECOMPOSING_TIME
);
System.out.println("分解进度: " + (decompositionProgress * 100) + "%");
```

### 为尸体添加元数据

虽然 PlayerBodyEntity 本身不存储额外数据，但你可以使用其他机制来关联额外信息：

```java
// 方法 1：使用 NBT 数据（需要自定义组件）
NbtCompound tag = new NbtCompound();
body.writeCustomDataToNbt(tag);
tag.putString("deathCause", "stabbed_with_knife");
tag.putLong("deathTime", world.getTime());
// 再次写入时保留这些数据

// 方法 2：创建关联的数据结构
static class DeathRecord {
    UUID playerUuid;
    long deathTime;
    Vec3d deathPos;
    String deathReason;
    @Nullable ServerPlayerEntity killer;
}

// 保存所有死亡记录
static Map<UUID, DeathRecord> deathRecords = new HashMap<>();

public void recordDeath(PlayerBodyEntity body, ServerPlayerEntity killer, String reason) {
    UUID playerUuid = body.getPlayerUuid();
    deathRecords.put(playerUuid, new DeathRecord(
        playerUuid,
        body.getWorld().getTime(),
        body.getPos(),
        reason,
        killer
    ));
}
```

---

## 尸体渲染系统

### 渲染管道

尸体的渲染分为两部分：

1. **完整尸体（完全不透明）**
   - 使用 PlayerEntityModel 渲染
   - 显示玩家的完整身体

2. **骨架（半透明）**
   - 使用 PlayerSkeletonEntityModel 渲染
   - 在完整尸体淡出后显示
   - 用于表示分解过程

### 自定义尸体渲染

```java
// 如果想在尸体上添加自定义效果：

@Mixin(PlayerBodyEntityRenderer.class)
public class CustomBodyRendererMixin {
    
    @Inject(method = "render", at = @At("TAIL"))
    private void renderCustomEffects(
        PlayerBodyEntity body, float yaw, float tickDelta, 
        MatrixStack matrices, VertexConsumerProvider consumers, int light,
        CallbackInfo ci
    ) {
        // 添加自定义渲染逻辑
        // 例如：添加粒子效果、发光效果等
        
        if (body.age < GameConstants.TIME_TO_DECOMPOSITION) {
            // 新鲜尸体：添加鲜血粒子
            // ...
        } else {
            // 分解中的尸体：添加魂魄/分解粒子
            // ...
        }
    }
}
```

### 获取尸体的视觉状态

```java
public class BodyVisualState {
    public float getBodyAlpha(PlayerBodyEntity body) {
        float progress = Math.min(1.0f,
            (float) (body.age - GameConstants.TIME_TO_DECOMPOSITION) / 
            GameConstants.DECOMPOSING_TIME
        );
        
        // progress = 0 → alpha = 1.0（完全不透明）
        // progress = 1 → alpha = 0.0（完全透明）
        return 1.0f - progress;
    }
    
    public float getSkeletonAlpha(PlayerBodyEntity body) {
        float progress = Math.min(1.0f,
            (float) (body.age - GameConstants.TIME_TO_DECOMPOSITION) / 
            GameConstants.DECOMPOSING_TIME
        );
        
        // progress = 0 → alpha = 0.0（完全透明）
        // progress = 1 → alpha = 1.0（完全不透明）
        return progress;
    }
    
    public Vec3d getBodyPosition(PlayerBodyEntity body) {
        float progress = Math.min(1.0f,
            (float) (body.age - GameConstants.TIME_TO_DECOMPOSITION) / 
            GameConstants.DECOMPOSING_TIME
        );
        
        // 分解时向下移动
        float easeDown = Easing.CUBIC_IN.ease(progress, 0, -1, 1);
        return body.getPos().add(0, easeDown, 0);
    }
}
```

---

## 事件和回调

### 尸体生成事件

当玩家死亡且生成尸体时：

```java
// 在 GameFunctions.killPlayer() 中
public static void killPlayer(PlayerEntity victim, boolean spawnBody, 
                              @Nullable PlayerEntity killer, 
                              Identifier deathReason) {
    // ... 其他逻辑 ...
    
    if (spawnBody) {
        // 创建尸体实体
        PlayerBodyEntity body = WatheEntities.PLAYER_BODY.create(victim.getWorld());
        if (body != null) {
            // 设置关联的玩家
            body.setPlayerUuid(victim.getUuid());
            
            // 计算生成位置（在玩家前方）
            Vec3d spawnPos = victim.getPos().add(
                victim.getRotationVector().normalize().multiply(1)
            );
            body.refreshPositionAndAngles(
                spawnPos.getX(), victim.getY(), spawnPos.getZ(), 
                victim.getHeadYaw(), 0f
            );
            body.setYaw(victim.getHeadYaw());
            body.setHeadYaw(victim.getHeadYaw());
            
            // 添加到世界
            victim.getWorld().spawnEntity(body);
        }
    }
}
```

### 监听尸体生成

```java
// 使用事件回调

public class BodySpawnListener {
    public static void onEntityAdd(World world, Entity entity) {
        if (entity instanceof PlayerBodyEntity body) {
            UUID playerUuid = body.getPlayerUuid();
            System.out.println("尸体生成! 玩家: " + playerUuid);
            
            // 触发自定义事件
            onBodySpawned(body);
        }
    }
    
    private static void onBodySpawned(PlayerBodyEntity body) {
        // 你的自定义逻辑
    }
}
```

### 尸体移除事件

```java
// 监听尸体被移除（分解或尸体袋）

@Mixin(PlayerBodyEntity.class)
public class BodyRemovalMixin {
    @Inject(method = "discard", at = @At("HEAD"))
    private void onBodyDiscard(CallbackInfo ci) {
        PlayerBodyEntity self = (PlayerBodyEntity) (Object) this;
        UUID playerUuid = self.getPlayerUuid();
        
        // 记录尸体移除事件
        System.out.println("尸体移除: " + playerUuid);
    }
}
```

---

## 集成代码示例

### 示例 1：创建一个尸体追踪系统

```java
public class BodyTracker {
    private final Map<UUID, BodyRecord> activeBodyRecords = new HashMap<>();
    
    static class BodyRecord {
        UUID playerUuid;
        long spawnTime;
        Vec3d spawnPos;
        @Nullable ServerPlayerEntity killer;
        
        BodyRecord(UUID playerUuid, long spawnTime, Vec3d spawnPos, 
                  @Nullable ServerPlayerEntity killer) {
            this.playerUuid = playerUuid;
            this.spawnTime = spawnTime;
            this.spawnPos = spawnPos;
            this.killer = killer;
        }
        
        public long getTimeSinceDeath(long currentTime) {
            return currentTime - spawnTime;
        }
        
        public boolean isDecomposing(long currentTime) {
            return getTimeSinceDeath(currentTime) > GameConstants.TIME_TO_DECOMPOSITION;
        }
        
        public boolean isGone(long currentTime) {
            return getTimeSinceDeath(currentTime) > 
                   (GameConstants.TIME_TO_DECOMPOSITION + GameConstants.DECOMPOSING_TIME);
        }
    }
    
    public void registerBody(PlayerBodyEntity body, @Nullable ServerPlayerEntity killer) {
        activeBodyRecords.put(body.getPlayerUuid(), 
            new BodyRecord(body.getPlayerUuid(), body.getWorld().getTime(),
                          body.getPos(), killer));
    }
    
    public void tick(ServerWorld world) {
        long currentTime = world.getTime();
        
        activeBodyRecords.entrySet().removeIf(entry -> {
            BodyRecord record = entry.getValue();
            
            if (record.isGone(currentTime)) {
                System.out.println("尸体完全分解: " + entry.getKey());
                return true;
            }
            
            if (record.isDecomposing(currentTime)) {
                // 定期清理已分解的尸体
            }
            
            return false;
        });
    }
    
    public BodyRecord getBody(UUID playerUuid) {
        return activeBodyRecords.get(playerUuid);
    }
}
```

### 示例 2：为尸体系统添加装饰效果

```java
public class BodyEffects {
    public static void addDeathEffects(PlayerBodyEntity body, ServerWorld world) {
        Vec3d pos = body.getPos();
        
        // 生成鲜血粒子
        for (int i = 0; i < 10; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 2;
            double offsetY = world.random.nextDouble() * 2;
            double offsetZ = (world.random.nextDouble() - 0.5) * 2;
            
            world.spawnParticles(
                ParticleTypes.DUST,  // 或其他粒子
                pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                1, 0, 0, 0, 0
            );
        }
        
        // 播放声音
        world.playSound(null, pos.x, pos.y, pos.z,
                       SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.NEUTRAL,
                       1.0f, 1.0f);
    }
    
    public static void addDecompositionEffects(PlayerBodyEntity body, ServerWorld world) {
        if (body.age % 20 == 0 && body.age > GameConstants.TIME_TO_DECOMPOSITION) {
            Vec3d pos = body.getPos();
            
            // 生成腐烂粒子
            for (int i = 0; i < 3; i++) {
                double offsetX = (world.random.nextDouble() - 0.5) * 1;
                double offsetY = world.random.nextDouble() + 1;
                double offsetZ = (world.random.nextDouble() - 0.5) * 1;
                
                world.spawnParticles(
                    ParticleTypes.FALLING_DUST,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    1, 0.1, 0.1, 0.1, 0.05
                );
            }
        }
    }
}
```

### 示例 3：检测特定玩家的尸体

```java
public class BodyDetection {
    public static @Nullable PlayerBodyEntity findBodyOfPlayer(ServerWorld world, UUID playerUuid) {
        // 搜索世界中该玩家的尸体
        return world.getEntitiesByType(WatheEntities.PLAYER_BODY)
            .stream()
            .filter(body -> body.getPlayerUuid().equals(playerUuid))
            .findFirst()
            .orElse(null);
    }
    
    public static List<PlayerBodyEntity> findBodiesTowardEntity(ServerWorld world, 
                                                                 Entity center, 
                                                                 double range) {
        // 找到范围内的所有尸体
        return world.getEntitiesByType(WatheEntities.PLAYER_BODY)
            .stream()
            .filter(body -> body.getPos().squaredDistanceTo(center.getPos()) <= range * range)
            .toList();
    }
    
    public static boolean isBodyFresh(PlayerBodyEntity body) {
        return body.age < GameConstants.TIME_TO_DECOMPOSITION;
    }
    
    public static boolean isBodyDecomposing(PlayerBodyEntity body) {
        return body.age >= GameConstants.TIME_TO_DECOMPOSITION &&
               body.age < (GameConstants.TIME_TO_DECOMPOSITION + GameConstants.DECOMPOSING_TIME);
    }
}
```

### 示例 4：与 ActionWizardry 集成（法术系统）

```java
public class BodySpellIntegration {
    public static void addBodyHiding(PlayerBodyEntity body) {
        // 使尸体对其他玩家隐形（需要客户端支持）
        // body.setInvisible(true);
    }
    
    public static void restrainBody(PlayerBodyEntity body) {
        // 让尸体无法被尸体袋移除（需要标记）
        // 方式1：编辑NBT数据
        // 方式2：创建尸体的包装实体
    }
    
    public static void makeBodyInvincible(PlayerBodyEntity body, boolean invincible) {
        if (!invincible) {
            // 允许伤害（但原本尸体本身总是无敌的）
        }
    }
    
    public static void animateBodyDecay(PlayerBodyEntity body, ServerWorld world) {
        // 加快分解速度
        if (body.age > GameConstants.TIME_TO_DECOMPOSITION) {
            // body.age += 增加值;
        }
    }
}
```

---

## 常见问题

### Q1: 如何在尸体上检测玩家身份？

```java
PlayerBodyEntity body = ...;
UUID playerUuid = body.getPlayerUuid();

// 尝试获取在线玩家
ServerPlayerManager manager = server.getPlayerManager();
ServerPlayerEntity player = manager.getPlayer(playerUuid);

if (player != null) {
    // 玩家在线
    System.out.println("玩家: " + player.getName().getString());
} else {
    // 玩家已离线
    // 需要查询玩家数据存储或缓存
    System.out.println("玩家已离线，UUID: " + playerUuid);
}
```

### Q2: 如何计算尸体的分解百分比？

```java
public float getDecompositionPercent(PlayerBodyEntity body) {
    int age = body.age;
    int startDecompose = GameConstants.TIME_TO_DECOMPOSITION;
    int endDecompose = startDecompose + GameConstants.DECOMPOSING_TIME;
    
    if (age < startDecompose) {
        return 0f;  // 完全新鲜
    } else if (age > endDecompose) {
        return 100f;  // 完全分解
    } else {
        float progress = (float) (age - startDecompose) / GameConstants.DECOMPOSING_TIME;
        return progress * 100f;
    }
}

// 使用示例
float percent = getDecompositionPercent(body);
System.out.println(String.format("分解进度: %.1f%%", percent));
```

### Q3: 如何确保尸体不会被自动删除？

```java
// 尸体会在 DECOMPOSING_TIME 后自动消失
// 如果需要保留尸体，可以：

// 方法 1：定期重置尸体的年龄
@Mixin(PlayerBodyEntity.class)
public class BodyPersistenceMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void preventDecay(CallbackInfo ci) {
        PlayerBodyEntity self = (PlayerBodyEntity) (Object) this;
        
        // 如果尸体即将消失，重置其年龄
        if (self.age > GameConstants.TIME_TO_DECOMPOSITION + GameConstants.DECOMPOSING_TIME - 20) {
            self.age = GameConstants.TIME_TO_DECOMPOSITION;
        }
    }
}

// 方法 2：定期创建新的尸体实体
public void refreshBody(PlayerBodyEntity oldBody, ServerWorld world) {
    PlayerBodyEntity newBody = WatheEntities.PLAYER_BODY.create(world);
    if (newBody != null) {
        newBody.setPlayerUuid(oldBody.getPlayerUuid());
        newBody.refreshPositionAndAngles(
            oldBody.getX(), oldBody.getY(), oldBody.getZ(),
            oldBody.getHeadYaw(), 0f
        );
        world.spawnEntity(newBody);
    }
    oldBody.discard();
}
```

### Q4: 尸体能否被其他 mod 伤害？

```java
// 不能。尸体的伤害免疫配置是硬编码的：

// 尸体总是返回 true
@Override
public boolean isInvulnerable() {
    return true;
}

// 尸体仅允许虚空伤害和通用杀死伤害
@Override
public boolean isInvulnerableTo(DamageSource damageSource) {
    return !damageSource.isOf(DamageTypes.GENERIC_KILL) && 
           !damageSource.isOf(DamageTypes.OUT_OF_WORLD);
}

// 如果其他 mod 需要伤害尸体，需要使用虚空伤害（OUT_OF_WORLD）
// 或通用杀死伤害（GENERIC_KILL），这些会导致尸体消失
```

### Q5: 如何自定义尸体的渲染？

```java
// 使用 Mixin 修改 PlayerBodyEntityRenderer 的行为

@Mixin(PlayerBodyEntityRenderer.class)
public class CustomBodyRenderMixin {
    
    @ModifyVariable(
        method = "render(Ldev/doctor4t/wathe/entity/PlayerBodyEntity;FFFFV",
        at = @At("STORE"),
        ordinal = 0
    )
    private float modifyAlpha(float original, PlayerBodyEntity body) {
        // 自定义透明度计算
        // 返回修改后的 alpha 值
        return original * 0.5f;  // 示例：半透明
    }
}
```

### Q6: 如何从 BodyBag 移除中恢复尸体？

```java
// BodyBagItem 使用的是 body.discard()
// 这是不可逆的操作

// 如果需要保护某个尸体不被尸体袋移除：

@Mixin(BodyBagItem.class)
public class BodyBagProtectionMixin {
    @Inject(method = "useOnEntity", at = @At("HEAD"), cancellable = true)
    private void preventRemoval(ItemStack stack, PlayerEntity user, 
                               LivingEntity entity, Hand hand, 
                               CallbackInfoReturnable<ActionResult> cir) {
        if (entity instanceof PlayerBodyEntity body) {
            // 检查是否受保护
            if (isBodyProtected(body)) {
                user.sendMessage(Text.literal("这具尸体无法移除!"), true);
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
    
    private boolean isBodyProtected(PlayerBodyEntity body) {
        // 你的保护逻辑
        return false;
    }
}
```

### Q7: 如何在尸体出现时发送通知？

```java
@Mixin(EntitySpawnS2CPacket.class)
public class BodySpawnNotificationMixin {
    @Inject(method = "apply", at = @At("HEAD"))
    private void onEntitySpawn(ClientPlayPacketListener listener, CallbackInfo ci) {
        // 客户端检测尸体生成
        if (this.entity instanceof PlayerBodyEntity body) {
            System.out.println("检测到尸体!");
        }
    }
}

// 服务端通知
public void notifyBodySpawned(PlayerBodyEntity body, ServerWorld world) {
    for (ServerPlayerEntity player : world.getPlayers(
            entity -> entity.getPos().isInRange(body.getPos(), 64))) {
        player.sendMessage(
            Text.literal("有人死了!").styled(s -> s.withColor(0xFF0000)),
            false
        );
    }
}
```

---

## 与其他系统的交互

### 与经济系统的交互

```java
// 尸体袋在购买时花费 200 元
// 使用尸体袋时有 5 分钟的冷却时间

int BODY_BAG_PRICE = 200;
int BODY_BAG_COOLDOWN = 6000;  // ticks (5 分钟)

// 验证玩家是否可以使用尸体袋
public boolean canUseBodyBag(ServerPlayerEntity player) {
    return !player.getItemCooldownManager().isCoolingDown(WatheItems.BODY_BAG) &&
           PlayerShopComponent.KEY.get(player).balance >= BODY_BAG_PRICE;
}
```

### 与游戏事件的交互

```java
// 玩家死亡时：
// 1. PlayerMood 重置为默认值
// 2. 尸体在死亡位置生成
// 3. 玩家变为旁观者模式
// 4. 分红和 money 分配给杀手

GameFunctions.killPlayer(victim, true, killer, deathReason);
```

---

**文档版本**: 1.0  
**最后更新**: 2026-01-09  
**维护者**: doctor4t
