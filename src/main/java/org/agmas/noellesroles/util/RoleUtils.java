package org.agmas.noellesroles.util;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;

import java.util.*;

public class RoleUtils {
    /**
     * 获取启用的Role对象列表
     * @return 启用的Role对象列表
     */
    public static List<Role> getEnabledRoleObjects() {
        HarpyModLoaderConfig config = HarpyModLoaderConfig.HANDLER.instance();
        List<Role> enabledRoles = new ArrayList<>();
        
        // 完全兼容ListRolesCommand的过滤逻辑
        for (Role role : TMMRoles.ROLES) {
            String roleName = role.identifier().getPath();
            if (!config.disabled.contains(roleName)) {
                enabledRoles.add(role);
            }
        }
        return enabledRoles;
    }

    /**
     * 获取启用的角色名字符串列表
     * @return 格式化后的角色名列表
     */
    public static List<String> getEnabledRoleNames() {
        List<Role> roleObjects = getEnabledRoleObjects();
        List<String> roleNames = new ArrayList<>(roleObjects.size());
        
        // 完全兼容ListRolesCommand的格式化逻辑
        for (Role role : roleObjects) {
            roleNames.add(formatRoleName(role));
        }
        return roleNames;
    }

    /**
     * 获取启用的角色名与Role对象的映射
     * @return Map<角色名字符串, Role对象>
     */
    public static Map<String, Role> getEnabledRoleMap() {
        List<Role> roleObjects = getEnabledRoleObjects();
        Map<String, Role> roleMap = new LinkedHashMap<>(roleObjects.size());
        
        for (Role role : roleObjects) {
            String friendlyName = formatRoleName(role);
            roleMap.put(friendlyName, role);
        }
        return roleMap;
    }
    /**
     * 获取启用的平民角色名与Role对象的映射
     * @return Map<平民角色名字符串, Role对象>
     */
    public static Map<String, Role> getEnabledInnocentRoleMap() {
        List<Role> innocentRoleObjects = getEnabledInnocentRoles();
        Map<String, Role> innocentRoleMap = new LinkedHashMap<>(innocentRoleObjects.size());
        
        for (Role role : innocentRoleObjects) {
            String friendlyName = formatRoleName(role);
            innocentRoleMap.put(friendlyName, role);
        }
        return innocentRoleMap;
    }
    /**
     * 获取启用的平民角色列表
     * @return 启用的平民角色对象列表
     */
    public static List<Role> getEnabledInnocentRoles() {
        HarpyModLoaderConfig config = HarpyModLoaderConfig.HANDLER.instance();
        List<Role> innocentRoles = new ArrayList<>();
        
        for (Role role : TMMRoles.ROLES) {
            String roleName = role.identifier().getPath();
            // 同时满足：未被禁用 + 是平民角色
            if (!config.disabled.contains(roleName)&& !roleName.equals("discovery_civilian") && role.isInnocent()) {
                innocentRoles.add(role);
            }
        }
        return innocentRoles;
    }

    /**
     * 格式化角色名（命名牌显示用）
     * @param role Role对象
     * @return 格式化后的名字
     */
    public static String formatRoleName(Role role) {
        String baseName = role.identifier().getPath();
        return baseName.replace('_', ' ')
                       .substring(0, 1).toUpperCase() +
                       baseName.substring(1);
    }

    /**
     * 检查角色是否启用
     * @param roleName 角色标识符路径
     * @return 是否启用
     */
    public static boolean isRoleEnabled(String roleName) {
        HarpyModLoaderConfig config = HarpyModLoaderConfig.HANDLER.instance();
        return !config.disabled.contains(roleName);
    }
}
