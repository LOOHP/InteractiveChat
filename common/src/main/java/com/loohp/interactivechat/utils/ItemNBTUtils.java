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

package com.loohp.interactivechat.utils;

import com.google.gson.Gson;
import com.loohp.interactivechat.nms.NMS;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.serializer.gson.GsonDataComponentValue;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemNBTUtils {

    private static final Gson GSON = new Gson();

    public static ItemStack getItemFromNBTJson(String json) {
        return NMS.getInstance().getItemFromNBTJson(json);
    }

    public static String getNMSItemStackJson(ItemStack itemStack) {
        return NMS.getInstance().getNMSItemStackJson(itemStack);
    }

    public static String getNMSItemStackCommandComponent(ItemStack itemStack) {
        Map<Key, DataComponentValue> components = getNMSItemStackDataComponents(itemStack);
        List<String> componentStrings = new ArrayList<>(components.size());
        for (Map.Entry<Key, DataComponentValue> entry : components.entrySet()) {
            Key key = entry.getKey();
            DataComponentValue value = entry.getValue();
            if (value instanceof DataComponentValue.Removed) {
                componentStrings.add("!" + key);
            } else if (value instanceof GsonDataComponentValue) {
                componentStrings.add(key + "=" + GSON.toJson(((GsonDataComponentValue) value).element()));
            } else if (value instanceof DataComponentValue.TagSerializable) {
                componentStrings.add(key + "=" + ((DataComponentValue.TagSerializable) value).asBinaryTag().string());
            }
        }
        String namespacedKey = NMS.getInstance().getNMSItemStackNamespacedKey(itemStack).asString();
        if (componentStrings.isEmpty()) {
            return namespacedKey;
        }
        return namespacedKey + "[" + String.join(",", componentStrings) + "]";
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
