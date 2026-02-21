package org.agmas.noellesroles.voice;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Consumer;

public class VoiceChatManager {

    private static ModerationConfig config;

    public static void init(File configDir) throws IOException {
        File cfgFile = new File(configDir, "voicechat_moderation.json");
        config = new ModerationConfig(cfgFile);
        config.load();
        setupEvents();
    }

    private static void setupEvents() {
        try {
            // Use reflection to avoid a hard compile-time dependency on the VoiceChat intercompat class.
            Class<?> ccmClass = Class.forName("de.maxhenkel.voicechat.intercompatibility.FabricCommonCompatibilityManager");
            Field instField = ccmClass.getField("INSTANCE");
            Object instance = instField.get(null);

            // Try exact match first
            Method target = null;
            try {
                target = ccmClass.getMethod("onServerVoiceChatConnected", java.util.function.Consumer.class);
            } catch (NoSuchMethodException ignored) {
            }

            // Fallback: find any single-arg method with the expected name
            if (target == null) {
                for (Method mm : ccmClass.getMethods()) {
                    if (mm.getName().equals("onServerVoiceChatConnected") && mm.getParameterCount() == 1) {
                        target = mm;
                        break;
                    }
                }
            }

            if (target != null) {
                final Method callMethod = target;
                Consumer<Object> consumer = (obj) -> {
                    try {
                        if (obj instanceof ServerPlayerEntity) {
                            ServerPlayerEntity player = (ServerPlayerEntity) obj;
                            if (config != null && config.isMuted(player.getUuid())) {
                                player.sendMessage(Text.translatable("message.noellesroles.voice.muted_blocked"), true);

                                // Try to enforce mute via VoiceChat intercompat: attempt NetManager disconnect or PermissionManager deny.
                                try {
                                    Method getNet = null;
                                    try {
                                        getNet = ccmClass.getMethod("getNetManager");
                                    } catch (NoSuchMethodException ignored) {}

                                    if (getNet != null) {
                                        Object netMgr = getNet.invoke(instance);
                                        if (netMgr != null) {
                                            // Try common method names that could disconnect/stop voice for a player
                                            Method[] nmMethods = netMgr.getClass().getMethods();
                                            for (Method m : nmMethods) {
                                                String name = m.getName().toLowerCase();
                                                Class<?>[] params = m.getParameterTypes();
                                                try {
                                                    if ((name.contains("disconnect") || name.contains("remove") || name.contains("kick")) && params.length == 1) {
                                                        if (params[0].isAssignableFrom(ServerPlayerEntity.class)) {
                                                            m.invoke(netMgr, player);
                                                            break;
                                                        } else if (params[0].isAssignableFrom(UUID.class)) {
                                                            m.invoke(netMgr, player.getUuid());
                                                            break;
                                                        }
                                                    }
                                                } catch (Throwable ignored) {
                                                }
                                            }
                                        }
                                    }

                                    // Try permission manager approach: create one and set a restrictive permission if available
                                    Method createPm = null;
                                    try {
                                        createPm = ccmClass.getMethod("createPermissionManager");
                                    } catch (NoSuchMethodException ignored) {}
                                    if (createPm != null) {
                                        Object pm = createPm.invoke(instance);
                                        if (pm != null) {
                                            // Try to find a method that can set permissions for a player
                                            for (Method m : pm.getClass().getMethods()) {
                                                String name = m.getName().toLowerCase();
                                                Class<?>[] params = m.getParameterTypes();
                                                try {
                                                    if ((name.contains("set") || name.contains("grant") || name.contains("add")) && params.length >= 2) {
                                                        // search for signature (ServerPlayer, String, int) or (UUID, String, int)
                                                        if (params[0].isAssignableFrom(ServerPlayerEntity.class) && params[1].isAssignableFrom(String.class) && params[2].isAssignableFrom(int.class)) {
                                                            // set a deny-level permission for speaking (best-effort)
                                                            try {
                                                                m.invoke(pm, player, "voicechat.speak", 4);
                                                                break;
                                                            } catch (Throwable ignored) {}
                                                        } else if (params[0].isAssignableFrom(UUID.class) && params[1].isAssignableFrom(String.class) && params[2].isAssignableFrom(int.class)) {
                                                            try {
                                                                m.invoke(pm, player.getUuid(), "voicechat.speak", 4);
                                                                break;
                                                            } catch (Throwable ignored) {}
                                                        }
                                                    }
                                                } catch (Throwable ignored) {
                                                }
                                            }
                                        }
                                    }
                                } catch (Throwable ignored) {
                                }
                            }
                        }
                    } catch (Throwable ignored) {
                    }
                };

                // If the parameter type is not java.util.function.Consumer, try to adapt
                Class<?> paramType = callMethod.getParameterTypes()[0];
                if (paramType.isAssignableFrom(java.util.function.Consumer.class)) {
                    callMethod.invoke(instance, consumer);
                } else {
                    // Try to wrap consumer into the expected functional interface via a proxy
                    Object proxy = java.lang.reflect.Proxy.newProxyInstance(
                            paramType.getClassLoader(),
                            new Class[]{paramType},
                            (proxyObj, method, args) -> method.invoke(consumer, args)
                    );
                    try {
                        callMethod.invoke(instance, proxy);
                    } catch (IllegalArgumentException iae) {
                        // Last resort: try invoking with raw consumer (may still work)
                        callMethod.invoke(instance, (Object) consumer);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            // VoiceChat not present on compile/runtime classpath — ignore
        } catch (Throwable t) {
            // Any other reflection problem — swallow to avoid breaking mod load
        }
    }

    public static void mutePlayer(ServerPlayerEntity player, long durationMs, String reason) throws IOException {
        ModerationConfig.MutedPlayer mp = new ModerationConfig.MutedPlayer();
        mp.uuid = player.getUuid();
        mp.name = player.getName().getString();
        mp.muteUntil = durationMs == 0 ? 0 : System.currentTimeMillis() + durationMs;
        mp.reason = reason;
        config.addMute(mp);
        config.save();
        player.sendMessage(Text.translatable("message.noellesroles.voice.muted", reason), true);
    }

    public static void unmutePlayer(ServerPlayerEntity player) throws IOException {
        config.removeMute(player.getUuid());
        config.save();
        player.sendMessage(Text.translatable("message.noellesroles.voice.unmuted"), true);
    }

    public static void deafenPlayer(ServerPlayerEntity player, long durationMs, String reason) throws IOException {
        ModerationConfig.DeafenedPlayer dp = new ModerationConfig.DeafenedPlayer();
        dp.uuid = player.getUuid();
        dp.name = player.getName().getString();
        dp.until = durationMs == 0 ? 0 : System.currentTimeMillis() + durationMs;
        dp.reason = reason;
        config.addDeafen(dp);
        config.save();
        player.sendMessage(Text.translatable("message.noellesroles.voice.deafened", reason), true);
    }

    public static void undeafenPlayer(ServerPlayerEntity player) throws IOException {
        config.removeDeafen(player.getUuid());
        config.save();
        player.sendMessage(Text.translatable("message.noellesroles.voice.undeafened"), true);
    }

    public static boolean isMuted(UUID uuid) {
        return config != null && config.isMuted(uuid);
    }

    public static boolean isDeafened(UUID uuid) {
        return config != null && config.isDeafened(uuid);
    }
}
