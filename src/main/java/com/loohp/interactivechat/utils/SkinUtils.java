package com.loohp.interactivechat.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.InteractiveChat;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public class SkinUtils {
	
	private static Class<?> craftPlayerClass;
	private static Class<?> nmsEntityPlayerClass;
	private static Method craftPlayerGetHandleMethod;
	private static Method nmsEntityPlayerGetProfileMethod;
	private static Class<?> craftSkullMetaClass;
	private static Field craftSkullMetaProfileField;

	static {
		try {
			craftPlayerClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.entity.CraftPlayer");
			nmsEntityPlayerClass = NMSUtils.getNMSClass("net.minecraft.server.%s.EntityPlayer", "net.minecraft.server.level.EntityPlayer");
			craftPlayerGetHandleMethod = craftPlayerClass.getMethod("getHandle");
			nmsEntityPlayerGetProfileMethod = nmsEntityPlayerClass.getMethod("getProfile");
			craftSkullMetaClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.inventory.CraftMetaSkull");
			craftSkullMetaProfileField = craftSkullMetaClass.getDeclaredField("profile");
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public static String getSkinJsonFromProfile(Player player) throws Exception {
		Object playerNMS = craftPlayerGetHandleMethod.invoke(craftPlayerClass.cast(player));
		GameProfile profile = (GameProfile) nmsEntityPlayerGetProfileMethod.invoke(playerNMS);
		Collection<Property> textures = profile.getProperties().get("textures");
		if (textures == null || textures.isEmpty()) {
			return null;
		}
		Property property = textures.iterator().next();
		return new String(Base64.getDecoder().decode(property.getValue()));
	}
	
	public static String getSkinValue(ItemMeta skull) {
        SkullMeta meta = (SkullMeta) skull;
        GameProfile profile = null;

        try {
            craftSkullMetaProfileField.setAccessible(true);
            profile = (GameProfile) craftSkullMetaProfileField.get(meta);
        } catch (SecurityException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        if (profile != null && !profile.getProperties().get("textures").isEmpty()) {
            for (Property property : profile.getProperties().get("textures")) {
                if (!property.getValue().isEmpty()) return property.getValue();
            }
        }

        return null;
    }
	
	@SuppressWarnings("deprecation")
	public static ItemStack getSkull(UUID uuid) {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_12)) meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        else meta.setOwner(uuid.toString());

        head.setItemMeta(meta);
        return head;
    }


}
