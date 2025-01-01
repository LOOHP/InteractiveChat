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

package com.loohp.interactivechat.proxy.bungee;

import com.loohp.interactivechat.proxy.objectholders.BackendInteractiveChatData;
import com.loohp.interactivechat.registry.Registry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CommandsBungee extends Command implements TabExecutor {

    public CommandsBungee() {
        super("interactivechatproxy", null, "icp");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxyServer.getInstance().getScheduler().runAsync(InteractiveChatBungee.plugin, () -> {
            try {
                if (args.length == 0) {
                    defaultMessage(sender);
                    return;
                }

                if (args[0].equalsIgnoreCase("backendinfo") && InteractiveChatBungee.hasPermission(sender, "interactivechat.backendinfo").get()) {
                    InteractiveChatBungee.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(ChatColor.AQUA + "Proxy -> InteractiveChat: " + InteractiveChatBungee.plugin.getDescription().getVersion() + " (PM Protocol: " + Registry.PLUGIN_MESSAGING_PROTOCOL_VERSION + ")"));
                    InteractiveChatBungee.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(ChatColor.AQUA + "Expected latency: " + InteractiveChatBungee.delay + " ms"));
                    InteractiveChatBungee.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(ChatColor.AQUA + "Backends under this proxy:"));
                    ProxyServer.getInstance().getServers().values().stream().sorted(Comparator.comparing(each -> each.getName())).forEach(server -> {
                        String name = server.getName();
                        BackendInteractiveChatData data = InteractiveChatBungee.serverInteractiveChatInfo.get(name);
                        if (data == null) {
                            InteractiveChatBungee.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(ChatColor.RED + name + " -> Attempting to retrieve data from backend..."));
                        } else {
                            String minecraftVersion = data.getExactMinecraftVersion();
                            if (data.isOnline()) {
                                if (!data.hasInteractiveChat()) {
                                    InteractiveChatBungee.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(ChatColor.YELLOW + name + " -> InteractiveChat: NOT INSTALLED (PM Protocol: -1) | Minecraft: " + minecraftVersion + " | Ping: " + (data.getPing() < 0 ? "N/A" : (data.getPing() + " ms"))));
                                } else {
                                    InteractiveChatBungee.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(ChatColor.GREEN + name + " -> InteractiveChat: " + data.getVersion() + " (PM Protocol: " + data.getProtocolVersion() + ") | Minecraft: " + minecraftVersion + " | Ping: " + (data.getPing() < 0 ? "N/A" : (data.getPing() + " ms"))));
                                }
                            } else {
                                InteractiveChatBungee.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(ChatColor.RED + name + " -> Status: OFFLINE"));
                            }

                        }
                    });
                    return;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            defaultMessage(sender);
        });
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> tab = new ArrayList<>();
        switch (args.length) {
            case 1:
                if ("backendinfo".startsWith(args[0].toLowerCase()) && sender.hasPermission("interactivechat.backendinfo")) {
                    tab.add("backendinfo");
                }
                break;
        }

        return tab;
    }

    private void defaultMessage(CommandSender sender) {
        InteractiveChatBungee.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(ChatColor.AQUA + "InteractiveChat written by LOOHP!"));
        InteractiveChatBungee.sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(ChatColor.GOLD + "You are running InteractiveChat " + ChatColor.GREEN + "(Bungeecord)" + ChatColor.GOLD + " version: " + InteractiveChatBungee.plugin.getDescription().getVersion()));
        Component update = LegacyComponentSerializer.legacySection().deserialize(ChatColor.YELLOW + "Use " + ChatColor.GREEN + "/interactivechat update" + ChatColor.YELLOW + " for update checks!");
        update = update.clickEvent(ClickEvent.runCommand("/interactivechat update"));
        update = update.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(ChatColor.LIGHT_PURPLE + "Or Click Me!")));
        InteractiveChatBungee.sendMessage(sender, update);
    }

}
