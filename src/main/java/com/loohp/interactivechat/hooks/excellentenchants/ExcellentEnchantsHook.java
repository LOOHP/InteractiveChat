/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2023. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2023. Contributors
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

package com.loohp.interactivechat.hooks.excellentenchants;

import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.hook.impl.ProtocolHook;
import su.nightexpress.nightcore.util.Plugins;

public class ExcellentEnchantsHook {

    public static void init() {
        Plugin eco = Bukkit.getPluginManager().getPlugin("ExcellentEnchants");

        if (Config.ENCHANTMENTS_DISPLAY_MODE.get() == 2 && Plugins.isLoaded("ProtocolLib")) {
            InteractiveChatAPI.registerItemStackTransformProvider(eco, 1, (itemStack, uuid) -> {
                ICPlayer icPlayer = ICPlayerFactory.getICPlayer(uuid);
                boolean isCreative = icPlayer != null && icPlayer.isLocal() && icPlayer.getLocalPlayer().getGameMode().equals(GameMode.CREATIVE);
                return setExcellentEnchantsLore(itemStack, isCreative);
            });
        }
    }

    public static ItemStack setExcellentEnchantsLore(ItemStack itemStack, boolean isCreative) {
        return ProtocolHook.update(itemStack, isCreative);
    }

}
