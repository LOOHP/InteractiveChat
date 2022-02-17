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
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Collection;
import java.util.UUID;

public class SkinUtils {

    private static final String PLAYER_INFO_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s";

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
            try {
                nmsEntityPlayerGetProfileMethod = nmsEntityPlayerClass.getMethod("getProfile");
            } catch (Exception e) {
                nmsEntityPlayerGetProfileMethod = nmsEntityPlayerClass.getMethod("fp");
            }
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
                if (!property.getValue().isEmpty()) {
                    return property.getValue();
                }
            }
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack getSkull(UUID uuid) {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_12)) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        } else {
            meta.setOwner(uuid.toString());
        }

        head.setItemMeta(meta);
        return head;
    }

    public static String getSkinURLFromUUID(UUID uuid) throws Exception {
        JSONObject jsonResponse = HTTPRequestUtils.getJSONResponse(PLAYER_INFO_URL.replaceFirst("%s", uuid.toString()));
        if (jsonResponse.containsKey("error")) {
            throw new RuntimeException("Unable to retrieve skin url from Mojang servers for the player " + uuid);
        }
        JSONArray propertiesArray = (JSONArray) jsonResponse.get("properties");
        for (Object obj : propertiesArray) {
            JSONObject property = (JSONObject) obj;
            if (property.get("name").toString().equals("textures")) {
                String base64 = property.get("value").toString();
                JSONObject textureJson = (JSONObject) new JSONParser().parse(new String(Base64.getDecoder().decode(base64)));
                return ((JSONObject) ((JSONObject) textureJson.get("textures")).get("SKIN")).get("url").toString();
            }
        }
        throw new RuntimeException("Unable to retrieve skin url from Mojang servers for the player " + uuid);
    }


}
