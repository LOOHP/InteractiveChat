/*
 * This file is part of InteractiveChat.
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

import com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Base64;
import java.util.UUID;

public class SkinUtils {

    private static final String PLAYER_INFO_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s";

    public static String getSkinJsonFromProfile(Player player) {
        return new String(Base64.getDecoder().decode(getSkinValue(player)));
    }

    public static String getSkinValue(Player player) {
        return NMS.getInstance().getSkinValue(player);
    }

    public static String getSkinValue(ItemMeta skull) {
        return NMS.getInstance().getSkinValue(skull);
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

        Player player = Bukkit.getPlayer(uuid);
        try {
            if (player != null) {
                String base64 = getSkinValue(player);
                if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_20_5)) {
                    head = Bukkit.getUnsafe().modifyItemStack(head, "minecraft:player_head[minecraft:profile={properties:[{name:\"textures\",value:\"" + base64 + "\"}]}]");
                } else {
                    head = Bukkit.getUnsafe().modifyItemStack(head, "{SkullOwner: {Properties: {textures: [{Value: \"" + base64 + "\"}]}}}");
                }
            }
        } catch (Throwable ignore) {
        }

        return head;
    }

    public static String getSkinURLFromUUID(UUID uuid) throws Exception {
        JSONObject jsonResponse = HTTPRequestUtils.getJSONResponse(PLAYER_INFO_URL.replaceFirst("%s", uuid.toString()));
        if (jsonResponse.containsKey("errorMessage")) {
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
