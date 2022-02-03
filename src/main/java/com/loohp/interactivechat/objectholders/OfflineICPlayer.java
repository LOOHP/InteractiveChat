package com.loohp.interactivechat.objectholders;

import org.bukkit.Bukkit;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OfflineICPlayer {

    protected final UUID uuid;
    protected String offlineName;
    protected int selectedSlot;
    protected boolean rightHanded;
    protected int experienceLevel;
    protected EntityEquipment remoteEquipment;
    protected Inventory remoteInventory;
    protected Inventory remoteEnderchest;

    private Map<String, Object> properties;

    protected OfflineICPlayer(UUID uuid, String offlineName, int selectedSlot, boolean rightHanded, int experienceLevel, ICPlayerEquipment equipment, Inventory inventory, Inventory enderchest) {
        this.uuid = uuid;
        this.offlineName = offlineName;
        this.selectedSlot = selectedSlot;
        this.rightHanded = rightHanded;
        this.experienceLevel = experienceLevel;
        this.remoteEquipment = equipment;
        this.remoteInventory = inventory;
        this.remoteEnderchest = enderchest;
        this.properties = new HashMap<>();
    }

    protected OfflineICPlayer(UUID uuid, int selectedSlot, boolean rightHanded, int experienceLevel, ICPlayerEquipment equipment, Inventory inventory, Inventory enderchest) {
        this(uuid, Bukkit.getOfflinePlayer(uuid).getName(), selectedSlot, rightHanded, experienceLevel, equipment, inventory, enderchest);
    }

    protected OfflineICPlayer(UUID uuid) {
        this(uuid, 0, true, 0, new ICPlayerEquipment(), Bukkit.createInventory(null, 54), Bukkit.createInventory(null, 18));
    }

    public UUID getUniqueId() {
        return uuid;
    }

    protected void setEnderchest(Inventory remoteEnderchest) {
        this.remoteEnderchest = remoteEnderchest;
    }

    public String getName() {
        return offlineName;
    }

    protected void setName(String offlineName) {
        this.offlineName = offlineName;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    protected void setSelectedSlot(int selectedSlot) {
        this.selectedSlot = selectedSlot;
    }

    public boolean isRightHanded() {
        return rightHanded;
    }

    protected void setRightHanded(boolean rightHanded) {
        this.rightHanded = rightHanded;
    }

    public int getExperienceLevel() {
        return experienceLevel;
    }

    protected void setExperienceLevel(int experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public EntityEquipment getEquipment() {
        return remoteEquipment;
    }

    protected void setEquipment(EntityEquipment remoteEquipment) {
        this.remoteEquipment = remoteEquipment;
    }

    public Inventory getInventory() {
        return remoteInventory;
    }

    protected void setInventory(Inventory remoteInventory) {
        this.remoteInventory = remoteInventory;
    }

    public ItemStack getMainHandItem() {
        return getInventory().getItem(getSelectedSlot());
    }

    public ItemStack getOffHandItem() {
        return getInventory().getSize() > 40 ? getInventory().getItem(40) : null;
    }

    public Inventory getEnderChest() {
        return remoteEnderchest;
    }

    public ICPlayer getPlayer() {
        return ICPlayerFactory.getICPlayer(uuid);
    }

    public void updateOfflineData() {
        ICPlayerFactory.getOfflineICPlayer(uuid);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }

    public Object addProperties(String key, Object value) {
        return properties.put(key, value);
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
            return other.uuid == null;
        } else {
            return uuid.equals(other.uuid);
        }
    }

}
