/*
 * This file is part of InteractiveChat4.
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

package com.loohp.interactivechat.utils;

import com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.objectholders.ICMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class CompassUtils {

    public static boolean isLodestoneCompass(ItemStack itemStack) {
        if (!ICMaterial.from(itemStack).isMaterial(XMaterial.COMPASS)) {
            return false;
        }
        try {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta instanceof CompassMeta) {
                return ((CompassMeta) meta).getLodestone() != null;
            }
            return false;
        } catch (Throwable e) {
            return false;
        }
    }

    public static ItemStack hideLodestoneCompassPosition(ItemStack itemStack) {
        if (isLodestoneCompass(itemStack)) {
            CompassMeta compassMeta = (CompassMeta) itemStack.getItemMeta();
            if (compassMeta.hasLodestone()) {
                Location location = compassMeta.getLodestone();
                World world = location.getWorld() == null ? Bukkit.getWorlds().get(0) : location.getWorld();
                compassMeta.setLodestone(world.getSpawnLocation());
                itemStack = itemStack.clone();
                itemStack.setItemMeta(compassMeta);
            }
        } else if (itemStack.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta meta = (BlockStateMeta) itemStack.getItemMeta();
            BlockState state = meta.getBlockState();
            if (state instanceof InventoryHolder) {
                hideLodestoneCompassesPosition(((InventoryHolder) state).getInventory());
                meta.setBlockState(state);
                itemStack = itemStack.clone();
                itemStack.setItemMeta(meta);
            }
        }
        return itemStack;
    }

    public static void hideLodestoneCompassesPosition(Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                if (isLodestoneCompass(itemStack)) {
                    inventory.setItem(i, hideLodestoneCompassPosition(itemStack));
                } else if (itemStack.getItemMeta() instanceof BlockStateMeta) {
                    BlockStateMeta meta = (BlockStateMeta) itemStack.getItemMeta();
                    BlockState state = meta.getBlockState();
                    if (state instanceof InventoryHolder) {
                        Inventory container = ((InventoryHolder) state).getInventory();
                        if (inventory != container) {
                            hideLodestoneCompassesPosition(container);
                            meta.setBlockState(state);
                            itemStack.setItemMeta(meta);
                        }
                    }
                }
            }
        }
    }

}
