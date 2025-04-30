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

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public interface IOfflineICPlayer {

    UUID getUniqueId();

    OfflinePlayer getLocalOfflinePlayer();

    boolean isOnline();

    String getName();

    int getSelectedSlot();

    boolean isRightHanded();

    int getExperienceLevel();

    EntityEquipment getEquipment();

    Inventory getInventory();

    ItemStack getMainHandItem();

    ItemStack getOffHandItem();

    Inventory getEnderChest();

    IICPlayer getPlayer();

    void updateOfflineData();

    Object getProperty(String key);

    boolean hasProperty(String key);

    Map<String, Object> getProperties();

    Object addProperties(String key, Object value);

}
