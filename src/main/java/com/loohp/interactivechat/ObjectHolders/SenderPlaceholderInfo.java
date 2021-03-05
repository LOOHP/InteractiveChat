package com.loohp.interactivechat.ObjectHolders;

import java.util.UUID;

import org.bukkit.Bukkit;

import com.loohp.interactivechat.InteractiveChat;

public class SenderPlaceholderInfo {
	
	private String uuidmatch;
	private UUID sender;

	public SenderPlaceholderInfo(ICPlayer sender, String uuidmatch) {
		this.uuidmatch = uuidmatch;
		this.sender = sender.getUniqueId();
		run();
	}

	public String getUUIDMatch() {
		return uuidmatch;
	}

	public UUID getSender() {
		return sender;
	}

	private void run() {
		Bukkit.getScheduler().runTaskLaterAsynchronously(InteractiveChat.plugin, () -> InteractiveChat.senderPlaceholderMatch.remove(uuidmatch), 300);
	}
}
