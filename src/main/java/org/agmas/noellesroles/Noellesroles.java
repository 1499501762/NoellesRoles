package org.agmas.noellesroles;

import java.util.concurrent.ThreadLocalRandom;

import dev.doctor4t.wathe.Wathe;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.client.util.WatheItemTooltips;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.api.event.AllowPlayerDeath;
import dev.doctor4t.wathe.api.event.AllowPlayerPunching;
import dev.doctor4t.wathe.api.event.CanSeePoison;
import dev.doctor4t.wathe.api.event.ShouldDropOnDeath;
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

import java.awt.*;
import java.lang.reflect.Constructor;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.List;

public class Noellesroles implements ModInitializer {

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
    public static Identifier SPEEDY_ID = Identifier.of(MOD_ID, "speedy");
    public static Identifier SNIPER_ID = Identifier.of(MOD_ID, "sniper");
    public static Identifier TROLL_ID = Identifier.of(MOD_ID, "troll");
    public static Identifier DETECTIVE_ID = Identifier.of(MOD_ID, "detective");
    public static Identifier EMPTY_MODIFIER_ID = Identifier.of(MOD_ID, "empty_modifier");
    public static Identifier THIEF_ID = Identifier.of(MOD_ID, "thief");

    public static Identifier THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID = Identifier.of(MOD_ID, "the_insane_damned_paranoid_killer");

    public static HashMap<Role, RoleAnnouncementTexts.RoleAnnouncementText> roleRoleAnnouncementTextHashMap = new HashMap<>();
    // 把角色注册最好修改一下：
    public static Role JESTER = WatheRoles.registerRole(new Role(JESTER_ID,new Color(255,86,243).getRGB() ,false,false, Role.MoodType.FAKE,Integer.MAX_VALUE,true));
    public static Role MORPHLING =WatheRoles.registerRole(new Role(MORPHLING_ID, new Color(170, 2, 61).getRGB(),false,true, Role.MoodType.FAKE,Integer.MAX_VALUE,true));
    public static Role CONDUCTOR =WatheRoles.registerRole(new Role(CONDUCTOR_ID, new Color(255, 205, 84).getRGB(),true,false, Role.MoodType.REAL,WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role AWESOME_BINGLUS = WatheRoles.registerRole(new Role(AWESOME_BINGLUS_ID, new Color(155, 255, 168).getRGB(),true,false, Role.MoodType.REAL,WatheRoles.CIVILIAN.getMaxSprintTime(),false));

    public static Role BARTENDER =WatheRoles.registerRole(new Role(BARTENDER_ID, new Color(217,241,240).getRGB(),true,false, Role.MoodType.REAL,WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role NOISEMAKER =WatheRoles.registerRole(new Role(NOISEMAKER_ID, new Color(200, 255, 0).getRGB(),true,false, Role.MoodType.REAL,WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role SWAPPER = WatheRoles.registerRole(new Role(SWAPPER_ID, new Color(57, 4, 170).getRGB(),false,true, Role.MoodType.FAKE,Integer.MAX_VALUE,true));
    public static Role PHANTOM =WatheRoles.registerRole(new Role(PHANTOM_ID, new Color(80, 5, 5, 192).getRGB(),false,true, Role.MoodType.FAKE,Integer.MAX_VALUE,true));

    public static Role VOODOO =WatheRoles.registerRole(new Role(VOODOO_ID, new Color(128, 114, 253).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES =WatheRoles.registerRole(new Role(THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID, new Color(255, 0, 0, 192).getRGB(),false,true, Role.MoodType.FAKE,Integer.MAX_VALUE,true));
    //public static Role TRAPPER =WatheRoles.registerRole(new Role(TRAPPER_ID, new Color(132, 186, 167).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role CORONER =WatheRoles.registerRole(new Role(CORONER_ID, new Color(122, 122, 122).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false));

    public static Role EXECUTIONER =WatheRoles.registerRole(new Role(EXECUTIONER_ID, new Color(74, 27, 5).getRGB(),false,false,Role.MoodType.FAKE, WatheRoles.CIVILIAN.getMaxSprintTime(),true));
    public static Role RECALLER = WatheRoles.registerRole(new Role(RECALLER_ID, new Color(158, 255, 255).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false));

    public static Role VULTURE =WatheRoles.registerRole(new Role(VULTURE_ID, new Color(181, 103, 0).getRGB(),false,false,Role.MoodType.FAKE, WatheRoles.CIVILIAN.getMaxSprintTime(),true));
    public static Role BETTER_VIGILANTE =WatheRoles.registerRole(new Role(BETTER_VIGILANTE_ID, new Color(0, 255, 255).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    // public static Role GUESSER =WatheRoles.registerRole(new Role(GUESSER_ID, new Color(158, 43, 25, 191).getRGB(),false,true, Role.MoodType.FAKE,Integer.MAX_VALUE,true));

    public static Role MIMIC = WatheRoles.registerRole(new Role(MIMIC_ID, new Color(255, 137, 155).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role SNIPER = WatheRoles.registerRole(new Role(SNIPER_ID, new Color(255, 70, 70).getRGB(),false,true, Role.MoodType.FAKE,Integer.MAX_VALUE,true));
    public static Role TROLL = WatheRoles.registerRole(new Role(TROLL_ID, new Color(255, 255, 158).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role DETECTIVE = WatheRoles.registerRole(new Role(DETECTIVE_ID, new Color(155, 155, 58).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false));
    public static Role THIEF = WatheRoles.registerRole(new Role(THIEF_ID, new Color(100, 100, 100).getRGB(),true,false,Role.MoodType.REAL, WatheRoles.CIVILIAN.getMaxSprintTime(),false));

    // public static Modifier EMPTY_MODIFIER = HMLModifiers.registerModifier(new Modifier(EMPTY_MODIFIER_ID, new Color(255, 255, 255, 255).getRGB(), null, null, false, false));
    public static Modifier TINY = HMLModifiers.registerModifier(new Modifier(TINY_ID, new Color(255, 223, 142).getRGB(), new ArrayList<>(List.of(MORPHLING)), null, false, false));
    public static Modifier CHAMELEON = HMLModifiers.registerModifier(new Modifier(CHAMELEON_ID, new Color(198, 255, 137).getRGB(), new ArrayList<>(List.of(PHANTOM,MORPHLING)), null, false, false));
    public static Modifier GUESSER = HMLModifiers.registerModifier(new Modifier(GUESSER_ID, new Color(158, 43, 25, 191).getRGB(), 
            new ArrayList<>(List.of(THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES,
                    SNIPER,
                    THIEF,
                    DETECTIVE,
                    SWAPPER,
                    VOODOO)), 
            null, 
            true, false));
    
    // public static Modifier SNIPER_MODIFIER = HMLModifiers.registerModifier(new Modifier(SNIPER_ID, new Color(255, 70, 70).getRGB(),new ArrayList<>(List.of(SNIPER)),null,false,false));

    public static final CustomPayload.Id<MorphC2SPacket> MORPH_PACKET = MorphC2SPacket.ID;
    public static final CustomPayload.Id<SwapperC2SPacket> SWAP_PACKET = SwapperC2SPacket.ID;
    public static final CustomPayload.Id<SniperC2SPacket> SNIPER_PACKET = SniperC2SPacket.ID;
    public static final CustomPayload.Id<DetectiveC2SPacket> DETECTIVE_PACKET = DetectiveC2SPacket.ID;
    public static final CustomPayload.Id<AbilityC2SPacket> ABILITY_PACKET = AbilityC2SPacket.ID;
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

        NoellesRolesConfig.HANDLER.load();
        ModItems.init();

        Harpymodloader.setRoleMaximum(CONDUCTOR_ID,1);
        Harpymodloader.setRoleMaximum(EXECUTIONER_ID,1);
        Harpymodloader.setRoleMaximum(VULTURE_ID,1);
        Harpymodloader.setRoleMaximum(JESTER_ID,1);
        Harpymodloader.setRoleMaximum(BETTER_VIGILANTE_ID,1);
        Harpymodloader.setRoleMaximum(SNIPER_ID,1);
        Harpymodloader.setRoleMaximum(TROLL_ID,1);
        Harpymodloader.setRoleMaximum(DETECTIVE_ID,1);
        Harpymodloader.setRoleMaximum(THIEF_ID,1);

        PayloadTypeRegistry.playC2S().register(MorphC2SPacket.ID, MorphC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(AbilityC2SPacket.ID, AbilityC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SwapperC2SPacket.ID, SwapperC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(VultureEatC2SPacket.ID, VultureEatC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(GuessC2SPacket.ID, GuessC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SniperC2SPacket.ID, SniperC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(DetectiveC2SPacket.ID, DetectiveC2SPacket.CODEC);

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
        abilityPlayerComponent.cooldown = NoellesRolesConfig.HANDLER.instance().generalCooldownTicks;
        
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
        if (role.equals(DETECTIVE)) {
            DetectivePlayerComponent detectiveComp = DetectivePlayerComponent.KEY.get(player);
            detectiveComp.reset(player.getWorld().getPlayers().size());
            detectiveComp.sync();
        }
        if (role.equals(AWESOME_BINGLUS)) {
            for (int i = 0; i < 16; i++) {
                player.giveItemStack(WatheItems.NOTE.getDefaultStack());
            }
        }
    }

    public void registerEvents() {
        AllowPlayerDeath.EVENT.register(((PlayerEntity playerEntityVictim, PlayerEntity playerEntityKiller, Identifier identifier) -> {
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
        AllowPlayerPunching.EVENT.register(((playerEntity, playerEntity1) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(playerEntity.getWorld());
            if (gameWorldComponent.isRole(playerEntity, Noellesroles.MIMIC)) return (gameWorldComponent.getRole(playerEntity1) != null && KILLER_SIDED_NEUTRALS.contains(gameWorldComponent.getRole(playerEntity1))) || gameWorldComponent.canUseKillerFeatures(playerEntity1);
            return false;
        }));
        ModifierAssigned.EVENT.register(((playerEntity, modifier) -> {
            if (modifier.equals(TINY)) {
                playerEntity.getAttributeInstance(EntityAttributes.GENERIC_SCALE).removeModifier(tinyModifier);
                playerEntity.getAttributeInstance(EntityAttributes.GENERIC_SCALE).addPersistentModifier(tinyModifier);
            }
        }));
        ResetPlayerEvent.EVENT.register(((playerEntity) -> {
            playerEntity.getAttributeInstance(EntityAttributes.GENERIC_SCALE).removeModifier(tinyModifier);
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
        }));
        if (!NoellesRolesConfig.HANDLER.instance().shitpostRoles) {
            HarpyModLoaderConfig.HANDLER.load();
            if (!HarpyModLoaderConfig.HANDLER.instance().disabled.contains(AWESOME_BINGLUS_ID.getPath())) {
                HarpyModLoaderConfig.HANDLER.instance().disabled.add(AWESOME_BINGLUS_ID.getPath());
            }
            if (!HarpyModLoaderConfig.HANDLER.instance().disabled.contains(BETTER_VIGILANTE_ID.getPath())) {
                HarpyModLoaderConfig.HANDLER.instance().disabled.add(BETTER_VIGILANTE_ID.getPath());
            }
            if (!HarpyModLoaderConfig.HANDLER.instance().disabled.contains(THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID.getPath())) {
                HarpyModLoaderConfig.HANDLER.instance().disabled.add(THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID.getPath());
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


    public void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.MORPH_PACKET, (payload, context) -> {
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
                AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(context.player());
                abilityPlayerComponent.cooldown = GameConstants.getInTicks(1, 0);
                abilityPlayerComponent.sync();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SniperC2SPacket.ID, (payload, context) -> {
            PlayerEntity shooter = context.player();
            SniperPlayerComponent sniperComp = SniperPlayerComponent.KEY.get(shooter);
            AbilityPlayerComponent abilityComp = AbilityPlayerComponent.KEY.get(shooter);
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(shooter.getWorld());
        
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

            // 写入翻译后的身份文本并标记已猜测
            String identity;
            if (gameWorld.getRole(target.getUuid()) != null && gameWorld.getRole(target.getUuid()).identifier() != null) {
                Identifier rid = gameWorld.getRole(target.getUuid()).identifier();
                String key;
                if (rid.getNamespace() == MOD_ID) {
                    key = "announcement.role." + rid.getNamespace() + "." + rid.getPath();
                }else {
                    key = "announcement.role." + rid.getPath();
                }
                
                identity = Text.translatable(key).getString();
            } else {
                identity = "???";
            }
            detectiveComp.addOrUpdateGuess(target.getUuid(), identity, true);
            detectiveComp.detectRemaining--;
            detectiveComp.sync();

            // 设置冷却（复用通用冷却字段）
            abilityComp.cooldown = NoellesRolesConfig.HANDLER.instance().generalCooldownTicks;
            abilityComp.sync();
        });

        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.GUESS_PACKET, (payload, context) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(context.player().getWorld());
            WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(context.player().getWorld());
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
        });
    }



}
