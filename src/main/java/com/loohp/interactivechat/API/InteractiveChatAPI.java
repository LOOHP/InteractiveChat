package com.loohp.interactivechat.API;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.collect.BiMap;
import com.google.common.collect.Maps;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.ObjectHolders.ICPlaceholder;
import com.loohp.interactivechat.ObjectHolders.SharedDisplayTimeoutInfo;
import com.loohp.interactivechat.Utils.MCVersion;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class InteractiveChatAPI {
	
	/**
	 * Send a message to a {@link CommandSender} that won't be processed by InteractiveChat
	 * @param sender
	 * @param message
	 */
	public static void sendMessageUnprocessed(CommandSender sender, String message) {
		sendMessageUnprocessed(sender, new UUID(0, 0), message);
	}
	
	/**
	 * Send a message to a {@link CommandSender} that won't be processed by InteractiveChat
	 * @param sender
	 * @param component
	 */
	public static void sendMessageUnprocessed(CommandSender sender, BaseComponent component) {
		sendMessageUnprocessed(sender, new UUID(0, 0), component);
	}

	/**
	 * Send a message to a {@link CommandSender} that won't be processed by InteractiveChat
	 * @param sender
	 * @param component
	 */
	public static void sendMessageUnprocessed(CommandSender sender, BaseComponent[] component) {
		sendMessageUnprocessed(sender, new UUID(0, 0), component);
	}
	
	/**
	 * Send a message to a {@link CommandSender} that won't be processed by InteractiveChat
	 * @param sender
	 * @param uuid
	 * @param message
	 */
	public static void sendMessageUnprocessed(CommandSender sender, UUID uuid, String message) {
		sendMessageUnprocessed(sender, uuid, new TextComponent(message));
	}
	
	/**
	 * Send a message to a {@link CommandSender} that won't be processed by InteractiveChat
	 * @param sender
	 * @param uuid
	 * @param component
	 */
	public static void sendMessageUnprocessed(CommandSender sender, UUID uuid, BaseComponent component) {
		sendMessageUnprocessed(sender, uuid, new BaseComponent[] {component});
	}

	/**
	 * Send a message to a {@link CommandSender} that won't be processed by InteractiveChat
	 * @param sender
	 * @param uuid
	 * @param component
	 */
	public static void sendMessageUnprocessed(CommandSender sender, UUID uuid, BaseComponent[] component) {
		if (sender instanceof Player) {
			String json = ComponentSerializer.toString(component);
			PacketContainer packet = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.CHAT);
			if (!InteractiveChat.version.isLegacy() || InteractiveChat.version.equals(MCVersion.V1_12)) {
		        packet.getChatTypes().write(0, ChatType.SYSTEM);
	        } else {
	        	packet.getBytes().write(0, (byte) 1);
	        }
			packet.getChatComponents().write(0, WrappedChatComponent.fromJson(json));
			if (packet.getUUIDs().size() > 0) {
				packet.getUUIDs().write(0, uuid);
			}
			try {
				InteractiveChat.protocolManager.sendServerPacket((Player) sender, packet, false);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			sender.spigot().sendMessage(component);
		}
	}
	
	/**
	 * Get the placeholder keyword list
	 * @return The placeholder keyword list
	 */
	public static List<String> getPlaceholderList() {
		return InteractiveChat.placeholderList.stream().map(each -> each.getKeyword()).collect(Collectors.toList());
	}
	
	/**
	 * Get the placeholder list
	 * @return The placeholder list
	 */
	public static List<ICPlaceholder> getICPlaceholderList() {
		return new ArrayList<>(InteractiveChat.placeholderList);
	}
	
	/**
	 * Get the mention cooldown for the provided player
	 * @param player
	 * @return A unix timestamp
	 */
	public static long getPlayerMentionCooldown(Player player) {
		if (InteractiveChat.mentionCooldown.containsKey(player)) {
			return InteractiveChat.mentionCooldown.get(player);
		}
		return -1;
	}
	
	/**
	 * Set the mention cooldown for the provided player
	 * @param player
	 * @param time
	 */
	public static void setPlayerMentionCooldown(Player player, long time) {
		InteractiveChat.mentionCooldown.put(player, time);
	}
	
	/**
	 * Get all mention cooldowns
	 * @return A map of mention cooldowns
	 */
	public static Map<Player, Long> getMentionCooldownMap() {
		return new HashMap<>(InteractiveChat.mentionCooldown);
	}
	
	/**
	 * Get the cooldown of a specific placeholder for a player
	 * @param player
	 * @param placeholder
	 * @return A unix timestamp
	 */
	public static long getPlayerPlaceholderCooldown(Player player, String placeholder) {
		return getPlayerPlaceholderCooldown(player.getUniqueId(), placeholder);
	}
	
	/**
	 * Get the cooldown of a specific placeholder for a player
	 * @param uuid
	 * @param placeholder
	 * @return A unix timestamp
	 */
	public static long getPlayerPlaceholderCooldown(UUID uuid, String placeholder) {
		if (InteractiveChat.placeholderCooldowns.containsKey(uuid)) {
			if (InteractiveChat.placeholderCooldowns.get(uuid).containsKey(placeholder)) {
				return InteractiveChat.placeholderCooldowns.get(uuid).get(placeholder);
			}
		}
		return -1;
	}
	
	/**
	 * Set the cooldown of a specific placeholder for a player
	 * @param player
	 * @param placeholder
	 * @param time
	 */
	public static void setPlayerPlaceholderCooldown(Player player, String placeholder, long time) {
		setPlayerPlaceholderCooldown(player.getUniqueId(), placeholder, time);
	}
	
	/**
	 * Set the cooldown of a specific placeholder for a player
	 * @param uuid
	 * @param placeholder
	 * @param time
	 */
	public static void setPlayerPlaceholderCooldown(UUID uuid, String placeholder, long time) {
		if (!InteractiveChat.placeholderCooldowns.containsKey(uuid)) {
			InteractiveChat.placeholderCooldowns.put(uuid, new ConcurrentHashMap<String, Long>());
		}
		InteractiveChat.placeholderCooldowns.get(uuid).put(placeholder, time);
	}
	
	/**
	 * Get the universal cooldown for a player
	 * @param player
	 * @return A unix timestamp
	 */
	public static long getPlayerUniversalCooldown(Player player) {
		return getPlayerUniversalCooldown(player.getUniqueId());
	}
	
	/**
	 * Get the universal cooldown for a player
	 * @param uuid
	 * @return A unix timestamp
	 */
	public static long getPlayerUniversalCooldown(UUID uuid) {
		if (InteractiveChat.universalCooldowns.containsKey(uuid)) {
			return InteractiveChat.universalCooldowns.get(uuid);
		}
		return -1;
	}
	
	/**
	 * Set the universal cooldown for a player
	 * @param player
	 * @param time
	 */
	public static void setPlayerUniversalCooldown(Player player, long time) {
		setPlayerUniversalCooldown(player.getUniqueId(), time);
	}
	
	/**
	 * Set the universal cooldown for a player
	 * @param uuid
	 * @param time
	 */
	public static void setPlayerUniversalCooldown(UUID uuid, long time) {
		InteractiveChat.universalCooldowns.put(uuid, time);
	}
	
	/**
	 * Whether a placeholder is on cooldown for a player
	 * @param player
	 * @param placeholder
	 * @return True/False
	 */
	public static boolean isPlaceholderOnCooldown(Player player, String placeholder) {
		long unix = System.currentTimeMillis();
		return isPlaceholderOnCooldown(player, placeholder, unix);
	}
	
	/**
	 * Whether a placeholder is on cooldown for a player
	 * @param uuid
	 * @param placeholder
	 * @return True/False
	 */
	public static boolean isPlaceholderOnCooldown(UUID uuid, String placeholder) {
		long unix = System.currentTimeMillis();
		return isPlaceholderOnCooldown(uuid, placeholder, unix);
	}
	
	/**
	 * Whether a placeholder is on cooldown for a player at a given time
	 * @param player
	 * @param placeholder
	 * @param time
	 * @return True/False
	 */
	public static boolean isPlaceholderOnCooldown(Player player, String placeholder, long time) {
		return isPlaceholderOnCooldown(player.getUniqueId(), placeholder, time);
	}
	
	/**
	 * Whether a placeholder is on cooldown for a player at a given time
	 * @param uuid
	 * @param placeholder
	 * @param time
	 * @return True/False
	 */
	public static boolean isPlaceholderOnCooldown(UUID uuid, String placeholder, long time) {
		if (InteractiveChat.universalCooldowns.containsKey(uuid)) {
			if (InteractiveChat.universalCooldowns.get(uuid) > time) {
				return true;
			}
		}
		if (InteractiveChat.placeholderCooldowns.containsKey(uuid)) {
			if (InteractiveChat.placeholderCooldowns.get(uuid).containsKey(placeholder)) {
				if (InteractiveChat.placeholderCooldowns.get(uuid).get(placeholder) > time) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static enum SharedType {
		ITEM,
		INVENTORY,
		INVENTORY1_UPPER,
		INVENTORY1_LOWER,
		ENDERCHEST;
	}
	
	/**
	 * Get the shared inventory list
	 * @param type
	 * @return The shared inventory list
	 */
	public static BiMap<String, Inventory> getItemShareList(SharedType type) {
		switch (type) {
		case ITEM:
			return Maps.unmodifiableBiMap(InteractiveChat.itemDisplay);
		case INVENTORY:
			return Maps.unmodifiableBiMap(InteractiveChat.inventoryDisplay);
		case INVENTORY1_UPPER:
			return Maps.unmodifiableBiMap(InteractiveChat.inventoryDisplay1Upper);
		case INVENTORY1_LOWER:
			return Maps.unmodifiableBiMap(InteractiveChat.inventoryDisplay1Lower);
		case ENDERCHEST:
			return Maps.unmodifiableBiMap(InteractiveChat.enderDisplay);
		}
		return null;
	}
		
	/**
	 * Get the shared map list
	 * @return The shared map list
	 */
	public static Map<String, ItemStack> getMapShareList() {
		return Collections.unmodifiableMap(InteractiveChat.mapDisplay);
	}
	
	/**
	 * Add an inventory to the shared inventory list
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
			InteractiveChat.itemDisplayTimeouts.add(new SharedDisplayTimeoutInfo(hash, 0, System.currentTimeMillis() + InteractiveChat.itemDisplayTimeout));
			break;
		case INVENTORY:
			InteractiveChat.inventoryDisplay.put(hash, inventory);
			InteractiveChat.itemDisplayTimeouts.add(new SharedDisplayTimeoutInfo(hash, 1, System.currentTimeMillis() + InteractiveChat.itemDisplayTimeout));
			break;
		case INVENTORY1_UPPER:
			InteractiveChat.inventoryDisplay1Upper.put(hash, inventory);
			InteractiveChat.itemDisplayTimeouts.add(new SharedDisplayTimeoutInfo(hash, 2, System.currentTimeMillis() + InteractiveChat.itemDisplayTimeout));
			break;
		case INVENTORY1_LOWER:
			InteractiveChat.inventoryDisplay1Lower.put(hash, inventory);
			InteractiveChat.itemDisplayTimeouts.add(new SharedDisplayTimeoutInfo(hash, 3, System.currentTimeMillis() + InteractiveChat.itemDisplayTimeout));
			break;
		case ENDERCHEST:
			InteractiveChat.enderDisplay.put(hash, inventory);
			InteractiveChat.itemDisplayTimeouts.add(new SharedDisplayTimeoutInfo(hash, 4, System.currentTimeMillis() + InteractiveChat.itemDisplayTimeout));
			break;
		}
		return hash;
	}
	
	/**
	 * Add a map to the shared map list
	 * @param hash key
	 * @param item
	 * @return The hashed key which can be used to retrieve the inventory
	 */
	public static String addMapToMapSharedList(String hash, ItemStack item) {
		InteractiveChat.mapDisplay.put(hash, item);
		InteractiveChat.itemDisplayTimeouts.add(new SharedDisplayTimeoutInfo(hash, 5, System.currentTimeMillis() + InteractiveChat.itemDisplayTimeout));
		return hash;
	}
	
	/**
	 * Register a function that the plugin will fetch nicknames from when it is needed.
	 * @param plugin
	 * @param provider
	 */
	public static void registerNicknameProvider(Plugin plugin, Function<UUID, List<String>> provider) {
		InteractiveChat.pluginNicknames.put(plugin, provider);
	}
	
	/**
	 * Unregister the nickname provider of the provided plugin
	 * @param plugin
	 */
	public static void unregisterNicknameProvider(Plugin plugin) {
		InteractiveChat.pluginNicknames.remove(plugin);
	}
	
	/**
	 * Get the plugins registered to provide nicknames
	 * @return A set of registered plguins
	 */
	public static Set<Plugin> getRegisteredNicknameProviders() {
		return Collections.unmodifiableSet(InteractiveChat.pluginNicknames.keySet());
	}
	
	/**
	 * Get the nickname function provided by the provided plugin
	 * @param plugin
	 * @return The function which returns the list of plugins provided by this plugin
	 */
	public static Function<UUID, List<String>> getNicknameProvider(Plugin plugin) {
		return InteractiveChat.pluginNicknames.get(plugin);
	}
	
	/**
	 * Get all plugin provided nicknames of the provided player, can return an empty {@link List}
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
	 * @param uuid, predicate
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
}
