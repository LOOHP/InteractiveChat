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

package com.loohp.interactivechat.placeholderapi;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.data.PlayerDataManager.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion {

    @Override
    public String getAuthor() {
        return String.join(", ", InteractiveChat.plugin.getDescription().getAuthors());
    }

    @Override
    public String getIdentifier() {
        return "interactivechat";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getRequiredPlugin() {
        return InteractiveChat.plugin.getName();
    }

    @Override
    public String onRequest(OfflinePlayer offlineplayer, String identifier) {

        if (identifier.equals("mentiontoggle")) {
            PlayerData pd;
            if (offlineplayer.isOnline()) {
                Player player = offlineplayer.getPlayer();
                pd = InteractiveChat.playerDataManager.getPlayerData(player);
            } else {
                pd = InteractiveChat.database.getPlayerInfo(offlineplayer.getUniqueId());
            }
            return pd == null ? "enabled" : (pd.isMentionDisabled() ? "disabled" : "enabled");
        }

        if (identifier.equals("invdisplaylayout")) {
            PlayerData pd;
            if (offlineplayer.isOnline()) {
                Player player = offlineplayer.getPlayer();
                pd = InteractiveChat.playerDataManager.getPlayerData(player);
            } else {
                pd = InteractiveChat.database.getPlayerInfo(offlineplayer.getUniqueId());
            }
            return (pd == null ? InteractiveChat.invDisplayLayout : pd.getInventoryDisplayLayout()) + "";
        }

        return null;
    }

}
