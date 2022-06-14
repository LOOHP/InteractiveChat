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

import com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.InteractiveChat;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import io.github.bananapuncher714.nbteditor.NBTEditor.NBTCompound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemStackUtils {

    private static final ItemStack AIR = new ItemStack(Material.AIR);

    private static Class<?> craftItemStackClass;
    private static Class<?> nmsItemStackClass;
    private static Method asBukkitCopyMethod;
    private static Method asNMSCopyMethod;

    static {
        try {
            craftItemStackClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.inventory.CraftItemStack");
            nmsItemStackClass = NMSUtils.getNMSClass("net.minecraft.server.%s.ItemStack", "net.minecraft.world.item.ItemStack");
            asBukkitCopyMethod = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
            asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
        } catch (ClassNotFoundException | SecurityException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static Component getDisplayName(ItemStack itemstack) {
        return getDisplayName(itemstack, ChatColor.WHITE);
    }

    public static Component getDisplayName(ItemStack itemstack, ChatColor defaultRarityColor) {
        if (itemstack == null) {
            itemstack = AIR.clone();
        }
        XMaterial xMaterial = XMaterialUtils.matchXMaterial(itemstack);
        ChatColor rarityChatColor = RarityUtils.getRarityColor(itemstack);
        if (rarityChatColor.equals(ChatColor.WHITE)) {
            rarityChatColor = defaultRarityColor;
        }
        Component component = Component.empty();
        if (rarityChatColor != null) {
            component = component.color(ColorUtils.toTextColor(rarityChatColor));
        }
        if (!itemstack.getType().equals(Material.AIR) && NBTEditor.contains(itemstack, "display", "Name")) {
            String name = NBTEditor.getString(itemstack, "display", "Name");
            if (!InteractiveChat.version.isLegacy()) {
                component = component.decorate(TextDecoration.ITALIC);
            }
            try {
                if (JsonUtils.isValid(name)) {
                    component = component.append(InteractiveChatComponentSerializer.gson().deserialize(name));
                } else {
                    component = component.append(LegacyComponentSerializer.legacySection().deserialize(name));
                }
            } catch (Throwable e) {
                component = component.append(LegacyComponentSerializer.legacySection().deserialize(name));
            }
        } else {
            boolean displayNameCompleted = false;
            if (itemstack.hasItemMeta() && itemstack.getItemMeta() instanceof BookMeta) {
                BookMeta meta = (BookMeta) itemstack.getItemMeta();
                String rawTitle = meta.getTitle();
                if (rawTitle != null) {
                    displayNameCompleted = true;
                    component = component.append(LegacyComponentSerializer.legacySection().deserialize(rawTitle));
                }
            }
            if (!displayNameCompleted) {
                TranslatableComponent translatableComponent = Component.translatable(LanguageUtils.getTranslationKey(itemstack));
                if (xMaterial.equals(XMaterial.PLAYER_HEAD)) {
                    String owner = NBTEditor.getString(itemstack, "SkullOwner", "Name");
                    if (owner != null) {
                        translatableComponent = translatableComponent.args(Component.text(owner));
                    }
                }
                component = component.append(translatableComponent);
            }
        }
        return component;
    }

    public static List<Component> getLore(ItemStack itemstack) {
        if (!itemstack.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = itemstack.getItemMeta();
        if (!meta.hasLore()) {
            return null;
        }
        int size = meta.getLore().size();
        if (size <= 0) {
            return Collections.emptyList();
        }
        NBTCompound loreNbt = NBTEditor.getNBTCompound(itemstack, "tag", "display", "Lore");
        List<Component> loreLines = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String line = NBTEditor.getString(loreNbt, i);
            try {
                if (JsonUtils.isValid(line)) {
                    loreLines.add(InteractiveChatComponentSerializer.gson().deserialize(line));
                } else {
                    loreLines.add(LegacyComponentSerializer.legacySection().deserialize(line));
                }
            } catch (Throwable e) {
                loreLines.add(LegacyComponentSerializer.legacySection().deserialize(line));
            }
        }
        return loreLines;
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
        try {
            return (ItemStack) asBukkitCopyMethod.invoke(null, handle);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return null;
        }
    }

    public static Object toNMSCopy(ItemStack itemstack) {
        try {
            return asNMSCopyMethod.invoke(null, itemstack);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return null;
        }
    }

}
