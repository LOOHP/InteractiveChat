package com.loohp.interactivechat;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.CustomPlaceholderClickEvent;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.CustomPlaceholderHoverEvent;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.CustomPlaceholderReplaceText;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.ParsePlayer;
import com.loohp.interactivechat.ObjectHolders.ICPlaceholder;
import com.loohp.interactivechat.Utils.ChatColorUtils;
import com.loohp.interactivechat.Utils.MCVersion;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;

public class ConfigManager {
	
	private static MCVersion version = InteractiveChat.version;
	
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
		
		String colorCodeString = getConfig().getString("Chat.TranslateAltColorCode");
		InteractiveChat.chatAltColorCode = colorCodeString.length() == 1 ? Optional.of(colorCodeString.charAt(0)) : Optional.empty();
		
		InteractiveChat.useCustomPlaceholderPermissions = getConfig().getBoolean("Settings.UseCustomPlaceholderPermissions");
		
		InteractiveChat.FilterUselessColorCodes = getConfig().getBoolean("Settings.FilterUselessColorCodes");
		
		InteractiveChat.AllowMention = getConfig().getBoolean("Chat.AllowMention");
		
		InteractiveChat.universalCooldown = getConfig().getLong("Settings.UniversalCooldown") * 1000;
		
		InteractiveChat.NoPermission = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.NoPermission"));
		InteractiveChat.InvExpired = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.InvExpired"));
		InteractiveChat.ReloadPlugin = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.ReloadPlugin"));
		InteractiveChat.Console = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.PlayerOnlyCommand"));
		InteractiveChat.InvalidPlayer = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.InvalidPlayer"));
		
		InteractiveChat.useItem = getConfig().getBoolean("ItemDisplay.Item.Enabled");
		InteractiveChat.useInventory = getConfig().getBoolean("ItemDisplay.Inventory.Enabled");
		InteractiveChat.useEnder = getConfig().getBoolean("ItemDisplay.EnderChest.Enabled");
		
		InteractiveChat.itemCaseSensitive = getConfig().getBoolean("ItemDisplay.Item.CaseSensitive");
		InteractiveChat.invCaseSensitive = getConfig().getBoolean("ItemDisplay.Inventory.CaseSensitive");
		InteractiveChat.enderCaseSensitive = getConfig().getBoolean("ItemDisplay.EnderChest.CaseSensitive");
	
		InteractiveChat.itemPlaceholder = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.Keyword"));
		InteractiveChat.invPlaceholder = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Inventory.Keyword"));
		InteractiveChat.enderPlaceholder = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.EnderChest.Keyword"));
		
		for (String alias : getConfig().getStringList("ItemDisplay.Item.Aliases")) {
			alias = ChatColorUtils.translateAlternateColorCodes('&', alias);
			InteractiveChat.aliasesMapping.put(alias, InteractiveChat.itemPlaceholder);
		}
		for (String alias : getConfig().getStringList("ItemDisplay.Inventory.Aliases")) {
			alias = ChatColorUtils.translateAlternateColorCodes('&', alias);
			InteractiveChat.aliasesMapping.put(alias, InteractiveChat.invPlaceholder);
		}
		for (String alias : getConfig().getStringList("ItemDisplay.EnderChest.Aliases")) {
			alias = ChatColorUtils.translateAlternateColorCodes('&', alias);
			InteractiveChat.aliasesMapping.put(alias, InteractiveChat.enderPlaceholder);
		}
		
		InteractiveChat.itemReplaceText = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.Text"));
		InteractiveChat.invReplaceText = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Inventory.Text"));
		InteractiveChat.enderReplaceText = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.EnderChest.Text"));
		
		InteractiveChat.itemTitle = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.InventoryTitle"));
		InteractiveChat.invTitle = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Inventory.InventoryTitle"));
		InteractiveChat.enderTitle = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.EnderChest.InventoryTitle"));
		
		try {
			if (version.isLegacy()) {
				String str = getConfig().getString("ItemDisplay.Item.Frame.Primary");
				Material material = str.contains(":") ? Material.valueOf(str.substring(0, str.lastIndexOf(":"))) : Material.valueOf(str);
				short data = str.contains(":") ? Short.valueOf(str.substring(str.lastIndexOf(":") + 1)) : 0;
				InteractiveChat.itemFrame1 = new ItemStack(material, 1, data);
			} else {
				InteractiveChat.itemFrame1 = new ItemStack(Material.valueOf(getConfig().getString("ItemDisplay.Item.Frame.Primary")), 1);
			}
			if (version.isLegacy()) {
				String str = getConfig().getString("ItemDisplay.Item.Frame.Secondary");
				Material material = str.contains(":") ? Material.valueOf(str.substring(0, str.lastIndexOf(":"))) : Material.valueOf(str);
				short data = str.contains(":") ? Short.valueOf(str.substring(str.lastIndexOf(":") + 1)) : 0;
				InteractiveChat.itemFrame2 = new ItemStack(material, 1, data);
			} else {
				InteractiveChat.itemFrame2 = new ItemStack(Material.valueOf(getConfig().getString("ItemDisplay.Item.Frame.Secondary")), 1);
			}
		} catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You have an invalid material name in the config! Please take a look at the Q&A section on the resource page! (ItemDisplay.Item.Frame)");
			e.printStackTrace();
		}
		
		if (InteractiveChat.plugin.getConfig().contains("Secret.t")) {
			InteractiveChat.t = InteractiveChat.plugin.getConfig().getBoolean("Secret.t");
		}
		
		InteractiveChat.usePlayerName = getConfig().getBoolean("Player.UsePlayerNameInteraction");
		InteractiveChat.usePlayerNameHoverEnable = getConfig().getBoolean("Player.Hover.Enable");
		List<String> stringList = getConfig().getStringList("Player.Hover.Text");
		InteractiveChat.usePlayerNameHoverText = ChatColorUtils.translateAlternateColorCodes('&', String.join("\n", stringList));
		InteractiveChat.usePlayerNameClickEnable = getConfig().getBoolean("Player.Click.Enable");
		InteractiveChat.usePlayerNameClickAction = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Player.Click.Action"));
		InteractiveChat.usePlayerNameClickValue = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Player.Click.Value"));
		InteractiveChat.usePlayerNameCaseSensitive = getConfig().getBoolean("Player.CaseSensitive");
		InteractiveChat.usePlayerNameOnTranslatables = getConfig().getBoolean("Player.UseOnTranslatableComponents");
		
		InteractiveChat.PlayerNotFoundHoverEnable = getConfig().getBoolean("Settings.PlayerNotFound.Hover.Enable");
		List<String> stringList2 = getConfig().getStringList("Settings.PlayerNotFound.Hover.Text");
		InteractiveChat.PlayerNotFoundHoverText = ChatColorUtils.translateAlternateColorCodes('&', String.join("\n", stringList2));
		InteractiveChat.PlayerNotFoundClickEnable = getConfig().getBoolean("Settings.PlayerNotFound.Click.Enable");
		InteractiveChat.PlayerNotFoundClickAction = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Settings.PlayerNotFound.Click.Action"));
		InteractiveChat.PlayerNotFoundClickValue = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Settings.PlayerNotFound.Click.Value"));
		InteractiveChat.PlayerNotFoundReplaceEnable = getConfig().getBoolean("Settings.PlayerNotFound.Replace.Enable");
		InteractiveChat.PlayerNotFoundReplaceText = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Settings.PlayerNotFound.Replace.ReplaceText"));
		
		InteractiveChat.placeholderList.clear();
		if (InteractiveChat.useItem) {
			InteractiveChat.placeholderList.add(new ICPlaceholder(InteractiveChat.itemPlaceholder, InteractiveChat.itemCaseSensitive));
		}
		if (InteractiveChat.useInventory) {
			InteractiveChat.placeholderList.add(new ICPlaceholder(InteractiveChat.invPlaceholder, InteractiveChat.invCaseSensitive));
		}
		if (InteractiveChat.useEnder) {
			InteractiveChat.placeholderList.add(new ICPlaceholder(InteractiveChat.enderPlaceholder, InteractiveChat.enderCaseSensitive));
		}
		for (int customNo = 1; ConfigManager.getConfig().contains("CustomPlaceholders." + String.valueOf(customNo)); customNo++) {
			ConfigurationSection s = ConfigManager.getConfig().getConfigurationSection("CustomPlaceholders." + String.valueOf(customNo));
			ParsePlayer parseplayer = ParsePlayer.fromString(s.getString("ParsePlayer"));
			boolean casesensitive = s.getBoolean("CaseSensitive");			
			String placeholder = s.getString("Text");
			boolean parseKeyword = s.getBoolean("ParseKeyword");
			long cooldown = s.getLong("Cooldown") * 1000;
			boolean hoverEnabled = s.getBoolean("Hover.Enable");
			String hoverText = String.join("\n", s.getStringList("Hover.Text"));
			boolean clickEnabled = s.getBoolean("Click.Enable");
			String clickAction = s.getString("Click.Action").toUpperCase();
			String clickValue = s.getString("Click.Value");
			boolean replaceEnabled = s.getBoolean("Replace.Enable");
			String replaceText = s.getString("Replace.ReplaceText");
			List<String> aliases = s.getStringList("Aliases");

			InteractiveChat.placeholderList.add(new CustomPlaceholder(customNo, parseplayer, placeholder, aliases, parseKeyword, casesensitive, cooldown, new CustomPlaceholderHoverEvent(hoverEnabled, hoverText), new CustomPlaceholderClickEvent(clickEnabled, clickEnabled ? ClickEvent.Action.valueOf(clickAction) : null, clickValue), new CustomPlaceholderReplaceText(replaceEnabled, replaceText)));
			
			for (String alias : aliases) {
				alias = ChatColorUtils.translateAlternateColorCodes('&', alias);
				InteractiveChat.aliasesMapping.put(alias, placeholder);
			}
		}
		
		if (InteractiveChat.bungeecordMode) {
			InteractiveChat.queueRemoteUpdate = true;
		}
		
		InteractiveChat.commandList = getConfig().getStringList("Settings.CommandsToParse");
		
		InteractiveChat.maxPlacholders = getConfig().getInt("Settings.MaxPlaceholders");
		InteractiveChat.limitReachMessage = getConfig().getString("Messages.LimitReached");
		
		InteractiveChat.mentionHightlight = getConfig().getString("Chat.MentionHighlight");
		List<String> stringList3 = getConfig().getStringList("Chat.MentionHoverText");
		InteractiveChat.mentionHover = String.join("\n", stringList3);
		InteractiveChat.mentionDuration = getConfig().getLong("Chat.MentionedTitleDuration");
		
		InteractiveChat.mentionEnable = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.EnableMentions"));
		InteractiveChat.mentionDisable = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.DisableMentions"));
		
		InteractiveChat.UpdaterEnabled = getConfig().getBoolean("Options.Updater");
		InteractiveChat.cancelledMessage = getConfig().getBoolean("Options.ShowCancelledNotice");
		
		InteractiveChat.clickableCommands = getConfig().getBoolean("Commands.Enabled");
		String[] commandsFormat = getConfig().getString("Commands.Format").split("\\{Command\\}");
		InteractiveChat.clickableCommandsPrefix = ChatColorUtils.translateAlternateColorCodes('&', commandsFormat[0]);
		InteractiveChat.clickableCommandsSuffix = ChatColorUtils.translateAlternateColorCodes('&', commandsFormat[commandsFormat.length - 1]);
		InteractiveChat.clickableCommandsAction = ClickEvent.Action.valueOf(getConfig().getString("Commands.Action"));
		InteractiveChat.clickableCommandsFormat = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Commands.Text"));
		InteractiveChat.clickableCommandsHoverText = ChatColorUtils.translateAlternateColorCodes('&', String.join("\n", getConfig().getStringList("Commands.HoverMessage")));
		InteractiveChat.clickableCommandsEnforceColors = getConfig().getBoolean("Commands.EnforceReplaceTextColor");
		
		InteractiveChat.block30000 = getConfig().getBoolean("Settings.BlockMessagesLongerThan30000RegardlessOfVersion");
		
		InteractiveChat.messageToIgnore = getConfig().getStringList("Settings.MessagesToIgnore").stream().collect(Collectors.toSet());
	}
}