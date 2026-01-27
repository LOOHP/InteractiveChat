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
import su.nightexpress.excellentenchants.EnchantsAPI;
import su.nightexpress.excellentenchants.enchantment.EnchantRegistry;
import su.nightexpress.excellentenchants.tooltip.TooltipManager;
import su.nightexpress.excellentenchants.tooltip.TooltipPlugins;
import su.nightexpress.nightcore.util.Plugins;

public class ExcellentEnchantsHook {

    public static void init() {
        Plugin excellentEnchants = Bukkit.getPluginManager().getPlugin("ExcellentEnchants");
        if (EnchantsAPI.getPlugin().getTooltipManager() != null && (Plugins.isInstalled(TooltipPlugins.PACKET_EVENTS) || Plugins.isInstalled(TooltipPlugins.PROTOCOL_LIB))) {
            InteractiveChatAPI.registerItemStackTransformProvider(excellentEnchants, 1, (itemStack, uuid) -> {
                ICPlayer icPlayer = ICPlayerFactory.getICPlayer(uuid);
                return setExcellentEnchantsInfo(itemStack, icPlayer);
            });
        }
    }

    public static ItemStack setExcellentEnchantsInfo(ItemStack itemStack, ICPlayer icPlayer) {
        TooltipManager tooltipManager = EnchantsAPI.getPlugin().getTooltipManager();
        if (tooltipManager == null) {
            return itemStack;
        }
        if (icPlayer != null && icPlayer.isLocal() && tooltipManager.isReadyForTooltipUpdate(icPlayer.getLocalPlayer())) {
            itemStack = tooltipManager.addDescription(itemStack);
        }
        if (InteractiveChat.excellentEnchantsStripEnchantments && itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            for (Enchantment enchantment : itemMeta.getEnchants().keySet()) {
                if (EnchantRegistry.isRegistered(enchantment)) {
                    itemMeta.removeEnchant(enchantment);
                }
            }
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

}
