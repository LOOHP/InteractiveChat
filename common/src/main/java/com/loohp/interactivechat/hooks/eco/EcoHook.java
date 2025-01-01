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

package com.loohp.interactivechat.hooks.eco;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.willfp.eco.core.display.Display;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EcoHook {

    public static void init() {
        Plugin eco = Bukkit.getPluginManager().getPlugin("eco");

        InteractiveChatAPI.registerItemStackTransformProvider(eco, 1, (itemStack, uuid) -> {
            try {
                if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                    return itemStack;
                }
                if (uuid == null) {
                    return setEcoLore(itemStack).get(1000, TimeUnit.MILLISECONDS);
                }
                ICPlayer icPlayer = ICPlayerFactory.getICPlayer(uuid);
                if (icPlayer == null || !icPlayer.isLocal()) {
                    return setEcoLore(itemStack).get(1000, TimeUnit.MILLISECONDS);
                }
                return setEcoLore(itemStack, icPlayer.getLocalPlayer()).get(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
            return itemStack;
        });
    }

    public static Future<ItemStack> setEcoLore(ItemStack itemStack) {
        return setEcoLore(itemStack, null);
    }

    public static Future<ItemStack> setEcoLore(ItemStack itemStack, Player player) {
        InteractiveChat plugin = InteractiveChat.getPlugin(InteractiveChat.class);
        if (!InteractiveChat.ecoSetLoreOnMainThread || plugin.getScheduler().isGlobalTickThread()) {
            return CompletableFuture.completedFuture(setEcoLore0(itemStack.clone(), player));
        } else {
            CompletableFuture<ItemStack> future = new CompletableFuture<>();
            plugin.getScheduler().runAtEntity(player, (task) -> {
                setEcoLore0(itemStack.clone(), player);
                future.complete(itemStack);
            });
            return future;
        }
    }

    private static ItemStack setEcoLore0(ItemStack itemStack, Player player) {
        return Display.displayAndFinalize(itemStack.clone(), player);
    }

}
