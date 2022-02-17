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
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.loohp.interactivechat.InteractiveChat;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;

public class DataTypeIO {

    public static Inventory readInventory(ByteArrayDataInput in, Charset charset) throws IOException {
        int encodingScheme = in.readByte();
        InventoryType type = InventoryType.valueOf(readString(in, charset));
        boolean hasTitle = in.readBoolean();
        String title = hasTitle ? readString(in, charset) : null;
        switch (encodingScheme) {
            case 0:
                String data = readString(in, charset);
                return InventoryUtils.fromBase64(data, title);
            case 1:
                int size = in.readInt();
                Inventory inventory;
                if (type.equals(InventoryType.CHEST)) {
                    inventory = hasTitle ? Bukkit.createInventory(null, size + (size % 9), title) : Bukkit.createInventory(null, size + (size % 9));
                } else {
                    inventory = hasTitle ? Bukkit.createInventory(null, type, title) : Bukkit.createInventory(null, type);
                }
                for (int i = 0; i < inventory.getSize(); i++) {
                    inventory.setItem(i, readItemStack(in, charset));
                }
                return inventory;
            default:
                throw new IllegalArgumentException("Unknown encodingScheme version!");
        }
    }

    public static void writeInventory(ByteArrayDataOutput out, int encodingScheme, String title, Inventory inventory, Charset charset) throws IOException {
        out.writeByte(encodingScheme);
        writeString(out, inventory.getType().name(), charset);
        if (title == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            writeString(out, title, charset);
        }
        switch (encodingScheme) {
            case 0:
                writeString(out, InventoryUtils.toBase64(inventory), charset);
                break;
            case 1:
                out.writeInt(inventory.getSize());
                for (int i = 0; i < inventory.getSize(); i++) {
                    writeItemStack(out, 1, inventory.getItem(i), charset);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown encodingScheme version!");
        }
    }

    @SuppressWarnings("deprecation")
    public static ItemStack readItemStack(ByteArrayDataInput in, Charset charset) throws IOException {
        int encodingScheme = in.readByte();
        switch (encodingScheme) {
            case 0:
                String data = readString(in, charset);
                YamlConfiguration config = new YamlConfiguration();
                try {
                    config.loadFromString(data);
                } catch (Throwable e) {
                    return null;
                }
                return config.getItemStack("i", null);
            case 1:
                if (in.readBoolean()) {
                    XMaterial material = XMaterial.valueOf(readString(in, charset));
                    ItemStack itemStack = material.parseItem();
                    if (itemStack == null) {
                        itemStack = InteractiveChat.unknownReplaceItem.clone();
                        ItemMeta meta = itemStack.getItemMeta();
                        meta.setDisplayName(meta.getDisplayName().replace("{Type}", material.toString()));
                        itemStack.setItemMeta(meta);
                        itemStack.setAmount(in.readInt());

                        if (in.readBoolean()) {
                            in.readInt();
                        }
                        readString(in, charset);
                    } else {
                        itemStack.setAmount(in.readInt());
                        boolean setDurability = in.readBoolean();
                        int durability = setDurability ? in.readInt() : -1;
                        String nbtStr = readString(in, charset);
                        ItemStack fromTag = ItemNBTUtils.getItemFromNBTJson(nbtStr);
                        if (fromTag != null && fromTag.getType().equals(itemStack.getType())) {
                            itemStack = fromTag;
                        }
                        if (setDurability) {
                            if (InteractiveChat.version.isLegacy()) {
                                itemStack.setDurability((short) durability);
                            } else {
                                ItemMeta meta = itemStack.getItemMeta();
                                ((Damageable) meta).setDamage(durability);
                                itemStack.setItemMeta(meta);
                            }
                        }
                    }
                    return itemStack;
                } else {
                    return null;
                }
            default:
                throw new IllegalArgumentException("Unknown encodingScheme version!");
        }
    }

    @SuppressWarnings("deprecation")
    public static void writeItemStack(ByteArrayDataOutput out, int defaultEncodingScheme, ItemStack itemStack, Charset charset) throws IOException {
        int encodingScheme = defaultEncodingScheme;
        ByteArrayDataOutput itemByte = ByteStreams.newDataOutput();
        switch (encodingScheme) {
            case 0:
                try {
                    YamlConfiguration config = new YamlConfiguration();
                    config.set("i", itemStack);
                    writeString(itemByte, config.saveToString(), charset);
                    break;
                } catch (Throwable e) {
                    //Fallback to encodingScheme 1
                    encodingScheme = 1;
                    itemByte = ByteStreams.newDataOutput();
                }
            case 1:
                if (itemStack == null) {
                    itemByte.writeBoolean(false);
                } else {
                    itemByte.writeBoolean(true);
                    XMaterial material = FilledMapUtils.isFilledMap(itemStack) ? XMaterial.FILLED_MAP : XMaterialUtils.matchXMaterial(itemStack);
                    writeString(itemByte, material.name(), charset);
                    itemByte.writeInt(itemStack.getAmount());
                    boolean isDamagable = itemStack.getType().getMaxDurability() > 0;
                    if (isDamagable) {
                        itemByte.writeBoolean(true);
                        itemByte.writeInt(InteractiveChat.version.isLegacy() ? itemStack.getDurability() : ((Damageable) itemStack.getItemMeta()).getDamage());
                    } else {
                        itemByte.writeBoolean(false);
                    }
                    String nbt = ItemNBTUtils.getNMSItemStackJson(itemStack);
                    writeString(itemByte, nbt, charset);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown encodingScheme version!");
        }
        out.writeByte(encodingScheme);
        out.write(itemByte.toByteArray());
    }

    public static UUID readUUID(ByteArrayDataInput in) throws IOException {
        return new UUID(in.readLong(), in.readLong());
    }

    public static void writeUUID(ByteArrayDataOutput out, UUID uuid) throws IOException {
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
    }

    public static String readString(ByteArrayDataInput in, Charset charset) throws IOException {
        int length = in.readInt();

        if (length == -1) {
            throw new IOException("Premature end of stream.");
        }

        byte[] b = new byte[length];
        in.readFully(b);
        return new String(b, charset);
    }

    public static int getStringLength(String string, Charset charset) throws IOException {
        byte[] bytes = string.getBytes(charset);
        return bytes.length;
    }

    public static void writeString(ByteArrayDataOutput out, String string, Charset charset) throws IOException {
        byte[] bytes = string.getBytes(charset);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

}
