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

package com.loohp.interactivechat.modules;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.api.events.PreExternalResponseSendEvent;
import com.loohp.interactivechat.data.PlayerDataManager.PlayerData;
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.objectholders.ProcessSenderResult;
import com.loohp.interactivechat.objectholders.WebData;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentFont;
import com.loohp.interactivechat.utils.ComponentModernizing;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.CustomStringUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.ItemStackUtils;
import com.loohp.interactivechat.utils.JsonUtils;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;
import com.loohp.interactivechat.utils.RarityUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessExternalMessage {

    private static final InteractiveChat initPlugin = InteractiveChat.plugin;

    private static Plugin plugin;

    /**
     * This is to support /reload
     */
    private static Object getInstance() throws Exception {
        Field externalProcessorField;
        if (plugin == null || !plugin.isEnabled()) {
            plugin = Bukkit.getPluginManager().getPlugin("InteractiveChat");
        }
        externalProcessorField = plugin.getClass().getField("externalProcessor");
        return externalProcessorField.get(plugin);
    }

    public static String processWithoutReceiver(String message) {
        if (initPlugin.isEnabled()) {
            return initPlugin.externalProcessor.processWithoutReceiver0(message);
        } else {
            try {
                Object obj = getInstance();
                Method processWithoutReceiver0Method = obj.getClass().getMethod("processWithoutReceiver0", String.class);
                return (String) processWithoutReceiver0Method.invoke(obj, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return message;
        }
    }

    public static String processAndRespond(Player receiver, String component, boolean preview) throws Exception {
        if (initPlugin.isEnabled()) {
            return initPlugin.externalProcessor.processAndRespond0(receiver, component, preview);
        } else {
            try {
                Object obj = getInstance();
                Method processAndRespond0Method = obj.getClass().getMethod("processAndRespond0", Player.class, String.class, boolean.class);
                return (String) processAndRespond0Method.invoke(obj, receiver, component, preview);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return component;
        }
    }

    @SuppressWarnings("deprecation")
    public String processWithoutReceiver0(String message) {
        UUID senderUUID = ProcessAccurateSender.find(message);
        ICPlayer sender;
        if (senderUUID == null) {
            sender = null;
        } else {
            sender = ICPlayerFactory.getICPlayer(senderUUID);
        }

        message = message.replaceAll(ProcessCommands.COLOR_IGNORE_PATTERN_0.pattern(), "").replaceAll(ProcessCommands.COLOR_IGNORE_PATTERN_1.pattern(), "").replaceAll(ProcessAccurateSender.COLOR_IGNORE_PATTERN.pattern(), "");
        message = message.replaceAll(ProcessAccurateSender.PATTERN_0.pattern(), "$2");
        Matcher matcher = ProcessAccurateSender.PATTERN_0.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String replacement = Registry.ID_UNESCAPE_PATTERN.matcher(matcher.group(2)).replaceAll(">");
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        message = sb.toString();
        message = Registry.MENTION_TAG_CONVERTER.revertTags(message);

        if (sender == null) {
            return message;
        }

        long now = System.currentTimeMillis();
        long uniCooldown = InteractiveChatAPI.getPlayerUniversalCooldown(sender.getUniqueId()) - now;

        if (!(uniCooldown < 0 || uniCooldown + 100 > InteractiveChat.universalCooldown)) {
            return message;
        }

        if (InteractiveChat.rgbTags) {
            message = CustomStringUtils.clearPluginRGBTags(message);
        }
        if (InteractiveChat.fontTags) {
            message = CustomStringUtils.clearPluginFontTags(message);
        }

        Component component = LegacyComponentSerializer.legacySection().deserialize(message);

        for (ICPlaceholder placeholder : InteractiveChatAPI.getICPlaceholderList()) {
            if (!placeholder.isBuildIn()) {
                CustomPlaceholder customP = (CustomPlaceholder) placeholder;
                if (!InteractiveChat.useCustomPlaceholderPermissions || (InteractiveChat.useCustomPlaceholderPermissions && PlayerUtils.hasPermission(sender.getUniqueId(), customP.getPermission(), true, 250))) {
                    if (customP.getKeyword().matcher(message).find()) {
                        if (customP.getReplace().isEnabled()) {
                            String replace = ChatColor.WHITE + ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(sender, customP.getReplace().getReplaceText()));
                            component = ComponentReplacing.replace(component, customP.getKeyword().pattern(), true, result -> {
                                String replaceString = CustomStringUtils.applyReplacementRegex(replace, result, 1);
                                return LegacyComponentSerializer.legacySection().deserialize(replaceString);
                            });
                        }
                    }
                }
            }
        }

        if (InteractiveChat.t && WebData.getInstance() != null) {
            for (CustomPlaceholder customP : WebData.getInstance().getSpecialPlaceholders()) {
                if (customP.getKeyword().matcher(message).find()) {
                    if (customP.getReplace().isEnabled()) {
                        String replace = ChatColor.WHITE + ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(sender, customP.getReplace().getReplaceText()));
                        component = ComponentReplacing.replace(component, customP.getKeyword().pattern(), true, result -> {
                            String replaceString = CustomStringUtils.applyReplacementRegex(replace, result, 1);
                            return LegacyComponentSerializer.legacySection().deserialize(replaceString);
                        });
                    }
                }
            }
        }

        if (InteractiveChat.useItem && PlayerUtils.hasPermission(sender.getUniqueId(), "interactivechat.module.item", true, 250)) {
            Pattern placeholder = InteractiveChat.itemPlaceholder.getKeyword();
            if (placeholder.matcher(message).find()) {
                ItemStack item = sender.getEquipment().getItemInHand();
                if (item == null) {
                    item = new ItemStack(Material.AIR);
                }
                String itemStr = InteractiveChatComponentSerializer.bungeecordApiLegacy().serialize(ItemStackUtils.getDisplayName(item), InteractiveChat.language);

                int amount = item.getAmount();
                if (item == null || item.getType().equals(Material.AIR)) {
                    amount = 1;
                }

                itemStr = RarityUtils.getRarityColor(item) + itemStr;

                String replaceText;
                if (amount == 1) {
                    replaceText = PlaceholderParser.parse(sender, InteractiveChat.itemSingularReplaceText.replace("{Item}", itemStr));
                } else {
                    replaceText = PlaceholderParser.parse(sender, InteractiveChat.itemReplaceText.replace("{Amount}", String.valueOf(amount)).replace("{Item}", itemStr));
                }
                component = ComponentReplacing.replace(component, placeholder.pattern(), true, LegacyComponentSerializer.legacySection().deserialize(replaceText));
            }
        }

        if (InteractiveChat.useInventory && PlayerUtils.hasPermission(sender.getUniqueId(), "interactivechat.module.inventory", true, 250)) {
            Pattern placeholder = InteractiveChat.invPlaceholder.getKeyword();
            if (placeholder.matcher(message).find()) {
                String replaceText = PlaceholderParser.parse(sender, InteractiveChat.invReplaceText);
                component = ComponentReplacing.replace(component, placeholder.pattern(), true, LegacyComponentSerializer.legacySection().deserialize(replaceText));
            }
        }

        if (InteractiveChat.useEnder && PlayerUtils.hasPermission(sender.getUniqueId(), "interactivechat.module.enderchest", true, 250)) {
            Pattern placeholder = InteractiveChat.enderPlaceholder.getKeyword();
            if (placeholder.matcher(message).find()) {
                String replaceText = PlaceholderParser.parse(sender, InteractiveChat.enderReplaceText);
                component = ComponentReplacing.replace(component, placeholder.pattern(), true, LegacyComponentSerializer.legacySection().deserialize(replaceText));
            }
        }

        return LegacyComponentSerializer.builder().character(ChatColorUtils.COLOR_CHAR).hexColors().useUnusualXRepeatedCharacterHexFormat().build().serialize(component);
    }

    public String processAndRespond0(Player receiver, String json, boolean preview) throws Exception {
        Component component = InteractiveChatComponentSerializer.gson().deserialize(json);
        Component originalComponent = component;

        component = ComponentModernizing.modernize(component);

        try {
            if (LegacyComponentSerializer.legacySection().serialize(component).isEmpty()) {
                return json;
            }
        } catch (Exception e) {
            return json;
        }

        if ((InteractiveChat.version.isOld()) && JsonUtils.containsKey(InteractiveChatComponentSerializer.gson().serialize(component), "translate")) {
            return json;
        }

        Optional<ICPlayer> sender = Optional.empty();
        String rawMessageKey = InteractiveChatComponentSerializer.plainText().serializeOr(component, "");

        InteractiveChat.keyTime.putIfAbsent(rawMessageKey, System.currentTimeMillis());

        Long timeKey = InteractiveChat.keyTime.get(rawMessageKey);
        long unix = timeKey == null ? System.currentTimeMillis() : timeKey;

        ProcessSenderResult commandSender = ProcessCommands.process(component);
        if (commandSender.getSender() != null) {
            sender = Optional.ofNullable(ICPlayerFactory.getICPlayer(commandSender.getSender()));
        }
        ProcessSenderResult chatSender = null;
        if (!sender.isPresent()) {
            if (InteractiveChat.useAccurateSenderFinder) {
                chatSender = ProcessAccurateSender.process(component);
                if (chatSender.getSender() != null) {
                    sender = Optional.ofNullable(ICPlayerFactory.getICPlayer(chatSender.getSender()));
                }
            }
        }
        if (!sender.isPresent() && !InteractiveChat.useAccurateSenderFinder) {
            sender = SenderFinder.getSender(component, rawMessageKey);
        }

        component = commandSender.getComponent();
        if (chatSender != null) {
            component = chatSender.getComponent();
        }

        String text = LegacyComponentSerializer.legacySection().serialize(component);
        if (InteractiveChat.messageToIgnore.stream().anyMatch(each -> text.matches(each))) {
            return json;
        }

        if (sender.isPresent()) {
            InteractiveChat.keyPlayer.put(rawMessageKey, sender.get());
        }

        String server;
        if (sender.isPresent() && !sender.get().isLocal()) {
            try {
                TimeUnit.MILLISECONDS.sleep(InteractiveChat.remoteDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            server = sender.get().getServer();
        } else {
            server = ICPlayer.LOCAL_SERVER_REPRESENTATION;
        }

        component = ComponentReplacing.replace(component, Registry.ID_PATTERN.pattern(), Registry.ID_PATTERN_REPLACEMENT);

        if (InteractiveChat.usePlayerName) {
            component = PlayernameDisplay.process(component, sender, receiver, unix);
        }

        if (InteractiveChat.allowMention && sender.isPresent()) {
            PlayerData data = InteractiveChat.playerDataManager.getPlayerData(receiver);
            if (data == null || !data.isMentionDisabled()) {
                component = MentionDisplay.process(component, receiver, sender.get(), unix, !Bukkit.isPrimaryThread());
            }
        }
        component = ComponentReplacing.replace(component, Registry.MENTION_TAG_CONVERTER.getReversePattern().pattern(), true, (result, components) -> {
            return LegacyComponentSerializer.legacySection().deserialize(result.group(2));
        });

        Collection<ICPlaceholder> serverPlaceholderList = InteractiveChat.remotePlaceholderList.get(server);
        if (server.equals(ICPlayer.LOCAL_SERVER_REPRESENTATION) || serverPlaceholderList == null) {
            serverPlaceholderList = InteractiveChat.placeholderList.values();
        }
        component = CustomPlaceholderDisplay.process(component, sender, receiver, serverPlaceholderList, unix);

        if (InteractiveChat.useItem) {
            component = ItemDisplay.process(component, sender, receiver, preview, unix);
        }

        if (InteractiveChat.useInventory) {
            component = InventoryDisplay.process(component, sender, receiver, preview, unix);
        }

        if (InteractiveChat.useEnder) {
            component = EnderchestDisplay.process(component, sender, receiver, preview, unix);
        }

        if (InteractiveChat.clickableCommands) {
            component = CommandsDisplay.process(component);
        }

        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16) && InteractiveChat.fontTags) {
            if (!sender.isPresent() || (sender.isPresent() && PlayerUtils.hasPermission(sender.get().getUniqueId(), "interactivechat.customfont.translate", true, 5))) {
                component = ComponentFont.parseFont(component);
            }
        }

        InteractiveChat.plugin.getScheduler().runLater((task) -> {
            InteractiveChat.keyTime.remove(rawMessageKey);
            InteractiveChat.keyPlayer.remove(rawMessageKey);
        }, 5);

        String newJson = InteractiveChatComponentSerializer.gson().serialize(component);

        PreExternalResponseSendEvent event = new PreExternalResponseSendEvent(!Bukkit.isPrimaryThread(), receiver, component, sender.map(each -> each.getUniqueId()).orElse(null), originalComponent, InteractiveChat.sendOriginalIfTooLong);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isSendOriginalIfCancelled() && newJson.length() > InteractiveChat.packetStringMaxLength) {
            String originalJson = InteractiveChatComponentSerializer.gson().serialize(originalComponent);
            if (originalJson.length() > InteractiveChat.packetStringMaxLength) {
                return "{\"text\":\"\"}";
            } else {
                return originalJson;
            }
        }

        return newJson;
    }

}
