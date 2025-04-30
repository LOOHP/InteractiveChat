/*
 * This file is part of InteractiveChat4.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
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

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ICPlayer extends OfflineICPlayer implements IICPlayer {

    public static final String LOCAL_SERVER_REPRESENTATION = "*local_server";
    public static final String EMPTY_SERVER_REPRESENTATION = "*invalid";

    private String remoteServer;
    private String remoteName;
    private boolean rightHanded;
    private Set<String> remoteNicknames;
    private Map<String, String> remotePlaceholders;
    private boolean remoteVanished;

    protected ICPlayer(String server, String name, UUID uuid, boolean rightHanded, int selectedSlot, int experienceLevel, Inventory inventory, Inventory enderchest, boolean remoteVanished) {
        super(uuid, selectedSlot, rightHanded, experienceLevel, inventory, enderchest);
        this.remoteServer = server;
        this.remoteName = name;
        this.remoteNicknames = new HashSet<>();
        this.remotePlaceholders = new HashMap<>();
        this.remoteVanished = remoteVanished;
    }

    protected ICPlayer(Player player) {
        super(player.getUniqueId(), player.getInventory().getHeldItemSlot(), InteractiveChat.version.isOld() || player.getMainHand().equals(MainHand.RIGHT), player.getLevel(), Bukkit.createInventory(ICInventoryHolder.INSTANCE, 54), Bukkit.createInventory(ICInventoryHolder.INSTANCE, InventoryUtils.getDefaultEnderChestSize()));
        this.remoteServer = EMPTY_SERVER_REPRESENTATION;
        this.remoteName = player.getName();
        this.remoteNicknames = new HashSet<>();
        this.remotePlaceholders = new HashMap<>();
    }

    @Override
    public boolean isLocal() {
        return Bukkit.getPlayer(uuid) != null;
    }

    @Override
    public boolean isRemote() {
        return remoteServer != null && !remoteServer.equals(EMPTY_SERVER_REPRESENTATION);
    }

    @Override
    public boolean isOnline() {
        return isLocal() || isRemote();
    }

    @Override
    public boolean isValid() {
        return isOnline();
    }

    @Override
    public Player getLocalPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    @Override
    public String getRemoteServer() {
        return remoteServer;
    }

    @Override
    public void setRemoteServer(String server) {
        remoteServer = server;
    }

    @Override
    public String getServer() {
        return isLocal() ? LOCAL_SERVER_REPRESENTATION : remoteServer;
    }

    @Override
    public String getName() {
        return isLocal() ? getLocalPlayer().getName() : remoteName;
    }

    @Override
    public String getDisplayName() {
        return isLocal() ? getLocalPlayer().getDisplayName() : remoteName;
    }

    protected void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public boolean isVanished() {
        return isLocal() ? getLocalPlayer().getMetadata("vanished").stream().anyMatch(each -> each.asBoolean()) : remoteVanished;
    }

    @Override
    public void setRemoteVanished(boolean remoteVanished) {
        this.remoteVanished = remoteVanished;
    }

    @Override
    public boolean isRightHanded() {
        if (InteractiveChat.version.isOld()) {
            return true;
        } else {
            return isLocal() ? getLocalPlayer().getMainHand().name().equalsIgnoreCase("RIGHT") : rightHanded;
        }
    }

    @Override
    public void setRemoteRightHanded(boolean rightHanded) {
        this.rightHanded = rightHanded;
    }

    @Override
    public int getSelectedSlot() {
        return isLocal() ? getLocalPlayer().getInventory().getHeldItemSlot() : selectedSlot;
    }

    @Override
    public void setRemoteSelectedSlot(int selectedSlot) {
        this.selectedSlot = selectedSlot;
    }

    @Override
    public int getExperienceLevel() {
        return isLocal() ? getLocalPlayer().getLevel() : experienceLevel;
    }

    @Override
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

    @Override
    public void setRemoteInventory(Inventory inventory) {
        remoteInventory = inventory;
    }

    @Override
    public ItemStack getMainHandItem() {
        return getInventory().getItem(getSelectedSlot());
    }

    @Override
    public ItemStack getOffHandItem() {
        return getInventory().getSize() > 40 ? getInventory().getItem(40) : null;
    }

    @Override
    public Inventory getEnderChest() {
        return isLocal() ? getLocalPlayer().getEnderChest() : remoteEnderchest;
    }

    @Override
    public void setRemoteEnderChest(Inventory enderchest) {
        remoteEnderchest = enderchest;
    }

    @Override
    public Set<String> getNicknames() {
        Set<String> nicknames = new HashSet<>(InteractiveChat.nicknameManager.getNicknames(uuid));
        if (!isLocal()) {
            nicknames.addAll(remoteNicknames);
        }
        return nicknames;
    }

    @Override
    public Set<String> getRemoteNicknames() {
        return remoteNicknames;
    }

    @Override
    public void setRemoteNicknames(Set<String> remoteNicknames) {
        this.remoteNicknames = remoteNicknames;
    }

    @Override
    public Map<String, String> getRemotePlaceholdersMapping() {
        return remotePlaceholders;
    }

    @Override
    public ICPlayer getPlayer() {
        return this;
    }

}
