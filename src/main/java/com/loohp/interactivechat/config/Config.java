package com.loohp.interactivechat.config;

import com.loohp.interactivechat.utils.FileUtils;
import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

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

    public static void reloadConfigs() throws InvalidConfigurationException, IOException {
        for (Config config : CONFIGS.values()) {
            config.reload();
        }
    }

    public static void saveConfigs() {
        for (Config config : CONFIGS.values()) {
            config.save();
        }
    }

    public static Config loadConfig(String id, File file, InputStream ifNotFound, InputStream def, boolean refreshComments, Consumer<Config> dataFixer) throws IOException, InvalidConfigurationException {
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

    public static Config loadConfig(String id, File file, InputStream ifNotFound, InputStream def, boolean refreshComments) throws IOException, InvalidConfigurationException {
        return loadConfig(id, file, ifNotFound, def, refreshComments, null);
    }

    public static Config loadConfig(String id, File file) {
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
    private YamlFile defConfig;
    private YamlFile config;

    private Config(File file, InputStream def, boolean refreshComments, Consumer<Config> dataFixer) throws IOException, InvalidConfigurationException {
        this.file = file;

        defConfig = YamlFile.loadConfiguration(def, true);
        config = new YamlFile(file);
        config.loadWithComments();

        if (dataFixer != null) {
            dataFixer.accept(this);
            config = new YamlFile(file);
            config.loadWithComments();
        }

        for (String path : defConfig.getValues(true).keySet()) {
            if (config.contains(path)) {
                if (refreshComments) {
                    config.setComment(path, defConfig.getComment(path, CommentType.BLOCK), CommentType.BLOCK);
                }
            } else if (!defConfig.isConfigurationSection(path)) {
                config.set(path, defConfig.get(path));
                config.setComment(path, defConfig.getComment(path, CommentType.BLOCK), CommentType.BLOCK);
            }
        }

        save();
    }

    private Config(File file) {
        config = YamlFile.loadConfiguration(file, true);
        save();
    }

    public File getFile() {
        return file;
    }

    public void save() {
        save(file);
    }

    public void save(File file) {
        try {
            for (String path : config.getValues(true).keySet()) {
                config.setComment(path, null, CommentType.SIDE);
            }
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() throws InvalidConfigurationException, IOException {
        config = new YamlFile(file);
        config.loadWithComments();
    }

    public YamlFile getConfiguration() {
        return config;
    }

}
