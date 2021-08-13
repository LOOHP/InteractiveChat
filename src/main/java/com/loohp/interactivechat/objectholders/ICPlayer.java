package com.loohp.interactivechat.objectholders;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.MainHand;

import com.loohp.interactivechat.InteractiveChat;

public class ICPlayer extends OfflineICPlayer {
	
	public static final String LOCAL_SERVER_REPRESENTATION = "*local_server";
	public static final String EMPTY_SERVER_REPRESENTATION = "*invalid";
	private static final Inventory EMPTY_INVENTORY = Bukkit.createInventory(null, 54);
	private static final Inventory EMPTY_ENDERCHEST = Bukkit.createInventory(null, 18);
	private static final ICPlayerEquipment EMPTY_EQUIPMENT = new ICPlayerEquipment();

	private String remoteServer;
	private String remoteName;
	private boolean rightHanded;
	private Map<String, String> remotePlaceholders;

	public ICPlayer(String server, String name, UUID uuid, boolean rightHanded, int selectedSlot, int experienceLevel, ICPlayerEquipment equipment, Inventory inventory, Inventory enderchest) {
		super(uuid, selectedSlot, experienceLevel, equipment, inventory, enderchest);
		this.remoteServer = server;
		this.remoteName = name;
		this.rightHanded = rightHanded;
		this.remotePlaceholders = new HashMap<>();
	}
	
	public ICPlayer(Player player) {
		super(player.getUniqueId(), player.getInventory().getHeldItemSlot(), player.getLevel(), EMPTY_EQUIPMENT, EMPTY_INVENTORY, EMPTY_ENDERCHEST);
		this.remoteServer = EMPTY_SERVER_REPRESENTATION;
		this.remoteName = player.getName();
		this.rightHanded = InteractiveChat.version.isOld() || player.getMainHand().equals(MainHand.RIGHT);
		this.remotePlaceholders = new HashMap<>();
	}

	public boolean isLocal() {
		return Bukkit.getPlayer(uuid) != null;
	}
	
	public boolean isValid() {
		return isLocal() ? true : (remoteServer != null);
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
		return isLocal() ? LOCAL_SERVER_REPRESENTATION : remoteServer;
	}

	@Override
	public String getName() {
		return isLocal() ? getLocalPlayer().getName() : remoteName;
	}

	public String getDisplayName() {
		return isLocal() ? getLocalPlayer().getDisplayName() : remoteName;
	}

	@Override
	public UUID getUniqueId() {
		return uuid;
	}

	public boolean isRightHanded() {
		if (InteractiveChat.version.isOld()) {
			return true;
		} else {
			return isLocal() ? getLocalPlayer().getMainHand().name().equalsIgnoreCase("RIGHT") : rightHanded;
		}
	}

	public void setRemoteRightHanded(boolean rightHanded) {
		this.rightHanded = rightHanded;
	}
	
	@Override
	public int getSelectedSlot() {
		return isLocal() ? getLocalPlayer().getInventory().getHeldItemSlot() : selectedSlot;
	}

	public void setRemoteSelectedSlot(int selectedSlot) {
		this.selectedSlot = selectedSlot;
	}

	@Override
	public int getExperienceLevel() {
		return isLocal() ? getLocalPlayer().getLevel() : experienceLevel;
	}

	public void setRemoteExperienceLevel(int experienceLevel) {
		this.experienceLevel = experienceLevel;
	}

	@Override
	public EntityEquipment getEquipment() {
		return isLocal() ? getLocalPlayer().getEquipment() : remoteEquipment;
	}

	@Override
	public Inventory getInventory() {
		return isLocal() ? getLocalPlayer().getInventory() : remoteInventory;
	}
	
	public void setRemoteInventory(Inventory inventory) {
		remoteInventory = inventory;
	}
	
	@Override
	public Inventory getEnderChest() {
		return isLocal() ? getLocalPlayer().getEnderChest() : remoteEnderchest;
	}
	
	public void setRemoteEnderChest(Inventory enderchest) {
		remoteEnderchest = enderchest;
	}

	public Map<String, String> getRemotePlaceholdersMapping() {
		return remotePlaceholders;
	}
}
