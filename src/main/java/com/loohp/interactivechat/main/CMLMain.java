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

package com.loohp.interactivechat.main;

import com.loohp.interactivechat.updater.Version;
import com.loohp.interactivechat.utils.FileUtils;
import com.loohp.interactivechat.utils.HTTPRequestUtils;
import com.loohp.yamlconfiguration.ConfigurationSection;
import com.loohp.yamlconfiguration.YamlConfiguration;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CMLMain {

    protected static BufferedReader IN = new BufferedReader(new InputStreamReader(System.in));

    public static void launch(String[] args) {
        try {
            YamlConfiguration pluginYaml = new YamlConfiguration(CMLMain.class.getClassLoader().getResourceAsStream("plugin.yml"));

            String pluginName = pluginYaml.getString("name");
            String version = pluginYaml.getString("version");

            System.out.println("Starting " + pluginName + " v" + version + " Tools...");
            System.out.println();
            main: while (true) {
                System.out.println("You are running " + pluginName + " v" + version);
                System.out.println();
                System.out.println("Links:");
                System.out.println("SpigotMC: \"https://www.spigotmc.org/resources/75870/\"");
                System.out.println("GitHub: \"https://github.com/LOOHP/InteractiveChat\"");
                System.out.println("Build Server: \"https://ci.loohpjames.com\"");
                System.out.println();
                System.out.println("Select one of the tools by typing in their corresponding number");

                System.out.println("1. Check for Updates   2. Validate Plugin Configs   3.Generate Default Configs   4. Exit");

                String input = IN.readLine();
                switch (input) {
                    case "1":
                        checkForUpdates(version);
                        break;
                    case "2":
                        validConfigs();
                        break;
                    case "3":
                        generateDefaultConfigs();
                        break;
                    default:
                        break main;
                }
            }
        } catch (Throwable e) {
            System.err.println("An error occurred!");
            e.printStackTrace();
        }
    }

    protected static void checkForUpdates(String localPluginVersion) throws URISyntaxException, IOException {
        JSONObject response = (JSONObject) HTTPRequestUtils.getJSONResponse("https://api.loohpjames.com/spigot/data").get("InteractiveChat");
        String spigotPluginVersion = (String) ((JSONObject) response.get("latestversion")).get("release");
        String devBuildVersion = (String) ((JSONObject) response.get("latestversion")).get("devbuild");
        int spigotPluginId = (int) (long) ((JSONObject) response.get("spigotmc")).get("pluginid");
        int posOfThirdDot = localPluginVersion.indexOf(".", localPluginVersion.indexOf(".", localPluginVersion.indexOf(".") + 1) + 1);
        Version currentDevBuild = new Version(localPluginVersion);
        Version currentRelease = new Version(localPluginVersion.substring(0, posOfThirdDot >= 0 ? posOfThirdDot : localPluginVersion.length()));
        Version spigotmc = new Version(spigotPluginVersion);
        Version devBuild = new Version(devBuildVersion);
        int input;
        if (currentRelease.compareTo(spigotmc) < 0) { //update
            System.out.println("There is a new version available! (" + currentDevBuild + ")\nLocal version: " + localPluginVersion + "");
            System.out.println("You can download a new build at: https://ci.loohpjames.com/job/InteractiveChat/");
        } else if (currentDevBuild.compareTo(devBuild) < 0) { //dev build update
            System.out.println("There is a new DEV build available! (" + currentDevBuild + ")\nLocal version: " + localPluginVersion);
            System.out.println("You can download a new build at: https://ci.loohpjames.com/job/InteractiveChat/");
        } else { //latest
            System.out.println("You are already running the latest version! (" + localPluginVersion + ")");
        }
    }

    protected static void validConfigs() throws IOException {
        File folder = new File("InteractiveChat");
        if (!folder.exists() || !folder.isDirectory()) {
            folder = new File("InteractiveChatBungee");
            if (!folder.exists() || !folder.isDirectory()) {
                System.out.println("Error: Plugin folder not found");
                return;
            }
        }
        Map<File, List<String>> results = new LinkedHashMap<>();
        for (File file : folder.listFiles()) {
            String fileName = file.getName();
            if (fileName.endsWith(".yml")) {
                YamlConfiguration yaml = new YamlConfiguration(new FileInputStream(file));
                results.put(file, validateConfigurationSection("", yaml));
            }
        }
        StringBuilder message = new StringBuilder("Validation Results: (Plugin Folder: " + folder.getAbsolutePath() + ")\n");
        for (Entry<File, List<String>> entry : results.entrySet()) {
            String fileName = entry.getKey().getName();
            List<String> errors = entry.getValue();
            message.append("\n").append(fileName).append(": ");
            if (errors.isEmpty()) {
                message.append("Valid!\n");
            } else {
                message.append("\n");
                for (String error : errors) {
                    message.append(error).append("\n");
                }
            }
        }
        message.append("\nNote that a valid config doesn't mean REGEX are valid.");
        System.out.println(message);
    }

    protected static List<String> validateConfigurationSection(String currentPath, ConfigurationSection section) {
        List<String> errors = new LinkedList<>();
        try {
            for (String key : section.getKeys(false)) {
                String path = currentPath.isEmpty() ? key : currentPath + "." + key;
                try {
                    Object value = section.get(key);
                    if (value instanceof ConfigurationSection) {
                        errors.addAll(validateConfigurationSection(path, (ConfigurationSection) value));
                    }
                } catch (Throwable e) {
                    errors.add("Failed to parse option around: " + path);
                }
            }
        } catch (Throwable e) {
            errors.add("Failed to parse option around: " + currentPath);
        }
        return errors;
    }

    protected static void generateDefaultConfigs() throws IOException {
        System.out.println("Generate configs for...");
        System.out.println("1. Spigot (Backend)   2. Bungeecord (Proxy)");
        String input = IN.readLine();
        File folder = null;
        if (input.equals("1")) {
            folder = new File("InteractiveChat", "generated");
            FileUtils.removeFolderRecursively(folder);
            folder.mkdirs();
            FileUtils.copy(CMLMain.class.getClassLoader().getResourceAsStream("config_default.yml"), new File(folder, "config.yml"));
            FileUtils.copy(CMLMain.class.getClassLoader().getResourceAsStream("storage.yml"), new File(folder, "storage.yml"));
        } else if (input.equals("2")) {
            folder = new File("InteractiveChatBungee", "generated");
            FileUtils.removeFolderRecursively(folder);
            folder.mkdirs();
            FileUtils.copy(CMLMain.class.getClassLoader().getResourceAsStream("config_proxy.yml"), new File(folder, "bungeeconfig.yml"));
        }
        if (folder != null) {
            System.out.println("Files saved at: " + folder.getAbsolutePath());
        }
    }

}
