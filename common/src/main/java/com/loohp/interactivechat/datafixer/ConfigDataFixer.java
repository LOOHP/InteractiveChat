/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
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

package com.loohp.interactivechat.datafixer;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.config.Config;
import com.loohp.interactivechat.updater.Version;
import com.loohp.interactivechat.utils.CustomStringUtils;
import org.simpleyaml.configuration.ConfigurationSection;

import java.io.File;
import java.util.Collections;

public class ConfigDataFixer {

    public static final String BASE_PLUGIN_VERSION = "4.1.1.8";

    public static void update(Config config) {
        String versionString = config.getConfiguration().getString("ConfigVersion");
        if (versionString == null) {
            versionString = BASE_PLUGIN_VERSION;
            config.getConfiguration().set("ConfigVersion", versionString);
        }
        Version configVersion = new Version(versionString);
        boolean backup = false;

        if (configVersion.compareTo(new Version("4.1.1.9")) < 0) {
            if (!backup) {
                config.save(new File(config.getFile().getParent(), config.getFile().getName() + "." + versionString + ".bak"));
            }
            backup = true;

            //Regex placeholder
            boolean itemCase = config.getConfiguration().getBoolean("ItemDisplay.Item.CaseSensitive", false);
            config.getConfiguration().set("ItemDisplay.Item.CaseSensitive", null);
            config.getConfiguration().set("ItemDisplay.Item.Aliases", null);
            config.getConfiguration().set("ItemDisplay.Item.Name", config.getConfiguration().getString("ItemDisplay.Item.Keyword"));
            config.getConfiguration().set("ItemDisplay.Item.Keyword", (itemCase ? "" : "(?i)") + CustomStringUtils.escapeMetaCharacters(config.getConfiguration().getString("ItemDisplay.Item.Keyword")));

            boolean invCase = config.getConfiguration().getBoolean("ItemDisplay.Inventory.CaseSensitive", false);
            config.getConfiguration().set("ItemDisplay.Inventory.CaseSensitive", null);
            config.getConfiguration().set("ItemDisplay.Inventory.Aliases", null);
            config.getConfiguration().set("ItemDisplay.Inventory.Name", config.getConfiguration().getString("ItemDisplay.Inventory.Keyword"));
            config.getConfiguration().set("ItemDisplay.Inventory.Keyword", (invCase ? "" : "(?i)") + CustomStringUtils.escapeMetaCharacters(config.getConfiguration().getString("ItemDisplay.Inventory.Keyword")));

            boolean enderCase = config.getConfiguration().getBoolean("ItemDisplay.EnderChest.CaseSensitive", false);
            config.getConfiguration().set("ItemDisplay.EnderChest.CaseSensitive", null);
            config.getConfiguration().set("ItemDisplay.EnderChest.Aliases", null);
            config.getConfiguration().set("ItemDisplay.EnderChest.Name", config.getConfiguration().getString("ItemDisplay.EnderChest.Keyword"));
            config.getConfiguration().set("ItemDisplay.EnderChest.Keyword", (enderCase ? "" : "(?i)") + CustomStringUtils.escapeMetaCharacters(config.getConfiguration().getString("ItemDisplay.EnderChest.Keyword")));

            for (int customNo = 1; config.getConfiguration().contains("CustomPlaceholders." + customNo); customNo++) {
                ConfigurationSection s = config.getConfiguration().getConfigurationSection("CustomPlaceholders." + customNo);
                String text = s.getString("Text");
                s.set("Text", null);
                boolean caseSensitive = s.getBoolean("CaseSensitive", false);
                s.set("CaseSensitive", null);
                s.set("Keyword", (caseSensitive ? "" : "(?i)") + CustomStringUtils.escapeMetaCharacters(text));
                s.set("Name", text);
                s.set("Aliases", null);
            }
        }

        if (configVersion.compareTo(new Version("4.1.1.13")) < 0) {
            if (!backup) {
                config.save(new File(config.getFile().getParent(), config.getFile().getName() + "." + versionString + ".bak"));
            }
            backup = true;

            config.getConfiguration().set("Settings.FormattingTags.AdditionalRGBFormats", Collections.singletonList("#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])"));
        }

        if (configVersion.compareTo(new Version("4.1.2.14")) < 0) {
            if (!backup) {
                config.save(new File(config.getFile().getParent(), config.getFile().getName() + "." + versionString + ".bak"));
            }
            backup = true;

            config.getConfiguration().set("Settings.ChatListeningPlugins", null);
        }

        config.getConfiguration().set("ConfigVersion", InteractiveChat.plugin.getDescription().getVersion());
        config.save();
    }

}
