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

package com.loohp.interactivechat.objectholders;

import com.loohp.interactivechat.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

    protected OfflineICPlayer(UUID uuid, String offlineName, int selectedSlot, boolean rightHanded, int experienceLevel, Inventory inventory, Inventory enderchest) {
        this.uuid = uuid;
        this.offlineName = offlineName;
        this.selectedSlot = selectedSlot;
        this.rightHanded = rightHanded;
        this.experienceLevel = experienceLevel;
        this.remoteEquipment = new ICPlayerEquipment(this);
        this.remoteInventory = inventory;
        this.remoteEnderchest = enderchest;
        this.properties = new HashMap<>();
    }

    protected OfflineICPlayer(UUID uuid, int selectedSlot, boolean rightHanded, int experienceLevel, Inventory inventory, Inventory enderchest) {
        this(uuid, Bukkit.getOfflinePlayer(uuid).getName(), selectedSlot, rightHanded, experienceLevel, inventory, enderchest);
    }

    protected OfflineICPlayer(UUID uuid) {
        this(uuid, 0, true, 0, Bukkit.createInventory(null, 54), Bukkit.createInventory(null, InventoryUtils.getDefaultEnderChestSize()));
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public OfflinePlayer getLocalOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public boolean isOnline() {
        ICPlayer icPlayer = getPlayer();
        return icPlayer != null && icPlayer.isOnline();
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OfflineICPlayer that = (OfflineICPlayer) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
