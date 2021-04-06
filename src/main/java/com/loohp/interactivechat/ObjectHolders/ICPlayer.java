package com.loohp.interactivechat.objectholders;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;

import com.loohp.interactivechat.InteractiveChat;

public class ICPlayer {
	
	public static final String LOCAL_SERVER_REPRESENTATION = "*local_server";

	private UUID uuid;
	private String remoteServer;
	private String remoteName;
	private boolean rightHanded;
	private int selectedSlot;
	private int experienceLevel;
	private EntityEquipment remoteEquipment;
	private Inventory remoteInventory;
	private Inventory remoteEnderchest;
	private final Map<String, String> remotePlaceholders;

	public ICPlayer(String server, String name, UUID uuid, boolean rightHanded, int selectedSlot, int experienceLevel, RemoteEquipment equipment, Inventory inventory, Inventory enderchest) {
		this.remoteServer = server;
		this.remoteName = name;
		this.uuid = uuid;
		this.rightHanded = rightHanded;
		this.selectedSlot = selectedSlot;
		this.experienceLevel = experienceLevel;
		this.remoteEquipment = equipment;
		this.remoteInventory = inventory;
		this.remoteEnderchest = enderchest;
		this.remotePlaceholders = new HashMap<>();
	}
	
	public ICPlayer(Player player) {
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
		return isLocal() ? LOCAL_SERVER_REPRESENTATION : remoteServer;
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
	
	public int getSelectedSlot() {
		return isLocal() ? getLocalPlayer().getInventory().getHeldItemSlot() : selectedSlot;
	}

	public void setRemoteSelectedSlot(int selectedSlot) {
		this.selectedSlot = selectedSlot;
	}

	public int getExperienceLevel() {
		return isLocal() ? getLocalPlayer().getLevel() : experienceLevel;
	}

	public void setRemoteExperienceLevel(int experienceLevel) {
		this.experienceLevel = experienceLevel;
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
}
