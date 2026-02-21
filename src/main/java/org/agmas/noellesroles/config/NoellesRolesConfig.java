package org.agmas.noellesroles.config;

import dev.doctor4t.wathe.game.GameConstants;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;

import java.util.List;

public class NoellesRolesConfig {
    public static ConfigClassHandler<NoellesRolesConfig> HANDLER = ConfigClassHandler.createBuilder(NoellesRolesConfig.class)
            .id(Identifier.of(Noellesroles.MOD_ID, "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve( Noellesroles.MOD_ID + ".json5"))
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry(comment = "Whether insane players will randomly see people as morphed.")
    public boolean insanePlayersSeeMorphs = true;
    @SerialEntry(comment = "Allows the shitpost roles to retain their disable/enable state after a server restart")
    public boolean shitpostRoles = false;

    @SerialEntry(comment = "Starting cooldown (in ticks)")
    public int generalCooldownTicks = GameConstants.getInTicks(0,30);

    @SerialEntry(comment = "Allow Natural deaths to trigger voodoo (deaths without an assigned killer)")
    public boolean voodooNonKillerDeaths = false;

    @SerialEntry(comment = "Makes voodoos act like Evil players when shot by a revolver (no backfire, no gun lost)")
    public boolean voodooShotLikeEvil = true;

    @SerialEntry(comment = "Civillians can get the guesser modifier.")
    public boolean allowCivillianGuessers = false;

    @SerialEntry(comment = "How the guesser dies after an incorrect guess.\n\"none\" (default) - nothing happens, 2 minute cooldown applied\n\"death\" kills the player with a voodoo death message\n\"explode\" explodes the guesser, killing anyone nearby")
    public String guesserDiesAfterIncorrectGuess = "none";

    @SerialEntry(comment = "How many players must be online for the Master Key to look like a master key and not a lockpick. (0 = key always looks like a lockpick, 1-6 = key always looks normal)")
    public int playerCountToMakeConducterKeyVisible = 10;

    @SerialEntry(comment = "Sniper role cooldown in ticks (default 90 seconds)")
    public int sniperCooldownTicks = GameConstants.getInTicks(1,30); // 90s

    @SerialEntry(comment = "Sniper role shot ratio (20% of players)")
    public double sniperShotRatio = 0.2;

        @SerialEntry(comment = "DJ role cooldown in ticks (default 60 seconds)")
        public int djCooldownTicks = GameConstants.getInTicks(1,0); // 60s

        @SerialEntry(comment = "DJ role effect range (blocks)")
        public double djEffectRange = 16.0;

        @SerialEntry(comment = "DJ healing disc health restoration per target")
        public double djHealingAmount = 10.0;

        @SerialEntry(comment = "DJ stamina restore amount per target (ticks)")
        public int djStaminaRestoreTicks = 25;

        @SerialEntry(comment = "DJ money disc income per use (not enforced by core, for integrations)")
        public int djMoneyIncome = 100;

        @SerialEntry(comment = "DJ song id list (网易云歌曲 ID 列表)")
        public java.util.List<Long> djSongIds = java.util.Arrays.asList(210255L, 284578L);

        @SerialEntry(comment = "DJ sanity (mood) restore amount per target (0..1 scale)")
        public float djSanRestoreAmount = 0.015f;

    @SerialEntry(comment = "Toll role cooldown in ticks (default 30 seconds)")
    public int tollCooldownTicks = GameConstants.getInTicks(0,25); // 25s

    @SerialEntry(comment = "Detective role ability ratio (20% of players)")
    public double detectiveAbilityRatio = 0.2;

}