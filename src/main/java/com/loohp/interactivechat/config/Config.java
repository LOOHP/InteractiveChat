package com.loohp.interactivechat.config;

import com.loohp.interactivechat.utils.FileUtils;
import com.loohp.yamlconfiguration.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Config {

    private static final Map<String, Config> CONFIGS = new HashMap<>();

    public static Config getConfig(String id) {
        return CONFIGS.get(id);
    }

    public static void reloadConfigs() throws IOException {
        for (Config config : CONFIGS.values()) {
            config.reload();
        }
    }

    public static void saveConfigs() {
        for (Config config : CONFIGS.values()) {
            config.save();
        }
    }

    public static Config loadConfig(String id, File file, InputStream ifNotFound, InputStream def, boolean refreshComments, Consumer<Config> dataFixer) throws IOException {
        if (CONFIGS.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate config id");
        }

        if (!file.exists()) {
            FileUtils.copy(ifNotFound, file);
        }

        Config config = new Config(file, def, refreshComments, dataFixer);
        CONFIGS.put(id, config);
        return config;
    }

    public static Config loadConfig(String id, File file, InputStream ifNotFound, InputStream def, boolean refreshComments) throws IOException {
        return loadConfig(id, file, ifNotFound, def, refreshComments, null);
    }

    public static Config loadConfig(String id, File file) throws IOException {
        if (getConfig(id) != null) {
            throw new IllegalArgumentException("Duplicate config id");
        }
        if (!file.exists()) {
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println();
                pw.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        Config config = new Config(file);
        CONFIGS.put(id, config);
        return config;
    }

    public static boolean unloadConfig(String id, boolean save) {
        Config config = CONFIGS.remove(id);
        if (config != null) {
            if (save) {
                config.save();
            }
            return true;
        } else {
            return false;
        }
    }

    private File file;
    private YamlConfiguration defConfig;
    private YamlConfiguration config;

    private Config(File file, InputStream def, boolean refreshComments, Consumer<Config> dataFixer) throws IOException {
        this.file = file;

        defConfig = new YamlConfiguration(def);
        config = new YamlConfiguration(file);

        if (dataFixer != null) {
            dataFixer.accept(this);
            config.reload();
        }

        for (String path : defConfig.getValues(true).keySet()) {
            if (config.contains(path)) {
                if (refreshComments) {
                    config.setAboveComment(path, defConfig.getAboveComment(path));
                }
            } else if (!defConfig.isConfigurationSection(path)) {
                config.set(path, defConfig.get(path));
                config.setAboveComment(path, defConfig.getAboveComment(path));
            }
        }

        save();
    }

    private Config(File file) throws IOException {
        config = new YamlConfiguration(file);
        save();
    }

    public File getFile() {
        return file;
    }

    public void save() {
        save(file);
    }

    public void save(File file) {
        config.save(file);
    }

    public void reload() {
        config.reload();
    }

    public YamlConfiguration getConfiguration() {
        return config;
    }

}
