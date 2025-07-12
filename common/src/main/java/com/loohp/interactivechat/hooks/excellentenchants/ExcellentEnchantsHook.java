/*
 * This file is part of InteractiveChat4.
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

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import su.nightexpress.excellentenchants.api.EnchantRegistry;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.hook.HookPlugin;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.util.Plugins;

import java.util.stream.Collectors;

public class ExcellentEnchantsHook {

    public static final String NAMESPACE = "excellentenchants";

    public static void init() {
        Plugin excellentEnchants = Bukkit.getPluginManager().getPlugin("ExcellentEnchants");
        if (Config.isDescriptionEnabled() && (Plugins.isInstalled(HookPlugin.PACKET_EVENTS) || Plugins.isInstalled(HookPlugin.PROTOCOL_LIB))) {
            InteractiveChatAPI.registerItemStackTransformProvider(excellentEnchants, 1, (itemStack, uuid) -> {
                ICPlayer icPlayer = ICPlayerFactory.getICPlayer(uuid);
                return setExcellentEnchantsInfo(itemStack, icPlayer);
            });
        }
    }

    public static ItemStack setExcellentEnchantsInfo(ItemStack itemStack, ICPlayer icPlayer) {
        if (icPlayer != null && icPlayer.isLocal() && EnchantUtils.canUpdateDisplay(icPlayer.getLocalPlayer())) {
            itemStack = EnchantUtils.addDescription(itemStack);
        }
        if (InteractiveChat.excellentEnchantsStripEnchantments && itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            System.out.println(EnchantRegistry.getRegistered().stream().map(e -> e.getBukkitEnchantment().getKey()).collect(Collectors.toList()));
            for (Enchantment enchantment : itemMeta.getEnchants().keySet()) {
                System.out.println(enchantment.getKey() + " -> " + EnchantRegistry.isRegistered(enchantment));
                if (EnchantRegistry.isRegistered(enchantment)) {
                    itemMeta.removeEnchant(enchantment);
                }
            }
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

}
