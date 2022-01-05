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
		InteractiveChat.mentionPair.remove(this);
	}
	
	private int run() {
		return Bukkit.getScheduler().runTaskTimer(InteractiveChat.plugin, () -> {
			if ((System.currentTimeMillis() - timestamp) > 3000) {
				Bukkit.getScheduler().cancelTask(taskid);
				InteractiveChat.mentionPair.remove(this);
			}
		}, 0, 5).getTaskId();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((receiver == null) ? 0 : receiver.hashCode());
		result = prime * result + ((sender == null) ? 0 : sender.hashCode());
		result = prime * result + taskid;
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof MentionPair)) {
			return false;
		}
		MentionPair other = (MentionPair) obj;
		if (receiver == null) {
			if (other.receiver != null) {
				return false;
			}
		} else if (!receiver.equals(other.receiver)) {
			return false;
		}
		if (sender == null) {
			if (other.sender != null) {
				return false;
			}
		} else if (!sender.equals(other.sender)) {
			return false;
		}
		if (taskid != other.taskid) {
			return false;
		}
		if (timestamp != other.timestamp) {
			return false;
		}
		return true;
	}

}
