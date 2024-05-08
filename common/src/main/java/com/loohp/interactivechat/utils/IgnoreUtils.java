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

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.loohp.interactivechat.InteractiveChat;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import org.bukkit.Bukkit;

import java.util.UUID;

public class IgnoreUtils {

    public static boolean isIgnore(UUID source, UUID target) {
        if (InteractiveChat.ventureChatHook) {
            MineverseChatPlayer venturePlayer = MineverseChatAPI.getMineverseChatPlayer(source);
            if (venturePlayer != null && venturePlayer.getIgnores().contains(target)) {
                return true;
            }
        }

        if (InteractiveChat.cmiHook) {
            CMIUser user = CMI.getInstance().getPlayerManager().getUser(source);
            if (user != null && user.isIgnoring(target)) {
                return true;
            }
        }

        if (InteractiveChat.essentialsHook) {
            Essentials ess3 = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            User sourceUser = ess3.getUser(source);
            User targetUser = ess3.getUser(target);
            return sourceUser != null && targetUser != null && sourceUser.isIgnoredPlayer(targetUser);
        }

        return false;
    }

}
