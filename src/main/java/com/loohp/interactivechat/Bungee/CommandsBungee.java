package com.loohp.interactivechat.Bungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class CommandsBungee extends Command {

	public CommandsBungee() {
		super("interactivechatbungee", null, "icb");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		sender.sendMessage(new TextComponent(ChatColor.AQUA + "InteractiveChat written by LOOHP!"));
		sender.sendMessage(new TextComponent(ChatColor.GOLD + "You are running InteractiveChat " + ChatColor.GREEN + "(Bungeecord)" + ChatColor.GOLD + " version: " + InteractiveChatBungee.plugin.getDescription().getVersion()));
		sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Check InteractiveChat on Spigot for updates!"));
	}

}
