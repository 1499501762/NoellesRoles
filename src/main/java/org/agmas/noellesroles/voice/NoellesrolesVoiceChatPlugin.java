package org.agmas.noellesroles.voice;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.dumb.DumbPlayerComponent;
import org.agmas.noellesroles.voice.VoiceChatManager;

public class NoellesrolesVoiceChatPlugin implements VoicechatPlugin {
    @Override
    public String getPluginId() {
        return Noellesroles.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        VoicechatPlugin.super.initialize(api);
    }

    public void paranoidEvent(MicrophonePacketEvent event) {
        VoicechatServerApi api = event.getVoicechat();
        ServerPlayerEntity spectator = ((ServerPlayerEntity)event.getSenderConnection().getPlayer().getPlayer());
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(spectator.getWorld());
        if (spectator.interactionManager.getGameMode().equals(GameMode.SPECTATOR)) {
            spectator.getWorld().getPlayers().forEach((p) -> {
                if (gameWorldComponent.isRole(p, Noellesroles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES) && GameFunctions.isPlayerAliveAndSurvival(p)) {
                    if (spectator.distanceTo(p) <= api.getVoiceChatDistance()) {
                        VoicechatConnection con = api.getConnectionOf(p.getUuid());
                        api.sendLocationalSoundPacketTo(con, event.getPacket().locationalSoundPacketBuilder()
                                        .position(api.createPosition(p.getX(), p.getY(), p.getZ()))
                                        .distance((float)api.getVoiceChatDistance())
                                        .build());
                    }
                }
            });
        }
    }

    public void dumbEvent(MicrophonePacketEvent event) {
        // 检查发送者是否有哑巴modifier
        if (event.getSenderConnection() == null || event.getSenderConnection().getPlayer() == null) return;
        ServerPlayerEntity sender = (ServerPlayerEntity) event.getSenderConnection().getPlayer().getPlayer();
        if (sender == null) return;

        DumbPlayerComponent dumbComp = DumbPlayerComponent.KEY.get(sender);
        boolean isDumb = dumbComp != null && dumbComp.isDumb();
        boolean isMuted = false;
        try {
            isMuted = VoiceChatManager.isMuted(sender.getUuid());
        } catch (Throwable ignored) {}

        // 如果是哑巴或被管理器静音，则取消本次语音包事件，防止语音传输，并给发送者一个 action-bar 提示
        if (isDumb || isMuted) {
            if (GameFunctions.isPlayerAliveAndSurvival(sender)) {
                sender.sendMessage(Text.translatable("message.noellesroles.voice.muted_blocked"), true);
                event.cancel();
            }
        }
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::paranoidEvent);
        registration.registerEvent(MicrophonePacketEvent.class, this::dumbEvent);
        VoicechatPlugin.super.registerEvents(registration);
    }
}
