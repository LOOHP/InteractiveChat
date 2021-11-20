package com.loohp.interactivechat.config;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.FileConfiguration;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.objectholders.BuiltInPlaceholder;
import com.loohp.interactivechat.objectholders.CompatibilityListener;
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ClickEventAction;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderClickEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderHoverEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderReplaceText;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ParsePlayer;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.WebData;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.LanguageUtils;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.XMaterialUtils;

import net.kyori.adventure.text.event.ClickEvent;
import net.md_5.bungee.api.ChatColor;

public class ConfigManager {
	
	private static final MCVersion VERSION = InteractiveChat.version;
	private static final String MAIN_CONFIG = "config";
	private static final String STORAGE_CONFIG = "storage";
	
	public static void setup() {
		Config.loadConfig(MAIN_CONFIG, new File(InteractiveChat.plugin.getDataFolder(), "config.yml"), InteractiveChat.class.getClassLoader().getResourceAsStream("config_default.yml"), InteractiveChat.class.getClassLoader().getResourceAsStream("config.yml"), true);
		Config.loadConfig(STORAGE_CONFIG, new File(InteractiveChat.plugin.getDataFolder(), "storage.yml"), InteractiveChat.class.getClassLoader().getResourceAsStream("storage.yml"), InteractiveChat.class.getClassLoader().getResourceAsStream("storage.yml"), true);
		loadConfig();
	}
	
	public static FileConfiguration getConfig() {
		return Config.getConfig(MAIN_CONFIG).getConfiguration();
	}
	
	public static FileConfiguration getStorageConfig() {
		return Config.getConfig(STORAGE_CONFIG).getConfiguration();
	}
	
	public static void saveConfig() {
		Config.saveConfigs();
	}
	
	public static void reloadConfig() {
		Config.reloadConfigs();
		loadConfig();
	}
	
	@SuppressWarnings("deprecation")
	public static void loadConfig() {		
		InteractiveChat.aliasesMapping.clear();
		
		InteractiveChat.itemTagMaxLength = getConfig().getInt("Settings.ItemTagMaxLength");
		InteractiveChat.packetStringMaxLength = getConfig().getInt("Settings.PacketStringMaxLength");
		
		InteractiveChat.parsePAPIOnMainThread = getConfig().getBoolean("Settings.ParsePAPIOnMainThread");
		InteractiveChat.useAccurateSenderFinder = getConfig().getBoolean("Settings.UseAccurateSenderParser");
		
		InteractiveChat.restoreIsolatedChatListeners();
		InteractiveChat.compatibilityListeners.clear();
		List<String> compatibility = getConfig().getStringList("Settings.ChatListeningPlugins");
		for (String str : compatibility) {
			try {
				String[] args = str.split(",");
				if (args.length == 3) {
					CompatibilityListener listener = new CompatibilityListener(args[0].replace("Plugin:", "").trim(), args[1].replace("Class:", "").trim(), EventPriority.valueOf(args[2].replace("EventPriority:", "").trim().toUpperCase()));
					InteractiveChat.compatibilityListeners.add(listener);
				}
			} catch (Exception e) {}
		}
		
		String colorCodeString = getConfig().getString("Chat.TranslateAltColorCode");
		InteractiveChat.chatAltColorCode = colorCodeString.length() == 1 ? Optional.of(colorCodeString.charAt(0)) : Optional.empty();
		
		InteractiveChat.useCustomPlaceholderPermissions = getConfig().getBoolean("Settings.UseCustomPlaceholderPermissions");
		
		InteractiveChat.filterUselessColorCodes = getConfig().getBoolean("Settings.FilterUselessColorCodes", true);
		
		InteractiveChat.allowMention = getConfig().getBoolean("Chat.AllowMention");
		
		InteractiveChat.universalCooldown = getConfig().getLong("Settings.UniversalCooldown") * 1000;
		
		InteractiveChat.noPermissionMessage = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.NoPermission"));
		InteractiveChat.invExpiredMessage = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.InvExpired"));
		InteractiveChat.reloadPluginMessage = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.ReloadPlugin"));
		InteractiveChat.noConsoleMessage = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.PlayerOnlyCommand"));
		InteractiveChat.invalidPlayerMessage = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.InvalidPlayer"));
		InteractiveChat.listPlaceholderHeader = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.ListPlaceholdersHeader"));
		InteractiveChat.listPlaceholderBody = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.ListPlaceholdersBody"));
		InteractiveChat.notEnoughArgs = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.NoEnoughArgs"));
		InteractiveChat.invalidArgs = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.InvalidArgs"));
		InteractiveChat.setInvDisplayLayout = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.SetInventoryDisplayLayout"));
		InteractiveChat.placeholderCooldownMessage = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.PlaceholderCooldown"));
		InteractiveChat.universalCooldownMessage = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.UniversalCooldown"));
		
		InteractiveChat.useItem = getConfig().getBoolean("ItemDisplay.Item.Enabled");
		InteractiveChat.useInventory = getConfig().getBoolean("ItemDisplay.Inventory.Enabled");
		InteractiveChat.useEnder = getConfig().getBoolean("ItemDisplay.EnderChest.Enabled");
		
		InteractiveChat.itemMapPreview = getConfig().getBoolean("ItemDisplay.Item.PreviewMaps");
		
		InteractiveChat.itemCaseSensitive = getConfig().getBoolean("ItemDisplay.Item.CaseSensitive");
		InteractiveChat.invCaseSensitive = getConfig().getBoolean("ItemDisplay.Inventory.CaseSensitive");
		InteractiveChat.enderCaseSensitive = getConfig().getBoolean("ItemDisplay.EnderChest.CaseSensitive");
	
		InteractiveChat.itemPlaceholder = getConfig().getString("ItemDisplay.Item.Keyword");
		InteractiveChat.invPlaceholder = getConfig().getString("ItemDisplay.Inventory.Keyword");
		InteractiveChat.enderPlaceholder = getConfig().getString("ItemDisplay.EnderChest.Keyword");
		
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
		InteractiveChat.itemSingularReplaceText = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.SingularText"));
		InteractiveChat.invReplaceText = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Inventory.Text"));
		InteractiveChat.enderReplaceText = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.EnderChest.Text"));
		
		InteractiveChat.itemAirAllow = getConfig().getBoolean("ItemDisplay.Item.EmptyItemSettings.AllowAir");
		InteractiveChat.itemAirErrorMessage = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.EmptyItemSettings.DisallowMessage"));
		
		InteractiveChat.itemTitle = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.InventoryTitle"));
		InteractiveChat.invTitle = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Inventory.InventoryTitle"));
		InteractiveChat.enderTitle = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.EnderChest.InventoryTitle"));
		
		InteractiveChat.itemGUI = getConfig().getBoolean("ItemDisplay.Item.GUIEnabled");
		InteractiveChat.translateHoverableItems = getConfig().getBoolean("ItemDisplay.Item.HoverableItemsTranslation.Enabled");
		InteractiveChat.hoverableItemTitle = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.HoverableItemsTranslation.InventoryTitle"));
		
		InteractiveChat.containerViewTitle = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Settings.ContainerViewTitle"));
		
		try {
			try {
				if (VERSION.isLegacy()) {
					String str = getConfig().getString("ItemDisplay.Item.Frame.Primary");
					Material material = str.contains(":") ? Material.valueOf(str.substring(0, str.lastIndexOf(":"))) : Material.valueOf(str);
					short data = str.contains(":") ? Short.valueOf(str.substring(str.lastIndexOf(":") + 1)) : 0;
					InteractiveChat.itemFrame1 = new ItemStack(material, 1, data);
				} else {
					InteractiveChat.itemFrame1 = new ItemStack(Material.valueOf(getConfig().getString("ItemDisplay.Item.Frame.Primary")), 1);
				}
			} catch (Exception e) {
				InteractiveChat.itemFrame1 = XMaterialUtils.matchXMaterial(getConfig().getString("ItemDisplay.Item.Frame.Primary")).parseItem();
				InteractiveChat.itemFrame1.setAmount(1);
			}
		} catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You have an invalid material name in the config! Please take a look at the Q&A section on the resource page! (ItemDisplay.Item.Frame.Primary)");
			e.printStackTrace();
		}
		
		try {
			try {
				if (VERSION.isLegacy()) {
					String str = getConfig().getString("ItemDisplay.Item.Frame.Secondary");
					Material material = str.contains(":") ? Material.valueOf(str.substring(0, str.lastIndexOf(":"))) : Material.valueOf(str);
					short data = str.contains(":") ? Short.valueOf(str.substring(str.lastIndexOf(":") + 1)) : 0;
					InteractiveChat.itemFrame2 = new ItemStack(material, 1, data);
				} else {
					InteractiveChat.itemFrame2 = new ItemStack(Material.valueOf(getConfig().getString("ItemDisplay.Item.Frame.Secondary")), 1);
				}
			} catch (Exception e) {
				InteractiveChat.itemFrame2 = XMaterialUtils.matchXMaterial(getConfig().getString("ItemDisplay.Item.Frame.Secondary")).parseItem();
				InteractiveChat.itemFrame2.setAmount(1);
			}
		} catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You have an invalid material name in the config! Please take a look at the Q&A section on the resource page! (ItemDisplay.Item.Frame.Secondary)");
			e.printStackTrace();
		}
		
		try {
			try {
				if (VERSION.isLegacy()) {
					String str = getConfig().getString("ItemDisplay.Inventory.Frame.Primary");
					Material material = str.contains(":") ? Material.valueOf(str.substring(0, str.lastIndexOf(":"))) : Material.valueOf(str);
					short data = str.contains(":") ? Short.valueOf(str.substring(str.lastIndexOf(":") + 1)) : 0;
					InteractiveChat.invFrame1 = new ItemStack(material, 1, data);
				} else {
					InteractiveChat.invFrame1 = new ItemStack(Material.valueOf(getConfig().getString("ItemDisplay.Inventory.Frame.Primary")), 1);
				}
			} catch (Exception e) {
				InteractiveChat.invFrame1 = XMaterialUtils.matchXMaterial(getConfig().getString("ItemDisplay.Inventory.Frame.Primary")).parseItem();
				InteractiveChat.invFrame1.setAmount(1);
			}
		} catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You have an invalid material name in the config! Please take a look at the Q&A section on the resource page! (ItemDisplay.Inventory.Frame.Primary)");
			e.printStackTrace();
		}
		
		try {
			try {
				if (VERSION.isLegacy()) {
					String str = getConfig().getString("ItemDisplay.Inventory.Frame.Secondary");
					Material material = str.contains(":") ? Material.valueOf(str.substring(0, str.lastIndexOf(":"))) : Material.valueOf(str);
					short data = str.contains(":") ? Short.valueOf(str.substring(str.lastIndexOf(":") + 1)) : 0;
					InteractiveChat.invFrame2 = new ItemStack(material, 1, data);
				} else {
					InteractiveChat.invFrame2 = new ItemStack(Material.valueOf(getConfig().getString("ItemDisplay.Inventory.Frame.Secondary")), 1);
				}
			} catch (Exception e) {
				InteractiveChat.invFrame2 = XMaterialUtils.matchXMaterial(getConfig().getString("ItemDisplay.Inventory.Frame.Secondary")).parseItem();
				InteractiveChat.invFrame2.setAmount(1);
			}
		} catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You have an invalid material name in the config! Please take a look at the Q&A section on the resource page! (ItemDisplay.Inventory.Frame.Secondary)");
			e.printStackTrace();
		}
		
		InteractiveChat.invSkullName = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Inventory.SkullDisplayName"));
		InteractiveChat.invDisplayLayout = getConfig().getInt("ItemDisplay.Inventory.Layout");
		
		InteractiveChat.itemDisplayTimeout = getConfig().getLong("ItemDisplay.Settings.Timeout") * 60 * 1000;
		
		if (getConfig().contains("Secret.t")) {
			InteractiveChat.t = getConfig().getBoolean("Secret.t");
		}
		
		InteractiveChat.usePlayerName = getConfig().getBoolean("Player.UsePlayerNameInteraction");
		InteractiveChat.usePlayerNameHoverEnable = getConfig().getBoolean("Player.Hover.Enable");
		List<String> stringList = getConfig().getStringList("Player.Hover.Text");
		InteractiveChat.usePlayerNameHoverText = ChatColorUtils.translateAlternateColorCodes('&', String.join("\n", stringList));
		InteractiveChat.usePlayerNameClickEnable = getConfig().getBoolean("Player.Click.Enable");
		InteractiveChat.usePlayerNameClickAction = getConfig().getString("Player.Click.Action");
		InteractiveChat.usePlayerNameClickValue = getConfig().getString("Player.Click.Value");
		InteractiveChat.usePlayerNameCaseSensitive = getConfig().getBoolean("Player.CaseSensitive");
		
		InteractiveChat.useTooltipOnTab = getConfig().getBoolean("TabCompletion.PlayerNameToolTip.Enabled");
		InteractiveChat.tabTooltip = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("TabCompletion.PlayerNameToolTip.ToolTip"));
		
		InteractiveChat.playerNotFoundHoverEnable = getConfig().getBoolean("Settings.PlayerNotFound.Hover.Enable");
		List<String> stringList2 = getConfig().getStringList("Settings.PlayerNotFound.Hover.Text");
		InteractiveChat.playerNotFoundHoverText = ChatColorUtils.translateAlternateColorCodes('&', String.join("\n", stringList2));
		InteractiveChat.playerNotFoundClickEnable = getConfig().getBoolean("Settings.PlayerNotFound.Click.Enable");
		InteractiveChat.playerNotFoundClickAction = getConfig().getString("Settings.PlayerNotFound.Click.Action");
		InteractiveChat.playerNotFoundClickValue = getConfig().getString("Settings.PlayerNotFound.Click.Value");
		InteractiveChat.playerNotFoundReplaceEnable = getConfig().getBoolean("Settings.PlayerNotFound.Replace.Enable");
		InteractiveChat.playerNotFoundReplaceText = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Settings.PlayerNotFound.Replace.ReplaceText"));
		
		InteractiveChat.placeholderList.clear();
		if (InteractiveChat.useItem) {
			String description = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.Description"));
			ICPlaceholder itemPlaceholder = new BuiltInPlaceholder(InteractiveChat.itemPlaceholder, InteractiveChat.itemCaseSensitive, description, "interactivechat.module.item", getConfig().getLong("ItemDisplay.Item.Cooldown") * 1000);
			InteractiveChat.placeholderList.put(itemPlaceholder.getInternalId(), itemPlaceholder);
		}
		if (InteractiveChat.useInventory) {
			String description = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Inventory.Description"));
			ICPlaceholder invPlaceholder = new BuiltInPlaceholder(InteractiveChat.invPlaceholder, InteractiveChat.invCaseSensitive, description, "interactivechat.module.inventory", getConfig().getLong("ItemDisplay.Inventory.Cooldown") * 1000);
			InteractiveChat.placeholderList.put(invPlaceholder.getInternalId(), invPlaceholder);
		}
		if (InteractiveChat.useEnder) {
			String description = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.EnderChest.Description"));
			ICPlaceholder enderPlaceholder = new BuiltInPlaceholder(InteractiveChat.enderPlaceholder, InteractiveChat.enderCaseSensitive, description, "interactivechat.module.enderchest", getConfig().getLong("ItemDisplay.EnderChest.Cooldown") * 1000);
			InteractiveChat.placeholderList.put(enderPlaceholder.getInternalId(), enderPlaceholder);
		}
		for (int customNo = 1; ConfigManager.getConfig().contains("CustomPlaceholders." + String.valueOf(customNo)); customNo++) {
			ConfigurationSection s = getConfig().getConfigurationSection("CustomPlaceholders." + String.valueOf(customNo));
			ParsePlayer parseplayer = ParsePlayer.fromString(s.getString("ParsePlayer"));
			boolean casesensitive = s.getBoolean("CaseSensitive");			
			String placeholder = s.getString("Text");
			boolean parseKeyword = s.getBoolean("ParseKeyword");
			long cooldown = s.getLong("Cooldown") * 1000;
			boolean hoverEnabled = s.getBoolean("Hover.Enable");
			String hoverText = ChatColorUtils.translateAlternateColorCodes('&', String.join("\n", s.getStringList("Hover.Text")));
			boolean clickEnabled = s.getBoolean("Click.Enable");
			String clickAction = s.getString("Click.Action").toUpperCase();
			String clickValue = s.getString("Click.Value");
			boolean replaceEnabled = s.getBoolean("Replace.Enable");
			String replaceText = ChatColorUtils.translateAlternateColorCodes('&', s.getString("Replace.ReplaceText"));
			List<String> aliases = s.getStringList("Aliases");
			String description = ChatColorUtils.translateAlternateColorCodes('&', s.getString("Description", "&7&oDescription missing"));

			ICPlaceholder customPlaceholder = new CustomPlaceholder(customNo, parseplayer, placeholder, aliases, parseKeyword, casesensitive, cooldown, new CustomPlaceholderHoverEvent(hoverEnabled, hoverText), new CustomPlaceholderClickEvent(clickEnabled, clickEnabled ? ClickEventAction.valueOf(clickAction) : null, clickValue), new CustomPlaceholderReplaceText(replaceEnabled, replaceText), description);
			InteractiveChat.placeholderList.put(customPlaceholder.getInternalId(), customPlaceholder);
			
			for (String alias : aliases) {
				alias = ChatColorUtils.translateAlternateColorCodes('&', alias);
				InteractiveChat.aliasesMapping.put(alias, placeholder);
			}
		}
		
		if (InteractiveChat.bungeecordMode) {
			InteractiveChat.queueRemoteUpdate = true;
		}
		
		InteractiveChat.commandList = getConfig().getStringList("Settings.CommandsToParse");
		
		InteractiveChat.maxPlaceholders = getConfig().getInt("Settings.MaxPlaceholders");
		InteractiveChat.limitReachMessage = getConfig().getString("Messages.LimitReached");
		
		InteractiveChat.mentionPrefix = getConfig().getString("Chat.MentionPrefix");
		InteractiveChat.mentionHightlight = getConfig().getString("Chat.MentionHighlight");
		List<String> stringList3 = getConfig().getStringList("Chat.MentionHoverText");
		InteractiveChat.mentionHover = String.join("\n", stringList3);
		InteractiveChat.mentionDuration = getConfig().getLong("Chat.MentionedTitleDuration");
		
		InteractiveChat.mentionEnable = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.EnableMentions"));
		InteractiveChat.mentionDisable = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.DisableMentions"));
		
		InteractiveChat.updaterEnabled = getConfig().getBoolean("Options.Updater");
		InteractiveChat.cancelledMessage = getConfig().getBoolean("Options.ShowCancelledNotice");
		
		InteractiveChat.clickableCommands = getConfig().getBoolean("Commands.Enabled");
		InteractiveChat.clickableCommandsFormat = getConfig().getString("Commands.Format");
		InteractiveChat.clickableCommandsAction = ClickEvent.Action.valueOf(getConfig().getString("Commands.Action"));
		InteractiveChat.clickableCommandsDisplay = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Commands.Text"));
		InteractiveChat.clickableCommandsHoverText = ChatColorUtils.translateAlternateColorCodes('&', String.join("\n", getConfig().getStringList("Commands.HoverMessage")));
		
		InteractiveChat.sendOriginalIfTooLong = getConfig().getBoolean("Settings.SendOriginalMessageIfExceedLengthLimit");
		
		InteractiveChat.messageToIgnore = getConfig().getStringList("Settings.MessagesToIgnore").stream().collect(Collectors.toSet());
		
		try {			
			try {
				if (VERSION.isLegacy()) {
					String str = getConfig().getString("ItemDisplay.Item.Frame.Secondary");
					Material material = str.contains(":") ? Material.valueOf(str.substring(0, str.lastIndexOf(":"))) : Material.valueOf(str);
					short data = str.contains(":") ? Short.valueOf(str.substring(str.lastIndexOf(":") + 1)) : 0;
					ItemStack unknown = new ItemStack(material, 1, data);
					ItemMeta meta = unknown.getItemMeta();
					meta.setDisplayName(ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Settings.BungeecordUnknownItem.DisplayName")));
					meta.setLore(getConfig().getStringList("Settings.BungeecordUnknownItem.Lore").stream().map(each -> ChatColorUtils.translateAlternateColorCodes('&', each)).collect(Collectors.toList()));
					unknown.setItemMeta(meta);
					InteractiveChat.unknownReplaceItem = unknown;
				} else {
					ItemStack unknown = new ItemStack(Material.valueOf(getConfig().getString("Settings.BungeecordUnknownItem.ReplaceItem").toUpperCase()));
					ItemMeta meta = unknown.getItemMeta();
					meta.setDisplayName(ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Settings.BungeecordUnknownItem.DisplayName")));
					meta.setLore(getConfig().getStringList("Settings.BungeecordUnknownItem.Lore").stream().map(each -> ChatColorUtils.translateAlternateColorCodes('&', each)).collect(Collectors.toList()));
					unknown.setItemMeta(meta);
					InteractiveChat.unknownReplaceItem = unknown;
				}
			} catch (Exception e) {
				ItemStack unknown = XMaterialUtils.matchXMaterial(getConfig().getString("Settings.BungeecordUnknownItem.ReplaceItem")).parseItem();
				unknown.setAmount(1);
				ItemMeta meta = unknown.getItemMeta();
				meta.setDisplayName(ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Settings.BungeecordUnknownItem.DisplayName")));
				meta.setLore(getConfig().getStringList("Settings.BungeecordUnknownItem.Lore").stream().map(each -> ChatColorUtils.translateAlternateColorCodes('&', each)).collect(Collectors.toList()));
				unknown.setItemMeta(meta);
				InteractiveChat.unknownReplaceItem = unknown;
			}
		} catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You have an invalid material name in the config! Please take a look at the Q&A section on the resource page! (Settings.BungeecordUnknownItem.ReplaceItem)");
			e.printStackTrace();
		}
		
		InteractiveChat.useBukkitDisplayName = getConfig().getBoolean("Chat.UseBukkitDisplayName");
		InteractiveChat.useEssentialsNicknames = getConfig().getBoolean("Chat.UseEssentialsNicknames");
		
		InteractiveChat.rgbTags = getConfig().contains("Settings.FormattingTags.AllowRGBTags") ? getConfig().getBoolean("Settings.FormattingTags.AllowRGBTags") : getConfig().getBoolean("Settings.ForamttingTags.AllowRGBTags");
		InteractiveChat.fontTags = getConfig().contains("Settings.FormattingTags.AllowFontTags") ? getConfig().getBoolean("Settings.FormattingTags.AllowFontTags") : getConfig().getBoolean("Settings.ForamttingTags.AllowFontTags");
		
		InteractiveChat.language = getConfig().getString("Settings.Language");
		
		Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
			LanguageUtils.loadTranslations(InteractiveChat.language);
			if (WebData.getInstance() == null) {
				WebData.newInstance();
			} else {
				WebData.getInstance().reload();
			}
		});
	}
}
