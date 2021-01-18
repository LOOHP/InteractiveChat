package com.loohp.interactivechat.ObjectHolders;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.loohp.interactivechat.InteractiveChat;

public class CommandPlaceholderInfo {
	
	private String placeholder;
	private String uuidmatch;
	private UUID sender;
	private Map<String, CommandPlaceholderInfo> map;
	
	public CommandPlaceholderInfo(PlayerWrapper sender, String placeholder, String uuidmatch, Map<String, CommandPlaceholderInfo> commandPlaceholderMatch) {
		this.placeholder = placeholder;
		this.uuidmatch = uuidmatch;
		this.sender = sender.getUniqueId();
		this.map = commandPlaceholderMatch;
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
		Bukkit.getScheduler().runTaskLaterAsynchronously(InteractiveChat.plugin, () -> map.remove(uuidmatch), 300);
	}

}
