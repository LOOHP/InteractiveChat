package com.loohp.interactivechat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.loohp.interactivechat.Utils.Updater;

import net.md_5.bungee.api.ChatColor;

public class ConfigManager {

	// Files & File Configs Here
	public static FileConfiguration config;
	public static File file;
	// --------------------------

	public static void setup() {
		if (!InteractiveChat.plugin.getDataFolder().exists()) {
			InteractiveChat.plugin.getDataFolder().mkdir();
		}
		file = new File(InteractiveChat.plugin.getDataFolder().getAbsolutePath() + "/config.yml");
		if (!file.exists()) {
			try {
				InputStream in = InteractiveChat.plugin.getClass().getResourceAsStream("/config.yml");
	            Files.copy(in, file.toPath());
				Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "The config.yml file has been created");
			} catch (IOException e) {
				Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Could not create the config.yml file");
			}
		}
        
        config = YamlConfiguration.loadConfiguration(file);
        saveConfig();
        
	    //-----Add new config parts
	    if (!config.contains("Settings.PlayerNotFound")) {
	    	InputStream FileIn = InteractiveChat.plugin.getClass().getResourceAsStream("/append.yml");
		    try {
		        // create a writer for permFile
		        BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
		        // create a reader for tmpFile
		        BufferedReader in = new BufferedReader(new InputStreamReader(FileIn, "UTF-8"));
		        String str;
		        while ((str = in.readLine()) != null) {
		            out.write("\n" + str);
		        }
		        in.close();
		        out.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
	    reloadConfig();
	    saveConfig();
	    
	    for (int customNo = 1; InteractiveChat.plugin.getConfig().contains("CustomPlaceholders." + String.valueOf(customNo)) == true; customNo = customNo + 1) {
	    	if (!getConfig().contains("CustomPlaceholders." + String.valueOf(customNo) + ".CaseSensitive")) {
	    		getConfig().set("CustomPlaceholders." + String.valueOf(customNo) + ".CaseSensitive", true);
	    	}
	    }
	    reloadConfig();
	    saveConfig();
	    
	    //-----Add unused warning
	    if (getConfig().contains("BuiltIn")) {
    		getConfig().set("BuiltIn.IMPORTANT_NOTICE1", "BuiltIn Section of the config is removed in 1.1.0 !!! Please edit the ItemDisplay Section instead!");
    		getConfig().set("BuiltIn.IMPORTANT_NOTICE2", "BuiltIn Section of the config is removed in 1.1.0 !!! Please edit the ItemDisplay Section instead!");
    		getConfig().set("BuiltIn.IMPORTANT_NOTICE3", "BuiltIn Section of the config is removed in 1.1.0 !!! Please edit the ItemDisplay Section instead!");
    	    saveConfig();
    	    reloadConfig();
    	}
	    //------Add cooldowns
	    if (!getConfig().contains("ItemDisplay.Item.Cooldown")) {
    		getConfig().set("ItemDisplay.Item.Cooldown", 0);   		
    	}
	    if (!getConfig().contains("ItemDisplay.Inventory.Cooldown")) {
    		getConfig().set("ItemDisplay.Inventory.Cooldown", 0);   		
    	}
	    if (!getConfig().contains("ItemDisplay.EnderChest.Cooldown")) {
    		getConfig().set("ItemDisplay.EnderChest.Cooldown", 0);   		
    	}
	    if (!getConfig().contains("Settings.UniversalCooldown")) {
    		getConfig().set("Settings.UniversalCooldown", 0);   		
    	}
	    for (int customNo = 1; getConfig().contains("CustomPlaceholders." + String.valueOf(customNo)) == true; customNo = customNo + 1) {
	    	if (!getConfig().contains("CustomPlaceholders." + String.valueOf(customNo) + ".ParseKeyword")) {
	    		getConfig().set("CustomPlaceholders." + String.valueOf(customNo) + ".ParseKeyword", false);   		
	    	}
	    }
	    if (getConfig().contains("ItemDisplay.Inventory.HoverMessage")) {
	    	String hover = getConfig().getString("ItemDisplay.Inventory.HoverMessage");
	    	if (hover != null) {
	    		if (!hover.contains("[") && !hover.contains("]")) {
		    		List<String> hoverList = new ArrayList<String>();
					hoverList.add(hover);
		    		getConfig().set("ItemDisplay.Inventory.HoverMessage", hoverList);   
	    		}
	    	}
    	}
	    if (getConfig().contains("ItemDisplay.EnderChest.HoverMessage")) {
	    	String hover = getConfig().getString("ItemDisplay.EnderChest.HoverMessage");
	    	if (hover != null) {
	    		if (!hover.contains("[") && !hover.contains("]")) {
		    		List<String> hoverList = new ArrayList<String>();
					hoverList.add(hover);
					getConfig().set("ItemDisplay.EnderChest.HoverMessage", hoverList);   
	    		}
	    	}
    	}
	    
	    if (!getConfig().contains("ItemDisplay.Inventory.HoverMessage")) {
	    	List<String> hoverList = new ArrayList<String>();
			hoverList.add("&bClick to view!");
    		getConfig().set("ItemDisplay.Inventory.HoverMessage", hoverList);   		
    	}
	    if (!getConfig().contains("ItemDisplay.EnderChest.HoverMessage")) {
			List<String> hoverList = new ArrayList<String>();
			hoverList.add("&dClick to view!");
    		getConfig().set("ItemDisplay.EnderChest.HoverMessage", hoverList);   		
    	}
	    
	    if (!getConfig().contains("Messages.LimitReached")) {
	    	getConfig().set("Messages.LimitReached", "&cPlease do now use excessive amount of placeholders in one message!");
	    }
	    
	    if (!getConfig().contains("Settings.MaxPlaceholders")) {
	    	getConfig().set("Settings.MaxPlaceholders", -1);
	    }
	    
	    if (!getConfig().contains("Chat.MentionHighlight")) {
	    	getConfig().set("Chat.MentionHighlight", "&e{MentionedPlayer}");
	    }
	    
	    if (!getConfig().contains("Chat.MentionHoverText")) {
			List<String> hoverList = new ArrayList<String>();
			hoverList.add("&b{Sender} mentioned you!");
    		getConfig().set("Chat.MentionHoverText", hoverList);   		
    	}
	    
	    if (!getConfig().contains("Options.Updater")) {
	    	getConfig().set("Options.Updater", true);
	    }
	    
	    saveConfig();
	    reloadConfig();
	    //------
	    loadConfig();
	}

	public static FileConfiguration getConfig() {
		return config;
	}

	public static void saveConfig() {
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void reloadConfig() {
		config = YamlConfiguration.loadConfiguration(file);
	}
	
	public static void loadConfig() {	
		InteractiveChat.AllowMention = ConfigManager.getConfig().getBoolean("Chat.AllowMention");
		
		InteractiveChat.NoPermission = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("Messages.NoPermission"));
		InteractiveChat.InvExpired = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("Messages.InvExpired"));
		InteractiveChat.ReloadPlugin = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("Messages.ReloadPlugin"));
		
		InteractiveChat.useItem = ConfigManager.getConfig().getBoolean("ItemDisplay.Item.Enabled");
		InteractiveChat.useInventory = ConfigManager.getConfig().getBoolean("ItemDisplay.Inventory.Enabled");
		InteractiveChat.useEnder = ConfigManager.getConfig().getBoolean("ItemDisplay.EnderChest.Enabled");
		
		InteractiveChat.itemCaseSensitive = ConfigManager.getConfig().getBoolean("ItemDisplay.Item.CaseSensitive");
		InteractiveChat.invCaseSensitive = ConfigManager.getConfig().getBoolean("ItemDisplay.Inventory.CaseSensitive");
		InteractiveChat.enderCaseSensitive = ConfigManager.getConfig().getBoolean("ItemDisplay.EnderChest.CaseSensitive");
	
		InteractiveChat.itemPlaceholder = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("ItemDisplay.Item.Keyword"));
		InteractiveChat.invPlaceholder = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("ItemDisplay.Inventory.Keyword"));
		InteractiveChat.enderPlaceholder = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("ItemDisplay.EnderChest.Keyword"));
		
		InteractiveChat.itemReplaceText = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("ItemDisplay.Item.Text"));
		InteractiveChat.invReplaceText = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("ItemDisplay.Inventory.Text"));
		InteractiveChat.enderReplaceText = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("ItemDisplay.EnderChest.Text"));
		
		InteractiveChat.itemTitle = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("ItemDisplay.Item.InventoryTitle"));
		InteractiveChat.invTitle = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("ItemDisplay.Inventory.InventoryTitle"));
		InteractiveChat.enderTitle = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("ItemDisplay.EnderChest.InventoryTitle"));
		
		InteractiveChat.usePlayerName = ConfigManager.getConfig().getBoolean("Player.UsePlayerNameInteraction");
		InteractiveChat.usePlayerNameHoverEnable = ConfigManager.getConfig().getBoolean("Player.Hover.Enable");
		List<String> stringList = ConfigManager.getConfig().getStringList("Player.Hover.Text");
		InteractiveChat.usePlayerNameHoverText = ChatColor.translateAlternateColorCodes('&', String.join("\n", stringList));
		InteractiveChat.usePlayerNameClickEnable = ConfigManager.getConfig().getBoolean("Player.Click.Enable");
		InteractiveChat.usePlayerNameClickAction = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("Player.Click.Action"));
		InteractiveChat.usePlayerNameClickValue = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("Player.Click.Value"));
		InteractiveChat.usePlayerNameCaseSensitive = ConfigManager.getConfig().getBoolean("Player.CaseSensitive");
		
		InteractiveChat.PlayerNotFoundHoverEnable = ConfigManager.getConfig().getBoolean("Settings.PlayerNotFound.Hover.Enable");
		List<String> stringList2 = ConfigManager.getConfig().getStringList("Settings.PlayerNotFound.Hover.Text");
		InteractiveChat.PlayerNotFoundHoverText = ChatColor.translateAlternateColorCodes('&', String.join("\n", stringList2));
		InteractiveChat.PlayerNotFoundClickEnable = ConfigManager.getConfig().getBoolean("Settings.PlayerNotFound.Click.Enable");
		InteractiveChat.PlayerNotFoundClickAction = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("Settings.PlayerNotFound.Click.Action"));
		InteractiveChat.PlayerNotFoundClickValue = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("Settings.PlayerNotFound.Click.Value"));
		InteractiveChat.PlayerNotFoundReplaceEnable = ConfigManager.getConfig().getBoolean("Settings.PlayerNotFound.Replace.Enable");
		InteractiveChat.PlayerNotFoundReplaceText = ChatColor.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("Settings.PlayerNotFound.Replace.ReplaceText"));
		
		InteractiveChat.placeholderList.clear();
		InteractiveChat.placeholderList.add(InteractiveChat.itemPlaceholder);
		InteractiveChat.placeholderList.add(InteractiveChat.invPlaceholder);
		InteractiveChat.placeholderList.add(InteractiveChat.enderPlaceholder);
		for (int customNo = 1; ConfigManager.getConfig().contains("CustomPlaceholders." + String.valueOf(customNo)) == true; customNo = customNo + 1) {
			InteractiveChat.placeholderList.add(ConfigManager.getConfig().getString("CustomPlaceholders." + String.valueOf(customNo) + ".Text"));
		}
		
		InteractiveChat.maxPlacholders = ConfigManager.getConfig().getInt("Settings.MaxPlaceholders");
		InteractiveChat.limitReachMessage = ConfigManager.getConfig().getString("Messages.LimitReached");
		
		InteractiveChat.mentionHightlight = ConfigManager.getConfig().getString("Chat.MentionHighlight");
		List<String> stringList3 = ConfigManager.getConfig().getStringList("Chat.MentionHoverText");
		InteractiveChat.mentionHover = String.join("\n", stringList3);
		
		if (InteractiveChat.UpdaterTaskID >= 0) {
			Bukkit.getScheduler().cancelTask(InteractiveChat.UpdaterTaskID);
		}
		InteractiveChat.UpdaterEnabled = InteractiveChat.plugin.getConfig().getBoolean("Options.Updater");
		if (InteractiveChat.UpdaterEnabled == true) {
			Updater.updaterInterval();
		}
	}
}