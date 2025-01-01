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

package com.loohp.interactivechat.metrics;

import com.loohp.interactivechat.InteractiveChat;

import java.util.concurrent.Callable;

public class Charts {

    public static void setup(Metrics metrics) {

        metrics.addCustomChart(new Metrics.SingleLineChart("total_placeholders", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return InteractiveChat.placeholderList.size();
            }
        }));

        metrics.addCustomChart(new Metrics.SingleLineChart("total_amount_of_messages_processing_per_interval", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                long amount = InteractiveChat.messagesCounter.getAndSet(0);
                return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("bungeecord_mode", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return InteractiveChat.bungeecordMode ? "Enabled" : "Disabled";
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("accurate_sender_parser_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return InteractiveChat.useAccurateSenderFinder ? "Enabled" : "Disabled";
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("item_display_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return InteractiveChat.useItem ? "Enabled" : "Disabled";
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("map_preview_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return InteractiveChat.itemMapPreview ? "Enabled" : "Disabled";
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("inventory_display_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return InteractiveChat.useInventory ? "Enabled" : "Disabled";
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("inventory_display_layout", new Callable<String>() {
            @Override
            public String call() throws Exception {
                if (!InteractiveChat.useInventory) {
                    return "Disabled";
                } else {
                    return "Layout " + InteractiveChat.invDisplayLayout;
                }
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("enderchest_display_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return InteractiveChat.useEnder ? "Enabled" : "Disabled";
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("clickable_command_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return InteractiveChat.clickableCommands ? "Enabled" : "Disabled";
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("player_name_info_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return InteractiveChat.usePlayerName ? "Enabled" : "Disabled";
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("mention_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return InteractiveChat.allowMention ? "Enabled" : "Disabled";
            }
        }));
    }

}
