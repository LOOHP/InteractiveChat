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

package com.loohp.interactivechat.config;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.datafixer.ConfigDataFixer;
import com.loohp.interactivechat.objectholders.BuiltInPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ClickEventAction;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderClickEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderHoverEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderReplaceText;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ParsePlayer;
import com.loohp.interactivechat.objectholders.ICMaterial;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.WebData;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.LanguageUtils;
import com.loohp.interactivechat.utils.MCVersion;
import net.kyori.adventure.text.event.ClickEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigManager {

    private static final MCVersion VERSION = InteractiveChat.version;
    private static final String MAIN_CONFIG = "config";
    private static final String STORAGE_CONFIG = "storage";

    public static void setup() throws IOException {
        Config.loadConfig(MAIN_CONFIG, new File(InteractiveChat.plugin.getDataFolder(), "config.yml"), InteractiveChat.class.getClassLoader().getResourceAsStream("config.yml"), InteractiveChat.class.getClassLoader().getResourceAsStream("config.yml"), true, config -> ConfigDataFixer.update(config), path -> !path.startsWith("CustomPlaceholders."));
        Config.loadConfig(STORAGE_CONFIG, new File(InteractiveChat.plugin.getDataFolder(), "storage.yml"), InteractiveChat.class.getClassLoader().getResourceAsStream("storage.yml"), InteractiveChat.class.getClassLoader().getResourceAsStream("storage.yml"), true);
        loadConfig();
    }

    public static YamlFile getConfig() {
        return Config.getConfig(MAIN_CONFIG).getConfiguration();
    }

    public static YamlFile getStorageConfig() {
        return Config.getConfig(STORAGE_CONFIG).getConfiguration();
    }

    public static void saveConfig() {
        Config.saveConfigs();
    }

    public static void reloadConfig() {
        try {
            Config.reloadConfigs();
            loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public static void loadConfig() {
        InteractiveChat.extraProxiedPacketProcessingDelay = getConfig().getInt("Options.ExtraProxiedPacketProcessingDelay");
        InteractiveChat.pluginMessagePacketVerbose = getConfig().getBoolean("Options.PluginMessagePacketVerbose");
        InteractiveChat.asyncChatThreadPoolExecutorCoreSize = getConfig().getInt("Options.AsyncChatThreadPoolExecutorCoreSize");
        InteractiveChat.asyncChatThreadPoolExecutorMaxSize = getConfig().getInt("Options.AsyncChatThreadPoolExecutorMaxSize");

        InteractiveChat.commandsEventPriority = EventPriority.valueOf(getConfig().getString("Settings.EventPriorities.Commands").toUpperCase());
        InteractiveChat.chatEventPriority = EventPriority.valueOf(getConfig().getString("Settings.EventPriorities.Chat").toUpperCase());

        InteractiveChat.forceUnsignedChatPackets = getConfig().getBoolean("Settings.ForceUnsignedChatPackets");
        InteractiveChat.forceUnsignedChatCommandPackets = getConfig().getBoolean("Settings.ForceUnsignedChatCommandPackets");
        InteractiveChat.hideServerUnsignedStatus = getConfig().getBoolean("Settings.HideServerUnsignedStatus");
        InteractiveChat.skipDetectSpamRateWhenDispatchingUnsignedPackets = getConfig().getBoolean("Settings.SkipDetectSpamRateWhenDispatchingUnsignedPackets");

        InteractiveChat.itemTagMaxLength = getConfig().getInt("Settings.ItemTagMaxLength");
        InteractiveChat.packetStringMaxLength = getConfig().getInt("Settings.PacketStringMaxLength");

        InteractiveChat.chatListener = getConfig().getBoolean("Settings.PacketsToListen.Chat");
        InteractiveChat.titleListener = getConfig().getBoolean("Settings.PacketsToListen.Title");

        InteractiveChat.chatPreviewRemoveClickAndHover = getConfig().getBoolean("Settings.ChatPreviewRemoveClickAndHover");
        InteractiveChat.usePaperModernChatEvent = getConfig().getBoolean("Settings.UsePaperModernChatEvent");
        InteractiveChat.paperChatEventEditOriginalMessageField = getConfig().getBoolean("Settings.PaperChatEventEditOriginalMessageField");

        InteractiveChat.parsePAPIOnMainThread = getConfig().getBoolean("Settings.ParsePAPIOnMainThread");
        InteractiveChat.useAccurateSenderFinder = getConfig().getBoolean("Settings.UseAccurateSenderParser");
        InteractiveChat.tagEveryIdentifiableMessage = getConfig().getBoolean("Settings.TagEveryIdentifiableMessage");

        String colorCodeString = getConfig().getString("Chat.TranslateAltColorCode");
        InteractiveChat.chatAltColorCode = colorCodeString.length() == 1 ? Optional.of(colorCodeString.charAt(0)) : Optional.empty();

        InteractiveChat.useCustomPlaceholderPermissions = getConfig().getBoolean("Settings.UseCustomPlaceholderPermissions");

        InteractiveChat.filterUselessColorCodes = getConfig().getBoolean("Settings.FilterUselessColorCodes", true);

        InteractiveChat.allowMention = getConfig().getBoolean("Chat.AllowMention");
        InteractiveChat.disableHere = getConfig().getBoolean("Chat.DisableHere");
        InteractiveChat.disableEveryone = getConfig().getBoolean("Chat.DisableEveryone");

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
        InteractiveChat.bedrockEventsMenuTitle = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.BedrockEventsMenu.Title"));
        InteractiveChat.bedrockEventsMenuContent = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.BedrockEventsMenu.Content"));
        InteractiveChat.bedrockEventsMenuRunSuggested = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.BedrockEventsMenu.RunSuggested"));

        InteractiveChat.useItem = getConfig().getBoolean("ItemDisplay.Item.Enabled");
        InteractiveChat.useInventory = getConfig().getBoolean("ItemDisplay.Inventory.Enabled");
        InteractiveChat.useEnder = getConfig().getBoolean("ItemDisplay.EnderChest.Enabled");

        InteractiveChat.itemMapPreview = getConfig().getBoolean("ItemDisplay.Item.PreviewMaps");

        Pattern itemPlaceholder = Pattern.compile(getConfig().getString("ItemDisplay.Item.Keyword"));
        Pattern invPlaceholder = Pattern.compile(getConfig().getString("ItemDisplay.Inventory.Keyword"));
        Pattern enderPlaceholder = Pattern.compile(getConfig().getString("ItemDisplay.EnderChest.Keyword"));

        InteractiveChat.itemReplaceText = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.Text"));
        InteractiveChat.itemSingularReplaceText = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.SingularText"));
        InteractiveChat.invReplaceText = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Inventory.Text"));
        InteractiveChat.enderReplaceText = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.EnderChest.Text"));

        InteractiveChat.itemAirAllow = getConfig().getBoolean("ItemDisplay.Item.EmptyItemSettings.AllowAir");
        InteractiveChat.itemAirErrorMessage = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.EmptyItemSettings.DisallowMessage"));

        InteractiveChat.itemTitle = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.InventoryTitle"));
        InteractiveChat.invTitle = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Inventory.InventoryTitle"));
        InteractiveChat.enderTitle = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.EnderChest.InventoryTitle"));

        InteractiveChat.itemHover = getConfig().getBoolean("ItemDisplay.Item.HoverEnabled");
        InteractiveChat.itemAlternativeHoverMessage = ChatColorUtils.translateAlternateColorCodes('&', String.join("\n", getConfig().getStringList("ItemDisplay.Item.AlternativeHoverMessage")));
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
                InteractiveChat.itemFrame1 = ICMaterial.from(getConfig().getString("ItemDisplay.Item.Frame.Primary")).parseItem();
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
                InteractiveChat.itemFrame2 = ICMaterial.from(getConfig().getString("ItemDisplay.Item.Frame.Secondary")).parseItem();
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
                InteractiveChat.invFrame1 = ICMaterial.from(getConfig().getString("ItemDisplay.Inventory.Frame.Primary")).parseItem();
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
                InteractiveChat.invFrame2 = ICMaterial.from(getConfig().getString("ItemDisplay.Inventory.Frame.Secondary")).parseItem();
                InteractiveChat.invFrame2.setAmount(1);
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You have an invalid material name in the config! Please take a look at the Q&A section on the resource page! (ItemDisplay.Inventory.Frame.Secondary)");
            e.printStackTrace();
        }

        InteractiveChat.invSkullName = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Inventory.SkullDisplayName"));
        InteractiveChat.invDisplayLayout = getConfig().getInt("ItemDisplay.Inventory.Layout");

        InteractiveChat.itemDisplayTimeout = getConfig().getLong("ItemDisplay.Settings.Timeout") * 60 * 1000;
        InteractiveChat.hideLodestoneCompassPos = getConfig().getBoolean("ItemDisplay.Settings.HideLodestoneCompassPos");

        if (getConfig().contains("Secret.t")) {
            InteractiveChat.t = getConfig().getBoolean("Secret.t");
        }

        InteractiveChat.usePlayerName = getConfig().getBoolean("Player.UsePlayerNameInteraction");
        InteractiveChat.usePlayerNameOverrideHover = getConfig().getBoolean("Player.OverrideOriginal.HoverEvent");
        InteractiveChat.usePlayerNameOverrideClick = getConfig().getBoolean("Player.OverrideOriginal.ClickEvent");
        InteractiveChat.usePlayerNameHoverEnable = getConfig().getBoolean("Player.Hover.Enable");
        List<String> stringList = getConfig().getStringList("Player.Hover.Text");
        InteractiveChat.usePlayerNameHoverText = ChatColorUtils.translateAlternateColorCodes('&', String.join("\n", stringList));
        InteractiveChat.usePlayerNameClickEnable = getConfig().getBoolean("Player.Click.Enable");
        InteractiveChat.usePlayerNameClickAction = getConfig().getString("Player.Click.Action");
        InteractiveChat.usePlayerNameClickValue = getConfig().getString("Player.Click.Value");
        InteractiveChat.usePlayerNameCaseSensitive = getConfig().getBoolean("Player.CaseSensitive");

        InteractiveChat.chatTabCompletionsEnabled = getConfig().getBoolean("TabCompletion.ChatTabCompletions.Enabled");
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

        InteractiveChat.ecoSetLoreOnMainThread = getConfig().getBoolean("Settings.Hooks.EcoSetLoreOnMainThread");

        InteractiveChat.placeholderList.clear();
        if (InteractiveChat.useItem) {
            String name = InteractiveChat.itemName = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.Name"));
            String description = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Item.Description"));
            InteractiveChat.itemPlaceholder = new BuiltInPlaceholder(itemPlaceholder, name, description, "interactivechat.module.item", getConfig().getLong("ItemDisplay.Item.Cooldown") * 1000);
            InteractiveChat.placeholderList.put(InteractiveChat.itemPlaceholder.getInternalId(), InteractiveChat.itemPlaceholder);
        }
        if (InteractiveChat.useInventory) {
            String name = InteractiveChat.invName = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Inventory.Name"));
            String description = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.Inventory.Description"));
            InteractiveChat.invPlaceholder = new BuiltInPlaceholder(invPlaceholder, name, description, "interactivechat.module.inventory", getConfig().getLong("ItemDisplay.Inventory.Cooldown") * 1000);
            InteractiveChat.placeholderList.put(InteractiveChat.invPlaceholder.getInternalId(), InteractiveChat.invPlaceholder);
        }
        if (InteractiveChat.useEnder) {
            String name = InteractiveChat.enderName = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.EnderChest.Name"));
            String description = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("ItemDisplay.EnderChest.Description"));
            InteractiveChat.enderPlaceholder = new BuiltInPlaceholder(enderPlaceholder, name, description, "interactivechat.module.enderchest", getConfig().getLong("ItemDisplay.EnderChest.Cooldown") * 1000);
            InteractiveChat.placeholderList.put(InteractiveChat.enderPlaceholder.getInternalId(), InteractiveChat.enderPlaceholder);
        }
        if (getConfig().isConfigurationSection("CustomPlaceholders")) {
            for (Map.Entry<String, Object> entry : getConfig().getConfigurationSection("CustomPlaceholders").getValues(false).entrySet()) {
                String key = entry.getKey();
                Object sectionObject = entry.getValue();
                if (sectionObject instanceof ConfigurationSection) {
                    ConfigurationSection s = (ConfigurationSection) sectionObject;
                    ParsePlayer parseplayer = ParsePlayer.fromString(s.getString("ParsePlayer", "sender"));
                    String placeholder = s.getString("Keyword", "$^");
                    boolean parseKeyword = s.getBoolean("ParseKeyword", false);
                    long cooldown = s.getLong("Cooldown", 0) * 1000;
                    boolean hoverEnabled = s.getBoolean("Hover.Enable", false);
                    String hoverText = ChatColorUtils.translateAlternateColorCodes('&', String.join("\n", s.getStringList("Hover.Text")));
                    boolean clickEnabled = s.getBoolean("Click.Enable", false);
                    String clickAction = s.getString("Click.Action", "SUGGEST_COMMAND").toUpperCase();
                    String clickValue = s.getString("Click.Value", "");
                    boolean replaceEnabled = s.getBoolean("Replace.Enable", false);
                    String replaceText = ChatColorUtils.translateAlternateColorCodes('&', s.getString("Replace.ReplaceText", ""));
                    String name = ChatColorUtils.translateAlternateColorCodes('&', s.getString("Name", placeholder.replace("\\", "")));
                    String description = ChatColorUtils.translateAlternateColorCodes('&', s.getString("Description", "&7&oDescription missing"));

                    ICPlaceholder customPlaceholder = new CustomPlaceholder(key, parseplayer, Pattern.compile(placeholder), parseKeyword, cooldown, new CustomPlaceholderHoverEvent(hoverEnabled, hoverText), new CustomPlaceholderClickEvent(clickEnabled, clickEnabled ? ClickEventAction.valueOf(clickAction) : null, clickValue), new CustomPlaceholderReplaceText(replaceEnabled, replaceText), name, description);
                    InteractiveChat.placeholderList.put(customPlaceholder.getInternalId(), customPlaceholder);
                } else {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You have an invalid custom placeholder in the config, keyed \"" + key + "\"");
                }
            }
        }

        if (InteractiveChat.bungeecordMode) {
            InteractiveChat.queueRemoteUpdate = true;
        }

        InteractiveChat.commandList = getConfig().getStringList("Settings.CommandsToParse");

        InteractiveChat.maxPlaceholders = getConfig().getInt("Settings.MaxPlaceholders");
        InteractiveChat.limitReachMessage = getConfig().getString("Messages.LimitReached");

        InteractiveChat.mentionPrefix = getConfig().getString("Chat.MentionPrefix");
        InteractiveChat.mentionHighlight = getConfig().getString("Chat.MentionHighlight");
        InteractiveChat.mentionHighlightOthers = getConfig().getString("Chat.MentionHighlightOthers");
        List<String> stringList3 = getConfig().getStringList("Chat.MentionHoverText");
        InteractiveChat.mentionHover = String.join("\n", stringList3);
        InteractiveChat.mentionDuration = getConfig().getDouble("Chat.MentionedTitleDuration");
        InteractiveChat.mentionCooldown = (long) (getConfig().getDouble("Chat.MentionCooldown") * 1000);

        InteractiveChat.mentionEnable = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.EnableMentions"));
        InteractiveChat.mentionDisable = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Messages.DisableMentions"));

        InteractiveChat.mentionTitle = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Chat.MentionedTitle"));
        InteractiveChat.mentionSubtitle = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Chat.KnownPlayerMentionSubtitle"));
        InteractiveChat.mentionActionbar = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Chat.KnownPlayerMentionActionbar"));
        InteractiveChat.mentionToast = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Chat.MentionToast"));
        InteractiveChat.mentionBossBarText = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Chat.MentionBossBar.Text"));
        InteractiveChat.mentionBossBarColorName = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Chat.MentionBossBar.Color"));
        InteractiveChat.mentionBossBarOverlayName = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Chat.MentionBossBar.Overlay"));

        InteractiveChat.mentionSound = getConfig().getString("Chat.MentionedSound");

        InteractiveChat.mentionTitleDuration = (int) Math.round(ConfigManager.getConfig().getDouble("Chat.MentionedTitleDuration") * 20);
        InteractiveChat.mentionBossBarDuration = (int) Math.round(ConfigManager.getConfig().getDouble("Chat.MentionBossBar.Duration") * 20);
        InteractiveChat.mentionBossBarRemoveDelay = (int) Math.round(ConfigManager.getConfig().getDouble("Chat.MentionBossBar.RemoveDelay") * 20);

        InteractiveChat.updaterEnabled = getConfig().getBoolean("Options.Updater");
        InteractiveChat.cancelledMessage = getConfig().getBoolean("Options.ShowCancelledNotice");

        InteractiveChat.clickableCommands = getConfig().getBoolean("Commands.Enabled");
        InteractiveChat.clickableCommandsFormat = getConfig().getString("Commands.Format");
        InteractiveChat.clickableCommandsAction = ClickEvent.Action.valueOf(getConfig().getString("Commands.Action"));
        InteractiveChat.clickableCommandsDisplay = ChatColorUtils.translateAlternateColorCodes('&', getConfig().getString("Commands.Text"));
        InteractiveChat.clickableCommandsHoverText = ChatColorUtils.translateAlternateColorCodes('&', String.join("\n", getConfig().getStringList("Commands.HoverMessage")));

        InteractiveChat.sendOriginalIfTooLong = getConfig().getBoolean("Settings.SendOriginalMessageIfExceedLengthLimit");

        InteractiveChat.messageToIgnore = new HashSet<>(getConfig().getStringList("Settings.MessagesToIgnore"));

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
                ItemStack unknown = ICMaterial.from(getConfig().getString("Settings.BungeecordUnknownItem.ReplaceItem")).parseItem();
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

        InteractiveChat.rgbTags = getConfig().getBoolean("Settings.FormattingTags.AllowRGBTags");
        InteractiveChat.fontTags = getConfig().getBoolean("Settings.FormattingTags.AllowFontTags");
        InteractiveChat.additionalRGBFormats = getConfig().getStringList("Settings.FormattingTags.AdditionalRGBFormats").stream().map(each -> Pattern.compile(each)).collect(Collectors.toList());

        InteractiveChat.language = getConfig().getString("Settings.Language");

        InteractiveChat.plugin.getScheduler().runAsync((task) -> {
            LanguageUtils.loadTranslations(InteractiveChat.language);
            if (WebData.getInstance() == null) {
                WebData.newInstance();
            } else {
                WebData.getInstance().reload();
            }
        });

        InteractiveChat.itemDisplay.clearAndSetTimeout(InteractiveChat.itemDisplayTimeout);
        InteractiveChat.inventoryDisplay.clearAndSetTimeout(InteractiveChat.itemDisplayTimeout);
        InteractiveChat.inventoryDisplay1Upper.clearAndSetTimeout(InteractiveChat.itemDisplayTimeout);
        InteractiveChat.inventoryDisplay1Lower.clearAndSetTimeout(InteractiveChat.itemDisplayTimeout);
        InteractiveChat.enderDisplay.clearAndSetTimeout(InteractiveChat.itemDisplayTimeout);
        InteractiveChat.mapDisplay.clearAndSetTimeout(InteractiveChat.itemDisplayTimeout);
        InteractiveChat.upperSharedInventory.clear();
        InteractiveChat.lowerSharedInventory.clear();
    }

}
