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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.loohp.interactivechat.InteractiveChat;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ItemNBTUtils {

    private static final BiMap<String, Integer> ENCHANTMENT_IDS = HashBiMap.create();

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

        ENCHANTMENT_IDS.put("protection", 0);
        ENCHANTMENT_IDS.put("fire_protection", 1);
        ENCHANTMENT_IDS.put("feather_falling", 2);
        ENCHANTMENT_IDS.put("blast_protection", 3);
        ENCHANTMENT_IDS.put("projectile_protection", 4);
        ENCHANTMENT_IDS.put("respiration", 5);
        ENCHANTMENT_IDS.put("aqua_affinity", 6);
        ENCHANTMENT_IDS.put("thorns", 7);
        ENCHANTMENT_IDS.put("depth_strider", 8);
        ENCHANTMENT_IDS.put("frost_walker", 9);
        ENCHANTMENT_IDS.put("binding_curse", 10);
        ENCHANTMENT_IDS.put("sharpness", 16);
        ENCHANTMENT_IDS.put("smite", 17);
        ENCHANTMENT_IDS.put("bane_of_arthropods", 18);
        ENCHANTMENT_IDS.put("knockback", 19);
        ENCHANTMENT_IDS.put("fire_aspect", 20);
        ENCHANTMENT_IDS.put("looting", 21);
        ENCHANTMENT_IDS.put("sweeping", 22);
        ENCHANTMENT_IDS.put("efficiency", 32);
        ENCHANTMENT_IDS.put("silk_touch", 33);
        ENCHANTMENT_IDS.put("unbreaking", 34);
        ENCHANTMENT_IDS.put("fortune", 35);
        ENCHANTMENT_IDS.put("power", 48);
        ENCHANTMENT_IDS.put("punch", 49);
        ENCHANTMENT_IDS.put("flame", 50);
        ENCHANTMENT_IDS.put("infinity", 51);
        ENCHANTMENT_IDS.put("luck_of_the_sea", 61);
        ENCHANTMENT_IDS.put("lure", 62);
        ENCHANTMENT_IDS.put("mending", 70);
        ENCHANTMENT_IDS.put("vanishing_curse", 71);
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
	/*
	@SuppressWarnings("deprecation")
	public static String convertToVersion(XMaterial material, String jsonNbt, MCVersion version) {
		try {
			PacketDataSerializer data = new PacketDataSerializer(Unpooled.buffer());
			data.a(MojangsonParser.parse(jsonNbt));
			CompoundTag nbt = (CompoundTag) new NBTInputStream(new ByteArrayInputStream(data.array())).readRawTag(512);
			if (version.isLegacy()) {
				if (InteractiveChat.version.isLegacy()) {
					nbt.putInt("id", material.parseMaterial().getId());
					nbt.putShort("Damage", material.getData());
				} else {
					nbt.putInt("id", ((Material) toLegacyMethod.invoke(null, material.parseMaterial())).getId());
					nbt.putShort("Damage", material.getData());
				}
				if (nbt.containsKey("tag") && nbt.getCompoundTag("tag").containsKey("Enchantments")) {
					String ench = SNBTUtil.toSNBT(nbt.getCompoundTag("tag").getCompoundTag("Enchantments"));
					nbt.getCompoundTag("tag").remove("Enchantments");
					for (Entry<String, Integer> entry : enchantmentIds.entrySet()) {
						ench = ench.replace("id:\"minecraft:" + entry.getKey() + "\"", "id:\"" + entry.getValue() + "s\"");
					}
					nbt.getCompoundTag("tag").put("ench", SNBTUtil.fromSNBT(ench));
				}
				if (nbt.containsKey("tag") && nbt.getCompoundTag("tag").containsKey("StoredEnchantments")) {
					String ench = SNBTUtil.toSNBT(nbt.getCompoundTag("tag").getCompoundTag("StoredEnchantments"));
					nbt.getCompoundTag("tag").remove("StoredEnchantments");
					for (Entry<String, Integer> entry : enchantmentIds.entrySet()) {
						ench = ench.replace("id:\"minecraft:" + entry.getKey() + "\"", "id:\"" + entry.getValue() + "s\"");
					}
					nbt.getCompoundTag("tag").put("ench", SNBTUtil.fromSNBT(ench));
				}
				return SNBTUtil.toSNBT(nbt);
			} else {
				return "{}";
			}
		} catch (Throwable t) {
			t.printStackTrace();
	        return "{}";
	    }
	}
	*/
}
