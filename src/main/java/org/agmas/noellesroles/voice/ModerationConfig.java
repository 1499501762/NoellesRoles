package org.agmas.noellesroles.voice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ModerationConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static class MutedPlayer {
        public UUID uuid;
        public String name;
        public long muteUntil; // 0 = 永久
        public String reason;
    }

    public static class DeafenedPlayer {
        public UUID uuid;
        public String name;
        public long until; // 0 = 永久
        public String reason;
    }

    private final File file;
    List<MutedPlayer> mutedList = new ArrayList<>();
    List<DeafenedPlayer> deafenedList = new ArrayList<>();

    public ModerationConfig(File file) {
        this.file = file;
    }

    public synchronized void load() throws IOException {
        if (!file.exists()) return;
        try (FileReader fr = new FileReader(file)) {
            Type type = new TypeToken<ConfigContainer>(){}.getType();
            ConfigContainer container = GSON.fromJson(fr, type);
            if (container != null) {
                this.mutedList = container.muted != null ? container.muted : new ArrayList<>();
                this.deafenedList = container.deafened != null ? container.deafened : new ArrayList<>();
            }
        }
    }

    public synchronized void save() throws IOException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        ConfigContainer container = new ConfigContainer();
        container.muted = this.mutedList;
        container.deafened = this.deafenedList;
        try (FileWriter fw = new FileWriter(file)) {
            GSON.toJson(container, fw);
        }
    }

    public synchronized boolean isMuted(UUID uuid) {
        long now = System.currentTimeMillis();
        Iterator<MutedPlayer> it = mutedList.iterator();
        while (it.hasNext()) {
            MutedPlayer mp = it.next();
            if (mp.uuid.equals(uuid)) {
                if (mp.muteUntil == 0 || mp.muteUntil > now) return true;
                it.remove();
                return false;
            }
        }
        return false;
    }

    public synchronized boolean isDeafened(UUID uuid) {
        long now = System.currentTimeMillis();
        Iterator<DeafenedPlayer> it = deafenedList.iterator();
        while (it.hasNext()) {
            DeafenedPlayer dp = it.next();
            if (dp.uuid.equals(uuid)) {
                if (dp.until == 0 || dp.until > now) return true;
                it.remove();
                return false;
            }
        }
        return false;
    }

    public synchronized void addMute(MutedPlayer mp) {
        removeMute(mp.uuid);
        mutedList.add(mp);
    }

    public synchronized void removeMute(UUID uuid) {
        mutedList.removeIf(m -> m.uuid.equals(uuid));
    }

    public synchronized void addDeafen(DeafenedPlayer dp) {
        removeDeafen(dp.uuid);
        deafenedList.add(dp);
    }

    public synchronized void removeDeafen(UUID uuid) {
        deafenedList.removeIf(d -> d.uuid.equals(uuid));
    }

    private static class ConfigContainer {
        List<MutedPlayer> muted;
        List<DeafenedPlayer> deafened;
    }
}
