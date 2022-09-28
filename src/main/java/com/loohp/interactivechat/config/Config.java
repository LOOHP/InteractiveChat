/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactivechat.config;

import com.loohp.interactivechat.utils.FileUtils;
import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

    public static Config loadConfig(String id, File file, InputStream ifNotFound, InputStream def, boolean refreshComments, Consumer<Config> dataFixer, Predicate<String> copyDefaultFilter) throws IOException {
        if (CONFIGS.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate config id");
        }

        if (!file.exists()) {
            FileUtils.copy(ifNotFound, file);
        }

        Config config = new Config(file, def, refreshComments, dataFixer, copyDefaultFilter);
        CONFIGS.put(id, config);
        return config;
    }

    public static Config loadConfig(String id, File file, InputStream ifNotFound, InputStream def, boolean refreshComments) throws IOException {
        return loadConfig(id, file, ifNotFound, def, refreshComments, null, path -> true);
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
    private YamlFile defConfig;
    private YamlFile config;

    private Config(File file, InputStream def, boolean refreshComments, Consumer<Config> dataFixer, Predicate<String> copyDefaultFilter) throws IOException {
        this.file = file;

        defConfig = new YamlFile();
        defConfig.options().useComments(true);
        defConfig.load(def);
        config = new YamlFile();
        config.options().useComments(true);
        config.load(file);


        if (dataFixer != null) {
            dataFixer.accept(this);
            reload();
        }

        for (String path : defConfig.getValues(true).keySet()) {
            if (config.contains(path)) {
                if (refreshComments) {
                    config.setComment(path, defConfig.getComment(path, CommentType.BLOCK), CommentType.BLOCK);
                }
            } else if (copyDefaultFilter.test(path) && !defConfig.isConfigurationSection(path)) {
                config.set(path, defConfig.get(path));
                config.setComment(path, defConfig.getComment(path, CommentType.BLOCK), CommentType.BLOCK);
            }
        }

        save();

    }

    private Config(File file) throws IOException {
        config = new YamlFile();
        config.options().useComments(true);
        config.load(file);
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
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reload() {
        try {
            config.load(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public YamlFile getConfiguration() {
        return config;
    }

}
