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

package com.loohp.interactivechat.proxy.bungee.metrics;

import com.loohp.interactivechat.proxy.bungee.InteractiveChatBungee;

import java.util.concurrent.Callable;

public class Charts {

    public static void setup(Metrics metrics) {

        metrics.addCustomChart(new Metrics.SingleLineChart("total_plugin_messages_relayed_per_interval", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                long amount = InteractiveChatBungee.pluginMessagesCounter.getAndSet(0);
                return (int) Math.min(Integer.MAX_VALUE, amount);
            }
        }));

        metrics.addCustomChart(new Metrics.SingleLineChart("servers_managed_by_bungeecord_with_interactivechat", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                long amount = InteractiveChatBungee.getBackendInteractiveChatInfo().values().stream().filter(each -> each.hasInteractiveChat()).count();
                return (int) Math.min(Integer.MAX_VALUE, amount);
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("accurate_sender_parser_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                return InteractiveChatBungee.useAccurateSenderFinder ? "Enabled" : "Disabled";
            }
        }));

    }

}
