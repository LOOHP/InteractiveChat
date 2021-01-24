package com.loohp.interactivechat.Bungee;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandsBungee extends Command implements TabExecutor {

	public CommandsBungee() {
		super("interactivechatbungee", null, "icb");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (args.length == 0) {
						defaultMessage(sender);
						return;
					}
					
					if (args[0].equalsIgnoreCase("backendinfo") && InteractiveChatBungee.hasPermission(sender, "interactivechat.backendinfo").get()) {
						sender.sendMessage(new TextComponent(ChatColor.AQUA + "Expected latency: " + InteractiveChatBungee.delay + " ms"));
						sender.sendMessage(new TextComponent(ChatColor.AQUA + "Backends under this proxy:"));
						ProxyServer.getInstance().getServers().values().stream().sorted(Comparator.comparing(each -> each.getName())).forEach(server -> {
							String name = server.getName();
							BackendInteractiveChatData data = InteractiveChatBungee.serverInteractiveChatInfo.get(name);
							if (data == null) {
								sender.sendMessage(new TextComponent(ChatColor.RED + name + " -> Attempting to retrieve data from backend..."));
							} else {
								String minecraftVersion = data.getExactMinecraftVersion();
								if (!data.hasInteractiveChat()) {
									sender.sendMessage(new TextComponent(ChatColor.YELLOW + name + " -> InteractiveChat: NOT INSTALLED | Minecraft: " + minecraftVersion + " | Ping: " + (data.getPing() < 0 ? "N/A" : (data.getPing() + " ms"))));
								} else {
									sender.sendMessage(new TextComponent(ChatColor.GREEN + name + " -> InteractiveChat: " + data.getVersion() + " | Minecraft: " + minecraftVersion + " | Ping: " + (data.getPing() < 0 ? "N/A" : (data.getPing() + " ms"))));
								}
							}
						});
						return;
					}
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
				
				defaultMessage(sender);
			}
		}).start();
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
	
	@SuppressWarnings("deprecation")
	private void defaultMessage(CommandSender sender) {
		sender.sendMessage(new TextComponent(ChatColor.AQUA + "InteractiveChat written by LOOHP!"));
		sender.sendMessage(new TextComponent(ChatColor.GOLD + "You are running InteractiveChat " + ChatColor.GREEN + "(Bungeecord)" + ChatColor.GOLD + " version: " + InteractiveChatBungee.plugin.getDescription().getVersion()));
		TextComponent update = new TextComponent(ChatColor.YELLOW + "Use " + ChatColor.GREEN + "/interactivechat update" + ChatColor.YELLOW + " for update checks!");
		update.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat update"));
		update.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent(ChatColor.LIGHT_PURPLE + "Or Click Me!")}));
		sender.sendMessage(update);
	}

}
