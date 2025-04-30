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

import com.loohp.interactivechat.nms.NMS;
import com.loohp.interactivechat.objectholders.ICInventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class InventoryUtils {

    public static final boolean CAN_USE_DROPPER_TYPE = platformSupportsDropperHolder();

    private static boolean platformSupportsDropperHolder() {
        Inventory inventory = Bukkit.createInventory(ICInventoryHolder.INSTANCE, InventoryType.DROPPER, "Title");
        return inventory.getHolder() instanceof ICInventoryHolder;
    }

    private static final ItemStack ITEM_STACK_AIR = new ItemStack(Material.AIR);

    public static int getDefaultEnderChestSize() {
        return Math.min(54, Math.max(9, toMultipleOf9(InventoryType.ENDER_CHEST.getDefaultSize())));
    }

    public static int toMultipleOf9(int num) {
        int remainder = num % 9;
        return remainder == 0 ? num : num + (9 - remainder);
    }

    public static void restorePlayerInventory(Player player) {
        sendFakePlayerInventory(player, player.getInventory(), true, true);
    }

    public static void sendFakePlayerInventory(Player player, Inventory inventory, boolean armor, boolean offhand) {
        NMS.getInstance().sendFakePlayerInventory(player, inventory, armor, offhand);
    }

    public static String toBase64(Inventory inventory) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(inventory.getSize());

            // Save every element in the list
            for (int i = 0; i < inventory.getSize(); i++) {
                dataOutput.writeObject(inventory.getItem(i));
            }

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static Inventory fromBase64(String data, InventoryType type, String title, InventoryHolder holder) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Inventory inventory;
            if (type.equals(InventoryType.CHEST)) {
                if (title == null || title.equals("")) {
                    inventory = Bukkit.getServer().createInventory(holder, dataInput.readInt());
                } else {
                    inventory = Bukkit.getServer().createInventory(holder, dataInput.readInt(), title);
                }
            } else {
                dataInput.readInt();
                if (title == null || title.equals("")) {
                    inventory = Bukkit.getServer().createInventory(holder, type);
                } else {
                    inventory = Bukkit.getServer().createInventory(holder, type, title);
                }
            }

            // Read the serialized inventory
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, (ItemStack) dataInput.readObject());
            }

            dataInput.close();
            return inventory;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    public static Inventory fromBase64(String data, String title, InventoryHolder holder) throws IOException {
        return fromBase64(data, InventoryType.CHEST, title, holder);
    }

}
