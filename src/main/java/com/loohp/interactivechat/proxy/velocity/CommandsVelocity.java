package com.loohp.interactivechat.proxy.velocity;

import java.util.Comparator;
import java.util.concurrent.ExecutionException;

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
				    	sender.sendMessage(Component.text(TextColor.AQUA + "Proxy -> InteractiveChat: " + InteractiveChatVelocity.plugin.getDescription().getVersion() + " (PM Protocol: " + Registry.PLUGIN_MESSAGING_PROTOCOL_VERSION + ")"));
						sender.sendMessage(Component.text(TextColor.AQUA + "Expected latency: " + InteractiveChatVelocity.delay + " ms"));
						sender.sendMessage(Component.text(TextColor.AQUA + "Backends under this proxy:"));
						InteractiveChatVelocity.plugin.getServer().getAllServers().stream().sorted(Comparator.comparing(each -> each.getServerInfo().getName())).forEach(server -> {
							String name = server.getServerInfo().getName();
							BackendInteractiveChatData data = InteractiveChatVelocity.serverInteractiveChatInfo.get(name);
							if (data == null) {
								sender.sendMessage(Component.text(TextColor.RED + name + " -> Attempting to retrieve data from backend..."));
							} else {
								String minecraftVersion = data.getExactMinecraftVersion();
								if (data.isOnline()) {
									if (!data.hasInteractiveChat()) {
										sender.sendMessage(Component.text(TextColor.YELLOW + name + " -> InteractiveChat: NOT INSTALLED (PM Protocol: -1) | Minecraft: " + minecraftVersion + " | Ping: " + (data.getPing() < 0 ? "N/A" : (data.getPing() + " ms"))));
									} else {
										sender.sendMessage(Component.text(TextColor.GREEN + name + " -> InteractiveChat: " + data.getVersion() + " (PM Protocol: " + data.getProtocolVersion() + ") | Minecraft: " + minecraftVersion + " | Ping: " + (data.getPing() < 0 ? "N/A" : (data.getPing() + " ms"))));
									}
								} else {
									sender.sendMessage(Component.text(TextColor.RED + name + " -> Status: OFFLINE"));
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
		sender.sendMessage(Component.text(TextColor.AQUA + "InteractiveChat written by LOOHP!"));
		sender.sendMessage(Component.text(TextColor.GOLD + "You are running InteractiveChat " + TextColor.GREEN + "(Velocity)" + TextColor.GOLD + " version: " + InteractiveChatVelocity.plugin.getDescription().getVersion()));
		TextComponent update = Component.text(TextColor.YELLOW + "Use " + TextColor.GREEN + "/interactivechat update" + TextColor.YELLOW + " for update checks!")
		.clickEvent(ClickEvent.runCommand("/interactivechat update"))
		.hoverEvent(Component.text(TextColor.LIGHT_PURPLE + "Or Click Me!").asHoverEvent());
		sender.sendMessage(update);
	}

}
