package com.loohp.interactivechat.ObjectHolders;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;

public class ICPlayer {
	
	public static final String LOCAL_SERVER_REPRESENTATION = "*local_server";

	private UUID uuid;

	private String remoteServer;
	private String remoteName;
	private EntityEquipment remoteEquipment;
	private Inventory remoteInventory;
	private Inventory remoteEnderchest;
	private final Map<String, String> remotePlaceholders;

	public ICPlayer(String server, String name, UUID uuid, RemoteEquipment equipment, Inventory inventory, Inventory enderchest) {
		this.remoteServer = server;
		this.remoteName = name;
		this.uuid = uuid;
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
