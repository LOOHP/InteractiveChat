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

package com.loohp.interactivechat.utils;

import com.loohp.interactivechat.nms.NMS;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapView;

import java.util.List;

public class FilledMapUtils {

    public static boolean isFilledMap(ItemStack itemStack) {
        try {
            return itemStack != null && itemStack.getItemMeta() != null && itemStack.getItemMeta() instanceof MapMeta;
        } catch (Exception e) {
            return false;
        }
    }

    public static MapView getMapView(ItemStack itemStack) {
        return NMS.getInstance().getMapView(itemStack);
    }

    public static int getMapId(ItemStack itemStack) {
        return NMS.getInstance().getMapId(itemStack);
    }

    public static boolean isContextual(MapView mapView) {
        return NMS.getInstance().isContextual(mapView);
    }

    public static byte[] getColors(MapView mapView, Player player) {
        return NMS.getInstance().getColors(mapView, player);
    }

    public static List<MapCursor> getCursors(MapView mapView, Player player) {
       return NMS.getInstance().getCursors(mapView, player);
    }

    public static List<?> toNMSMapIconList(List<MapCursor> mapCursors) {
        return NMS.getInstance().toNMSMapIconList(mapCursors);
    }

}
