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

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.nms.NMS;
import com.loohp.interactivechat.objectholders.ICMaterial;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemStackUtils {

    private static final ItemStack AIR = new ItemStack(Material.AIR);

    public static Component getDisplayName(ItemStack itemstack) {
        return getDisplayName(itemstack, ChatColor.WHITE);
    }

    public static Component getDisplayName(ItemStack itemstack, ChatColor defaultRarityColor) {
        return getDisplayName(itemstack, defaultRarityColor, true);
    }

    public static Component getDisplayName(ItemStack itemstack, ChatColor defaultRarityColor, boolean removeClickAndHover) {
        if (itemstack == null) {
            itemstack = AIR.clone();
        }
        ICMaterial icMaterial = ICMaterial.from(itemstack);
        ItemMeta itemMeta = itemstack.getItemMeta();

        Component component = Component.empty().append(NMS.getInstance().getItemStackDisplayName(itemstack));

        if (itemMeta != null && itemMeta.hasDisplayName()) {
            component = component.decorate(TextDecoration.ITALIC);
        }

        ChatColor rarityChatColor = NMS.getInstance().getRarityColor(itemstack);
        if (rarityChatColor.equals(ChatColor.WHITE)) {
            rarityChatColor = defaultRarityColor;
        }
        if (rarityChatColor != null) {
            component = component.colorIfAbsent(ColorUtils.toTextColor(rarityChatColor));
        }

        if (removeClickAndHover) {
            component = ComponentStyling.stripEvents(component);
        }
        return component;
    }

    public static List<Component> getLore(ItemStack itemstack) {
        ItemMeta meta = itemstack.getItemMeta();
        if (meta == null) {
            return null;
        }
        if (!meta.hasLore()) {
            return null;
        }
        return NMS.getInstance().getItemStackLore(itemstack);
    }

    public static boolean isArmor(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        String typeNameString = itemStack.getType().name();
        return typeNameString.endsWith("_HELMET") || typeNameString.endsWith("_CHESTPLATE") || typeNameString.endsWith("_LEGGINGS") || typeNameString.endsWith("_BOOTS");
    }

    public static boolean isWearable(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        if (isArmor(itemStack)) {
            return true;
        }
        String typeNameString = itemStack.getType().name();
        if (typeNameString.equals("ELYTRA")) {
            return true;
        }
        if (typeNameString.contains("HEAD") || typeNameString.contains("SKULL")) {
            return true;
        }
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_13)) {
            return typeNameString.equals("CARVED_PUMPKIN");
        } else {
            return typeNameString.equals("PUMPKIN");
        }
    }

    public static ItemStack toBukkitCopy(Object handle) {
        return NMS.getInstance().toBukkitCopy(handle);
    }

    public static Object toNMSCopy(ItemStack itemstack) {
        return NMS.getInstance().toNMSCopy(itemstack);
    }

}
