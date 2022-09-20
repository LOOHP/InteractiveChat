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

package com.loohp.interactivechat.hooks.ecoenchants;

import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.willfp.eco.core.display.Display;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class EcoHook {

    public static void init() {
        Plugin eco = Bukkit.getPluginManager().getPlugin("eco");

        InteractiveChatAPI.registerItemStackTransformProvider(eco, 1, (itemStack, uuid) -> {
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                return itemStack;
            }
            if (uuid == null) {
                return setEcoLores(itemStack);
            }
            ICPlayer icPlayer = ICPlayerFactory.getICPlayer(uuid);
            if (icPlayer == null || !icPlayer.isLocal()) {
                return setEcoLores(itemStack);
            }
            return setEcoLores(itemStack, icPlayer.getLocalPlayer());
        });
    }

    public static ItemStack setEcoLores(ItemStack itemStack) {
        return setEcoLores(itemStack, null);
    }

    public static ItemStack setEcoLores(ItemStack itemStack, Player player) {
        return Display.displayAndFinalize(itemStack.clone(), player);
    }

}
