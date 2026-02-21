package org.agmas.noellesroles.registry;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * 中心化 Role / Modifier 注册器。
 *
 * 用法：在注册角色或词条时通过此类进行注册并声明其是否包含 HUD/Shop/Ability。
 * 旧的直接注册调用会被注释保留以便回滚参考。
 */
public final class RoleModifierRegistry {

    public static class Meta {
        public final boolean isRole;
        public final boolean hasHud;
        public final boolean hasShop;
        public final boolean hasAbility;
        public final Identifier id;

        public Meta(Identifier id, boolean isRole, boolean hasHud, boolean hasShop, boolean hasAbility) {
            this.id = id;
            this.isRole = isRole;
            this.hasHud = hasHud;
            this.hasShop = hasShop;
            this.hasAbility = hasAbility;
        }
    }

    private static final Map<String, Meta> registry = new HashMap<>();

    private RoleModifierRegistry() {}

    public static Role registerRole(Role role, boolean hasHud, boolean hasShop, boolean hasAbility) {
        Role registered = WatheRoles.registerRole(role);
        Identifier id = registered.identifier();
        registry.put(id.toString(), new Meta(id, true, hasHud, hasShop, hasAbility));
        return registered;
    }

    public static Modifier registerModifier(Identifier id, Modifier modifier, boolean hasHud, boolean hasShop, boolean hasAbility) {
        // 如果 modifier 的 cannotBeAppliedTo 未配置，则自动生成一个基于已注册 Role 元数据的冲突列表。
        try {
            if (modifier.cannotBeAppliedTo == null || modifier.cannotBeAppliedTo.isEmpty()) {
                java.util.ArrayList<Role> hudConflicts = new java.util.ArrayList<>();
                java.util.ArrayList<Role> shopConflicts = new java.util.ArrayList<>();
                java.util.ArrayList<Role> abilityConflicts = new java.util.ArrayList<>();

                for (Role r : WatheRoles.ROLES) {
                    Meta m = registry.get(r.identifier().toString());
                    if (m == null) continue;
                    // 分别检测三种冲突类型，并分别收集
                    if (hasHud && m.hasHud) hudConflicts.add(r);
                    if (hasShop && m.hasShop) shopConflicts.add(r);
                    if (hasAbility && m.hasAbility) abilityConflicts.add(r);
                }

                // 合并三组冲突，去重并保持顺序
                java.util.LinkedHashSet<Role> combined = new java.util.LinkedHashSet<>();
                combined.addAll(hudConflicts);
                combined.addAll(shopConflicts);
                combined.addAll(abilityConflicts);

                if (!combined.isEmpty()) {
                    modifier.setCannotBeAppliedTo(new java.util.ArrayList<>(combined));
                }
            }
        } catch (Exception e) {
            // 在生成冲突列表时出现问题，保守起见不改动 modifier
        }

        Modifier registered = HMLModifiers.registerModifier(modifier);
        registry.put(id.toString(), new Meta(id, false, hasHud, hasShop, hasAbility));
        return registered;
    }

    public static Meta getMeta(Identifier id) {
        return registry.get(id.toString());
    }

    public static boolean hasHud(Identifier id) {
        Meta m = getMeta(id);
        return m != null && m.hasHud;
    }

    public static boolean hasShop(Identifier id) {
        Meta m = getMeta(id);
        return m != null && m.hasShop;
    }

    public static boolean hasAbility(Identifier id) {
        Meta m = getMeta(id);
        return m != null && m.hasAbility;
    }
}
