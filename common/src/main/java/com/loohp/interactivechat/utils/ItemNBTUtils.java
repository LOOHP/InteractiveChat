/*
 * This file is part of InteractiveChat.
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

package com.loohp.interactivechat.utils;

import com.loohp.interactivechat.nms.NMS;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.DataComponentValue;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ItemNBTUtils {

    public static ItemStack getItemFromNBTJson(String json) {
        return NMS.getInstance().getItemFromNBTJson(json);
    }

    public static String getNMSItemStackJson(ItemStack itemStack) {
        return NMS.getInstance().getNMSItemStackJson(itemStack);
    }

    public static Key getNMSItemStackNamespacedKey(ItemStack itemStack) {
        return NMS.getInstance().getNMSItemStackNamespacedKey(itemStack);
    }

    public static String getNMSItemStackTag(ItemStack itemStack) {
       return NMS.getInstance().getNMSItemStackTag(itemStack);
    }

    public static Map<Key, DataComponentValue> getNMSItemStackDataComponents(ItemStack itemStack) {
        return NMS.getInstance().getNMSItemStackDataComponents(itemStack);
    }

    public static ItemStack getItemStackFromDataComponents(ItemStack itemStack, Map<Key, DataComponentValue> dataComponents) {
        return NMS.getInstance().getItemStackFromDataComponents(itemStack, dataComponents);
    }

}
