package com.loohp.interactivechat.objectholders;

import java.util.UUID;

import org.bukkit.Bukkit;

import com.loohp.interactivechat.InteractiveChat;

public class MentionPair {
	
	private UUID sender;
	private UUID receiver;
	private long timestamp;
	private int taskid;
	
	public MentionPair(UUID sender, UUID reciever) {
		this.sender = sender;
		this.receiver = reciever;
		this.timestamp = System.currentTimeMillis();
		this.taskid = run();
	}
	
	public UUID getSender() {
		return sender;
	}
	
	public UUID getReciever() {
		return receiver;
	}
	
	public void remove() {
		Bukkit.getScheduler().cancelTask(taskid);
		InteractiveChat.mentionPair.remove(receiver);
	}
	
	private int run() {
		return Bukkit.getScheduler().runTaskTimer(InteractiveChat.plugin, () -> {
			if ((System.currentTimeMillis() - timestamp) > 3000) {
				Bukkit.getScheduler().cancelTask(taskid);
				InteractiveChat.mentionPair.remove(receiver);
			}
		}, 0, 5).getTaskId();
	}

}
