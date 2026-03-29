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

import java.lang.reflect.Method;

public class ExcellentEnchantsHook {

    private static volatile boolean tooltipTransformsEnabled = true;
    private static volatile boolean disableMessagePrinted = false;

    private static volatile boolean readyCheckInitialized = false;
    private static volatile boolean readyCheckSupported = false;
    private static Method readyCheckMethod;

    public static boolean init() {
        try {
            Plugin excellentEnchants = Bukkit.getPluginManager().getPlugin("ExcellentEnchants");
            if (excellentEnchants == null) {
                return false;
            }

            TooltipManager tooltipManager = EnchantsAPI.getPlugin().getTooltipManager();
            boolean packetHookInstalled = Plugins.isInstalled(TooltipPlugins.PACKET_EVENTS) || Plugins.isInstalled(TooltipPlugins.PROTOCOL_LIB);
            if (tooltipManager == null || !packetHookInstalled) {
                return false;
            }

            InteractiveChatAPI.registerItemStackTransformProvider(excellentEnchants, 1, (itemStack, uuid) -> {
                ICPlayer icPlayer = ICPlayerFactory.getICPlayer(uuid);
                return setExcellentEnchantsInfo(itemStack, icPlayer);
            });
            return true;
        } catch (Throwable throwable) {
            disableTooltipTransforms(throwable);
            return false;
        }
    }

    public static ItemStack setExcellentEnchantsInfo(ItemStack itemStack, ICPlayer icPlayer) {
        TooltipManager tooltipManager = null;
        if (tooltipTransformsEnabled) {
            try {
                tooltipManager = EnchantsAPI.getPlugin().getTooltipManager();
            } catch (Throwable throwable) {
                disableTooltipTransforms(throwable);
            }
        }

        if (tooltipTransformsEnabled && tooltipManager != null && canApplyTooltip(tooltipManager, icPlayer)) {
            try {
                itemStack = tooltipManager.addDescription(itemStack);
            } catch (Throwable throwable) {
                disableTooltipTransforms(throwable);
            }
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

    private static boolean canApplyTooltip(TooltipManager tooltipManager, ICPlayer icPlayer) {
        if (icPlayer == null || !icPlayer.isLocal() || icPlayer.getLocalPlayer() == null) {
            return false;
        }

        initializeReadyCheckMethod();
        if (!readyCheckSupported || readyCheckMethod == null) {
            return true;
        }

        try {
            Object result = readyCheckMethod.invoke(tooltipManager, icPlayer.getLocalPlayer());
            return (result instanceof Boolean) && (Boolean) result;
        } catch (Throwable throwable) {
            disableTooltipTransforms(throwable);
            return false;
        }
    }

    private static synchronized void initializeReadyCheckMethod() {
        if (readyCheckInitialized) {
            return;
        }

        readyCheckInitialized = true;
        try {
            readyCheckMethod = TooltipManager.class.getMethod("isReadyForTooltipUpdate", org.bukkit.entity.Player.class);
            readyCheckSupported = true;
        } catch (NoSuchMethodException ignored) {
            readyCheckSupported = false;
            readyCheckMethod = null;
        }
    }

    private static void disableTooltipTransforms(Throwable throwable) {
        tooltipTransformsEnabled = false;
        if (disableMessagePrinted || InteractiveChat.plugin == null) {
            return;
        }
        disableMessagePrinted = true;

        String reason = throwable == null ? "unknown error" : throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
        InteractiveChat.plugin.getLogger().warning("[InteractiveChat] Disabled ExcellentEnchants tooltip transforms due to API mismatch. " + reason);
        InteractiveChat.plugin.getLogger().warning("[InteractiveChat] Inventory/ender/item previews will continue without EE tooltip descriptions.");
    }

}
