package com.loohp.interactivechat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import com.loohp.interactivechat.Updater.Updater;

import net.md_5.bungee.api.ChatColor;

public class ConfigManager {
	
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
	
	public static void loadConfig() {	
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
		
		InteractiveChat.itemReplaceText = ChatColor.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.Text"));
		InteractiveChat.invReplaceText = ChatColor.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Inventory.Text"));
		InteractiveChat.enderReplaceText = ChatColor.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.EnderChest.Text"));
		
		InteractiveChat.itemTitle = ChatColor.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.InventoryTitle"));
		InteractiveChat.invTitle = ChatColor.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Inventory.InventoryTitle"));
		InteractiveChat.enderTitle = ChatColor.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.EnderChest.InventoryTitle"));
		
		InteractiveChat.itemFrame1 = Material.valueOf(getConfig().getString("ItemDisplay.Item.Frame.Primary"));
		InteractiveChat.itemFrame2 = Material.valueOf(getConfig().getString("ItemDisplay.Item.Frame.Secondary"));
		
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
		InteractiveChat.placeholderList.add(InteractiveChat.itemPlaceholder);
		InteractiveChat.placeholderList.add(InteractiveChat.invPlaceholder);
		InteractiveChat.placeholderList.add(InteractiveChat.enderPlaceholder);
		for (int customNo = 1; getConfig().contains("CustomPlaceholders." + String.valueOf(customNo)) == true; customNo = customNo + 1) {
			InteractiveChat.placeholderList.add(getConfig().getString("CustomPlaceholders." + String.valueOf(customNo) + ".Text"));
		}
		
		InteractiveChat.maxPlacholders = getConfig().getInt("Settings.MaxPlaceholders");
		InteractiveChat.limitReachMessage = getConfig().getString("Messages.LimitReached");
		
		InteractiveChat.mentionHightlight = getConfig().getString("Chat.MentionHighlight");
		List<String> stringList3 = getConfig().getStringList("Chat.MentionHoverText");
		InteractiveChat.mentionHover = String.join("\n", stringList3);
		InteractiveChat.mentionDuration = getConfig().getLong("Chat.MentionedTitleDuration");
		
		if (InteractiveChat.UpdaterTaskID >= 0) {
			Bukkit.getScheduler().cancelTask(InteractiveChat.UpdaterTaskID);
		}
		InteractiveChat.UpdaterEnabled = InteractiveChat.plugin.getConfig().getBoolean("Options.Updater");
		if (InteractiveChat.UpdaterEnabled == true) {
			Updater.updaterInterval();
		}
	}
}