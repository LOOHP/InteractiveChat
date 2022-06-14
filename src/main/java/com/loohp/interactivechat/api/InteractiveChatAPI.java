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

package com.loohp.interactivechat.api;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageSender;
import com.loohp.interactivechat.modules.ItemDisplay;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.objectholders.OfflineICPlayer;
import com.loohp.interactivechat.objectholders.PlaceholderCooldownManager;
import com.loohp.interactivechat.objectholders.ValueTrios;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.MCVersion;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InteractiveChatAPI {

    /**
     * Send a message to a {@link CommandSender} that won't be processed by InteractiveChat
     *
     * @param sender
     * @param message
     */
    public static void sendMessageUnprocessed(CommandSender sender, String message) {
        sendMessageUnprocessed(sender, new UUID(0, 0), message);
    }

    /**
     * Send a message to a {@link CommandSender} that won't be processed by InteractiveChat
     *
     * @param sender
     * @param component
     */
    public static void sendMessageUnprocessed(CommandSender sender, Component component) {
        sendMessageUnprocessed(sender, new UUID(0, 0), component);
    }

    /**
     * Send a message to a {@link CommandSender} that won't be processed by InteractiveChat
     *
     * @param sender
     * @param uuid
     * @param message
     */
    public static void sendMessageUnprocessed(CommandSender sender, UUID uuid, String message) {
        sendMessageUnprocessed(sender, uuid, Component.text(message));
    }

    /**
     * Send a message to a {@link CommandSender} that won't be processed by InteractiveChat
     *
     * @param sender
     * @param uuid
     * @param component
     */
    public static void sendMessageUnprocessed(CommandSender sender, UUID uuid, Component component) {
        String json = InteractiveChatComponentSerializer.gson().serialize(component);
        if (sender instanceof Player) {
            PacketContainer packet;
            if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19)) {
                packet = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.SYSTEM_CHAT);
                packet.getStrings().write(0, json);
                packet.getIntegers().write(0, 1);
            } else {
                packet = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.CHAT);
                if (!InteractiveChat.version.isLegacy() || InteractiveChat.version.equals(MCVersion.V1_12)) {
                    packet.getChatTypes().write(0, ChatType.SYSTEM);
                } else {
                    packet.getBytes().write(0, (byte) 1);
                }
                packet.getChatComponents().write(0, WrappedChatComponent.fromJson(json));
                if (packet.getUUIDs().size() > 0) {
                    packet.getUUIDs().write(0, uuid);
                }
            }
            InteractiveChat.protocolManager.sendServerPacket((Player) sender, packet, false);
        } else {
            sender.spigot().sendMessage(ComponentSerializer.parse(json));
        }
    }

    /**
     * Send an adventure component message
     *
     * @param receiver
     * @param component
     */
    public static void sendMessage(CommandSender receiver, Component component) {
        InteractiveChat.sendMessage(receiver, component);
    }

    /**
     * Marks a message with a tag that InteractiveChat understands which identifies the sender of the message.<br>
     * Only have an effect if UseAccurateSenderParser is enabled in the config.
     *
     * @param message
     * @param sender The {@link UUID} of the {@link Player} or {@link ICPlayer}
     * @return the sender marked message
     * @throws IllegalStateException if a sender is already marked in the given message
     */
    public static String markSender(String message, UUID sender) {
        if (InteractiveChat.useAccurateSenderFinder) {
            if (Registry.ID_PATTERN.matcher(message).find()) {
                throw new IllegalStateException("Sender is already marked in the given message: " + message);
            }
            for (ICPlaceholder icplaceholder : InteractiveChat.placeholderList.values()) {
                Pattern placeholder = icplaceholder.getKeyword();
                Matcher matcher = placeholder.matcher(message);
                if (matcher.find()) {
                    int start = matcher.start();
                    if ((start < 1 || message.charAt(start - 1) != '\\') || (start > 1 && message.charAt(start - 1) == '\\' && message.charAt(start - 2) == '\\')) {
                        String uuidmatch = "<chat=" + sender + ":" + Registry.ID_ESCAPE_PATTERN.matcher(message.substring(matcher.start(), matcher.end())).replaceAll("\\>") + ":>";
                        message = message.substring(0, matcher.start()) + uuidmatch + message.substring(matcher.end());
                        break;
                    }
                }
            }
        }
        return message;
    }

    /**
     * Create a {@link Component} for use containing the item display
     *
     * @param player
     * @param item
     * @return The item display component
     * @throws Exception
     */
    public static Component createItemDisplayComponent(Player player, ItemStack item) throws Exception {
        return ItemDisplay.createItemDisplay(ICPlayerFactory.getICPlayer(player), item);
    }

    /**
     * Get the placeholder keyword list
     *
     * @return The placeholder keyword list
     */
    public static List<Pattern> getPlaceholderList() {
        return InteractiveChat.placeholderList.values().stream().map(each -> each.getKeyword()).collect(Collectors.toList());
    }

    /**
     * Get the placeholder list
     *
     * @return The placeholder list
     */
    public static List<ICPlaceholder> getICPlaceholderList() {
        return new ArrayList<>(InteractiveChat.placeholderList.values());
    }

    /**
     * Get the cooldown of a specific placeholder for a player
     *
     * @param player
     * @param placeholder
     * @return A unix timestamp
     */
    public static long getPlayerPlaceholderCooldown(Player player, ICPlaceholder placeholder) {
        return getPlayerPlaceholderCooldown(player.getUniqueId(), placeholder);
    }

    /**
     * Get the cooldown of a specific placeholder for a player
     *
     * @param uuid
     * @param placeholder
     * @return A unix timestamp
     */
    public static long getPlayerPlaceholderCooldown(UUID uuid, ICPlaceholder placeholder) {
        return InteractiveChat.placeholderCooldownManager.getPlayerPlaceholderLastTimestamp(uuid, placeholder);
    }

    /**
     * Set the cooldown of a specific placeholder for a player
     *
     * @param player
     * @param placeholder
     * @param time
     */
    public static void setPlayerPlaceholderCooldown(Player player, ICPlaceholder placeholder, long time) {
        setPlayerPlaceholderCooldown(player.getUniqueId(), placeholder, time);
    }

    /**
     * Set the cooldown of a specific placeholder for a player
     *
     * @param uuid
     * @param placeholder
     * @param time
     */
    public static void setPlayerPlaceholderCooldown(UUID uuid, ICPlaceholder placeholder, long time) {
        InteractiveChat.placeholderCooldownManager.setPlayerPlaceholderLastTimestamp(uuid, placeholder, time);
    }

    /**
     * Get the universal cooldown for a player
     *
     * @param player
     * @return A unix timestamp
     */
    public static long getPlayerUniversalCooldown(Player player) {
        return getPlayerUniversalCooldown(player.getUniqueId());
    }

    /**
     * Get the universal cooldown for a player
     *
     * @param uuid
     * @return A unix timestamp
     */
    public static long getPlayerUniversalCooldown(UUID uuid) {
        return InteractiveChat.placeholderCooldownManager.getPlayerUniversalLastTimestamp(uuid);
    }

    /**
     * Set the universal cooldown for a player
     *
     * @param player
     * @param time
     */
    public static void setPlayerUniversalCooldown(Player player, long time) {
        setPlayerUniversalCooldown(player.getUniqueId(), time);
    }

    /**
     * Set the universal cooldown for a player
     *
     * @param uuid
     * @param time
     */
    public static void setPlayerUniversalCooldown(UUID uuid, long time) {
        InteractiveChat.placeholderCooldownManager.setPlayerUniversalLastTimestamp(uuid, time);
    }

    /**
     * Whether a placeholder is on cooldown for a player
     *
     * @param player
     * @param placeholder
     * @return True/False
     */
    public static boolean isPlaceholderOnCooldown(Player player, ICPlaceholder placeholder) {
        return isPlaceholderOnCooldown(player, placeholder, System.currentTimeMillis());
    }

    /**
     * Whether a placeholder is on cooldown for a player
     *
     * @param uuid
     * @param placeholder
     * @return True/False
     */
    public static boolean isPlaceholderOnCooldown(UUID uuid, ICPlaceholder placeholder) {
        return isPlaceholderOnCooldown(uuid, placeholder, System.currentTimeMillis());
    }

    /**
     * Whether a placeholder is on cooldown for a player at a given time
     *
     * @param player
     * @param placeholder
     * @param time
     * @return True/False
     */
    public static boolean isPlaceholderOnCooldown(Player player, ICPlaceholder placeholder, long time) {
        return isPlaceholderOnCooldown(player.getUniqueId(), placeholder, time);
    }

    /**
     * Whether a placeholder is on cooldown for a player at a given time
     *
     * @param uuid
     * @param placeholder
     * @param time
     * @return True/False
     */
    public static boolean isPlaceholderOnCooldown(UUID uuid, ICPlaceholder placeholder, long time) {
        return InteractiveChat.placeholderCooldownManager.isPlaceholderOnCooldownAt(uuid, placeholder, time);
    }

    /**
     * Get the placeholder cooldown manager
     *
     * @return PlaceholderCooldownManager
     */
    public static PlaceholderCooldownManager getPlaceholderCooldownManager() {
        return InteractiveChat.placeholderCooldownManager;
    }

    /**
     * Get the shared inventory list
     *
     * @param type
     * @return The shared inventory list
     */
    public static Map<String, Inventory> getItemShareList(SharedType type) {
        switch (type) {
            case ITEM:
                return Collections.unmodifiableMap(InteractiveChat.itemDisplay);
            case INVENTORY:
                return Collections.unmodifiableMap(InteractiveChat.inventoryDisplay);
            case INVENTORY1_UPPER:
                return Collections.unmodifiableMap(InteractiveChat.inventoryDisplay1Upper);
            case INVENTORY1_LOWER:
                return Collections.unmodifiableMap(InteractiveChat.inventoryDisplay1Lower);
            case ENDERCHEST:
                return Collections.unmodifiableMap(InteractiveChat.enderDisplay);
        }
        return null;
    }

    /**
     * Get the shared map list
     *
     * @return The shared map list
     */
    public static Map<String, ItemStack> getMapShareList() {
        return Collections.unmodifiableMap(InteractiveChat.mapDisplay);
    }

    /**
     * Add an inventory to the shared inventory list
     *
     * @param type
     * @param hash key
     * @param inventory
     * @return The hashed key which can be used to retrieve the inventory
     * @throws Exception
     */
    public static String addInventoryToItemShareList(SharedType type, String hash, Inventory inventory) throws Exception {
        switch (type) {
            case ITEM:
                InteractiveChat.itemDisplay.put(hash, inventory);
                InteractiveChat.upperSharedInventory.add(inventory);
                break;
            case INVENTORY:
                InteractiveChat.inventoryDisplay.put(hash, inventory);
                InteractiveChat.upperSharedInventory.add(inventory);
                break;
            case INVENTORY1_UPPER:
                InteractiveChat.inventoryDisplay1Upper.put(hash, inventory);
                InteractiveChat.upperSharedInventory.add(inventory);
                break;
            case INVENTORY1_LOWER:
                InteractiveChat.inventoryDisplay1Lower.put(hash, inventory);
                InteractiveChat.lowerSharedInventory.add(inventory);
                break;
            case ENDERCHEST:
                InteractiveChat.enderDisplay.put(hash, inventory);
                InteractiveChat.upperSharedInventory.add(inventory);
                break;
        }
        return hash;
    }

    /**
     * Add a map to the shared map list
     *
     * @param hash key
     * @param item
     * @return The hashed key which can be used to retrieve the inventory
     */
    public static String addMapToMapSharedList(String hash, ItemStack item) {
        InteractiveChat.mapDisplay.put(hash, item);
        return hash;
    }

    /**
     * Register a function that the plugin will fetch nicknames from when it is needed.
     *
     * @param plugin
     * @param provider
     */
    public static void registerNicknameProvider(Plugin plugin, Function<UUID, List<String>> provider) {
        InteractiveChat.pluginNicknames.put(plugin, provider);
    }

    /**
     * Unregister the nickname provider of the provided plugin
     *
     * @param plugin
     */
    public static void unregisterNicknameProvider(Plugin plugin) {
        InteractiveChat.pluginNicknames.remove(plugin);
    }

    /**
     * Get the plugins registered to provide nicknames
     *
     * @return A set of registered plugins
     */
    public static Set<Plugin> getRegisteredNicknameProviders() {
        return Collections.unmodifiableSet(InteractiveChat.pluginNicknames.keySet());
    }

    /**
     * Get the nickname function provided by the provided plugin
     *
     * @param plugin
     * @return The function which returns the list of plugins provided by this plugin
     */
    public static Function<UUID, List<String>> getNicknameProvider(Plugin plugin) {
        return InteractiveChat.pluginNicknames.get(plugin);
    }

    /**
     * Get all plugin provided nicknames of the provided player, can return an empty {@link List}
     *
     * @param uuid
     * @return A list of nicknames
     */
    public static List<String> getNicknames(UUID uuid) {
        List<String> nicks = new ArrayList<>();
        for (Entry<Plugin, Function<UUID, List<String>>> entry : InteractiveChat.pluginNicknames.entrySet()) {
            try {
                List<String> names = entry.getValue().apply(uuid);
                if (names != null) {
                    nicks.addAll(names);
                }
            } catch (Throwable e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] " + entry.getKey().getName() + " " + entry.getKey().getDescription().getVersion() + " threw an error while providing registered nicknames.");
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Unless this is Essentials, please contact that plugin's developer for support");
                e.printStackTrace();
            }
        }
        return nicks;
    }

    /**
     * Get all plugin provided nicknames of the provided player, can return an empty {@link List}
     *
     * @param uuid
     * @param predicate
     * @return A list of nicknames
     */
    public static List<String> getNicknames(UUID uuid, Predicate<String> predicate) {
        List<String> nicks = new ArrayList<>();
        for (Entry<Plugin, Function<UUID, List<String>>> entry : InteractiveChat.pluginNicknames.entrySet()) {
            try {
                List<String> names = entry.getValue().apply(uuid);
                if (names != null) {
                    for (String name : names) {
                        if (predicate.test(name)) {
                            nicks.add(name);
                        }
                    }
                }
            } catch (Throwable e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] " + entry.getKey().getName() + " " + entry.getKey().getDescription().getVersion() + " threw an error while providing registered nicknames.");
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Unless this is Essentials, please contact that plugin's developer for support");
                e.printStackTrace();
            }
        }
        return nicks;
    }

    /**
     * Get player uuid, name, ping list from the proxy server
     *
     * @return A CompletableFuture<ValueTrios<UUID, String, Integer>> object
     */
    public static CompletableFuture<List<ValueTrios<UUID, String, Integer>>> getBungeecordPlayerList() {
        CompletableFuture<List<ValueTrios<UUID, String, Integer>>> future = new CompletableFuture<>();
        try {
            BungeeMessageSender.requestBungeePlayerlist(System.currentTimeMillis(), future);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    /**
     * Get all online {@link ICPlayer}
     *
     * @return A Collection of ICPlayers
     */
    public static Collection<ICPlayer> getOnlineICPlayers() {
        return ICPlayerFactory.getOnlineICPlayers();
    }

    /**
     * Get all online icplayers' uuid
     *
     * @return A set of uuid
     */
    public static Set<UUID> getOnlineUUIDs() {
        return ICPlayerFactory.getOnlineUUIDs();
    }

    /**
     * Get {@link ICPlayer} from a {@link Player}
     *
     * @param player
     * @return An ICPlayer
     */
    public static ICPlayer getICPlayer(Player player) {
        return ICPlayerFactory.getICPlayer(player);
    }

    /**
     * Get {@link ICPlayer} from a {@link UUID}
     *
     * @param uuid
     * @return An ICPlayer or null if not found
     */
    public static ICPlayer getICPlayer(UUID uuid) {
        return ICPlayerFactory.getICPlayer(uuid);
    }

    /**
     * Get {@link ICPlayer} with the given username
     *
     * @param name
     * @return An ICPlayer or null if not found
     */
    public static ICPlayer getICPlayer(String name) {
        return ICPlayerFactory.getICPlayer(name);
    }

    /**
     * Get {@link ICPlayer} with the exact given name, case-insensitive.
     *
     * @param name
     * @return An ICPlayer or null if not found
     */
    public static ICPlayer getICPlayerExact(String name) {
        return ICPlayerFactory.getICPlayerExact(name);
    }

    /**
     * Get {@link OfflineICPlayer} from a {@link UUID}
     * If the offline player has never joined the server, null may be returned.
     *
     * @param uuid
     * @return An OfflineICPlayer or null if not found
     */
    public static OfflineICPlayer getOfflineICPlayer(UUID uuid) {
        return ICPlayerFactory.getOfflineICPlayer(uuid);
    }

    /**
     * Get {@link OfflineICPlayer} from the given username
     * This method may involve a blocking web request to get the UUID for the given name.
     * If the offline player has never joined the server, null may be returned.
     *
     * @param name
     * @return An OfflineICPlayer or null if not found
     */
    @Deprecated
    public static OfflineICPlayer getOfflineICPlayer(String name) {
        return ICPlayerFactory.getOfflineICPlayer(name);
    }

    public enum SharedType {

        ITEM(0),
        INVENTORY(1),
        INVENTORY1_UPPER(2),
        INVENTORY1_LOWER(3),
        ENDERCHEST(4);

        private static final Map<Integer, SharedType> MAPPINGS = new HashMap<>();

        static {
            for (SharedType type : values()) {
                MAPPINGS.put(type.getValue(), type);
            }
        }

        public static SharedType fromValue(int value) {
            return MAPPINGS.get(value);
        }

        private final int value;

        SharedType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

}
