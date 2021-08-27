package com.loohp.interactivechat.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.listeners.ClientSettingPacket;
import com.loohp.interactivechat.objectholders.DummyPlayer;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerEquipment;
import com.loohp.interactivechat.objectholders.OfflineICPlayer;
import com.loohp.interactivechat.objectholders.PermissionCache;

import net.craftersland.data.bridge.PD;
import net.craftersland.data.bridge.objects.DatabaseEnderchestData;
import net.craftersland.data.bridge.objects.DatabaseExperienceData;
import net.craftersland.data.bridge.objects.DatabaseInventoryData;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.io.SNBTUtil;
import net.querz.nbt.tag.CompoundTag;

public class PlayerUtils implements Listener {
	
	private static final Map<UUID, Map<String, PermissionCache>> PERMISSION_CACHE = new ConcurrentHashMap<>();
	private static final ItemStack AIR = new ItemStack(Material.AIR);
	
	static {
		Bukkit.getScheduler().runTaskTimerAsynchronously(InteractiveChat.plugin, () -> {
			long now = System.currentTimeMillis();
			Iterator<Entry<UUID, Map<String, PermissionCache>>> itr0 = PERMISSION_CACHE.entrySet().iterator();
			while (itr0.hasNext()) {
				Entry<UUID, Map<String, PermissionCache>> entry = itr0.next();
				Map<String, PermissionCache> map = entry.getValue();
				if (map == null || map.isEmpty()) {
					itr0.remove();
				} else {
					Iterator<PermissionCache> itr1 = map.values().iterator();
					while (itr1.hasNext()) {
						PermissionCache permissionCache = itr1.next();
						if (permissionCache.getTime() + 180000 < now) {
							itr1.remove();
						}
					}
				}
			}
		}, 0, 600);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		PERMISSION_CACHE.remove(event.getPlayer().getUniqueId());
	}
	
	public static boolean hasPermission(UUID uuid, String permission, boolean def, int timeout) {
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			Map<String, PermissionCache> map = PERMISSION_CACHE.get(uuid);
			if (map == null) {
				PERMISSION_CACHE.putIfAbsent(uuid, new ConcurrentHashMap<>());
				map = PERMISSION_CACHE.get(uuid);
			}
			PermissionCache cachedResult = map.get(permission);
			boolean result = cachedResult != null ? cachedResult.getValue() : player.hasPermission(permission);
			if (cachedResult == null) {
				map.put(permission, new PermissionCache(result, System.currentTimeMillis()));
			} else {
				cachedResult.setValue(result);
			}
			return result;
		} else {
			CompletableFuture<Boolean> future = new CompletableFuture<>();
			Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
				Map<String, PermissionCache> map = PERMISSION_CACHE.get(uuid);
				if (map == null) {
					PERMISSION_CACHE.putIfAbsent(uuid, new ConcurrentHashMap<>());
					map = PERMISSION_CACHE.get(uuid);
				}
				PermissionCache cachedResult = map.get(permission);
				boolean result = cachedResult != null ? cachedResult.getValue() : InteractiveChat.perms.playerHas(Bukkit.getWorlds().get(0).getName(), Bukkit.getOfflinePlayer(uuid), permission);
				future.complete(result);
				if (cachedResult == null) {
					map.put(permission, new PermissionCache(result, System.currentTimeMillis()));
				} else {
					cachedResult.setValue(result);
				}
			});
			try {
				return future.get(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				return def; 
			}
		}
	}
	
	public static void resetAllPermissionCache() {
		PERMISSION_CACHE.clear();
	}
	
	public static void resetPermissionCache(UUID uuid) {
		PERMISSION_CACHE.remove(uuid);
	}
	
	public static ItemStack getHeldItem(Player player) {
		return getHeldItem(new ICPlayer(player));
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getHeldItem(ICPlayer player) {
		ItemStack item;							
		if (InteractiveChat.version.isOld()) {
			if (player.getEquipment().getItemInHand() == null) {
				item = AIR.clone();
			} else if (player.getEquipment().getItemInHand().getType().equals(Material.AIR)) {
				item = AIR.clone();
			} else {				            								
				item = player.getEquipment().getItemInHand().clone();
			}
		} else {
			if (player.getEquipment().getItemInMainHand() == null) {
				item = AIR.clone();
			} else if (player.getEquipment().getItemInMainHand().getType().equals(Material.AIR)) {
				item = AIR.clone();
			} else {									
				item = player.getEquipment().getItemInMainHand().clone();
			}
		}
		return item;
	}
	
	public static enum ColorSettings {
		ON, 
		OFF, 
		WAITING
	}
	
	public static ColorSettings getColorSettings(Player player) {
		return ClientSettingPacket.getSettings(player);
	}
	
	public static int getProtocolVersion(Player player) {
		int protocolVersion = -1;
		if (InteractiveChat.viaVersionHook) {
			protocolVersion = us.myles.ViaVersion.api.Via.getAPI().getPlayerVersion(player.getUniqueId());
		} else if (InteractiveChat.procotcolSupportHook) {
			protocolVersion = protocolsupport.api.ProtocolSupportAPI.getProtocolVersion(player).getId();
		} else {
			protocolVersion = InteractiveChat.protocolManager.getProtocolVersion(player);
		}
		return protocolVersion;
	}
	
	public static OfflineICPlayer getOfflineICPlayer(UUID uuid) {
		Player onlinePlayer = Bukkit.getPlayer(uuid);
		if (onlinePlayer != null) {
			return new ICPlayer(onlinePlayer);
		}
		ICPlayer remoteICPlayer = InteractiveChat.remotePlayers.get(uuid);
		if (remoteICPlayer != null) {
			return remoteICPlayer;
		}
		File dat = new File(Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath() + "/playerdata", uuid.toString() + ".dat");
		if (!dat.exists()) {
			return null;
		}
		boolean mysqlPDBInventorySync = false;
		boolean mysqlPDBArmorSync = false;
		boolean mysqlPDBEnderChestSync = false;
		boolean mysqlPDBExpSync = false;
		String playerName = Bukkit.getOfflinePlayer(uuid).getName();
		Player dummyPlayer = new DummyPlayer(playerName, uuid);
		if (InteractiveChat.mysqlPDBHook) {
			mysqlPDBInventorySync = PD.instance.getConfigHandler().getBoolean("General.enableModules.shareInventory");
			mysqlPDBArmorSync = PD.instance.getConfigHandler().getBoolean("General.enableModules.shareArmor");
			if (!PD.instance.getInventoryStorageHandler().hasAccount(playerName)) {
				mysqlPDBInventorySync = false;
				mysqlPDBArmorSync = false;
			}
			mysqlPDBEnderChestSync = PD.instance.getConfigHandler().getBoolean("General.enableModules.shareEnderchest");
			if (!PD.instance.getEnderchestStorageHandler().hasAccount(playerName)) {
				mysqlPDBEnderChestSync = false;
			}
			mysqlPDBExpSync = PD.instance.getConfigHandler().getBoolean("General.enableModules.shareExperience");
			if (!PD.instance.getExperienceStorageHandler().hasAccount(playerName)) {
				mysqlPDBExpSync = false;
			}
		}
		try {
			NamedTag nbtData = NBTUtil.read(dat);
			CompoundTag rootTag = (CompoundTag) nbtData.getTag();
			int selectedSlot = rootTag.getInt("SelectedItemSlot");
			int xpLevel = rootTag.getInt("XpLevel");
			ICPlayerEquipment equipment = new ICPlayerEquipment();
			Inventory inventory = Bukkit.createInventory(null, 45);
			Inventory enderchest = Bukkit.createInventory(null, 27);
			for (CompoundTag entry : rootTag.getListTag("Inventory").asTypedList(CompoundTag.class)) {
				int slot = entry.getByte("Slot");
				entry.remove("Slot");
				ItemStack item = ItemNBTUtils.getItemFromNBTJson(SNBTUtil.toSNBT(entry));
				if (slot == 100) {
					equipment.setBoots(item);
					slot = 36;
				} else if (slot == 101) {
					equipment.setLeggings(item);
					slot = 37;
				} else if (slot == 102) {
					equipment.setChestplate(item);
					slot = 38;
				} else if (slot == 103) {
					equipment.setHelmet(item);
					slot = 39;
				} else if (slot == -106) {
					slot = 40;
				}
				inventory.setItem(slot, item);
			}
			for (CompoundTag entry : rootTag.getListTag("EnderItems").asTypedList(CompoundTag.class)) {
				int slot = entry.getByte("Slot");
				entry.remove("Slot");
				ItemStack item = ItemNBTUtils.getItemFromNBTJson(SNBTUtil.toSNBT(entry));
				enderchest.setItem(slot, item);
			}
			if (mysqlPDBInventorySync || mysqlPDBArmorSync) {
				DatabaseInventoryData invData = PD.instance.getInventoryStorageHandler().getData(dummyPlayer);
				if (mysqlPDBInventorySync) {
					ItemStack[] items = PD.instance.getItemStackSerializer().fromBase64(invData.getRawInventory());
					for (int i = 0; i < items.length && i < inventory.getSize(); i++) {
						inventory.setItem(i, items[i]);
					}
					selectedSlot = invData.getHotBarSlot();
				}
				if (mysqlPDBArmorSync) {
					ItemStack[] items = PD.instance.getItemStackSerializer().fromBase64(invData.getRawArmor());
					for (int i = 0; i < items.length && i < 4; i++) {
						inventory.setItem(i + 36, items[i]);
					}
				}
			}
			if (mysqlPDBEnderChestSync) {
				DatabaseEnderchestData enderData = PD.instance.getEnderchestStorageHandler().getData(dummyPlayer);
				ItemStack[] items = PD.instance.getItemStackSerializer().fromBase64(enderData.getRawEnderchest());
				for (int i = 0; i < items.length && i < enderchest.getSize(); i++) {
					enderchest.setItem(i, items[i]);
				}
			}
			if (mysqlPDBExpSync) {
				DatabaseExperienceData expData = PD.instance.getExperienceStorageHandler().getData(dummyPlayer);
				if (expData.getLevel() != null) {
					xpLevel = expData.getLevel();
				}
			}
			return new OfflineICPlayer(uuid, playerName, selectedSlot, xpLevel, equipment, inventory, enderchest);
		} catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

}
