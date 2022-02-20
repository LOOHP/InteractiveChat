/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
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

import com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageSender;
import com.loohp.interactivechat.data.PlayerDataManager.PlayerData;
import com.loohp.interactivechat.objectholders.CooldownResult;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.objectholders.MentionPair;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.InventoryUtils;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.PlayerUtils;
import com.loohp.interactivechat.utils.TimeUtils;
import com.loohp.interactivechat.utils.XMaterialUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Events implements Listener {

    private static final Set<InventoryClickEvent> cancelledInventory = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommand(PlayerCommandPreprocessEvent event) {

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
                            if (icplaceholder.getKeyword().equals(InteractiveChat.itemPlaceholder) && !InteractiveChat.itemAirAllow && PlayerUtils.getHeldItem(event.getPlayer()).getType().equals(Material.AIR) && PlayerUtils.hasPermission(event.getPlayer().getUniqueId(), "interactivechat.module.item", false, 200)) {
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

                if (!Registry.ID_PATTERN.matcher(command).find()) {
                    for (ICPlaceholder icplaceholder : InteractiveChat.placeholderList.values()) {
                        Pattern placeholder = icplaceholder.getKeyword();
                        Matcher matcher = placeholder.matcher(command);
                        if (matcher.find()) {
                            int start = matcher.start();
                            if ((start < 1 || command.charAt(start - 1) != '\\') || (start > 1 && command.charAt(start - 1) == '\\' && command.charAt(start - 2) == '\\')) {
                                String uuidmatch = "<cmd=" + event.getPlayer().getUniqueId() + ":" + command.substring(matcher.start(), matcher.end()) + ">";
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

    @EventHandler(priority = EventPriority.HIGH)
    public void checkChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (InteractiveChat.chatManagerHook) {
            return;
        }

        checkChatMessage(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void checkChatForChatManagerOrTranslateChatColor(AsyncPlayerChatEvent event) {

        translateAltColorCode(event);

        if (event.isCancelled()) {
            return;
        }

        String processedMessage = checkMention(event);
        event.setMessage(processedMessage);

        if (!InteractiveChat.chatManagerHook) {
            return;
        }

        checkChatMessage(event);
    }

    private void checkChatMessage(AsyncPlayerChatEvent event) {
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
                    if (icplaceholder.getKeyword().equals(InteractiveChat.itemPlaceholder) && !InteractiveChat.itemAirAllow && PlayerUtils.getHeldItem(event.getPlayer()).getType().equals(Material.AIR) && PlayerUtils.hasPermission(event.getPlayer().getUniqueId(), "interactivechat.module.item", false, 200)) {
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
        } else if (count > 0) {
            if (InteractiveChat.useAccurateSenderFinder && !message.startsWith("/") && !Registry.ID_PATTERN.matcher(message).find()) {
                for (ICPlaceholder icplaceholder : InteractiveChat.placeholderList.values()) {
                    Pattern placeholder = icplaceholder.getKeyword();
                    Matcher matcher = placeholder.matcher(message);
                    if (matcher.find()) {
                        int start = matcher.start();
                        if ((start < 1 || message.charAt(start - 1) != '\\') || (start > 1 && message.charAt(start - 1) == '\\' && message.charAt(start - 2) == '\\')) {
                            String uuidmatch = "<chat=" + event.getPlayer().getUniqueId() + ":" + message.substring(matcher.start(), matcher.end()) + ">";
                            message = message.substring(0, matcher.start()) + uuidmatch + message.substring(matcher.end());
                            break;
                        }
                    }
                }
            }
        }

        event.setMessage(message);

        String mapKey = ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', event.getMessage()));
        InteractiveChat.messages.put(mapKey, player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> InteractiveChat.messages.remove(mapKey), 60);

        if (InteractiveChat.bungeecordMode) {
            try {
                BungeeMessageSender.addMessage(System.currentTimeMillis(), ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', event.getMessage())), event.getPlayer().getUniqueId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String checkMention(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String message = event.getMessage();
        PlayerData data = InteractiveChat.playerDataManager.getPlayerData(sender);
        if (InteractiveChat.allowMention && (data == null || !data.isMentionDisabled())) {
            String processedMessage;
            if (!InteractiveChat.disableEveryone && (processedMessage = checkMentionEveryone(message, sender)) != null) {
                return processedMessage;
            }
            if (!InteractiveChat.disableHere && (processedMessage = checkMentionHere(message, sender)) != null) {
                return processedMessage;
            }
            if ((processedMessage = checkMentionPlayers(message, sender)) != null) {
                return processedMessage;
            }
        }
        return message;
    }

    private String checkMentionPlayers(String message, Player sender) {
        if (PlayerUtils.hasPermission(sender.getUniqueId(), "interactivechat.mention.player", false, 200)) {
            Map<String, UUID> playernames = new HashMap<>();
            for (ICPlayer player : ICPlayerFactory.getOnlineICPlayers()) {
                playernames.put(ChatColorUtils.stripColor(player.getName()), player.getUniqueId());
                if (InteractiveChat.useBukkitDisplayName && !ChatColorUtils.stripColor(player.getName()).equals(ChatColorUtils.stripColor(player.getDisplayName()))) {
                    playernames.put(ChatColorUtils.stripColor(player.getDisplayName()), player.getUniqueId());
                }
                List<String> names = InteractiveChatAPI.getNicknames(player.getUniqueId());
                for (String name : names) {
                    playernames.put(ChatColorUtils.stripColor(name), player.getUniqueId());
                }
            }
            for (Entry<String, UUID> entry : playernames.entrySet()) {
                String name = InteractiveChat.mentionPrefix + entry.getKey();
                UUID uuid = entry.getValue();
                int index = message.toLowerCase().indexOf(name.toLowerCase());
                if (index >= 0) {
                    message = Registry.MENTION_TAG_CONVERTER.convertToTag(name, message);
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

    private String checkMentionHere(String message, Player sender) {
        if (PlayerUtils.hasPermission(sender.getUniqueId(), "interactivechat.mention.here", false, 200)) {
            String name = InteractiveChat.mentionPrefix + "here";
            int index = message.toLowerCase().indexOf(name.toLowerCase());
            if (index >= 0) {
                message = Registry.MENTION_TAG_CONVERTER.convertToTag(name, message);
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

    private String checkMentionEveryone(String message, Player sender) {
        if (PlayerUtils.hasPermission(sender.getUniqueId(), "interactivechat.mention.everyone", false, 200)) {
            String name = InteractiveChat.mentionPrefix + "everyone";
            int index = message.toLowerCase().indexOf(name.toLowerCase());
            if (index >= 0) {
                message = Registry.MENTION_TAG_CONVERTER.convertToTag(name, message);
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

    private void translateAltColorCode(AsyncPlayerChatEvent event) {
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

    private void translateAltColorCode(PlayerCommandPreprocessEvent event) {
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getClickedInventory().getType().equals(InventoryType.CREATIVE)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        String hash = InteractiveChat.viewingInv1.get(player.getUniqueId());
        if (hash != null) {
            Inventory fakeInv = InteractiveChat.inventoryDisplay1Lower.get(hash);
            if (fakeInv == null) {
                Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> player.closeInventory());
            } else {
                Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> InventoryUtils.sendFakePlayerInventory(player, fakeInv, true, false));
            }
        }
        if (event.getView().getTopInventory() == null) {
            return;
        }
        Inventory topInventory = event.getView().getTopInventory();
        if (InteractiveChat.containerDisplay.contains(topInventory) || InteractiveChat.upperSharedInventory.contains(topInventory)) {

            event.setCancelled(true);
            cancelledInventory.add(event);

            if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                ItemStack item = event.getCurrentItem();
                inventoryAction(item, player, topInventory);
            } else if (InteractiveChat.viewingInv1.containsKey(player.getUniqueId())) {
                ItemStack item;
                if (event.getClickedInventory().equals(topInventory)) {
                    item = event.getCurrentItem();
                } else {
                    int rawSlot = event.getRawSlot();
                    int slot;
                    if (rawSlot < 81) {
                        slot = rawSlot - 45;
                    } else {
                        slot = rawSlot - 81;
                    }
                    Inventory bottomInventory = InteractiveChat.inventoryDisplay1Lower.get(hash);
                    if (bottomInventory != null) {
                        item = bottomInventory.getItem(slot);
                    } else {
                        item = null;
                    }
                }
                inventoryAction(item, player, topInventory);
            }
        }
    }

    private void inventoryAction(ItemStack item, Player player, Inventory topInventory) {
        if (item != null) {
            XMaterial xmaterial = XMaterialUtils.matchXMaterial(item);
            if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_14)) {
                if (xmaterial.equals(XMaterial.WRITTEN_BOOK)) {
                    player.openBook(item.clone());
                } else if (xmaterial.equals(XMaterial.WRITABLE_BOOK)) {
                    ItemStack book = XMaterial.WRITTEN_BOOK.parseItem();
                    if (book != null && book.getItemMeta() instanceof BookMeta) {
                        BookMeta ori = (BookMeta) item.getItemMeta();
                        BookMeta dis = (BookMeta) book.getItemMeta();
                        List<BaseComponent[]> pages = new ArrayList<>(ori.spigot().getPages());
                        if (pages.isEmpty()) {
                            dis.setPages(" ");
                        } else {
                            dis.spigot().setPages(pages);
                        }
                        dis.setTitle("Temp Book");
                        dis.setAuthor("InteractiveChat");
                        book.setItemMeta(dis);
                        player.openBook(book);
                    }
                }
            }
            if (!InteractiveChat.containerDisplay.contains(topInventory) && item.hasItemMeta() && item.getItemMeta() instanceof BlockStateMeta) {
                BlockState bsm = ((BlockStateMeta) item.getItemMeta()).getBlockState();
                if (bsm instanceof InventoryHolder) {
                    Inventory container = ((InventoryHolder) bsm).getInventory();
                    if ((container.getSize() % 9) == 0) {
                        Inventory displayInventory = Bukkit.createInventory(null, container.getSize() + 9, InteractiveChat.containerViewTitle);
                        ItemStack empty = InteractiveChat.itemFrame1.clone();
                        if (item.getType().equals(InteractiveChat.itemFrame1.getType())) {
                            empty = InteractiveChat.itemFrame2.clone();
                        }
                        ItemMeta emptyMeta = empty.getItemMeta();
                        emptyMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "");
                        empty.setItemMeta(emptyMeta);
                        for (int j = 0; j < 9; j++) {
                            displayInventory.setItem(j, empty);
                        }
                        displayInventory.setItem(4, item);
                        for (int i = 0; i < container.getSize(); i++) {
                            ItemStack containerItem = container.getItem(i);
                            displayInventory.setItem(i + 9, containerItem == null ? null : containerItem.clone());
                        }

                        InteractiveChat.containerDisplay.add(displayInventory);
                        Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> player.openInventory(displayInventory), 2);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClickHighest(InventoryClickEvent event) {
        if (cancelledInventory.remove(event)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory != null) {
            InteractiveChat.containerDisplay.remove(topInventory);
        }
        Player player = (Player) event.getPlayer();
        if (InteractiveChat.viewingInv1.remove(player.getUniqueId()) != null) {
            InventoryUtils.restorePlayerInventory(player);
        }
    }

}
