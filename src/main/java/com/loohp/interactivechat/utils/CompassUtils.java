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
import io.github.bananapuncher714.nbteditor.NBTEditor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

public class CompassUtils {

    public static boolean isLodestoneCompass(ItemStack itemStack) {
        return ICMaterial.from(itemStack).isMaterial(XMaterial.COMPASS) && (NBTEditor.contains(itemStack, "LodestoneDimension") || NBTEditor.contains(itemStack, "LodestonePos"));
    }

    public static ItemStack hideLodestoneCompassPosition(ItemStack itemStack) {
        if (!isLodestoneCompass(itemStack)) {
            throw new IllegalArgumentException("itemStack is not a lodestone compass");
        }
        CompassMeta compassMeta = (CompassMeta) itemStack.getItemMeta();
        if (compassMeta.hasLodestone()) {
            Location location = compassMeta.getLodestone();
            World world = location.getWorld() == null ? Bukkit.getWorlds().get(0) : location.getWorld();
            compassMeta.setLodestone(world.getSpawnLocation());
            itemStack.setItemMeta(compassMeta);
        }
        return itemStack;
    }

    public static void hideLodestoneCompassesPosition(Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack != null && !itemStack.getType().equals(Material.AIR) && isLodestoneCompass(itemStack)) {
                inventory.setItem(i, hideLodestoneCompassPosition(itemStack));
            }
        }
    }

}
