package com.loohp.interactivechat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

public class ConfigManager {
	
	private static String version = InteractiveChat.version;
	
	public static FileConfiguration getConfig() {
		return InteractiveChat.plugin.getConfig();
	}
	
	public static void saveConfig() {
		InteractiveChat.plugin.saveConfig();
	}
	
	public static void reloadConfig() {
		InteractiveChat.plugin.reloadConfig();
		loadConfig();
	}
	
	@SuppressWarnings("deprecation")
	public static void loadConfig() {
		InteractiveChat.aliasesMapping.clear();
		
		InteractiveChat.FilterUselessColorCodes = getConfig().getBoolean("Settings.FilterUselessColorCodes");
		
		InteractiveChat.AllowMention = getConfig().getBoolean("Chat.AllowMention");
		
		InteractiveChat.NoPermission = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.NoPermission"));
		InteractiveChat.InvExpired = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.InvExpired"));
		InteractiveChat.ReloadPlugin = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.ReloadPlugin"));
		
		InteractiveChat.useItem = getConfig().getBoolean("ItemDisplay.Item.Enabled");
		InteractiveChat.useInventory = getConfig().getBoolean("ItemDisplay.Inventory.Enabled");
		InteractiveChat.useEnder = getConfig().getBoolean("ItemDisplay.EnderChest.Enabled");
		
		InteractiveChat.itemCaseSensitive = getConfig().getBoolean("ItemDisplay.Item.CaseSensitive");
		InteractiveChat.invCaseSensitive = getConfig().getBoolean("ItemDisplay.Inventory.CaseSensitive");
		InteractiveChat.enderCaseSensitive = getConfig().getBoolean("ItemDisplay.EnderChest.CaseSensitive");
	
		InteractiveChat.itemPlaceholder = ChatColor.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.Keyword"));
		InteractiveChat.invPlaceholder = ChatColor.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Inventory.Keyword"));
		InteractiveChat.enderPlaceholder = ChatColor.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.EnderChest.Keyword"));
		
		for (String alias : getConfig().getStringList("ItemDisplay.Item.Aliases")) {
			alias = ChatColor.translateAlternateColorCodes('&', alias);
			InteractiveChat.aliasesMapping.put(alias, InteractiveChat.itemPlaceholder);
		}
		for (String alias : getConfig().getStringList("ItemDisplay.Inventory.Aliases")) {
			alias = ChatColor.translateAlternateColorCodes('&', alias);
			InteractiveChat.aliasesMapping.put(alias, InteractiveChat.invPlaceholder);
		}
		for (String alias : getConfig().getStringList("ItemDisplay.EnderChest.Aliases")) {
			alias = ChatColor.translateAlternateColorCodes('&', alias);
			InteractiveChat.aliasesMapping.put(alias, InteractiveChat.enderPlaceholder);
		}
		
		InteractiveChat.itemReplaceText = ChatColor.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.Text"));
		InteractiveChat.invReplaceText = ChatColor.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Inventory.Text"));
		InteractiveChat.enderReplaceText = ChatColor.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.EnderChest.Text"));
		
		InteractiveChat.itemTitle = ChatColor.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.InventoryTitle"));
		InteractiveChat.invTitle = ChatColor.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Inventory.InventoryTitle"));
		InteractiveChat.enderTitle = ChatColor.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.EnderChest.InventoryTitle"));
		
		Bukkit.getConsoleSender().sendMessage(version);
		if (version.contains("legacy")) {
			String str = getConfig().getString("ItemDisplay.Item.Frame.Primary");
			Material material = str.contains(":") ? Material.valueOf(str.substring(0, str.lastIndexOf(":"))) : Material.valueOf(str);
			short data = str.contains(":") ? Short.valueOf(str.substring(str.lastIndexOf(":") + 1)) : 0;
			InteractiveChat.itemFrame1 = new ItemStack(material, 1, data);
		} else {
			InteractiveChat.itemFrame1 = new ItemStack(Material.valueOf(getConfig().getString("ItemDisplay.Item.Frame.Primary")), 1);
		}
		if (version.contains("legacy")) {
			String str = getConfig().getString("ItemDisplay.Item.Frame.Secondary");
			Material material = str.contains(":") ? Material.valueOf(str.substring(0, str.lastIndexOf(":"))) : Material.valueOf(str);
			short data = str.contains(":") ? Short.valueOf(str.substring(str.lastIndexOf(":") + 1)) : 0;
			InteractiveChat.itemFrame2 = new ItemStack(material, 1, data);
		} else {
			InteractiveChat.itemFrame2 = new ItemStack(Material.valueOf(getConfig().getString("ItemDisplay.Item.Frame.Secondary")), 1);
		}
		
		InteractiveChat.usePlayerName = getConfig().getBoolean("Player.UsePlayerNameInteraction");
		InteractiveChat.usePlayerNameHoverEnable = getConfig().getBoolean("Player.Hover.Enable");
		List<String> stringList = getConfig().getStringList("Player.Hover.Text");
		InteractiveChat.usePlayerNameHoverText = ChatColor.translateAlternateColorCodes('&', String.join("\n", stringList));
		InteractiveChat.usePlayerNameClickEnable = getConfig().getBoolean("Player.Click.Enable");
		InteractiveChat.usePlayerNameClickAction = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Player.Click.Action"));
		InteractiveChat.usePlayerNameClickValue = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Player.Click.Value"));
		InteractiveChat.usePlayerNameCaseSensitive = getConfig().getBoolean("Player.CaseSensitive");
		
		InteractiveChat.PlayerNotFoundHoverEnable = getConfig().getBoolean("Settings.PlayerNotFound.Hover.Enable");
		List<String> stringList2 = getConfig().getStringList("Settings.PlayerNotFound.Hover.Text");
		InteractiveChat.PlayerNotFoundHoverText = ChatColor.translateAlternateColorCodes('&', String.join("\n", stringList2));
		InteractiveChat.PlayerNotFoundClickEnable = getConfig().getBoolean("Settings.PlayerNotFound.Click.Enable");
		InteractiveChat.PlayerNotFoundClickAction = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Settings.PlayerNotFound.Click.Action"));
		InteractiveChat.PlayerNotFoundClickValue = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Settings.PlayerNotFound.Click.Value"));
		InteractiveChat.PlayerNotFoundReplaceEnable = getConfig().getBoolean("Settings.PlayerNotFound.Replace.Enable");
		InteractiveChat.PlayerNotFoundReplaceText = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Settings.PlayerNotFound.Replace.ReplaceText"));
		
		InteractiveChat.placeholderList.clear();
		if (InteractiveChat.useItem) {
			InteractiveChat.placeholderList.add(InteractiveChat.itemPlaceholder);
		}
		if (InteractiveChat.useInventory) {
			InteractiveChat.placeholderList.add(InteractiveChat.invPlaceholder);
		}
		if (InteractiveChat.useEnder) {
			InteractiveChat.placeholderList.add(InteractiveChat.enderPlaceholder);
		}
		for (int customNo = 1; getConfig().contains("CustomPlaceholders." + String.valueOf(customNo)) == true; customNo = customNo + 1) {
			String placeholder = getConfig().getString("CustomPlaceholders." + String.valueOf(customNo) + ".Text");
			InteractiveChat.placeholderList.add(placeholder);			
			for (String alias : getConfig().getStringList("CustomPlaceholders." + String.valueOf(customNo) + ".Aliases")) {
				alias = ChatColor.translateAlternateColorCodes('&', alias);
				InteractiveChat.aliasesMapping.put(alias, placeholder);
			}
		}
		
		InteractiveChat.commandList = getConfig().getStringList("Settings.CommandsToParse");
		
		InteractiveChat.maxPlacholders = getConfig().getInt("Settings.MaxPlaceholders");
		InteractiveChat.limitReachMessage = getConfig().getString("Messages.LimitReached");
		
		InteractiveChat.mentionHightlight = getConfig().getString("Chat.MentionHighlight");
		List<String> stringList3 = getConfig().getStringList("Chat.MentionHoverText");
		InteractiveChat.mentionHover = String.join("\n", stringList3);
		InteractiveChat.mentionDuration = getConfig().getLong("Chat.MentionedTitleDuration");
		
		InteractiveChat.UpdaterEnabled = InteractiveChat.plugin.getConfig().getBoolean("Options.Updater");
	}
}