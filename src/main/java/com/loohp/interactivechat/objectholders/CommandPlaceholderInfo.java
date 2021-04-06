package com.loohp.interactivechat.objectholders;

import java.util.UUID;

import org.bukkit.Bukkit;

import com.loohp.interactivechat.InteractiveChat;

public class CommandPlaceholderInfo {
	
	private String placeholder;
	private String uuidmatch;
	private UUID sender;
	
	public CommandPlaceholderInfo(ICPlayer sender, String placeholder, String uuidmatch) {
		this.placeholder = placeholder;
		this.uuidmatch = uuidmatch;
		this.sender = sender.getUniqueId();
		run();
	}
	
	public String getPlaceholder() {
		return placeholder;
	}
	
	public String getUUIDMatch() {
		return uuidmatch;
	}
	
	public UUID getSender() {
		return sender;
	}
	
	private void run() {
		Bukkit.getScheduler().runTaskLaterAsynchronously(InteractiveChat.plugin, () -> InteractiveChat.commandPlaceholderMatch.remove(uuidmatch), 300);
	}

}
