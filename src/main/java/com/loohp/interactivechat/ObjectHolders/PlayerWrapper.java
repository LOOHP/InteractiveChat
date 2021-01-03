package com.loohp.interactivechat.ObjectHolders;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerWrapper {
	
	public static final String CURRENT_SERVER_REPRESENTATION = "current";

	private UUID uuid;

	private String remoteServer;
	private String remoteName;
	private EntityEquipment remoteEquipment;
	private Inventory remoteInventory;
	private Inventory remoteEnderchest;
	private final Map<String, String> remotePlaceholders;

	public PlayerWrapper(String server, String name, UUID uuid, RemoteEquipment equipment, Inventory inventory, Inventory enderchest) {
		remoteServer = server;
		remoteName = name;
		this.uuid = uuid;
		remoteEquipment = equipment;
		remoteInventory = inventory;
		remoteEnderchest = enderchest;
		remotePlaceholders = new HashMap<>();
	}
	
	public PlayerWrapper(Player player) {
		remotePlaceholders = new HashMap<>();
		uuid = player.getUniqueId();
	}

	public boolean isLocal() {
		return Bukkit.getPlayer(uuid) != null;
	}

	public Player getLocalPlayer() {
		return Bukkit.getPlayer(uuid);
	}
	
	public void setRemoteServer(String server) {
		remoteServer = server;
	}
	
	public String getRemoteServer() {
		return remoteServer;
	}
	
	public String getServer() {
		return isLocal() ? CURRENT_SERVER_REPRESENTATION : remoteServer;
	}

	public String getName() {
		return isLocal() ? getLocalPlayer().getName() : remoteName;
	}

	public String getDisplayName() {
		return isLocal() ? getLocalPlayer().getDisplayName() : remoteName;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public EntityEquipment getEquipment() {
		return isLocal() ? getLocalPlayer().getEquipment() : remoteEquipment;
	}

	public Inventory getInventory() {
		return isLocal() ? getLocalPlayer().getInventory() : remoteInventory;
	}
	
	public void setRemoteInventory(Inventory inventory) {
		remoteInventory = inventory;
	}
	
	public Inventory getEnderChest() {
		return isLocal() ? getLocalPlayer().getEnderChest() : remoteEnderchest;
	}
	
	public void setRemoteEnderChest(Inventory enderchest) {
		remoteEnderchest = enderchest;
	}

	public Map<String, String> getRemotePlaceholdersMapping() {
		return remotePlaceholders;
	}

	// =================

	public static class RemoteEquipment implements EntityEquipment {

		private Map<EquipmentSlot, ItemStack> mapping;

		public RemoteEquipment() {
			mapping = new HashMap<>();
		}

		@Override
		public void setItem(EquipmentSlot slot, ItemStack item) {
			mapping.put(slot, item);
		}

		@Override
		public ItemStack getItem(EquipmentSlot slot) {
			return mapping.get(slot);
		}

		@Override
		public ItemStack getItemInMainHand() {
			return mapping.get(EquipmentSlot.HAND);
		}

		@Override
		public void setItemInMainHand(ItemStack item) {
			mapping.put(EquipmentSlot.HAND, item);
		}

		@Override
		public ItemStack getItemInOffHand() {
			return mapping.get(EquipmentSlot.OFF_HAND);
		}

		@Override
		public void setItemInOffHand(ItemStack item) {
			mapping.put(EquipmentSlot.OFF_HAND, item);
		}

		@Override
		@Deprecated
		public ItemStack getItemInHand() {
			return mapping.get(EquipmentSlot.HAND);
		}

		@Override
		@Deprecated
		public void setItemInHand(ItemStack stack) {
			mapping.put(EquipmentSlot.HAND, stack);
		}

		@Override
		public ItemStack getHelmet() {
			return mapping.get(EquipmentSlot.HEAD);
		}

		@Override
		public void setHelmet(ItemStack helmet) {
			mapping.put(EquipmentSlot.HEAD, helmet);
		}

		@Override
		public ItemStack getChestplate() {
			return mapping.get(EquipmentSlot.CHEST);
		}

		@Override
		public void setChestplate(ItemStack chestplate) {
			mapping.put(EquipmentSlot.CHEST, chestplate);
		}

		@Override
		public ItemStack getLeggings() {
			return mapping.get(EquipmentSlot.LEGS);
		}

		@Override
		public void setLeggings(ItemStack leggings) {
			mapping.put(EquipmentSlot.LEGS, leggings);
		}

		@Override
		public ItemStack getBoots() {
			return mapping.get(EquipmentSlot.FEET);
		}

		@Override
		public void setBoots(ItemStack boots) {
			mapping.put(EquipmentSlot.FEET, boots);
		}

		@Override
		public ItemStack[] getArmorContents() {
			return new ItemStack[] { getHelmet(), getChestplate(), getLeggings(), getBoots() };
		}

		@Override
		public void setArmorContents(ItemStack[] items) {
			setHelmet(items[0]);
			setChestplate(items[1]);
			setLeggings(items[2]);
			setBoots(items[3]);
		}

		@Override
		public void clear() {
			mapping.clear();
		}

		@Override
		@Deprecated
		public float getItemInHandDropChance() {
			return 100;
		}

		@Override
		@Deprecated
		public void setItemInHandDropChance(float chance) {

		}

		@Override
		@Deprecated
		public float getItemInMainHandDropChance() {
			return 100;
		}

		@Override
		@Deprecated
		public void setItemInMainHandDropChance(float chance) {

		}

		@Override
		@Deprecated
		public float getItemInOffHandDropChance() {
			return 100;
		}

		@Override
		@Deprecated
		public void setItemInOffHandDropChance(float chance) {

		}

		@Override
		@Deprecated
		public float getHelmetDropChance() {
			return 100;
		}

		@Override
		@Deprecated
		public void setHelmetDropChance(float chance) {

		}

		@Override
		@Deprecated
		public float getChestplateDropChance() {
			return 100;
		}

		@Override
		@Deprecated
		public void setChestplateDropChance(float chance) {

		}

		@Override
		@Deprecated
		public float getLeggingsDropChance() {
			return 100;
		}

		@Override
		@Deprecated
		public void setLeggingsDropChance(float chance) {

		}

		@Override
		@Deprecated
		public float getBootsDropChance() {
			return 100;
		}

		@Override
		@Deprecated
		public void setBootsDropChance(float chance) {

		}

		@Override
		public Entity getHolder() {
			return null;
		}

		@Override
		public void setBoots(ItemStack arg0, boolean arg1) {
			setBoots(arg0);
		}

		@Override
		public void setChestplate(ItemStack arg0, boolean arg1) {
			setChestplate(arg0);
		}

		@Override
		public void setHelmet(ItemStack arg0, boolean arg1) {
			setHelmet(arg0);
		}

		@Override
		public void setItem(EquipmentSlot arg0, ItemStack arg1, boolean arg2) {
			setItem(arg0, arg1);
		}

		@Override
		public void setItemInMainHand(ItemStack arg0, boolean arg1) {
			setItemInMainHand(arg0);
		}

		@Override
		public void setItemInOffHand(ItemStack arg0, boolean arg1) {
			setItemInOffHand(arg0);
		}

		@Override
		public void setLeggings(ItemStack arg0, boolean arg1) {
			setLeggings(arg0);
		}
	}
}
