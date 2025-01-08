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

package com.loohp.interactivechat.listeners;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageSender;
import com.loohp.interactivechat.data.PlayerDataManager.PlayerData;
import com.loohp.interactivechat.objectholders.CooldownResult;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.objectholders.MentionPair;
import com.loohp.interactivechat.objectholders.SignedMessageModificationData;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.PlayerUtils;
import com.loohp.interactivechat.utils.TimeUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatEvents implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandLowest(PlayerCommandPreprocessEvent event) {
        checkSignedModificationsFromProxy(event);
        if (InteractiveChat.commandsEventPriority.equals(EventPriority.LOWEST)) {
            checkCommand(event);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCommandLow(PlayerCommandPreprocessEvent event) {
        if (InteractiveChat.commandsEventPriority.equals(EventPriority.LOW)) {
            checkCommand(event);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCommandNormal(PlayerCommandPreprocessEvent event) {
        if (InteractiveChat.commandsEventPriority.equals(EventPriority.NORMAL)) {
            checkCommand(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommandHigh(PlayerCommandPreprocessEvent event) {
        if (InteractiveChat.commandsEventPriority.equals(EventPriority.HIGH)) {
            checkCommand(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandHighest(PlayerCommandPreprocessEvent event) {
        if (InteractiveChat.commandsEventPriority.equals(EventPriority.HIGHEST)) {
            checkCommand(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommandMonitor(PlayerCommandPreprocessEvent event) {
        if (InteractiveChat.commandsEventPriority.equals(EventPriority.MONITOR)) {
            checkCommand(event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatLowest(AsyncPlayerChatEvent event) {
        checkSignedModificationsFromProxy(event);
        if (!InteractiveChat.usePaperModernChatEvent && InteractiveChat.chatEventPriority.equals(EventPriority.LOWEST)) {
            checkChat(event);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChatLow(AsyncPlayerChatEvent event) {
        if (!InteractiveChat.usePaperModernChatEvent && InteractiveChat.chatEventPriority.equals(EventPriority.LOW)) {
            checkChat(event);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChatNormal(AsyncPlayerChatEvent event) {
        if (!InteractiveChat.usePaperModernChatEvent && InteractiveChat.chatEventPriority.equals(EventPriority.NORMAL)) {
            checkChat(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChatHigh(AsyncPlayerChatEvent event) {
        if (!InteractiveChat.usePaperModernChatEvent && InteractiveChat.chatEventPriority.equals(EventPriority.HIGH)) {
            checkChat(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChatHighest(AsyncPlayerChatEvent event) {
        if (!InteractiveChat.usePaperModernChatEvent && InteractiveChat.chatEventPriority.equals(EventPriority.HIGHEST)) {
            checkChat(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChatMonitor(AsyncPlayerChatEvent event) {
        if (!InteractiveChat.usePaperModernChatEvent && InteractiveChat.chatEventPriority.equals(EventPriority.MONITOR)) {
            checkChat(event);
        }
    }

    public static void checkSignedModificationsFromProxy(AsyncPlayerChatEvent event) {
        event.setMessage(checkSignedModificationsFromProxy(event.getPlayer().getUniqueId(), event.getMessage()));
    }

    public static void checkSignedModificationsFromProxy(PlayerCommandPreprocessEvent event) {
        event.setMessage(checkSignedModificationsFromProxy(event.getPlayer().getUniqueId(), event.getMessage()));
    }

    public static String checkSignedModificationsFromProxy(UUID uuid, String originalMessage) {
        if (InteractiveChat.bungeecordMode) {
            List<SignedMessageModificationData> data = InteractiveChat.signedMessageModificationData.get(uuid);
            if (data != null) {
                long now = System.currentTimeMillis();
                synchronized (data) {
                    Iterator<SignedMessageModificationData> itr = data.iterator();
                    while (itr.hasNext()) {
                        SignedMessageModificationData modificationData = itr.next();
                        if (now - modificationData.getTime() > 5000) {
                            itr.remove();
                            continue;
                        }
                        if (modificationData.getOriginalMessage().equals(originalMessage)) {
                            itr.remove();
                            return modificationData.getModifiedMessage();
                        }
                    }
                }
            }
        }
        return originalMessage;
    }

    public static void checkChat(AsyncPlayerChatEvent event) {
        if (!InteractiveChat.bungeecordMode) {
            event.setMessage(Registry.ID_PATTERN.matcher(event.getMessage()).replaceAll(""));
        }
        translateAltColorCode(event);

        String processedMessage = checkMention(event);
        event.setMessage(processedMessage);

        checkChatMessage(event);
    }

    public static void checkCommand(PlayerCommandPreprocessEvent event) {
        if (!InteractiveChat.bungeecordMode) {
            event.setMessage(Registry.ID_PATTERN.matcher(event.getMessage()).replaceAll(""));
        }
        boolean flag = true;
        String command = event.getMessage();
        for (String parsecommand : InteractiveChat.commandList) {
            if (command.matches(parsecommand)) {
                if (flag) {
                    translateAltColorCode(event);
                    command = event.getMessage();
                    flag = false;
                }

                CooldownResult cooldownResult = InteractiveChat.placeholderCooldownManager.checkMessage(event.getPlayer().getUniqueId(), command);
                if (!cooldownResult.getOutcome().isAllowed()) {
                    event.setCancelled(true);
                    Component cancelmessage;
                    switch (cooldownResult.getOutcome()) {
                        case DENY_PLACEHOLDER:
                            cancelmessage = LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(event.getPlayer(), InteractiveChat.placeholderCooldownMessage.replace("{Time}", TimeUtils.getReadableTimeBetween(System.currentTimeMillis(), cooldownResult.getCooldownExpireTime())))));
                            cancelmessage = ComponentReplacing.replace(cancelmessage, "\\{Keyword\\}", Component.text(cooldownResult.getPlaceholder().getName()).hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(cooldownResult.getPlaceholder().getDescription()))));
                            break;
                        case DENY_UNIVERSAL:
                            cancelmessage = LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(event.getPlayer(), InteractiveChat.universalCooldownMessage.replace("{Time}", TimeUtils.getReadableTimeBetween(System.currentTimeMillis(), cooldownResult.getCooldownExpireTime())))));
                            break;
                        default:
                            cancelmessage = Component.empty();
                            break;
                    }
                    InteractiveChatAPI.sendMessageUnprocessed(event.getPlayer(), cancelmessage);
                    return;
                }

                int count = 0;
                for (ICPlaceholder icplaceholder : InteractiveChat.placeholderList.values()) {
                    Matcher matcher = icplaceholder.getKeyword().matcher(command);
                    if (matcher.find()) {
                        int start = matcher.start();
                        if ((start < 1 || command.charAt(start - 1) != '\\') || (start > 1 && command.charAt(start - 1) == '\\' && command.charAt(start - 2) == '\\')) {
                            if (icplaceholder.equals(InteractiveChat.itemPlaceholder) && !InteractiveChat.itemAirAllow && PlayerUtils.getHeldItem(event.getPlayer()).getType().equals(Material.AIR) && PlayerUtils.hasPermission(event.getPlayer().getUniqueId(), "interactivechat.module.item", false, 200)) {
                                event.setCancelled(true);
                                String cancelmessage = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(event.getPlayer(), InteractiveChat.itemAirErrorMessage));
                                event.getPlayer().sendMessage(cancelmessage);
                                return;
                            }
                            Matcher matcher1 = icplaceholder.getKeyword().matcher(command);
                            while (matcher1.find()) {
                                int startPos = matcher1.start();
                                if ((startPos < 1 || command.charAt(startPos - 1) != '\\') || (startPos > 1 && command.charAt(startPos - 1) == '\\' && command.charAt(startPos - 2) == '\\')) {
                                    count++;
                                }
                            }
                        }
                    }
                }
                if (InteractiveChat.maxPlaceholders >= 0 && count > InteractiveChat.maxPlaceholders) {
                    event.setCancelled(true);
                    String cancelmessage = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(event.getPlayer(), InteractiveChat.limitReachMessage));
                    event.getPlayer().sendMessage(cancelmessage);
                    return;
                } else if (count <= 0) {
                    return;
                }

                if (InteractiveChat.tagEveryIdentifiableMessage) {
                    String uuidmatch = " <cmd=" + event.getPlayer().getUniqueId() + ">";
                    event.setMessage(command + uuidmatch);
                    break;
                } else {
                    if (!Registry.ID_PATTERN.matcher(command).find()) {
                        for (ICPlaceholder icplaceholder : InteractiveChat.placeholderList.values()) {
                            Pattern placeholder = icplaceholder.getKeyword();
                            Matcher matcher = placeholder.matcher(command);
                            if (matcher.find()) {
                                int start = matcher.start();
                                if ((start < 1 || command.charAt(start - 1) != '\\') || (start > 1 && command.charAt(start - 1) == '\\' && command.charAt(start - 2) == '\\')) {
                                    String uuidmatch = "<cmd=" + event.getPlayer().getUniqueId() + ":" + Registry.ID_ESCAPE_PATTERN.matcher(command.substring(matcher.start(), matcher.end())).replaceAll("\\>") + ":>";
                                    command = command.substring(0, matcher.start()) + uuidmatch + command.substring(matcher.end());
                                    event.setMessage(command);
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    public static void checkChatMessage(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        Player player = event.getPlayer();

        CooldownResult cooldownResult = InteractiveChat.placeholderCooldownManager.checkMessage(event.getPlayer().getUniqueId(), message);
        if (!cooldownResult.getOutcome().isAllowed()) {
            event.setCancelled(true);
            Component cancelmessage;
            switch (cooldownResult.getOutcome()) {
                case DENY_PLACEHOLDER:
                    cancelmessage = LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(event.getPlayer(), InteractiveChat.placeholderCooldownMessage.replace("{Time}", TimeUtils.getReadableTimeBetween(System.currentTimeMillis(), cooldownResult.getCooldownExpireTime())))));
                    cancelmessage = ComponentReplacing.replace(cancelmessage, "\\{Keyword\\}", Component.text(cooldownResult.getPlaceholder().getName()).hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(cooldownResult.getPlaceholder().getDescription()))));
                    break;
                case DENY_UNIVERSAL:
                    cancelmessage = LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(event.getPlayer(), InteractiveChat.universalCooldownMessage.replace("{Time}", TimeUtils.getReadableTimeBetween(System.currentTimeMillis(), cooldownResult.getCooldownExpireTime())))));
                    break;
                default:
                    cancelmessage = Component.empty();
                    break;
            }
            InteractiveChatAPI.sendMessageUnprocessed(player, cancelmessage);
            return;
        }

        int count = 0;
        for (ICPlaceholder icplaceholder : InteractiveChat.placeholderList.values()) {
            Matcher matcher = icplaceholder.getKeyword().matcher(message);
            if (matcher.find()) {
                int start = matcher.start();
                if ((start < 1 || message.charAt(start - 1) != '\\') || (start > 1 && message.charAt(start - 1) == '\\' && message.charAt(start - 2) == '\\')) {
                    if (icplaceholder.equals(InteractiveChat.itemPlaceholder) && !InteractiveChat.itemAirAllow && PlayerUtils.getHeldItem(event.getPlayer()).getType().equals(Material.AIR) && PlayerUtils.hasPermission(event.getPlayer().getUniqueId(), "interactivechat.module.item", false, 200)) {
                        event.setCancelled(true);
                        String cancelmessage = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(event.getPlayer(), InteractiveChat.itemAirErrorMessage));
                        event.getPlayer().sendMessage(cancelmessage);
                        return;
                    }
                    Matcher matcher1 = icplaceholder.getKeyword().matcher(message);
                    while (matcher1.find()) {
                        int startPos = matcher1.start();
                        if ((startPos < 1 || message.charAt(startPos - 1) != '\\') || (startPos > 1 && message.charAt(startPos - 1) == '\\' && message.charAt(startPos - 2) == '\\')) {
                            count++;
                        }
                    }
                }
            }
        }
        if (InteractiveChat.maxPlaceholders >= 0 && count > InteractiveChat.maxPlaceholders) {
            event.setCancelled(true);
            String cancelmessage = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(event.getPlayer(), InteractiveChat.limitReachMessage));
            event.getPlayer().sendMessage(cancelmessage);
            return;
        } else {
            if (InteractiveChat.tagEveryIdentifiableMessage) {
                String uuidmatch = " <chat=" + event.getPlayer().getUniqueId() + ">";
                message = message + uuidmatch;
            } else if (count > 0) {
                if (InteractiveChat.useAccurateSenderFinder && !message.startsWith("/") && !Registry.ID_PATTERN.matcher(message).find()) {
                    for (ICPlaceholder icplaceholder : InteractiveChat.placeholderList.values()) {
                        Pattern placeholder = icplaceholder.getKeyword();
                        Matcher matcher = placeholder.matcher(message);
                        if (matcher.find()) {
                            int start = matcher.start();
                            if ((start < 1 || message.charAt(start - 1) != '\\') || (start > 1 && message.charAt(start - 1) == '\\' && message.charAt(start - 2) == '\\')) {
                                String uuidmatch = "<chat=" + event.getPlayer().getUniqueId() + ":" + Registry.ID_ESCAPE_PATTERN.matcher(message.substring(matcher.start(), matcher.end())).replaceAll("\\>") + ":>";
                                message = message.substring(0, matcher.start()) + uuidmatch + message.substring(matcher.end());
                                break;
                            }
                        }
                    }
                }
            }
        }

        event.setMessage(message);

        String mapKey = ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', event.getMessage()));
        InteractiveChat.messages.put(mapKey, player.getUniqueId());
        InteractiveChat.plugin.getScheduler().runLater((task) -> InteractiveChat.messages.remove(mapKey), 60);

        if (InteractiveChat.bungeecordMode) {
            try {
                BungeeMessageSender.addMessage(System.currentTimeMillis(), ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', event.getMessage())), event.getPlayer().getUniqueId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String checkMention(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String message = event.getMessage();
        PlayerData data = InteractiveChat.playerDataManager.getPlayerData(sender);
        if (InteractiveChat.allowMention && (data == null || !data.isMentionDisabled())) {
            String processedMessage;
            if (!InteractiveChat.disableEveryone && (processedMessage = checkMentionEveryone("chat", message, sender)) != null) {
                return processedMessage;
            }
            if (!InteractiveChat.disableHere && (processedMessage = checkMentionHere("chat", message, sender)) != null) {
                return processedMessage;
            }
            if ((processedMessage = checkMentionPlayers("chat", message, sender)) != null) {
                return processedMessage;
            }
        }
        return message;
    }

    public static String checkMentionPlayers(String senderTagType, String message, Player sender) {
        boolean senderTagged = Registry.ID_PATTERN.matcher(message).find();
        if (PlayerUtils.hasPermission(sender.getUniqueId(), "interactivechat.mention.player", false, 200)) {
            Map<String, UUID> playernames = new HashMap<>();
            for (ICPlayer player : ICPlayerFactory.getOnlineICPlayers()) {
                if (!player.isVanished()) {
                    playernames.put(ChatColorUtils.stripColor(player.getName()), player.getUniqueId());
                    if (InteractiveChat.useBukkitDisplayName && !ChatColorUtils.stripColor(player.getName()).equals(ChatColorUtils.stripColor(player.getDisplayName()))) {
                        playernames.put(ChatColorUtils.stripColor(player.getDisplayName()), player.getUniqueId());
                    }
                    List<String> names = InteractiveChatAPI.getNicknames(player.getUniqueId());
                    for (String name : names) {
                        playernames.put(ChatColorUtils.stripColor(name), player.getUniqueId());
                    }
                }
            }
            for (Entry<String, UUID> entry : playernames.entrySet()) {
                String name = InteractiveChat.mentionPrefix + entry.getKey();
                UUID uuid = entry.getValue();
                int index = message.toLowerCase().indexOf(name.toLowerCase());
                if (index >= 0) {
                    if (senderTagged) {
                        message = Registry.MENTION_TAG_CONVERTER.convertToTag(name, message);
                    } else {
                        String tagStyle = Registry.MENTION_TAG_CONVERTER.getTagStyle(name);
                        String uuidmatch = "<" + senderTagType + "=" + sender.getUniqueId() + ":" + Registry.ID_ESCAPE_PATTERN.matcher(tagStyle).replaceAll("\\>") + ":>";
                        message = message.replace(name, uuidmatch);
                    }
                    if (!uuid.equals(sender.getUniqueId())) {
                        InteractiveChat.mentionPair.add(new MentionPair(sender.getUniqueId(), uuid));
                        if (InteractiveChat.bungeecordMode) {
                            try {
                                BungeeMessageSender.forwardMentionPair(System.currentTimeMillis(), sender.getUniqueId(), uuid);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    return message;
                }
            }
        }
        return null;
    }

    public static String checkMentionHere(String senderTagType, String message, Player sender) {
        if (PlayerUtils.hasPermission(sender.getUniqueId(), "interactivechat.mention.here", false, 200)) {
            boolean senderTagged = Registry.ID_PATTERN.matcher(message).find();
            String name = InteractiveChat.mentionPrefix + "here";
            int index = message.toLowerCase().indexOf(name.toLowerCase());
            if (index >= 0) {
                if (senderTagged) {
                    message = Registry.MENTION_TAG_CONVERTER.convertToTag(name, message);
                } else {
                    String tagStyle = Registry.MENTION_TAG_CONVERTER.getTagStyle(name);
                    String uuidmatch = "<" + senderTagType + "=" + sender.getUniqueId() + ":" + Registry.ID_ESCAPE_PATTERN.matcher(tagStyle).replaceAll("\\>") + ":>";
                    message = message.replace(name, uuidmatch);
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    if (!uuid.equals(sender.getUniqueId())) {
                        InteractiveChat.mentionPair.add(new MentionPair(sender.getUniqueId(), uuid));
                        if (InteractiveChat.bungeecordMode) {
                            try {
                                BungeeMessageSender.forwardMentionPair(System.currentTimeMillis(), sender.getUniqueId(), uuid);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                return message;
            }
        }
        return null;
    }

    public static String checkMentionEveryone(String senderTagType, String message, Player sender) {
        if (PlayerUtils.hasPermission(sender.getUniqueId(), "interactivechat.mention.everyone", false, 200)) {
            boolean senderTagged = Registry.ID_PATTERN.matcher(message).find();
            String name = InteractiveChat.mentionPrefix + "everyone";
            int index = message.toLowerCase().indexOf(name.toLowerCase());
            if (index >= 0) {
                if (senderTagged) {
                    message = Registry.MENTION_TAG_CONVERTER.convertToTag(name, message);
                } else {
                    String tagStyle = Registry.MENTION_TAG_CONVERTER.getTagStyle(name);
                    String uuidmatch = "<" + senderTagType + "=" + sender.getUniqueId() + ":" + Registry.ID_ESCAPE_PATTERN.matcher(tagStyle).replaceAll("\\>") + ":>";
                    message = message.replace(name, uuidmatch);
                }
                List<UUID> players = new ArrayList<>();
                ICPlayerFactory.getOnlineICPlayers().forEach(each -> players.add(each.getUniqueId()));
                for (UUID uuid : players) {
                    if (!uuid.equals(sender.getUniqueId())) {
                        InteractiveChat.mentionPair.add(new MentionPair(sender.getUniqueId(), uuid));
                        if (InteractiveChat.bungeecordMode) {
                            try {
                                BungeeMessageSender.forwardMentionPair(System.currentTimeMillis(), sender.getUniqueId(), uuid);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                return message;
            }
        }
        return null;
    }

    public static void translateAltColorCode(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (PlayerUtils.hasPermission(player.getUniqueId(), "interactivechat.chatcolor.translate", false, 200)) {
            if (InteractiveChat.chatAltColorCode.isPresent()) {
                event.setMessage(ChatColorUtils.translateAlternateColorCodes(InteractiveChat.chatAltColorCode.get(), event.getMessage()));
            }
        } else {
            event.setMessage(ChatColorUtils.escapeColorCharacters(InteractiveChat.chatAltColorCode.orElse(' '), event.getMessage()));
        }
    }

    public static void translateAltColorCode(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (PlayerUtils.hasPermission(player.getUniqueId(), "interactivechat.chatcolor.translate", false, 200)) {
            if (InteractiveChat.chatAltColorCode.isPresent()) {
                String translated = ChatColorUtils.translateAlternateColorCodes(InteractiveChat.chatAltColorCode.get(), event.getMessage());
                event.setMessage(translated.substring(translated.indexOf("/")));
            }
        } else {
            event.setMessage(ChatColorUtils.escapeColorCharacters(InteractiveChat.chatAltColorCode.orElse(' '), event.getMessage()));
        }
    }

}
