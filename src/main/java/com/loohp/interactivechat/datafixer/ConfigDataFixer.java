package com.loohp.interactivechat.datafixer;

import java.io.File;

import org.simpleyaml.configuration.ConfigurationSection;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.config.Config;
import com.loohp.interactivechat.updater.Version;
import com.loohp.interactivechat.utils.CustomStringUtils;

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
		
		config.getConfiguration().set("ConfigVersion", InteractiveChat.plugin.getDescription().getVersion());
		config.save();
	}

}
