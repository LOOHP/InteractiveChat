package com.loohp.interactivechat.ObjectHolders;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;

public class CommandPlaceholderGroup {
	
	String placeholder;
	String uuidmatch;
	UUID sender;
	HashMap<String, CommandPlaceholderGroup> map;
	
	public CommandPlaceholderGroup(Player sender, String placeholder, String uuidmatch, HashMap<String, CommandPlaceholderGroup> mapToRemoveFrom) {
		this.placeholder = placeholder;
		this.uuidmatch = uuidmatch;
		this.sender = sender.getUniqueId();
		this.map = mapToRemoveFrom;
		run();
	}
	
	public String getPlaceholder() {
		return placeholder;
	}
	
	public String getUUIDMatch() {
		return uuidmatch;
	}
	
	public Player getSender() {
		return Bukkit.getPlayer(sender);
	}
	
	private void run() {
		Bukkit.getScheduler().runTaskLaterAsynchronously(InteractiveChat.plugin, () -> map.remove(uuidmatch), 300);
	}

}
