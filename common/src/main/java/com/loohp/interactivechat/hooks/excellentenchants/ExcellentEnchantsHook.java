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

package com.loohp.interactivechat.hooks.excellentenchants;

import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.hook.HookPlugin;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.util.Plugins;

public class ExcellentEnchantsHook {

    public static void init() {
        Plugin excellentEnchants = Bukkit.getPluginManager().getPlugin("ExcellentEnchants");
        if (Config.isDescriptionEnabled() && (Plugins.isInstalled(HookPlugin.PACKET_EVENTS) || Plugins.isInstalled(HookPlugin.PROTOCOL_LIB))) {
            InteractiveChatAPI.registerItemStackTransformProvider(excellentEnchants, 1, (itemStack, uuid) -> {
                ICPlayer icPlayer = ICPlayerFactory.getICPlayer(uuid);
                return setExcellentEnchantsLore(itemStack, icPlayer);
            });
        }
    }

    public static ItemStack setExcellentEnchantsLore(ItemStack itemStack, ICPlayer icPlayer) {
        if (icPlayer.isLocal() && !EnchantUtils.canUpdateDisplay(icPlayer.getLocalPlayer())) {
            return itemStack;
        }
        return EnchantUtils.addDescription(itemStack);
    }

}
