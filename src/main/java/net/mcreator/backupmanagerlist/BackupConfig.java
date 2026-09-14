package net.mcreator.backupmanagerlist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BackupConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ConfigData cachedConfig = null;

    public static class ConfigData {
        public List<String> searchPaths = new ArrayList<>();
    }

    public static ConfigData getConfig() {
        if (cachedConfig != null) {
            return cachedConfig;
        }

        File configFile = new File(Minecraft.getInstance().gameDirectory, "config/backup_manager_list.json");

        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            cachedConfig = createDefaultConfig(configFile);
            return cachedConfig;
        }

        try (FileReader reader = new FileReader(configFile)) {
            cachedConfig = GSON.fromJson(reader, ConfigData.class);
            if (cachedConfig == null || cachedConfig.searchPaths.isEmpty()) {
                cachedConfig = createDefaultConfig(configFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
            cachedConfig = createDefaultConfig(configFile);
        }

        return cachedConfig;
    }

    private static ConfigData createDefaultConfig(File configFile) {
        ConfigData config = new ConfigData();
        
        // Target system folder names using {world_id}
        config.searchPaths.add("backups/{world_id}");
        config.searchPaths.add("backups");
        config.searchPaths.add("simplebackups/{world_id}");
        config.searchPaths.add("ftbbackups/{world_id}");

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return config;
    }
}