package org.agmas.noellesroles;


import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.noellesroles.bartender.BartenderPlayerComponent;
import org.agmas.noellesroles.brawler.BrawlerPlayerComponent;
import org.agmas.noellesroles.pickpocket.PickpocketPlayerComponent;
import org.agmas.noellesroles.gambler.GamblerPlayerComponent;
import org.agmas.noellesroles.chameleon.ChameleonPlayerComponent;
import org.agmas.noellesroles.coroner.BodyDeathReasonComponent;
import org.agmas.noellesroles.detective.DetectivePlayerComponent;
import org.agmas.noellesroles.executioner.ExecutionerPlayerComponent;
import org.agmas.noellesroles.sniper.SniperPlayerComponent;
import org.agmas.noellesroles.voodoo.VoodooPlayerComponent;
import org.agmas.noellesroles.morphling.MorphlingPlayerComponent;
import org.agmas.noellesroles.recaller.RecallerPlayerComponent;
import org.agmas.noellesroles.vulture.VulturePlayerComponent;
import org.agmas.noellesroles.thief.ThiefPlayerComponent;
import org.agmas.noellesroles.dumb.DumbPlayerComponent;
import org.agmas.noellesroles.hypnotist.HypnotistPlayerComponent;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.util.Identifier;

public class NoellesRolesComponents implements EntityComponentInitializer, WorldComponentInitializer {
    public NoellesRolesComponents() {
    }

    public void registerEntityComponentFactories(@NotNull EntityComponentFactoryRegistry registry) {
        // Ensure Gambler component key is created through the registry at init-time
        GamblerPlayerComponent.KEY = ComponentRegistry.getOrCreate(Identifier.of(Noellesroles.MOD_ID, "gambler"), GamblerPlayerComponent.class);
        registry.beginRegistration(PlayerEntity.class, MorphlingPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(MorphlingPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, BartenderPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(BartenderPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, VoodooPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(VoodooPlayerComponent::new);
        registry.beginRegistration(PlayerBodyEntity.class, BodyDeathReasonComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(BodyDeathReasonComponent::new);
        registry.beginRegistration(PlayerEntity.class, AbilityPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(AbilityPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, ExecutionerPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(ExecutionerPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, RecallerPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(RecallerPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, VulturePlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(VulturePlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, SniperPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(SniperPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, DetectivePlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(DetectivePlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, ChameleonPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(ChameleonPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, ThiefPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(ThiefPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, BrawlerPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(BrawlerPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, GamblerPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(GamblerPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, HypnotistPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(HypnotistPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, PickpocketPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(PickpocketPlayerComponent::new);
        registry.beginRegistration(PlayerEntity.class, DumbPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(DumbPlayerComponent::new);
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry worldComponentFactoryRegistry) {
        worldComponentFactoryRegistry.register(ConfigWorldComponent.KEY, ConfigWorldComponent::new);
    }
}