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

package com.loohp.interactivechat.proxy.velocity;

import com.loohp.interactivechat.proxy.objectholders.BackendInteractiveChatData;
import com.loohp.interactivechat.registry.Registry;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.Comparator;
import java.util.concurrent.ExecutionException;

public class CommandsVelocity {

    public static void createBrigadierCommand() {

        LiteralCommandNode<CommandSource> backendinfoNode = LiteralArgumentBuilder
                .<CommandSource>literal("backendinfo")
                .requires(sender -> {
                    return sender.hasPermission("interactivechat.backendinfo");
                })
                .executes(command -> {
                    try {
                        CommandSource sender = command.getSource();
                        if (InteractiveChatVelocity.hasPermission(sender, "interactivechat.backendinfo").get()) {
                            InteractiveChatVelocity.sendMessage(sender, Component.text(TextColor.AQUA + "Proxy -> InteractiveChat: " + InteractiveChatVelocity.plugin.getDescription().getVersion() + " (PM Protocol: " + Registry.PLUGIN_MESSAGING_PROTOCOL_VERSION + ")"));
                            InteractiveChatVelocity.sendMessage(sender, Component.text(TextColor.AQUA + "Expected latency: " + InteractiveChatVelocity.delay + " ms"));
                            InteractiveChatVelocity.sendMessage(sender, Component.text(TextColor.AQUA + "Backends under this proxy:"));
                            InteractiveChatVelocity.plugin.getServer().getAllServers().stream().sorted(Comparator.comparing(each -> each.getServerInfo().getName())).forEach(server -> {
                                String name = server.getServerInfo().getName();
                                BackendInteractiveChatData data = InteractiveChatVelocity.serverInteractiveChatInfo.get(name);
                                if (data == null) {
                                    InteractiveChatVelocity.sendMessage(sender, Component.text(TextColor.RED + name + " -> Attempting to retrieve data from backend..."));
                                } else {
                                    String minecraftVersion = data.getExactMinecraftVersion();
                                    if (data.isOnline()) {
                                        if (!data.hasInteractiveChat()) {
                                            InteractiveChatVelocity.sendMessage(sender, Component.text(TextColor.YELLOW + name + " -> InteractiveChat: NOT INSTALLED (PM Protocol: -1) | Minecraft: " + minecraftVersion + " | Ping: " + (data.getPing() < 0 ? "N/A" : (data.getPing() + " ms"))));
                                        } else {
                                            InteractiveChatVelocity.sendMessage(sender, Component.text(TextColor.GREEN + name + " -> InteractiveChat: " + data.getVersion() + " (PM Protocol: " + data.getProtocolVersion() + ") | Minecraft: " + minecraftVersion + " | Ping: " + (data.getPing() < 0 ? "N/A" : (data.getPing() + " ms"))));
                                        }
                                    } else {
                                        InteractiveChatVelocity.sendMessage(sender, Component.text(TextColor.RED + name + " -> Status: OFFLINE"));
                                    }

                                }
                            });
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    return 1;
                })
                .build();

        LiteralCommandNode<CommandSource> rootNode = LiteralArgumentBuilder
                .<CommandSource>literal("interactivechatproxy")
                .then(backendinfoNode)
                .executes(command -> {
                    defaultMessage(command.getSource());
                    return 1;
                })
                .build();

        LiteralCommandNode<CommandSource> aliasNode1 = LiteralArgumentBuilder
                .<CommandSource>literal("icp")
                .then(backendinfoNode)
                .executes(command -> {
                    defaultMessage(command.getSource());
                    return 1;
                })
                .build();

        BrigadierCommand command = new BrigadierCommand(rootNode);
        BrigadierCommand alias1 = new BrigadierCommand(aliasNode1);

        CommandManager commandManager = InteractiveChatVelocity.plugin.getServer().getCommandManager();
        commandManager.register(command);
        commandManager.register(alias1);
    }

    private static void defaultMessage(CommandSource sender) {
        InteractiveChatVelocity.sendMessage(sender, Component.text(TextColor.AQUA + "InteractiveChat written by LOOHP!"));
        InteractiveChatVelocity.sendMessage(sender, Component.text(TextColor.GOLD + "You are running InteractiveChat " + TextColor.GREEN + "(Velocity)" + TextColor.GOLD + " version: " + InteractiveChatVelocity.plugin.getDescription().getVersion()));
        TextComponent update = Component.text(TextColor.YELLOW + "Use " + TextColor.GREEN + "/interactivechat update" + TextColor.YELLOW + " for update checks!")
                .clickEvent(ClickEvent.runCommand("/interactivechat update"))
                .hoverEvent(Component.text(TextColor.LIGHT_PURPLE + "Or Click Me!").asHoverEvent());
        InteractiveChatVelocity.sendMessage(sender, update);
    }

}
