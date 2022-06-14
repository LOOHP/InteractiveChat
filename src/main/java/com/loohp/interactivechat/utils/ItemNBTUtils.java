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
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ItemNBTUtils {

    private static Class<?> craftItemStackClass;
    private static Class<?> nmsItemStackClass;
    private static Method asNMSCopyMethod;
    private static Class<?> nmsNbtTagCompoundClass;
    private static Method saveNmsItemStackMethod;
    private static Constructor<?> nmsItemStackFromTagConstructor;
    private static Method nmsItemStackFromTagMethod;
    private static Constructor<?> nbtTagCompoundConstructor;
    private static Class<?> nmsMojangsonParserClass;
    private static Method parseMojangsonMethod;
    private static Method asBukkitCopyMethod;
    private static Method nbtTagCompoundGetStringMethod;
    private static Method nbtTagCompoundGetMethod;

    static {
        try {
            craftItemStackClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.inventory.CraftItemStack");
            nmsItemStackClass = NMSUtils.getNMSClass("net.minecraft.server.%s.ItemStack", "net.minecraft.world.item.ItemStack");
            asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            nmsNbtTagCompoundClass = NMSUtils.getNMSClass("net.minecraft.server.%s.NBTTagCompound", "net.minecraft.nbt.NBTTagCompound");
            saveNmsItemStackMethod = NMSUtils.reflectiveLookup(Method.class, () -> {
                return nmsItemStackClass.getMethod("save", nmsNbtTagCompoundClass);
            }, () -> {
                return nmsItemStackClass.getMethod("b", nmsNbtTagCompoundClass);
            });
            nbtTagCompoundConstructor = nmsNbtTagCompoundClass.getConstructor();
            nmsMojangsonParserClass = NMSUtils.getNMSClass("net.minecraft.server.%s.MojangsonParser", "net.minecraft.nbt.MojangsonParser");
            parseMojangsonMethod = NMSUtils.reflectiveLookup(Method.class, () -> {
                return nmsMojangsonParserClass.getMethod("parse", String.class);
            }, () -> {
                return nmsMojangsonParserClass.getMethod("a", String.class);
            });
            if (InteractiveChat.version.isOld()) {
                nmsItemStackFromTagMethod = nmsItemStackClass.getMethod("createStack", nmsNbtTagCompoundClass);
            } else {
                nmsItemStackFromTagConstructor = nmsItemStackClass.getDeclaredConstructor(nmsNbtTagCompoundClass);
            }
            asBukkitCopyMethod = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
            nbtTagCompoundGetStringMethod = NMSUtils.reflectiveLookup(Method.class, () -> {
                return nmsNbtTagCompoundClass.getMethod("getString", String.class);
            }, () -> {
                return nmsNbtTagCompoundClass.getMethod("l", String.class);
            });
            nbtTagCompoundGetMethod = NMSUtils.reflectiveLookup(Method.class, () -> {
                return nmsNbtTagCompoundClass.getMethod("get", String.class);
            }, () -> {
                return nmsNbtTagCompoundClass.getMethod("c", String.class);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ItemStack getItemFromNBTJson(String json) {
        try {
            Object nmsNbtTagCompoundObj = parseMojangsonMethod.invoke(null, json);
            Object nmsItemStackObj;
            if (InteractiveChat.version.isOld()) {
                nmsItemStackObj = nmsItemStackFromTagMethod.invoke(null, nmsNbtTagCompoundObj);
            } else {
                nmsItemStackFromTagConstructor.setAccessible(true);
                nmsItemStackObj = nmsItemStackFromTagConstructor.newInstance(nmsNbtTagCompoundObj);
            }
            return (ItemStack) asBukkitCopyMethod.invoke(null, nmsItemStackObj);
        } catch (Throwable e) {
            return null;
        }
    }

    public static String getNMSItemStackJson(ItemStack itemStack) {
        try {
            Object nmsNbtTagCompoundObj = nbtTagCompoundConstructor.newInstance();
            Object nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
            Object itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
            return itemAsJsonObject.toString();
        } catch (Throwable t) {
            return "{}";
        }
    }

    public static Key getNMSItemStackNamespacedKey(ItemStack itemStack) {
        try {
            Object nmsNbtTagCompoundObj = nbtTagCompoundConstructor.newInstance();
            Object nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
            Object itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
            String namespacedKeyString = nbtTagCompoundGetStringMethod.invoke(itemAsJsonObject, "id").toString();
            return Key.key(namespacedKeyString);
        } catch (Throwable t) {
            return Key.key("air");
        }
    }

    public static String getNMSItemStackTag(ItemStack itemStack) {
        try {
            Object nmsNbtTagCompoundObj = nbtTagCompoundConstructor.newInstance();
            Object nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
            Object itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
            return nbtTagCompoundGetMethod.invoke(itemAsJsonObject, "tag").toString();
        } catch (Throwable t) {
            return null;
        }
    }

}
