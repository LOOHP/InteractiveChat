package com.loohp.interactivechat.objectholders;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;

public class OfflineICPlayer {
	
	protected final UUID uuid;
	protected final String offlineName;
	protected int selectedSlot;
	protected int experienceLevel;
	protected EntityEquipment remoteEquipment;
	protected Inventory remoteInventory;
	protected Inventory remoteEnderchest;
	
	protected OfflineICPlayer(UUID uuid, String offlineName, int selectedSlot, int experienceLevel, ICPlayerEquipment equipment, Inventory inventory, Inventory enderchest) {
		this.uuid = uuid;
		this.offlineName = offlineName;
		this.selectedSlot = selectedSlot;
		this.experienceLevel = experienceLevel;
		this.remoteEquipment = equipment;
		this.remoteInventory = inventory;
		this.remoteEnderchest = enderchest;
	}
	
	protected OfflineICPlayer(UUID uuid, int selectedSlot, int experienceLevel, ICPlayerEquipment equipment, Inventory inventory, Inventory enderchest) {
		this(uuid, Bukkit.getOfflinePlayer(uuid).getName(), selectedSlot, experienceLevel, equipment, inventory, enderchest);
	}

	public UUID getUniqueId() {
		return uuid;
	}
	
	public String getName() {
		return offlineName;
	}
	
	public int getSelectedSlot() {
		return selectedSlot;
	}

	public int getExperienceLevel() {
		return experienceLevel;
	}
	
	public EntityEquipment getEquipment() {
		return remoteEquipment;
	}

	public Inventory getInventory() {
		return remoteInventory;
	}
	
	public Inventory getEnderChest() {
		return remoteEnderchest;
	}
	
	public ICPlayer getPlayer() {
		return ICPlayerFactory.getICPlayer(uuid);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof OfflineICPlayer)) {
			return false;
		}
		OfflineICPlayer other = (OfflineICPlayer) obj;
		if (uuid == null) {
			if (other.uuid != null) {
				return false;
			}
		} else if (!uuid.equals(other.uuid)) {
			return false;
		}
		return true;
	}

}
