package com.loohp.interactivechat.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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

    private static final BiMap<String, Integer> enchantmentIds = HashBiMap.create();

    static {
        try {
            craftItemStackClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.inventory.CraftItemStack");
            nmsItemStackClass = NMSUtils.getNMSClass("net.minecraft.server.%s.ItemStack", "net.minecraft.world.item.ItemStack");
            asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            nmsNbtTagCompoundClass = NMSUtils.getNMSClass("net.minecraft.server.%s.NBTTagCompound", "net.minecraft.nbt.NBTTagCompound");
            try {
                saveNmsItemStackMethod = nmsItemStackClass.getMethod("save", nmsNbtTagCompoundClass);
            } catch (Exception e) {
                saveNmsItemStackMethod = nmsItemStackClass.getMethod("b", nmsNbtTagCompoundClass);
            }
            nbtTagCompoundConstructor = nmsNbtTagCompoundClass.getConstructor();
            nmsMojangsonParserClass = NMSUtils.getNMSClass("net.minecraft.server.%s.MojangsonParser", "net.minecraft.nbt.MojangsonParser");
            try {
                parseMojangsonMethod = nmsMojangsonParserClass.getMethod("parse", String.class);
            } catch (Exception e) {
                parseMojangsonMethod = nmsMojangsonParserClass.getMethod("a", String.class);
            }
            if (InteractiveChat.version.isOld()) {
                nmsItemStackFromTagMethod = nmsItemStackClass.getMethod("createStack", nmsNbtTagCompoundClass);
            } else {
                nmsItemStackFromTagConstructor = nmsItemStackClass.getDeclaredConstructor(nmsNbtTagCompoundClass);
            }
            asBukkitCopyMethod = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
            try {
                nbtTagCompoundGetStringMethod = nmsNbtTagCompoundClass.getMethod("getString", String.class);
            } catch (Exception e) {
                nbtTagCompoundGetStringMethod = nmsNbtTagCompoundClass.getMethod("l", String.class);
            }
            try {
                nbtTagCompoundGetMethod = nmsNbtTagCompoundClass.getMethod("get", String.class);
            } catch (Exception e) {
                nbtTagCompoundGetMethod = nmsNbtTagCompoundClass.getMethod("c", String.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        enchantmentIds.put("protection", 0);
        enchantmentIds.put("fire_protection", 1);
        enchantmentIds.put("feather_falling", 2);
        enchantmentIds.put("blast_protection", 3);
        enchantmentIds.put("projectile_protection", 4);
        enchantmentIds.put("respiration", 5);
        enchantmentIds.put("aqua_affinity", 6);
        enchantmentIds.put("thorns", 7);
        enchantmentIds.put("depth_strider", 8);
        enchantmentIds.put("frost_walker", 9);
        enchantmentIds.put("binding_curse", 10);
        enchantmentIds.put("sharpness", 16);
        enchantmentIds.put("smite", 17);
        enchantmentIds.put("bane_of_arthropods", 18);
        enchantmentIds.put("knockback", 19);
        enchantmentIds.put("fire_aspect", 20);
        enchantmentIds.put("looting", 21);
        enchantmentIds.put("sweeping", 22);
        enchantmentIds.put("efficiency", 32);
        enchantmentIds.put("silk_touch", 33);
        enchantmentIds.put("unbreaking", 34);
        enchantmentIds.put("fortune", 35);
        enchantmentIds.put("power", 48);
        enchantmentIds.put("punch", 49);
        enchantmentIds.put("flame", 50);
        enchantmentIds.put("infinity", 51);
        enchantmentIds.put("luck_of_the_sea", 61);
        enchantmentIds.put("lure", 62);
        enchantmentIds.put("mending", 70);
        enchantmentIds.put("vanishing_curse", 71);
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
