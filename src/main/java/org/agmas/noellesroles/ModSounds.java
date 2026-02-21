package org.agmas.noellesroles;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * 模组音效注册表
 */
public class ModSounds {
    public static final SoundEvent BRAWLER_ABILITY = registerSoundEvent("brawler_ability");
    public static final SoundEvent BRAWLER_HIT = registerSoundEvent("brawler_hit");
    public static final SoundEvent PICKPOCKET_SUCCESS = registerSoundEvent("pickpocket_success");
    public static final SoundEvent PICKPOCKET_FAIL = registerSoundEvent("pickpocket_fail");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of(Noellesroles.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void initialize() {
        // 静态初始化时自动注册
    }
}
