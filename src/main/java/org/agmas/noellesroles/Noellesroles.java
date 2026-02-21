package org.agmas.noellesroles;

import java.util.concurrent.ThreadLocalRandom;

import dev.doctor4t.wathe.Wathe;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.*;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.client.util.WatheItemTooltips;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.api.event.AllowPlayerDeath;
import dev.doctor4t.wathe.api.event.AllowPlayerPunching;
import dev.doctor4t.wathe.api.event.CanSeePoison;
import dev.doctor4t.wathe.api.event.ShouldDropOnDeath;
// import dev.doctor4t.wathe.api.SprintingTicksAccessor;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.WatheParticles;
import dev.doctor4t.wathe.index.WatheSounds;
import dev.doctor4t.wathe.util.AnnounceWelcomePayload;
import dev.doctor4t.wathe.util.GunShootPayload;
import dev.doctor4t.wathe.util.Scheduler;
import dev.doctor4t.wathe.util.ShopEntry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Vec3d;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.harpymodloader.events.ModifierAssigned;
import org.agmas.harpymodloader.events.ModifierRemoved;
import org.agmas.harpymodloader.events.ResetPlayerEvent;
import org.agmas.harpymodloader.modifiers.HMLModifiers;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.agmas.noellesroles.bartender.BartenderPlayerComponent;
import org.agmas.noellesroles.brawler.BrawlerChargeHandler;
import org.agmas.noellesroles.brawler.BrawlerPlayerComponent;
import org.agmas.noellesroles.pickpocket.PickpocketPlayerComponent;
import org.agmas.noellesroles.gambler.GamblerPlayerComponent;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.coroner.BodyDeathReasonComponent;
import org.agmas.noellesroles.detective.DetectivePlayerComponent;
import org.agmas.noellesroles.executioner.ExecutionerPlayerComponent;
import org.agmas.noellesroles.framing.FramingShopEntry;
import org.agmas.noellesroles.morphling.MorphlingPlayerComponent;
import org.agmas.noellesroles.packet.*;
import org.agmas.noellesroles.recaller.RecallerPlayerComponent;
import org.agmas.noellesroles.sniper.SniperPlayerComponent;
import org.agmas.noellesroles.util.RoleUtils;
import org.agmas.noellesroles.util.Effects;
import org.agmas.noellesroles.util.PlayerBodyCreater;
import org.agmas.noellesroles.voodoo.VoodooPlayerComponent;
import org.agmas.noellesroles.vulture.VulturePlayerComponent;
import org.agmas.noellesroles.thief.ThiefPlayerComponent;
import org.agmas.noellesroles.hypnotist.HypnotistPlayerComponent;

import org.agmas.noellesroles.registry.RoleModifierRegistry;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.List;

public class Noellesroles implements ModInitializer {

    // 跟踪因 Scarecrow 赋予黑暗效果而需要循环播放心跳音的玩家
    public static final Map<UUID, Integer> SCARECROW_HEARTBEAT_COOLDOWNS = new HashMap<>();
    // 跟踪正在播放的 DJ：剩余播放时长（ticks）
    public static final Map<UUID, Integer> DJ_PLAYING_REMAINING_TICKS = new HashMap<>();
    // 跟踪每个正在播放的 DJ 的下一次检测倒计时（ticks），每 100 tick 检查一次（约 5 秒）
    public static final Map<UUID, Integer> DJ_PLAY_CHECK_COOLDOWNS = new HashMap<>();
    // 标记玩家当前一次 mood 改变的来源（例如 "DJ"），供混入拦截使用
    public static final java.util.Map<UUID, String> PLAYER_MOOD_CHANGE_SOURCE = new java.util.concurrent.ConcurrentHashMap<>();

    public static String MOD_ID = "noellesroles";
    // // 集合：方便查找和罗列
    // public static final Map<Identifier, Role> ROLE_MAP = new HashMap<>();
    // // 专门存附属模组角色
    // public static final List<Role> MOD_ROLES = new ArrayList<>();
    // public static final List<Identifier> MOD_ROLE_IDS = new ArrayList<>();
    // // 注册函数
    // public static Role registerModRole(Identifier id, Role role) {
    //     ROLE_MAP.put(id, role);
    //     MOD_ROLES.add(role);
    //     MOD_ROLE_IDS.add(id);
    //     return role;
    // }

    
    public static Identifier JESTER_ID = Identifier.of(MOD_ID, "jester");
    public static Identifier MORPHLING_ID = Identifier.of(MOD_ID, "morphling");
    public static Identifier CONDUCTOR_ID = Identifier.of(MOD_ID, "conductor");
    public static Identifier BARTENDER_ID = Identifier.of(MOD_ID, "bartender");
    public static Identifier NOISEMAKER_ID = Identifier.of(MOD_ID, "noisemaker");
    public static Identifier PHANTOM_ID = Identifier.of(MOD_ID, "phantom");
    public static Identifier AWESOME_BINGLUS_ID = Identifier.of(MOD_ID, "awesome_binglus");
    public static Identifier SWAPPER_ID = Identifier.of(MOD_ID, "swapper");
    public static Identifier GUESSER_ID = Identifier.of(MOD_ID, "guesser");
    public static Identifier VOODOO_ID = Identifier.of(MOD_ID, "voodoo");
    public static Identifier TRAPPER_ID = Identifier.of(MOD_ID, "trapper");
    public static Identifier CORONER_ID = Identifier.of(MOD_ID, "coroner");
    public static Identifier RECALLER_ID = Identifier.of(MOD_ID, "recaller");
    public static Identifier MIMIC_ID = Identifier.of(MOD_ID, "mimic");
    public static Identifier EXECUTIONER_ID = Identifier.of(MOD_ID, "executioner");
    public static Identifier VULTURE_ID = Identifier.of(MOD_ID, "vulture");
    public static Identifier BETTER_VIGILANTE_ID = Identifier.of(MOD_ID, "better_vigilante");
    public static Identifier TINY_ID = Identifier.of(MOD_ID, "tiny");
    public static Identifier CHAMELEON_ID = Identifier.of(MOD_ID, "chameleon");
    public static Identifier GRAVEROBBER_ID = Identifier.of(MOD_ID, "graverobber");
    public static Identifier FEATHER_ID = Identifier.of(MOD_ID, "feather");
    public static Identifier SNIPER_ID = Identifier.of(MOD_ID, "sniper");
    public static Identifier TROLL_ID = Identifier.of(MOD_ID, "troll");
    public static Identifier SCARECROW_ID = Identifier.of(MOD_ID, "scarecrow");
    public static Identifier HYPNOTIST_ID = Identifier.of(MOD_ID, "hypnotist");
    public static Identifier DJ_ID = Identifier.of(MOD_ID, "dj");
    public static Identifier DETECTIVE_ID = Identifier.of(MOD_ID, "detective");
    public static Identifier EMPTY_MODIFIER_ID = Identifier.of(MOD_ID, "empty_modifier");
    public static Identifier THIEF_ID = Identifier.of(MOD_ID, "thief");
    public static Identifier BRAWLER_ID = Identifier.of(MOD_ID, "brawler");
    public static Identifier GAMBLER_ID = Identifier.of(MOD_ID, "gambler");
    public static Identifier PICKPOCKET_ID = Identifier.of(MOD_ID, "pickpocket");
    public static Identifier DUMB_ID = Identifier.of(MOD_ID, "dumb");

    // DJ_PACKET removed: DJ now triggers via general ability packet/handler

    public static Identifier THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID = Identifier.of(MOD_ID, "the_insane_damned_paranoid_killer");

    public static HashMap<Role, RoleAnnouncementTexts.RoleAnnouncementText> roleRoleAnnouncementTextHashMap = new HashMap<>();
    // 把角色注册最好修改一下：
    // Legacy: public static Role JESTER = WatheRoles.registerRole(new Role(JESTER_ID,new Color(255,86,243).getRGB() ,false,false, Role.MoodType.FAKE,Integer.MAX_VALUE,true));
    public static Role JESTER = RoleModifierRegistry.registerRole(new Role(JESTER_ID,new Color(255,86,243).getRGB() ,false,false, Role.MoodType.FAKE,Integer.MAX_VALUE,true), false, false, false);
    // Legacy: public static Role MORPHLING =WatheRoles.registerRole(new Role(MORPHLING_ID, new Color(170, 2, 61).getRGB(),false,true, Role.MoodType.FAKE,Integer.MAX_VALUE,true));
    public static Role MORPHLING = RoleModifierRegistry.registerRole(new Role(MORPHLING_ID, new Color(170, 2, 61).getRGB(),false,true, Role.MoodType.FAKE,Integer.MAX_VALUE,true), false, false, true);
    // Legacy: public static Role CONDUCTOR =WatheRoles.registerRole(new Role(CONDUCTOR_ID, new Color(255, 205, 84).getRGB(),true,false, Role.MoodType.REAL,WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role CONDUCTOR = RoleModifierRegistry.registerRole(new Role(CONDUCTOR_ID, new Color(255, 205, 84).getRGB(),true,false, Role.MoodType.REAL,WatheRoles.CIVILIAN.getMaxSprintTime(),false), true, false, false);
    // Legacy: public static Role AWESOME_BINGLUS = WatheRoles.registerRole(new Role(AWESOME_BINGLUS_ID, new Color(155, 255, 168).getRGB(),true,false, Role.MoodType.REAL,WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role AWESOME_BINGLUS = RoleModifierRegistry.registerRole(new Role(AWESOME_BINGLUS_ID, new Color(155, 255, 168).getRGB(),true,false, Role.MoodType.REAL,WatheRoles.CIVILIAN.getMaxSprintTime(),false), false, false, false);

    // Legacy: public static Role BARTENDER =WatheRoles.registerRole(new Role(BARTENDER_ID, new Color(217,241,240).getRGB(),true,false, Role.MoodType.REAL,WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role BARTENDER = RoleModifierRegistry.registerRole(new Role(BARTENDER_ID, new Color(217,241,240).getRGB(),true,false, Role.MoodType.REAL,WatheRoles.CIVILIAN.getMaxSprintTime(),false), false, true, false);
    // Legacy: public static Role NOISEMAKER =WatheRoles.registerRole(new Role(NOISEMAKER_ID, new Color(200, 255, 0).getRGB(),true,false, Role.MoodType.REAL,WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role NOISEMAKER = RoleModifierRegistry.registerRole(new Role(NOISEMAKER_ID, new Color(200, 255, 0).getRGB(),true,false, Role.MoodType.REAL,WatheRoles.CIVILIAN.getMaxSprintTime(),false), false, true, false);
    // Legacy: public static Role SWAPPER = WatheRoles.registerRole(new Role(SWAPPER_ID, new Color(57, 4, 170).getRGB(),false,true, Role.MoodType.FAKE,Integer.MAX_VALUE,true));
    public static Role SWAPPER = RoleModifierRegistry.registerRole(new Role(SWAPPER_ID, new Color(57, 4, 170).getRGB(),false,true, Role.MoodType.FAKE,Integer.MAX_VALUE,true), false, false, true);
    // Legacy: public static Role PHANTOM =WatheRoles.registerRole(new Role(PHANTOM_ID, new Color(80, 5, 5, 192).getRGB(),false,true, Role.MoodType.FAKE,Integer.MAX_VALUE,true));
    public static Role PHANTOM = RoleModifierRegistry.registerRole(new Role(PHANTOM_ID, new Color(80, 5, 5, 192).getRGB(),false,true, Role.MoodType.FAKE,Integer.MAX_VALUE,true), true, false, true);

    // Legacy: public static Role VOODOO =WatheRoles.registerRole(new Role(VOODOO_ID, new Color(128, 114, 253).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role VOODOO = RoleModifierRegistry.registerRole(new Role(VOODOO_ID, new Color(128, 114, 253).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false), false, false, true);
    // Legacy: public static Role THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES =WatheRoles.registerRole(new Role(THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID, new Color(255, 0, 0, 192).getRGB(),false,true, Role.MoodType.FAKE,Integer.MAX_VALUE,true));
    public static Role THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES = RoleModifierRegistry.registerRole(new Role(THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID, new Color(255, 0, 0, 192).getRGB(),false,true, Role.MoodType.FAKE,Integer.MAX_VALUE,true), false, false, false);
    // Legacy: public static Role TRAPPER =WatheRoles.registerRole(new Role(TRAPPER_ID, new Color(132, 186, 167).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role TRAPPER = RoleModifierRegistry.registerRole(new Role(TRAPPER_ID, new Color(132, 186, 167).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false), false, true, false);
    // Legacy: public static Role CORONER =WatheRoles.registerRole(new Role(CORONER_ID, new Color(122, 122, 122).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role CORONER = RoleModifierRegistry.registerRole(new Role(CORONER_ID, new Color(122, 122, 122).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false), true, false, false);

    // Legacy: public static Role EXECUTIONER =WatheRoles.registerRole(new Role(EXECUTIONER_ID, new Color(74, 27, 5).getRGB(),false,false,Role.MoodType.FAKE, WatheRoles.CIVILIAN.getMaxSprintTime(),true));
    public static Role EXECUTIONER = RoleModifierRegistry.registerRole(new Role(EXECUTIONER_ID, new Color(74, 27, 5).getRGB(),false,false,Role.MoodType.FAKE, WatheRoles.CIVILIAN.getMaxSprintTime(),true), true, false, false);
    // Legacy: public static Role RECALLER = WatheRoles.registerRole(new Role(RECALLER_ID, new Color(158, 255, 255).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role RECALLER = RoleModifierRegistry.registerRole(new Role(RECALLER_ID, new Color(158, 255, 255).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false), true, false, true);

    // Legacy: public static Role VULTURE =WatheRoles.registerRole(new Role(VULTURE_ID, new Color(181, 103, 0).getRGB(),false,false,Role.MoodType.FAKE, WatheRoles.CIVILIAN.getMaxSprintTime(),true));
    public static Role VULTURE = RoleModifierRegistry.registerRole(new Role(VULTURE_ID, new Color(181, 103, 0).getRGB(),false,false,Role.MoodType.FAKE, WatheRoles.CIVILIAN.getMaxSprintTime(),true), true, false, true);
    // Legacy: public static Role BETTER_VIGILANTE =WatheRoles.registerRole(new Role(BETTER_VIGILANTE_ID, new Color(0, 255, 255).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role BETTER_VIGILANTE = RoleModifierRegistry.registerRole(new Role(BETTER_VIGILANTE_ID, new Color(0, 255, 255).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false), false, false, false);
    // public static Role GUESSER =WatheRoles.registerRole(new Role(GUESSER_ID, new Color(158, 43, 25, 191).getRGB(),false,true, Role.MoodType.FAKE,Integer.MAX_VALUE,true));

    // Legacy: public static Role MIMIC = WatheRoles.registerRole(new Role(MIMIC_ID, new Color(255, 137, 155).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role MIMIC = RoleModifierRegistry.registerRole(new Role(MIMIC_ID, new Color(255, 137, 155).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false), false, false, false);
    // Legacy: public static Role SNIPER = WatheRoles.registerRole(new Role(SNIPER_ID, new Color(255, 70, 70).getRGB(),false,true, Role.MoodType.FAKE,Integer.MAX_VALUE,true));
    public static Role SNIPER = RoleModifierRegistry.registerRole(new Role(SNIPER_ID, new Color(255, 70, 70).getRGB(),false,true, Role.MoodType.FAKE,Integer.MAX_VALUE,true), false, false, true);
    // Legacy: public static Role TROLL = WatheRoles.registerRole(new Role(TROLL_ID, new Color(255, 255, 158).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role TROLL = RoleModifierRegistry.registerRole(new Role(TROLL_ID, new Color(255, 255, 158).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false), true, false, true);
    public static Role SCARECROW = RoleModifierRegistry.registerRole(new Role(SCARECROW_ID, new Color(200,150,50).getRGB(), false, true, Role.MoodType.FAKE,Integer.MAX_VALUE,true), true, true, true);
    public static Role HYPNOTIST = RoleModifierRegistry.registerRole(new Role(HYPNOTIST_ID, new Color(180,50,200).getRGB(), false, true, Role.MoodType.FAKE,Integer.MAX_VALUE,true), true, true, true);
    public static Role DJ = RoleModifierRegistry.registerRole(new Role(DJ_ID, new Color(255,220,80).getRGB(), true, false, Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(), false), false, true, true);
    // Legacy: public static Role DETECTIVE = WatheRoles.registerRole(new Role(DETECTIVE_ID, new Color(155, 155, 58).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role DETECTIVE = RoleModifierRegistry.registerRole(new Role(DETECTIVE_ID, new Color(155, 155, 58).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false), true, false, true);
    // Legacy: public static Role THIEF = WatheRoles.registerRole(new Role(THIEF_ID, new Color(100, 100, 100).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role THIEF = RoleModifierRegistry.registerRole(new Role(THIEF_ID, new Color(100, 100, 100).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false), true, false, true);
    public static Role GAMBLER = RoleModifierRegistry.registerRole(new Role(GAMBLER_ID, new Color(180, 30, 180).getRGB(), true, false, Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(), false), true, true, true);
    // Legacy: public static Role PICKPOCKET = WatheRoles.registerRole(new Role(PICKPOCKET_ID, new Color(64, 64, 64).getRGB(), true, false, Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(), false));
    public static Role PICKPOCKET = RoleModifierRegistry.registerRole(new Role(PICKPOCKET_ID, new Color(64, 64, 64).getRGB(), true, false, Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(), false), true, true, true);
        // public static Modifier EMPTY_MODIFIER = HMLModifiers.registerModifier(new Modifier(EMPTY_MODIFIER_ID, new Color(255, 255, 255, 255).getRGB(), null, null, false, false));
        // Legacy: public static Modifier TINY = HMLModifiers.registerModifier(new Modifier(TINY_ID, new Color(255, 166, 0).getRGB(), new ArrayList<>(List.of(MORPHLING)), null, false, false));
        public static Modifier TINY;
        // Legacy: public static Modifier CHAMELEON = HMLModifiers.registerModifier(new Modifier(CHAMELEON_ID, new Color(198, 255, 137, 255).getRGB(), new ArrayList<>(List.of(PHANTOM,MORPHLING)), null, false, false));
        public static Modifier CHAMELEON;
        // Legacy: public static Modifier GUESSER = HMLModifiers.registerModifier(new Modifier(GUESSER_ID, new Color(158, 43, 25, 255).getRGB(), 
        //         new ArrayList<>(List.of(THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES,
        //                 SNIPER,
        //                 THIEF,
        //                 DETECTIVE,
        //                 SWAPPER,
        //                 VOODOO)), 
        //         null, 
        //         true, false));
        public static Modifier GUESSER;

        // Legacy: public static Modifier SNIPER_MODIFIER = HMLModifiers.registerModifier(new Modifier(SNIPER_ID, new Color(255, 70, 70).getRGB(),new ArrayList<>(List.of(SNIPER)),null,false,false));
        // Legacy: public static Modifier GRAVEROBBER = HMLModifiers.registerModifier(new Modifier(GRAVEROBBER_ID, new Color(174, 95, 95, 255).getRGB(),null,null,true,false));
        public static Modifier GRAVEROBBER;
        // Legacy: public static Modifier FEATHER = HMLModifiers.registerModifier(new Modifier(FEATHER_ID, new Color(255, 236, 161, 255).getRGB(),null,null,false,false));
        public static Modifier FEATHER;
        // Legacy: public static Modifier BRAWLER = HMLModifiers.registerModifier(new Modifier(BRAWLER_ID, new Color(139, 69, 19).getRGB(), null, null, false, false));
        public static Modifier BRAWLER;
        public static Modifier DUMB;

        static {
            // Initialize modifiers after roles have been registered so auto-generated cannotBeAppliedTo can see all roles.
            TINY = RoleModifierRegistry.registerModifier(TINY_ID, new Modifier(TINY_ID, new Color(255, 166, 0).getRGB(), new ArrayList<>(List.of(MORPHLING)), null, false, false), false, false, false);
            CHAMELEON = RoleModifierRegistry.registerModifier(CHAMELEON_ID, new Modifier(CHAMELEON_ID, new Color(198, 255, 137, 255).getRGB(), new ArrayList<>(List.of(PHANTOM,MORPHLING)), null, false, false), false, false, false);
            GUESSER = RoleModifierRegistry.registerModifier(GUESSER_ID, new Modifier(GUESSER_ID, new Color(158, 43, 25, 255).getRGB(), null, null, true, false), true, false, true);
            GRAVEROBBER = RoleModifierRegistry.registerModifier(GRAVEROBBER_ID, new Modifier(GRAVEROBBER_ID, new Color(174, 95, 95, 255).getRGB(),null,null,true,false), false, false, false);
            FEATHER = RoleModifierRegistry.registerModifier(FEATHER_ID, new Modifier(FEATHER_ID, new Color(255, 236, 161, 255).getRGB(),null,null,false,false), false, false, false);
            DUMB = RoleModifierRegistry.registerModifier(DUMB_ID, new Modifier(DUMB_ID, new Color(100, 100, 100).getRGB(), null, null, false, false), false, true, false);
            BRAWLER = RoleModifierRegistry.registerModifier(BRAWLER_ID, new Modifier(BRAWLER_ID, new Color(139, 69, 19).getRGB(), null, null, false, false), true, false, true);
        }




    public static final CustomPayload.Id<MorphC2SPacket> MORPH_PACKET = MorphC2SPacket.ID;
    public static final CustomPayload.Id<SwapperC2SPacket> SWAP_PACKET = SwapperC2SPacket.ID;
    public static final CustomPayload.Id<SniperC2SPacket> SNIPER_PACKET = SniperC2SPacket.ID;
    public static final CustomPayload.Id<DetectiveC2SPacket> DETECTIVE_PACKET = DetectiveC2SPacket.ID;
    public static final CustomPayload.Id<AbilityC2SPacket> ABILITY_PACKET = AbilityC2SPacket.ID;
    public static final CustomPayload.Id<ScarecrowC2SPacket> SCARECROW_PACKET = ScarecrowC2SPacket.ID;
    public static final CustomPayload.Id<HypnotistC2SPacket> HYPNOTIST_PACKET = HypnotistC2SPacket.ID;
    public static final CustomPayload.Id<VultureEatC2SPacket> VULTURE_PACKET = VultureEatC2SPacket.ID;
    public static final CustomPayload.Id<GuessC2SPacket> GUESS_PACKET = GuessC2SPacket.ID;
    public static final ArrayList<Role> VANNILA_ROLES = new ArrayList<>();
    public static final ArrayList<Identifier> VANNILA_ROLE_IDS = new ArrayList<>();
    public static final ArrayList<Role> KILLER_SIDED_NEUTRALS = new ArrayList<>();

    public static ArrayList<ShopEntry> FRAMING_ROLES_SHOP = new ArrayList<>();

    public static Identifier VOODOO_MAGIC_DEATH_REASON = Identifier.of(Noellesroles.MOD_ID, "voodoo");

    @Override
    public void onInitialize() {
        VANNILA_ROLES.add(WatheRoles.KILLER);
        VANNILA_ROLES.add(WatheRoles.VIGILANTE);
        VANNILA_ROLES.add(WatheRoles.CIVILIAN);
        VANNILA_ROLES.add(WatheRoles.LOOSE_END);

        KILLER_SIDED_NEUTRALS.add(VULTURE);
        KILLER_SIDED_NEUTRALS.add(JESTER);
        KILLER_SIDED_NEUTRALS.add(EXECUTIONER);

        VANNILA_ROLE_IDS.add(WatheRoles.LOOSE_END.identifier());
        VANNILA_ROLE_IDS.add(WatheRoles.VIGILANTE.identifier());
        VANNILA_ROLE_IDS.add(WatheRoles.CIVILIAN.identifier());
        VANNILA_ROLE_IDS.add(WatheRoles.KILLER.identifier());

        FRAMING_ROLES_SHOP.add(new FramingShopEntry(WatheItems.LOCKPICK.getDefaultStack(), 50, ShopEntry.Type.TOOL));
        FRAMING_ROLES_SHOP.add(new FramingShopEntry(ModItems.DELUSION_VIAL.getDefaultStack(), 30, ShopEntry.Type.POISON));
        FRAMING_ROLES_SHOP.add(new FramingShopEntry(WatheItems.FIRECRACKER.getDefaultStack(), 5, ShopEntry.Type.TOOL));
        FRAMING_ROLES_SHOP.add(new FramingShopEntry(WatheItems.NOTE.getDefaultStack(), 5, ShopEntry.Type.TOOL));

        // Add Gambler shop entry: jammed revolver available for 800
        // GameConstants.SHOP_ENTRIES.add(new ShopEntry(ModItems.JAMMED_REVOLVER.getDefaultStack(), 800, ShopEntry.Type.WEAPON));
        
        // Add Dumb modifier shop entry: note available for 50
        // GameConstants.SHOP_ENTRIES.add(new ShopEntry(WatheItems.NOTE.getDefaultStack(), 50, ShopEntry.Type.TOOL));
        
        // // Troll shop entries: CROWBAR (25) and BLACKOUT (200)
        // GameConstants.SHOP_ENTRIES.add(new ShopEntry(WatheItems.CROWBAR.getDefaultStack(), 25, ShopEntry.Type.TOOL));
        // GameConstants.SHOP_ENTRIES.add(new ShopEntry(WatheItems.BLACKOUT.getDefaultStack(), 200, ShopEntry.Type.TOOL));

        NoellesRolesConfig.HANDLER.load();
        ModItems.init();
        NoellesRolesEntities.init();

        Harpymodloader.setRoleMaximum(CONDUCTOR_ID,1);
        Harpymodloader.setRoleMaximum(EXECUTIONER_ID,1);
        Harpymodloader.setRoleMaximum(VULTURE_ID,1);
        Harpymodloader.setRoleMaximum(JESTER_ID,1);
        Harpymodloader.setRoleMaximum(BETTER_VIGILANTE_ID,1);
        Harpymodloader.setRoleMaximum(SNIPER_ID,1);
        Harpymodloader.setRoleMaximum(TROLL_ID,1);
        Harpymodloader.setRoleMaximum(SCARECROW_ID,1);
        Harpymodloader.setRoleMaximum(DETECTIVE_ID,1);
        Harpymodloader.setRoleMaximum(THIEF_ID,1);
        Harpymodloader.setRoleMaximum(GAMBLER_ID,1);
        Harpymodloader.setRoleMaximum(PICKPOCKET_ID,1);
        Harpymodloader.setRoleMaximum(TROLL_ID, 1);
        

        PayloadTypeRegistry.playC2S().register(MorphC2SPacket.ID, MorphC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(AbilityC2SPacket.ID, AbilityC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SwapperC2SPacket.ID, SwapperC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(VultureEatC2SPacket.ID, VultureEatC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(GuessC2SPacket.ID, GuessC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SniperC2SPacket.ID, SniperC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(DetectiveC2SPacket.ID, DetectiveC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ScarecrowC2SPacket.ID, ScarecrowC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(HypnotistC2SPacket.ID, HypnotistC2SPacket.CODEC);

        // 初始化音效
        ModSounds.initialize();

        registerEvents();

        registerPackets();

        if (NoellesRolesConfig.HANDLER.instance().allowCivillianGuessers) {
            GUESSER.killerOnly = false;
        }
        //NoellesRolesEntities.init();

    }

    EntityAttributeModifier tinyModifier = new EntityAttributeModifier(Identifier.of(MOD_ID, "tiny_modifier"), -0.15, EntityAttributeModifier.Operation.ADD_VALUE);

    /**
     * 根据角色初始化玩家（发放物品、初始化Component等）
     */
    private void initializePlayerRole(PlayerEntity player, Role role) {
        AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(player);
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        abilityPlayerComponent.reset();
        // abilityPlayerComponent.cooldown = NoellesRolesConfig.HANDLER.instance().generalCooldownTicks;
        // Reset player's money to the game start value on role initialization
        PlayerShopComponent playerShop = PlayerShopComponent.KEY.get(player);
        if (role.equals(GAMBLER)) {
            GamblerPlayerComponent gamblerComp = GamblerPlayerComponent.KEY.get(player);
            playerShop.balance = GameConstants.MONEY_START;
            playerShop.sync();
            abilityPlayerComponent.cooldown = GamblerPlayerComponent.GAMBLE_COOLDOWN_TICKS;
            abilityPlayerComponent.sync();
            gamblerComp.reset();
            gamblerComp.sync();
        }        
        if (role.equals(EXECUTIONER)) {
            ExecutionerPlayerComponent executionerPlayerComponent = (ExecutionerPlayerComponent) ExecutionerPlayerComponent.KEY.get(player);
            executionerPlayerComponent.won = false;
            executionerPlayerComponent.reset();
            executionerPlayerComponent.sync();
        }
        if (role.equals(VULTURE)) {
            VulturePlayerComponent vulturePlayerComponent = VulturePlayerComponent.KEY.get(player);
            vulturePlayerComponent.reset();
            vulturePlayerComponent.bodiesRequired = (int)((player.getWorld().getPlayers().size()/3f) - Math.floor(player.getWorld().getPlayers().size()/6f));
            vulturePlayerComponent.sync();
        }
        if (role.equals(BETTER_VIGILANTE)) {
            player.giveItemStack(WatheItems.GRENADE.getDefaultStack());
        }
        if (role.equals(MIMIC)) {
            player.giveItemStack(ModItems.FAKE_KNIFE.getDefaultStack());
        }
        if (role.equals(JESTER)) {
            player.giveItemStack(ModItems.FAKE_KNIFE.getDefaultStack());
            player.giveItemStack(ModItems.FAKE_REVOLVER.getDefaultStack());
        }
        if (role.equals(CONDUCTOR)) {
            player.giveItemStack(ModItems.MASTER_KEY.getDefaultStack());
        }
        if (role.equals(SNIPER)) {
            SniperPlayerComponent sniperPlayerComponent = SniperPlayerComponent.KEY.get(player);
            abilityPlayerComponent.cooldown = NoellesRolesConfig.HANDLER.instance().sniperCooldownTicks;
            sniperPlayerComponent.reset(player.getWorld().getPlayers().size());
            sniperPlayerComponent.sync();
        }
        if (role.equals(HYPNOTIST)) {
            try {
                HypnotistPlayerComponent hypComp = HypnotistPlayerComponent.KEY.get(player);
                if (hypComp != null) {
                    hypComp.reset(player.getWorld().getPlayers().size());
                    hypComp.sync();
                }
            } catch (Exception ex) {
                String m = ex.getMessage() == null ? "" : ex.getMessage();
                Log.info(LogCategory.GENERAL, "Hypnotist component missing when initializing role for %s: %s".formatted(player.getUuid(), m));
            }
        }
        if (role.equals(DETECTIVE)) {
            DetectivePlayerComponent detectiveComp = DetectivePlayerComponent.KEY.get(player);
            detectiveComp.reset(player.getWorld().getPlayers().size());
            detectiveComp.sync();
        }
        if (role.equals(BRAWLER)) {
            BrawlerPlayerComponent brawlerComp = BrawlerPlayerComponent.KEY.get(player);
            brawlerComp.reset();
            brawlerComp.sync();
        }
        if (role.equals(PICKPOCKET)) {
            PickpocketPlayerComponent pickComp = PickpocketPlayerComponent.KEY.get(player);
            pickComp.reset();
            pickComp.sync();
        }
        if (role.equals(AWESOME_BINGLUS)) {
            for (int i = 0; i < 16; i++) {
                player.giveItemStack(WatheItems.NOTE.getDefaultStack());
            }
        }
        if (role.equals(SCARECROW)) {
            abilityPlayerComponent.cooldown = GameConstants.getInTicks(1,30);
            abilityPlayerComponent.sync();
        }
    }

    public void registerEvents() {
        AllowPlayerDeath.EVENT.register(((playerEntityVictim, playerEntityKiller, identifier) -> {
            if (identifier == GameConstants.DeathReasons.FELL_OUT_OF_TRAIN) return true;
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(playerEntityVictim.getWorld());
            
            // THIEF偷取被击杀者的身份
            if (playerEntityKiller != null && gameWorldComponent.isRole(playerEntityKiller, Noellesroles.THIEF)) {
                ThiefPlayerComponent thiefComponent = ThiefPlayerComponent.KEY.get(playerEntityKiller);
                if (!thiefComponent.hasStolen) {
                    // 收回THIEF的真刀
                    removeKnifeFromPlayer(playerEntityKiller);

                    Identifier victimRole = gameWorldComponent.getRole(playerEntityVictim.getUuid()).identifier();
                    thiefComponent.stealIdentity(victimRole);
                    
                    // 找到对应的角色并初始化
                    initializePlayerRole(playerEntityKiller, gameWorldComponent.getRole(playerEntityVictim));
                }
            }
            
            if (gameWorldComponent.isRole(playerEntityVictim,Noellesroles.JESTER)) {
                PlayerPsychoComponent component =  PlayerPsychoComponent.KEY.get(playerEntityVictim);
                if (component.getPsychoTicks() > GameConstants.getInTicks(0,44)) {
                    return false;
                }
            }
            BartenderPlayerComponent bartenderPlayerComponent = BartenderPlayerComponent.KEY.get(playerEntityVictim);
            if (bartenderPlayerComponent.armor > 0) {
                playerEntityVictim.getWorld().playSound(playerEntityVictim, playerEntityVictim.getBlockPos(), WatheSounds.ITEM_PSYCHO_ARMOUR, SoundCategory.MASTER, 5.0F, 1.0F);
                bartenderPlayerComponent.armor--;
                return false;
            }

            return true;
        }));
        //
        // Mimic & Executioner backfire
        //
        AllowPlayerDeath.EVENT.register(((playerEntity, killer,identifier) -> {
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(playerEntity.getWorld());
            if (identifier.equals(GameConstants.DeathReasons.FELL_OUT_OF_TRAIN) && killer != null) {
                if (gameWorldComponent.isRole(killer, MIMIC) && gameWorldComponent.isInnocent(playerEntity)) {
                    GameFunctions.killPlayer(killer, true, null, Identifier.of(MOD_ID, "modded_backfire"));
                }
            }
            if (identifier.equals(GameConstants.DeathReasons.GUN) && killer != null) {
                if (gameWorldComponent.isRole(killer, EXECUTIONER) && ExecutionerPlayerComponent.KEY.get(killer).target != playerEntity.getUuid()) {
                    GameFunctions.killPlayer(killer, true, null, Identifier.of(MOD_ID, "modded_backfire"));
                }
            }
            return true;
        }));
        AllowPlayerPunching.EVENT.register(((playerEntity, playerEntity1) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(playerEntity.getWorld());
            return gameWorldComponent.isRole(playerEntity, Noellesroles.MIMIC) && playerEntity.getMainHandStack().isOf(ModItems.FAKE_KNIFE);
        }));
        ModifierAssigned.EVENT.register(((playerEntity, modifier) -> {
            if (modifier.equals(TINY)) {
                playerEntity.getAttributeInstance(EntityAttributes.GENERIC_SCALE).removeModifier(tinyModifier);
                playerEntity.getAttributeInstance(EntityAttributes.GENERIC_SCALE).addPersistentModifier(tinyModifier);
            }
            if (modifier.equals(FEATHER)) {
                playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, StatusEffectInstance.INFINITE, 0, true, false));
            }
            if (modifier.equals(BRAWLER)) {
                BrawlerPlayerComponent brawlerComp = BrawlerPlayerComponent.KEY.get(playerEntity);
                brawlerComp.reset();
                brawlerComp.sync();
            }
            if (modifier.equals(DUMB)) {
                // 先取消所有玩家的禁言，再对该玩家启用禁言
                ServerWorld world = (ServerWorld) playerEntity.getWorld();
                for (ServerPlayerEntity player : world.getPlayers(p -> true)) {
                    org.agmas.noellesroles.dumb.DumbPlayerComponent dumbComp = org.agmas.noellesroles.dumb.DumbPlayerComponent.KEY.get(player);
                    dumbComp.reset();
                    dumbComp.sync();
                }
                // 对当前玩家启用禁言
                org.agmas.noellesroles.dumb.DumbPlayerComponent dumbComp = org.agmas.noellesroles.dumb.DumbPlayerComponent.KEY.get(playerEntity);
                dumbComp.setDumb(true);
            }
            
        }));
        ResetPlayerEvent.EVENT.register(((playerEntity) -> {
            // 确保 NetMusic 不再跟随此实体（对局结束/重置时移除实体播放器）
            try {
                com.github.tartaricacid.netmusic.command.NetMusicCommand.stopFollowForEntity(playerEntity);
                Log.info(LogCategory.GENERAL, "NetMusic: stopFollowForEntity called for %s".formatted(playerEntity.getName().getString()));
            } catch (Throwable ignored) {}
            playerEntity.removeStatusEffect(StatusEffects.SLOW_FALLING);
            playerEntity.getAttributeInstance(EntityAttributes.GENERIC_SCALE).removeModifier(tinyModifier);
            // 重置壮汉状态
            BrawlerPlayerComponent brawlerComp = BrawlerPlayerComponent.KEY.get(playerEntity);
            brawlerComp.reset();
            brawlerComp.sync();
            // 重置窃贼状态
            PickpocketPlayerComponent pickComp = PickpocketPlayerComponent.KEY.get(playerEntity);
            pickComp.reset();
            pickComp.sync();
            // 重置哑巴状态
            org.agmas.noellesroles.dumb.DumbPlayerComponent dumbComp = org.agmas.noellesroles.dumb.DumbPlayerComponent.KEY.get(playerEntity);
            dumbComp.reset();
            dumbComp.sync();
            // 重置催眠师状态（按当前玩家数重新计算本局次数）
            try {
                HypnotistPlayerComponent hypComp = HypnotistPlayerComponent.KEY.get(playerEntity);
                hypComp.reset(playerEntity.getWorld().getPlayers().size());
                hypComp.sync();
            } catch (Exception ignored) {}
        }));
        CanSeePoison.EVENT.register((player)->{
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
            if (gameWorldComponent.isRole((PlayerEntity) player, Noellesroles.BARTENDER)) {
                return true;
            }
            return false;
        });
        ShouldDropOnDeath.EVENT.register(((itemStack,identifier) -> {
            return itemStack.isOf(ModItems.MASTER_KEY);
        }));
        ModdedRoleAssigned.EVENT.register((player,role)->{
            if (role.equals(THIEF)) {
                ThiefPlayerComponent thiefComponent = ThiefPlayerComponent.KEY.get(player);
                thiefComponent.reset();
                // 给THIEF发一把真刀用于击杀
                player.giveItemStack(WatheItems.KNIFE.getDefaultStack());
            } else {
                // 对其他角色使用统一的初始化方法
                initializePlayerRole(player, role);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(((server) -> {
            // 壮汉冲刺逻辑处理
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                BrawlerPlayerComponent brawlerComp = BrawlerPlayerComponent.KEY.get(player);
                if (brawlerComp.isCharging()) {
                    // 每个 Tick 处理冲刺逻辑（碰撞检测、效果应用）
                    BrawlerChargeHandler.tickCharge(player, brawlerComp);
                }
            }
            
            if (server.getPlayerManager().getCurrentPlayerCount() >= 12) {
                Harpymodloader.setRoleMaximum(MIMIC,1);
            } else {
                Harpymodloader.setRoleMaximum(MIMIC,0);
            }
            if (server.getPlayerManager().getCurrentPlayerCount() >= 8) {
                Harpymodloader.setRoleMaximum(VULTURE,1);
            } else {
                Harpymodloader.setRoleMaximum(VULTURE,0);
            }
            // 处理 Scarecrow 心跳循环播放：对仍然具有 DARKNESS 状态的玩家每隔若干 tick 播放心跳
            if (!SCARECROW_HEARTBEAT_COOLDOWNS.isEmpty()) {
                Iterator<Map.Entry<UUID, Integer>> it = SCARECROW_HEARTBEAT_COOLDOWNS.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<UUID, Integer> e = it.next();
                    UUID uuid = e.getKey();
                    int cooldown = e.getValue();
                    ServerPlayerEntity p = server.getPlayerManager().getPlayer(uuid);
                    if (p == null) { // 玩家不在线，移除
                        it.remove();
                        continue;
                    }
                    // 如果玩家不再拥有黑暗效果，则移除跟踪
                    if (!p.hasStatusEffect(StatusEffects.DARKNESS)) {
                        it.remove();
                        continue;
                    }
                    // 每 20 tick 播放一次心跳音
                    if (cooldown <= 0) {
                        p.getServerWorld().playSound(null, p.getBlockPos(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                        e.setValue(18);
                    } else {
                        e.setValue(cooldown - 1);
                    }
                }
            }

            // 处理 DJ 持续播放检测：对正在播放的 DJ 每 100 tick（约 5s）检查实体播放器状态
            if (!DJ_PLAYING_REMAINING_TICKS.isEmpty()) {
                Iterator<Map.Entry<UUID, Integer>> it = DJ_PLAYING_REMAINING_TICKS.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<UUID, Integer> e = it.next();
                    UUID uuid = e.getKey();
                    int remaining = e.getValue();
                    ServerPlayerEntity p = server.getPlayerManager().getPlayer(uuid);
                    if (p == null) { // 玩家不在线，移除
                        it.remove();
                        DJ_PLAY_CHECK_COOLDOWNS.remove(uuid);
                        continue;
                    }

                    // 递减剩余播放时长
                    remaining = Math.max(0, remaining - 1);
                    if (remaining <= 0) {
                        // 播放时长已到，清理播放状态
                        try {
                            org.agmas.noellesroles.dj.DJPlayerComponent djComp = org.agmas.noellesroles.dj.DJPlayerComponent.KEY.get(p);
                            if (djComp != null) { djComp.isPlaying = false; djComp.sync(); }
                        } catch (Exception ex2) {
                            String msg2 = ex2.getMessage() == null ? "" : ex2.getMessage();
                            if (msg2.contains("provides no component")) {
                                Log.info(LogCategory.GENERAL, "DJ component missing for %s during finish; cleaning up".formatted(uuid.toString()));
                            } else {
                                Log.info(LogCategory.GENERAL, "DJ finish error for %s: %s".formatted(uuid.toString(), ex2.getMessage()));
                            }
                        }
                        Log.info(LogCategory.GENERAL, "DJ finished by duration: player=%s uuid=%s".formatted(p.getName().getString(), uuid.toString()));
                        it.remove();
                        DJ_PLAY_CHECK_COOLDOWNS.remove(uuid);
                        continue;
                    }

                    e.setValue(remaining);

                    // 处理检查倒计时
                    Integer cd = DJ_PLAY_CHECK_COOLDOWNS.getOrDefault(uuid, 0);
                    if (cd <= 0) {
                        // 执行播放状态检查
                        try {
                            if (p.getWorld() instanceof net.minecraft.server.world.ServerWorld sw) {
                                com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer te = com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.getMusicPlayerForEntity(p);
                                com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.NbtRecord rec = com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.getVirtualEntitySession(sw, p.getUuid());
                                boolean playing;
                                String reason;
                                if (te != null) {
                                    playing = te.isPlay();
                                    reason = "te";
                                } else if (rec != null) {
                                    // 虚拟会话存在但 TileEntity 未创建 — 将其视为正在播放（NetMusic 可能以虚拟会话为准）
                                    playing = true;
                                    reason = "rec";
                                } else {
                                    playing = false;
                                    reason = "none";
                                }
                                String teState = (te == null) ? "te=null" : "te=present";
                                String recState = (rec == null) ? "rec=null" : "rec.seconds=" + rec.songTime;
                                Log.info(LogCategory.GENERAL, "DJ check: player=%s uuid=%s playing=%b reason=%s %s %s remaining=%d".formatted(p.getName().getString(), uuid.toString(), playing, reason, teState, recState, remaining));

                                if (playing) {
                                    // 如果实体仍在播放，则每次检查时重施加效果（不再扣费），并记录详细日志
                                    double range = NoellesRolesConfig.HANDLER.instance().djEffectRange;
                                    double rangeSq = range * range;
                                    int healedLocal = 0;
                                    int moodLocal = 0;
                                    for (ServerPlayerEntity target : p.getServer().getPlayerManager().getPlayerList()) {
                                        if (target.getWorld() != p.getWorld()) continue;
                                        if (target.getPos().squaredDistanceTo(p.getPos()) > rangeSq) continue;
                                        try {
                                                int add = NoellesRolesConfig.HANDLER.instance().djStaminaRestoreTicks;
                                                restoreSprintTicks(target, add);
                                                healedLocal++;
                                                Log.info(LogCategory.GENERAL, "DJ periodic stamina restored to %s (%s): addedTicks=%d".formatted(target.getName().getString(), target.getUuid().toString(), add));
                                            } catch (Exception exHeal) {
                                            String msgH = exHeal.getMessage() == null ? "" : exHeal.getMessage();
                                            Log.info(LogCategory.GENERAL, "DJ periodic stamina restore failed for %s (%s): %s".formatted(target.getName().getString(), target.getUuid().toString(), msgH));
                                        }
                                        try {
                                            dev.doctor4t.wathe.cca.PlayerMoodComponent mood = dev.doctor4t.wathe.cca.PlayerMoodComponent.KEY.get(target);
                                            if (mood != null) {
                                                float cur = mood.getMood();
                                                float add = NoellesRolesConfig.HANDLER.instance().djSanRestoreAmount;
                                                float next = Math.min(1.0f, cur + add);
                                                PLAYER_MOOD_CHANGE_SOURCE.put(target.getUuid(), "DJ");
                                                try {
                                                    mood.setMood(next);
                                                    Log.info(LogCategory.GENERAL, "DJ periodic mood updated for %s (%s): cur=%s add=%s next=%s".formatted(target.getName().getString(), target.getUuid().toString(), cur, add, next));
                                                } finally {
                                                    PLAYER_MOOD_CHANGE_SOURCE.remove(target.getUuid());
                                                }
                                                mood.sync();
                                                moodLocal++;
                                            }
                                        } catch (Exception exMood) {
                                            String msgM = exMood.getMessage() == null ? "" : exMood.getMessage();
                                            Log.info(LogCategory.GENERAL, "DJ periodic mood failed for %s (%s): %s".formatted(target.getName().getString(), target.getUuid().toString(), msgM));
                                        }
                                    }
                                    Log.info(LogCategory.GENERAL, "DJ periodic effects for player=%s uuid=%s healed=%d mood=%d".formatted(p.getName().getString(), uuid.toString(), healedLocal, moodLocal));
                                    // 重置下一次检查
                                    DJ_PLAY_CHECK_COOLDOWNS.put(uuid, 100);
                                    continue;
                                } else {
                                    // 实体未播放，尝试通过组件判断或清理；如果组件缺失，则记录但不要立即移除，以便后续再尝试
                                    try {
                                        org.agmas.noellesroles.dj.DJPlayerComponent djComp = org.agmas.noellesroles.dj.DJPlayerComponent.KEY.get(p);
                                        if (djComp != null) { djComp.isPlaying = false; djComp.sync(); }
                                        if (djComp == null && te == null) {
                                            String recInfo = (rec == null) ? "no_virtual_session" : ("virtual_session_seconds=" + rec.songTime);
                                            Log.info(LogCategory.GENERAL, "DJ component missing for %s during check; te=null %s; will retry later".formatted(uuid.toString(), recInfo));
                                            // postpone removal and reset check cooldown so we re-evaluate later
                                            DJ_PLAY_CHECK_COOLDOWNS.put(uuid, 100);
                                            continue;
                                        }
                                    } catch (Exception ex3) {
                                        String msg3 = ex3.getMessage() == null ? "" : ex3.getMessage();
                                        Log.info(LogCategory.GENERAL, "DJ component error for %s: %s".formatted(uuid.toString(), msg3));
                                    }
                                    Log.info(LogCategory.GENERAL, "DJ stopped according to entity player=%s uuid=%s".formatted(p.getName().getString(), uuid.toString()));
                                    it.remove();
                                    DJ_PLAY_CHECK_COOLDOWNS.remove(uuid);
                                    continue;
                                }
                            }
                        } catch (Exception ex) { Log.info(LogCategory.GENERAL, "DJ check error for %s: %s".formatted(uuid.toString(), ex.getMessage())); }
                    } else {
                        DJ_PLAY_CHECK_COOLDOWNS.put(uuid, cd - 1);
                    }
                }
            }
        }));
        if (!NoellesRolesConfig.HANDLER.instance().shitpostRoles) {
            HarpyModLoaderConfig.HANDLER.load();
            if (!HarpyModLoaderConfig.HANDLER.instance().disabled.contains(AWESOME_BINGLUS_ID.toString())) {
                HarpyModLoaderConfig.HANDLER.instance().disabled.add(AWESOME_BINGLUS_ID.toString());
            }
            if (!HarpyModLoaderConfig.HANDLER.instance().disabled.contains(BETTER_VIGILANTE_ID.toString())) {
                HarpyModLoaderConfig.HANDLER.instance().disabled.add(BETTER_VIGILANTE_ID.toString());
            }
            if (!HarpyModLoaderConfig.HANDLER.instance().disabled.contains(THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID.toString())) {
                HarpyModLoaderConfig.HANDLER.instance().disabled.add(THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID.toString());
            }
            HarpyModLoaderConfig.HANDLER.save();
        }


    }

    /**
     * 移除玩家背包中所有真刀（无论耐久/NBT）。
     */
    private void removeKnifeFromPlayer(PlayerEntity player) {
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.isOf(WatheItems.KNIFE)) {
                inventory.setStack(slot, ItemStack.EMPTY);
            }
        }
    }

    // Centralized DJ execution: picks song, triggers NetMusic, applies effects and cooldown
    private void handleDjPlay(ServerPlayerEntity player) {
        if (player == null) return;
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        if (!gameWorldComponent.isRole(player, DJ)) return;
        if (!dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(player)) return;

        AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(player);
        if (abilityPlayerComponent.cooldown > 0) return;

        java.util.List<Long> songs = NoellesRolesConfig.HANDLER.instance().djSongIds;
        if (songs == null || songs.isEmpty()) {
            player.sendMessage(Text.translatable("message.dj.fail.no_songs"), true);
            return;
        }
        long songId = songs.get(ThreadLocalRandom.current().nextInt(songs.size()));

        try {
            org.agmas.noellesroles.dj.DJPlayerComponent djComp = org.agmas.noellesroles.dj.DJPlayerComponent.KEY.get(player);
            if (djComp != null) { djComp.currentSongId = songId; djComp.isPlaying = true; djComp.sync(); }
        } catch (Exception ignored) {}

        // require shop component and sufficient balance
        PlayerShopComponent shop = PlayerShopComponent.KEY.get(player);
        if (shop == null) {
            Log.info(LogCategory.GENERAL, "DJ start aborted: player=%s songId=%d shop=null".formatted(player.getName().getString(), songId));
            return;
        }
        Log.info(LogCategory.GENERAL, "DJ start: player=%s songId=%d balance=%d".formatted(player.getName().getString(), songId, shop.balance));
        if (shop.balance < 100) {
            player.sendMessage(Text.translatable("tip.dj.not_enough_money"), true);
            return;
        }

        boolean started = com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.playEntityBySongId(player, songId);
        Log.info(LogCategory.GENERAL, "DJ playback requested for %s song=%d started=%b".formatted(player.getName().getString(), songId, started));
        if (!started) {
            player.sendMessage(Text.translatable("message.dj.fail.no_songs"), true);
            return;
        }

        // deduct cost after successful playback
        shop.balance -= 100;
        shop.sync();

        // 如果 NetMusic 可用，尝试获取虚拟会话以确定歌曲时长，并注册定期检查（每 100 tick）
        try {
            if (player.getWorld() instanceof net.minecraft.server.world.ServerWorld sw) {
                com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.NbtRecord rec = com.github.tartaricacid.netmusic.tileentity.EntityMusicPlayerManager.getVirtualEntitySession(sw, player.getUuid());
                if (rec != null) {
                    int seconds = rec.songTime; // 时长（秒）
                    int ticks = Math.max(1, seconds * 20);
                    DJ_PLAYING_REMAINING_TICKS.put(player.getUuid(), ticks);
                    DJ_PLAY_CHECK_COOLDOWNS.put(player.getUuid(), 100); // 首次在 5 秒后检查
                    Log.info(LogCategory.GENERAL, "DJ session registered for %s: seconds=%d ticks=%d".formatted(player.getName().getString(), seconds, ticks));
                } else {
                    // 如果无法获取会话，则仍插入一次性检查以确保后续能检测到停止状态
                    DJ_PLAYING_REMAINING_TICKS.put(player.getUuid(), NoellesRolesConfig.HANDLER.instance().djCooldownTicks);
                    DJ_PLAY_CHECK_COOLDOWNS.put(player.getUuid(), 100);
                    Log.info(LogCategory.GENERAL, "DJ session not found for %s; fallback remainingTicks=%d".formatted(player.getName().getString(), NoellesRolesConfig.HANDLER.instance().djCooldownTicks));
                }
            }
        } catch (Exception ignored) {}

        double range = NoellesRolesConfig.HANDLER.instance().djEffectRange;
        double rangeSq = range * range;
        int healedCount = 0;
        int moodCount = 0;
        for (ServerPlayerEntity p : player.getServer().getPlayerManager().getPlayerList()) {
            if (p.getWorld() != player.getWorld()) continue;
            if (p.getPos().squaredDistanceTo(player.getPos()) > rangeSq) continue;

            try {
                // 将原先的治疗改为恢复冲刺耐力（sprintingTicks）
                int addTicks = NoellesRolesConfig.HANDLER.instance().djStaminaRestoreTicks;
                restoreSprintTicks(p, addTicks);
                healedCount++;
                Log.info(LogCategory.GENERAL, "DJ stamina restored to %s (%s): addedTicks=%d".formatted(p.getName().getString(), p.getUuid().toString(), addTicks));
            } catch (Exception ex) {
                String msg = ex.getMessage() == null ? "" : ex.getMessage();
                Log.info(LogCategory.GENERAL, "DJ stamina restore failed for %s (%s): %s".formatted(p.getName().getString(), p.getUuid().toString(), msg));
                player.sendMessage(Text.literal("DJ stamina restore failed for " + p.getName().getString() + ": " + msg), true);
            }

            try {
                dev.doctor4t.wathe.cca.PlayerMoodComponent mood = dev.doctor4t.wathe.cca.PlayerMoodComponent.KEY.get(p);
                if (mood != null) {
                    float cur = mood.getMood();
                    float add = NoellesRolesConfig.HANDLER.instance().djSanRestoreAmount;
                    float next = Math.min(1.0f, cur + add);
                    // 标记此 mood 变更的来源为 DJ，混入会据此决定是否发放金币
                    PLAYER_MOOD_CHANGE_SOURCE.put(p.getUuid(), "DJ");
                    try {
                        mood.setMood(next);
                        Log.info(LogCategory.GENERAL, "DJ mood updated for %s (%s): cur=%s add=%s next=%s".formatted(p.getName().getString(), p.getUuid().toString(), cur, add, next));
                    } finally {
                        PLAYER_MOOD_CHANGE_SOURCE.remove(p.getUuid());
                    }
                    mood.sync();
                    moodCount++;
                }
            } catch (Exception ex) {
                String msg = ex.getMessage() == null ? "" : ex.getMessage();
                Log.info(LogCategory.GENERAL, "DJ mood update failed for %s (%s): %s".formatted(p.getName().getString(), p.getUuid().toString(), msg));
                player.sendMessage(Text.literal("DJ mood update failed for " + p.getName().getString() + ": " + msg), true);
            }
        }

        // 向触发者发送简短汇总，帮助确认效果是否生效
        player.sendMessage(Text.literal("DJ affected players: stamina_restored=" + healedCount + ", mood=" + moodCount), true);

        abilityPlayerComponent.cooldown = NoellesRolesConfig.HANDLER.instance().djCooldownTicks;
        abilityPlayerComponent.sync();

        player.sendMessage(Text.translatable("message.dj.started"), true);
    }

    // 尝试恢复玩家的冲刺耐力（sprintingTicks）。如果角色为无限冲刺（maxSprintTime == -1），则跳过。
    private void restoreSprintTicks(ServerPlayerEntity player, float ticksToAdd) {
        try {
            dev.doctor4t.wathe.api.SprintingTicksAccessor acc = (dev.doctor4t.wathe.api.SprintingTicksAccessor) player;
            float before = acc.wathe$getSprintingTicks();
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
            Role role = gameWorldComponent.getRole(player);
            int maxSprint = (role == null) ? WatheRoles.CIVILIAN.getMaxSprintTime() : role.getMaxSprintTime();
            if (maxSprint == -1) return; // 无限冲刺
            float target = Math.min((float)maxSprint, before + ticksToAdd);
            acc.wathe$setSprintingTicks(target);
            float after = acc.wathe$getSprintingTicks();
            // 若未生效（被其他逻辑覆盖），尝试调用官方恢复到上限的方法作为兜底
            if (after < target - 1e-3f) {
                acc.wathe$restoreSprintTicks();
                after = acc.wathe$getSprintingTicks();
            }
            Log.info(LogCategory.GENERAL, "DJ stamina restore for %s: before=%s add=%s target=%s after=%s".formatted(player.getName().getString(), before, ticksToAdd, target, after));
        } catch (Throwable t) {
            String msg = t.getMessage() == null ? "" : t.getMessage();
            Log.info(LogCategory.GENERAL, "restoreSprintTicks failed for %s: %s".formatted(player.getUuid().toString(), msg));
        }
    }


    public void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.MORPH_PACKET, (payload, context) -> {
            if (!dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(context.player())) return;

            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(context.player().getWorld());
            AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(context.player());

            if (payload.player() == null) return;
            if (context.player().getWorld().getPlayerByUuid(payload.player()) == null) return;

            if (gameWorldComponent.isRole(context.player(), VOODOO)) {
                if (abilityPlayerComponent.cooldown > 0) return;
                abilityPlayerComponent.cooldown = GameConstants.getInTicks(0, 30);
                abilityPlayerComponent.sync();
                VoodooPlayerComponent voodooPlayerComponent = (VoodooPlayerComponent) VoodooPlayerComponent.KEY.get(context.player());
                voodooPlayerComponent.setTarget(payload.player());

            }
            if (gameWorldComponent.isRole(context.player(), MORPHLING)) {
                MorphlingPlayerComponent morphlingPlayerComponent = (MorphlingPlayerComponent) MorphlingPlayerComponent.KEY.get(context.player());
                morphlingPlayerComponent.startMorph(payload.player());
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.VULTURE_PACKET, (payload, context) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(context.player().getWorld());
            AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(context.player());

            if (gameWorldComponent.isRole(context.player(), VULTURE) && GameFunctions.isPlayerAliveAndSurvival(context.player())) {
                if (abilityPlayerComponent.cooldown > 0) return;
                abilityPlayerComponent.sync();
                List<PlayerBodyEntity> playerBodyEntities = context.player().getWorld().getEntitiesByType(TypeFilter.equals(PlayerBodyEntity.class), context.player().getBoundingBox().expand(10), (playerBodyEntity -> {
                    return playerBodyEntity.getUuid().equals(payload.playerBody());
                }));
                if (!playerBodyEntities.isEmpty()) {
                    BodyDeathReasonComponent bodyDeathReasonComponent = BodyDeathReasonComponent.KEY.get(playerBodyEntities.getFirst());
                    if (!bodyDeathReasonComponent.vultured) {
                        abilityPlayerComponent.cooldown = GameConstants.getInTicks(0, 20);
                        VulturePlayerComponent vulturePlayerComponent = VulturePlayerComponent.KEY.get(context.player());
                        vulturePlayerComponent.bodiesEaten++;
                        vulturePlayerComponent.sync();
                        context.player().getServerWorld().playSound(null, context.player().getBlockPos(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.MASTER, 1.0F, 0.5F);
                        context.player().addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 2));
                        if (vulturePlayerComponent.bodiesEaten >= vulturePlayerComponent.bodiesRequired) {
                            ArrayList<Role> shuffledKillerRoles = new ArrayList<>(WatheRoles.ROLES);
                            shuffledKillerRoles.removeIf(role -> Harpymodloader.VANNILA_ROLES.contains(role) || !role.canUseKiller() || HarpyModLoaderConfig.HANDLER.instance().disabled.contains(role.identifier().getPath()));
                            if (shuffledKillerRoles.isEmpty()) shuffledKillerRoles.add(WatheRoles.KILLER);
                            Collections.shuffle(shuffledKillerRoles);

                            PlayerShopComponent playerShopComponent = (PlayerShopComponent) PlayerShopComponent.KEY.get(context.player());
                            gameWorldComponent.addRole(context.player(),shuffledKillerRoles.getFirst());
                            ModdedRoleAssigned.EVENT.invoker().assignModdedRole(context.player(),shuffledKillerRoles.getFirst());
                            playerShopComponent.setBalance(100);
                            PlayerPoisonComponent.KEY.get(context.player()).reset();
                            if (Harpymodloader.VANNILA_ROLES.contains(gameWorldComponent.getRole(context.player()))) {
                                ServerPlayNetworking.send((ServerPlayerEntity) context.player(), new AnnounceWelcomePayload(RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(WatheRoles.KILLER), gameWorldComponent.getAllKillerTeamPlayers().size(), 0));
                            } else {
                                ServerPlayNetworking.send((ServerPlayerEntity) context.player(), new AnnounceWelcomePayload(RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(Harpymodloader.autogeneratedAnnouncements.get(gameWorldComponent.getRole(context.player()))), gameWorldComponent.getAllKillerTeamPlayers().size(), 0));
                            }
                        }

                        bodyDeathReasonComponent.vultured = true;
                        bodyDeathReasonComponent.sync();
                    }
                }

            }
        });
        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.SWAP_PACKET, (payload, context) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(context.player().getWorld());
            if (gameWorldComponent.isRole(context.player(), SWAPPER)) {
                if (payload.player() != null) {
                    if (context.player().getWorld().getPlayerByUuid(payload.player()) != null) {
                        if (payload.player2() != null) {
                            if (context.player().getWorld().getPlayerByUuid(payload.player2()) != null) {
                                PlayerEntity player1 = context.player().getWorld().getPlayerByUuid(payload.player2());
                                PlayerEntity player2 = context.player().getWorld().getPlayerByUuid(payload.player());
                                Vec3d swapperPos = context.player().getWorld().getPlayerByUuid(payload.player2()).getPos();
                                Vec3d swappedPos = context.player().getWorld().getPlayerByUuid(payload.player()).getPos();
                                if (!context.player().getWorld().isSpaceEmpty(player1)) return;
                                if (!context.player().getWorld().isSpaceEmpty(player2)) return;
                                context.player().getWorld().getPlayerByUuid(payload.player2()).refreshPositionAfterTeleport(swappedPos.x, swappedPos.y, swappedPos.z);
                                context.player().getWorld().getPlayerByUuid(payload.player()).refreshPositionAfterTeleport(swapperPos.x, swapperPos.y, swapperPos.z);
                            }
                        }
                    }
                }
                if (!dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(context.player())) return;

                AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(context.player());
                abilityPlayerComponent.cooldown = GameConstants.getInTicks(1, 0);
                abilityPlayerComponent.sync();
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.SCARECROW_PACKET, (payload, context) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(context.player().getWorld());
            if (!gameWorldComponent.isRole(context.player(), SCARECROW)) return;
            if (payload.player() == null) return;
            if (!dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(context.player())) return;

            PlayerEntity __p = context.player().getWorld().getPlayerByUuid(payload.player());
            if (!(__p instanceof ServerPlayerEntity target)) return;
            if (!GameFunctions.isPlayerAliveAndSurvival(target)) return;

            // 清空理智值（SAN）并同步
            PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(target);
            try { mood.setMood(0f); mood.sync(); } catch (Exception ignored) {}

            // 给予 20 秒黑暗效果（翻倍），发送提示并开始循环播放心跳音直到效果结束
            int darknessTicks = 600; // 30 秒 = 600 ticks
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, darknessTicks, 0, false, false));
            target.sendMessage(Text.translatable("message.scarecrow.notice"), true);
            // 将玩家加入心跳播放跟踪表（立即触发一次）
            SCARECROW_HEARTBEAT_COOLDOWNS.put(target.getUuid(), 0);

            AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(context.player());
            abilityPlayerComponent.cooldown = GameConstants.getInTicks(1,30);
            abilityPlayerComponent.sync();
        });

        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.HYPNOTIST_PACKET, (payload, context) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(context.player().getWorld());
            if (!gameWorldComponent.isRole(context.player(), HYPNOTIST)) return;
            if (payload.player() == null) return;
            if (!dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(context.player())) return;

            PlayerEntity __p = context.player().getWorld().getPlayerByUuid(payload.player());
            if (!(__p instanceof ServerPlayerEntity target)) return;
            if (!GameFunctions.isPlayerAliveAndSurvival(target)) return;

            PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(target);
            if (mood == null) return;
            // only allow targeting players whose mood is below half (0.5)
            if (mood.getMood() >= 0.5f) {
                context.player().sendMessage(Text.translatable("message.hypnotist.fail.not_zero"), true);
                return;
            }

            // 使用组件的本局次数控制（与 Sniper 一致模式）
            try {
                HypnotistPlayerComponent hypnotistComp = HypnotistPlayerComponent.KEY.get(context.player());
                if (hypnotistComp != null) {
                    if (!hypnotistComp.hasUsesRemaining()) {
                        context.player().sendMessage(Text.translatable("message.hypnotist.fail.already_used"), true);
                        return;
                    }
                    hypnotistComp.consumeUse();
                    hypnotistComp.sync();
                }
            } catch (Exception ignored) {}

            // schedule the delayed conversion check after 20s (400 ticks)
            int delayTicks = 400;
            Scheduler.schedule(() -> {
                // re-validate on server thread
                if (!GameFunctions.isPlayerAliveAndSurvival(target)) return;
                PlayerMoodComponent mood2 = PlayerMoodComponent.KEY.get(target);
                if (mood2 == null) return;
                // success if target's mood remains below half
                if (mood2.getMood() < 0.5f) {
                    // choose a random killer-role
                    ArrayList<Role> shuffledKillerRoles = new ArrayList<>(WatheRoles.ROLES);
                    shuffledKillerRoles.removeIf(role -> Harpymodloader.VANNILA_ROLES.contains(role) || !role.canUseKiller() || HarpyModLoaderConfig.HANDLER.instance().disabled.contains(role.identifier().getPath()));
                    if (shuffledKillerRoles.isEmpty()) shuffledKillerRoles.add(WatheRoles.KILLER);
                    Collections.shuffle(shuffledKillerRoles);

                    gameWorldComponent.addRole(target, shuffledKillerRoles.get(0));
                    ModdedRoleAssigned.EVENT.invoker().assignModdedRole(target, shuffledKillerRoles.get(0));

                    // notify all players
                    // String tgtName = target.getGameProfile().getName();
                    Text announce = Text.translatable("message.hypnotist.announcement");
                    for (ServerPlayerEntity p : target.getServer().getPlayerManager().getPlayerList()) {
                        p.sendMessage(announce, false);
                    }

                    // notify target and initiator
                    target.sendMessage(Text.translatable("message.hypnotist.notice"), true);
                    context.player().sendMessage(Text.translatable("message.hypnotist.success", target.getName().getString()), true);
                } else {
                    context.player().sendMessage(Text.translatable("message.hypnotist.fail.recovered"), true);
                }
            }, delayTicks);

            AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(context.player());
            abilityPlayerComponent.cooldown = GameConstants.getInTicks(2,0);
            abilityPlayerComponent.sync();

            // usage is consumed earlier when checking the component; nothing further to mark here
        });
        // DJ_PACKET receiver removed; DJ now uses the general ability handler (`ABILITY_PACKET`).

        ServerPlayNetworking.registerGlobalReceiver(SniperC2SPacket.ID, (payload, context) -> {
            PlayerEntity shooter = context.player();
            SniperPlayerComponent sniperComp = SniperPlayerComponent.KEY.get(shooter);
            AbilityPlayerComponent abilityComp = AbilityPlayerComponent.KEY.get(shooter);
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(shooter.getWorld());
        
            if (!dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(shooter)) return;

            // 验证包完整性
            if (!payload.target().equals(shooter.getUuid())) {
                sniperComp.targetUUID = payload.target();
            }
            if (!payload.guessedIdentifier().equals(gameWorld.getRole(shooter.getUuid()).identifier())) {
                sniperComp.guessedIdentity = payload.guessedIdentifier();
            }
            // 防止自我射击
            if (sniperComp.targetUUID.equals(shooter.getUuid()) || sniperComp.guessedIdentity.equals(gameWorld.getRole(shooter.getUuid()).identifier())) return;
            // 验证狙击手身份 + 子弹数量 + 冷却状态
            if (!gameWorld.isRole(shooter, Noellesroles.SNIPER)) return;
            if (!sniperComp.hasShotsRemaining()) return;
            if (abilityComp.cooldown > 0) return; // 复用通用冷却字段
            // 输出调试信息到控制台
            // System.out.println("[Sniper Debug] Actual role: " + gameWorld.getRole(sniperComp.boundTarget).identifier().toString());
            // System.out.println("[Sniper Debug] Guessed role: " + sniperComp.guessedIdentity.toString());
            // System.out.println("[Sniper Debug] Guessed result: " + gameWorld.getRole(sniperComp.boundTarget).identifier().toString().equals(sniperComp.guessedIdentity.toString()));

            // 目标验证
            
            sniperComp.shotsRemaining--;
            sniperComp.sync();
            // immediately set cooldown to block further activations during the delay
            abilityComp.cooldown = NoellesRolesConfig.HANDLER.instance().sniperCooldownTicks;
            abilityComp.sync();

            Scheduler.schedule(() -> {
            Effects.playCockingSound(shooter,2f);
            shooter.addStatusEffect(new StatusEffectInstance(
                StatusEffects.GLOWING, // 使用原版发光效果
                60, // 3秒持续时间（20ticks/秒）
                0 // 效果等级
            ));
            if (!gameWorld.getRole(sniperComp.targetUUID).identifier().equals(sniperComp.guessedIdentity)) return;
            
            // 执行匿名击杀（借鉴巫毒师逻辑）
            Effects.playShootingEffects(shooter,.6f);
            PlayerEntity target = shooter.getWorld().getPlayerByUuid(sniperComp.targetUUID);
            GameFunctions.killPlayer(target, true, shooter, Identifier.of(Noellesroles.MOD_ID, "sniper"));
            // 更新状态：消耗子弹 + 设置冷却
            // ability.cooldown = GameConstants.getInTicks(1, 30); 90秒冷却
            sniperComp.targetUUID = shooter.getUuid();
            sniperComp.guessedIdentity = gameWorld.getRole(shooter.getUuid()).identifier();
            abilityComp.cooldown = NoellesRolesConfig.HANDLER.instance().sniperCooldownTicks;
            abilityComp.sync();}, 40); // 延迟40tick执行，先上膛后开枪
        });

        // 侦探查询：仅注册接收器占位，后续在服务端写入 DetectivePlayerComponent
        ServerPlayNetworking.registerGlobalReceiver(DetectiveC2SPacket.ID, (payload, context) -> {
            PlayerEntity detective = context.player();
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(detective.getWorld());
            AbilityPlayerComponent abilityComp = AbilityPlayerComponent.KEY.get(detective);
            DetectivePlayerComponent detectiveComp = DetectivePlayerComponent.KEY.get(detective);

            if (!dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(detective)) return;

            // 身份校验 + 冷却/次数校验
            if (!gameWorld.isRole(detective, Noellesroles.DETECTIVE)) return;
            if (abilityComp.cooldown > 0) return;
            if (!detectiveComp.hasDetectRemaining()) return;

            // 目标校验
            if (payload.target() == null) return;
            if (payload.target().equals(detective.getUuid())) return;
            PlayerEntity target = detective.getWorld().getPlayerByUuid(payload.target());
            if (target == null) return;
            // 可选：禁止自查
            if (target.getUuid().equals(detective.getUuid())) return;

            // 写入翻译键（由客户端调用 Text.translatable 渲染），并标记已猜测
            String identityKey;
            if (gameWorld.getRole(target.getUuid()) != null && gameWorld.getRole(target.getUuid()).identifier() != null) {
                Identifier rid = gameWorld.getRole(target.getUuid()).identifier();
                if (rid.getNamespace() == MOD_ID) {
                    identityKey = "announcement.role." + rid.getNamespace() + "." + rid.getPath();
                } else {
                    identityKey = "announcement.role." + rid.getPath();
                }
            } else {
                identityKey = "???";
            }
            detectiveComp.addOrUpdateGuess(target.getUuid(), identityKey, true);
            detectiveComp.detectRemaining--;
            detectiveComp.sync();

            // 设置冷却（复用通用冷却字段）
            abilityComp.cooldown = NoellesRolesConfig.HANDLER.instance().generalCooldownTicks;
            abilityComp.sync();
        });

        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.GUESS_PACKET, (payload, context) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(context.player().getWorld());
            WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(context.player().getWorld());
            if (!dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(context.player())) return;
            if (worldModifierComponent.isRole(context.player(), GUESSER)) {
                if (payload.player() != null) {
                    if (context.player().getWorld().getPlayerByUuid(payload.player()) != null) {
                        ServerPlayerEntity target = (ServerPlayerEntity) context.player().getWorld().getPlayerByUuid(payload.player());
                        ServerPlayerEntity player = context.player();
                        if (target == null) return;
                        if (payload.guess() != null) {
                            boolean wrong = gameWorldComponent.getRole(target) == null;

                            if (!wrong) {
                                wrong = !gameWorldComponent.getRole(target).identifier().getPath().equalsIgnoreCase(payload.guess());

                                if (!gameWorldComponent.isInnocent(player)) {
                                    if (KILLER_SIDED_NEUTRALS.contains(gameWorldComponent.getRole(target))) wrong = true;
                                    if (gameWorldComponent.getRole(target).canUseKiller()) wrong = true;
                                }
                                if (Harpymodloader.SPECIAL_ROLES.contains(gameWorldComponent.getRole(target))) wrong = true;
                            }
                            if (!wrong) {
                                player.playSoundToPlayer(SoundEvents.ENTITY_PIG_DEATH, SoundCategory.MASTER, 1, 1);
                                GameFunctions.killPlayer(target, true, player, VOODOO_MAGIC_DEATH_REASON);
                            } else {
                                player.playSoundToPlayer(SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.MASTER, 1, 1);
                                if (NoellesRolesConfig.HANDLER.instance().guesserDiesAfterIncorrectGuess.equalsIgnoreCase("death")) {
                                    GameFunctions.killPlayer(player, true, null, VOODOO_MAGIC_DEATH_REASON);
                                }
                                if (NoellesRolesConfig.HANDLER.instance().guesserDiesAfterIncorrectGuess.equalsIgnoreCase("explode")) {
                                    player.getServerWorld().playSound(null, player.getBlockPos(), WatheSounds.ITEM_GRENADE_EXPLODE, SoundCategory.PLAYERS, 5.0F, 1.0F + player.getRandom().nextFloat() * 0.1F - 0.05F);
                                    player.getServerWorld().spawnParticles(WatheParticles.BIG_EXPLOSION, player.getX(), player.getY() + 0.1F, player.getZ(), 1, 0.0F, 0.0F, 0.0F, 0.0F);
                                    player.getServerWorld().spawnParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 0.1F, player.getZ(), 100, 0.0F, 0.0F, 0.0F, 0.2F);

                                    for(ServerPlayerEntity player2 : player.getServerWorld().getPlayers((serverPlayerEntity) -> player.getBoundingBox().expand(2.0F).contains(serverPlayerEntity.getPos()) && GameFunctions.isPlayerAliveAndSurvival(serverPlayerEntity))) {
                                        GameFunctions.killPlayer(player2, true, player, GameConstants.DeathReasons.GRENADE);
                                    }
                                }
                            }
                        }
                    }
                }
                AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(context.player());
                abilityPlayerComponent.cooldown = GameConstants.getInTicks(2, 0);
                abilityPlayerComponent.sync();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.ABILITY_PACKET, (payload, context) -> {
            // Prevent non-alive/spectator/creative players from using abilities
            if (!dev.doctor4t.wathe.game.GameFunctions.isPlayerAliveAndSurvival(context.player())) return;

            AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(context.player());
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(context.player().getWorld());
            if (gameWorldComponent.isRole(context.player(), RECALLER) && abilityPlayerComponent.cooldown <= 0) {
                RecallerPlayerComponent recallerPlayerComponent = RecallerPlayerComponent.KEY.get(context.player());
                PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(context.player());
                if (!recallerPlayerComponent.placed) {
                    abilityPlayerComponent.cooldown = GameConstants.getInTicks(0,10);
                    recallerPlayerComponent.setPosition();
                }
                else if (playerShopComponent.balance >= 100) {
                    playerShopComponent.balance -= 100;
                    playerShopComponent.sync();
                    abilityPlayerComponent.cooldown = GameConstants.getInTicks(0,30);
                    recallerPlayerComponent.teleport();
                }

            }
            if (gameWorldComponent.isRole(context.player(), DJ) && abilityPlayerComponent.cooldown <= 0) {
                if (!(context.player() instanceof ServerPlayerEntity serverPlayer)) return;
                handleDjPlay(serverPlayer);
                return;
            }
            if (gameWorldComponent.isRole(context.player(), PHANTOM) && abilityPlayerComponent.cooldown <= 0) {
                context.player().addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 30 * 20,0,true,false,true));
                abilityPlayerComponent.cooldown = GameConstants.getInTicks(1, 30);
            }
            if (gameWorldComponent.isRole(context.player(), TROLL) && abilityPlayerComponent.cooldown <= 0) {
                PlayerEntity player = context.player();
                PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(context.player());
                    if (playerShopComponent.balance >= 50) {
                    playerShopComponent.balance -= 50;
                    playerShopComponent.sync();
                    abilityPlayerComponent.cooldown = NoellesRolesConfig.HANDLER.instance().tollCooldownTicks;
                    // switch (ThreadLocalRandom.current().nextInt(11)) {
                    switch (abilityPlayerComponent.abilityStates) {
                        case 0 -> {
                            player.giveItemStack(ModItems.FAKE_REVOLVER.getDefaultStack());
                        }
                        case 1 -> {
                            player.giveItemStack(ModItems.FAKE_KNIFE.getDefaultStack());
                        }
                        case 2 -> {
                            switch (ThreadLocalRandom.current().nextInt(5)) {
                                case 0 -> {
                                    Effects.playKnifePrepareSound(player,1f);
                                }
                                case 1 -> {
                                    Effects.playShootingEffects(player,1f);
                                }
                                case 2 -> {
                                    Effects.playCockingSound(player,1f);
                                }
                                case 3 -> {
                                    Effects.playGrenadeExplodeSound(player,1f);
                                }
                                case 4 -> {
                                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 100, 0));
                                }
                            }
                        }
                    }
                    if(abilityPlayerComponent.abilityStates < 3) {
                        abilityPlayerComponent.abilityStates++;
                        abilityPlayerComponent.sync();
                    }
                    
                }

            }

            // Gambler ability：15s 冷却，50% 概率将玩家余额翻倍，否则减半（向下取整到 5 的倍数）
            if (gameWorldComponent.isRole(context.player(), GAMBLER) && abilityPlayerComponent.cooldown <= 0) {
                GamblerPlayerComponent gamblerComp = GamblerPlayerComponent.KEY.get(context.player());
                PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(context.player());
                // 仅允许未使用过的玩家触发一次赌博能力
                if (abilityPlayerComponent.cooldown <= 0 && !gamblerComp.hasUsedGamble) {
                    int original = playerShopComponent.balance;
                    // 阶梯式胜率：最低 50%，随持有金额提高
                    double winChance = 0.5;
                    if (original >= 400) winChance = 0.8;
                    else if (original >= 200) winChance = 0.7;
                    else if (original >= 100) winChance = 0.6;

                    boolean win = ThreadLocalRandom.current().nextDouble() < winChance;
                    if (win) {
                        playerShopComponent.balance = original * 2;
                        playerShopComponent.sync();
                        // reset loss streak on win
                        gamblerComp.lossStreak = 0;
                        gamblerComp.sync();
                        context.player().getServerWorld().playSound(null, context.player().getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
                        context.player().sendMessage(Text.translatable("message.gambler.win", playerShopComponent.balance), true);
                    } else {
                        int half = original / 2;
                        half = (half / 5) * 5; // 向下取整到 5 的倍数
                        playerShopComponent.balance = half;
                        playerShopComponent.sync();
                        // increment loss streak and check guarantee
                        gamblerComp.lossStreak++;
                        gamblerComp.totalLosses++;
                        // 3 连输触发保底
                        if (gamblerComp.lossStreak >= 3) {
                            try {
                                PlayerShopComponent.usePsychoMode(context.player());
                            } catch (Exception e) {
                                // ignore if not available
                            }
                            gamblerComp.lossStreak = 0;
                            gamblerComp.totalLosses = 0;
                        }
                        // 累计 10 次失败触发全局保底
                        else if (gamblerComp.totalLosses >= 10) {
                            try {
                                PlayerShopComponent.usePsychoMode(context.player());
                            } catch (Exception e) {
                                // ignore
                            }
                            gamblerComp.lossStreak = 0;
                            gamblerComp.totalLosses = 0;
                        }
                        gamblerComp.sync();
                        context.player().getServerWorld().playSound(null, context.player().getBlockPos(), SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                        context.player().sendMessage(Text.translatable("message.gambler.lose", playerShopComponent.balance), true);
                    }

                    // gamblerComp.cooldown = GamblerPlayerComponent.GAMBLE_COOLDOWN_TICKS;
                    // gamblerComp.sync();
                    // 同时设置通用能力冷却，避免重复触发
                    abilityPlayerComponent.cooldown = GamblerPlayerComponent.GAMBLE_COOLDOWN_TICKS;
                    abilityPlayerComponent.sync();

                    // 标记为已使用（每局一次）
                    gamblerComp.hasUsedGamble = true;
                    gamblerComp.sync();
                }
            }
                // Scarecrow ability moved to dedicated selection packet (ScarecrowC2SPacket)
            
            // 壮汉词条冲刺能力
            WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(context.player().getWorld());
            if (worldModifierComponent.isModifier(context.player().getUuid(), BRAWLER) && abilityPlayerComponent.cooldown <= 0) {
                BrawlerPlayerComponent brawlerComp = BrawlerPlayerComponent.KEY.get(context.player());
                brawlerComp.startCharge();
                abilityPlayerComponent.cooldown = BrawlerPlayerComponent.CHARGE_COOLDOWN_TICKS;
                abilityPlayerComponent.sync();
                
                // 播放冲刺音效
                context.player().getServerWorld().playSound(
                    null,
                    context.player().getX(),
                    context.player().getY(),
                    context.player().getZ(),
                    ModSounds.BRAWLER_ABILITY,
                    SoundCategory.PLAYERS,
                    2.0F,
                    1.0F + context.player().getRandom().nextFloat() * 0.1F - 0.05F
                );
            }
            // 窃贼（Pickpocket）偷窃能力：在近距离窃取一名玩家（一次仅一个目标），窃得固定 +25，但目标余额变为原来的一半
            if (gameWorldComponent.isRole(context.player(), PICKPOCKET) && abilityPlayerComponent.cooldown <= 0) {
                // 在玩家附近寻找第一个可窃取目标
                ServerPlayerEntity thief = (ServerPlayerEntity) context.player();
                ServerPlayerEntity target = null;
                double bestDistSq = Double.MAX_VALUE;
                for (ServerPlayerEntity p : thief.getServerWorld().getPlayers()) {
                    if (p.getUuid().equals(thief.getUuid())) continue;
                    if (!GameFunctions.isPlayerAliveAndSurvival(p)) continue;
                    double dx = p.getX() - thief.getX();
                    double dy = p.getY() - thief.getY();
                    double dz = p.getZ() - thief.getZ();
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq <= PickpocketPlayerComponent.STEAL_RANGE * PickpocketPlayerComponent.STEAL_RANGE) {
                        if (distSq < bestDistSq) {
                            bestDistSq = distSq;
                            target = p;
                        }
                    }
                }
                if (target != null) {
                    // 检查目标是否在盯着窃贼（若盯着，则窃取失败）
                    Vec3d targetLook = target.getRotationVec(1.0F);
                    Vec3d toThief = new Vec3d(thief.getX() - target.getX(), thief.getEyeY() - target.getEyeY(), thief.getZ() - target.getZ());
                    double dist = toThief.length();
                    boolean isLooking = false;
                    if (dist > 0.0001) {
                        Vec3d toThiefNorm = toThief.normalize();
                        double dot = targetLook.dotProduct(toThiefNorm);
                        // dot 取值范围 -1..1，越大代表越朝向窃贼。阈值 0.85 约等于 31° 以内
                        isLooking = dot >= 0.85;
                    }

                    if (isLooking) {
                        // 窃取失败反馈
                        abilityPlayerComponent.cooldown = 60; // 3 秒短冷却，防止连尝试
                        abilityPlayerComponent.sync();

                        thief.getServerWorld().playSound(null, thief.getX(), thief.getY(), thief.getZ(), SoundEvents.ENTITY_WITHER_DEATH, SoundCategory.PLAYERS, 1.0F, 1.0F);
                        target.getServerWorld().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_WITHER_DEATH, SoundCategory.PLAYERS, 1.0F, 1.0F);

                        // 粒子提示（服务器广播）
                        if (thief.getServerWorld() instanceof ServerWorld serverWorld) {
                            serverWorld.spawnParticles(ParticleTypes.CLOUD, thief.getX(), thief.getY() + 1.0, thief.getZ(), 8, 0.1, 0.1, 0.1, 0.02);
                            serverWorld.spawnParticles(ParticleTypes.ANGRY_VILLAGER, target.getX(), target.getY() + 1.0, target.getZ(), 6, 0.1, 0.1, 0.1, 0.02);
                        }

                        thief.sendMessage(Text.translatable("message.pickpocket.fail"), true);
                        target.sendMessage(Text.translatable("message.pickpocket.victim_noticed"), true);
                    } else {
                        // 成功窃取
                        PlayerShopComponent thiefShop = PlayerShopComponent.KEY.get(thief);
                        PlayerShopComponent victimShop = PlayerShopComponent.KEY.get(target);
                        int originalVictim = victimShop.balance;
                        int victimNew = originalVictim / 2; // 目标减少为一半
                        // 向下取整到 5 的倍数，保证余下金额为 5 的倍数
                        victimNew = (victimNew / 5) * 5;
                        victimShop.balance = victimNew;
                        victimShop.sync();

                        thiefShop.balance += PickpocketPlayerComponent.STEAL_AMOUNT; // 窃贼固定获得 +25
                        thiefShop.sync();

                        abilityPlayerComponent.cooldown = PickpocketPlayerComponent.COOLDOWN_TICKS;
                        abilityPlayerComponent.sync();

                        // 成功时播放升级音效并生成粒子与文字提示
                        thief.getServerWorld().playSound(null, thief.getX(), thief.getY(), thief.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
                        if (thief.getServerWorld() instanceof ServerWorld serverWorld) {
                            serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, thief.getX(), thief.getY() + 1.0, thief.getZ(), 10, 0.2, 0.2, 0.2, 0.05);
                            serverWorld.spawnParticles(ParticleTypes.SMOKE, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.1, 0.1, 0.1, 0.02);
                        }

                        thief.sendMessage(Text.translatable("message.pickpocket.success", PickpocketPlayerComponent.STEAL_AMOUNT), true);
                        target.sendMessage(Text.translatable("message.pickpocket.victim", victimShop.balance), true);
                    }
                }
            }
        });
    }



}
