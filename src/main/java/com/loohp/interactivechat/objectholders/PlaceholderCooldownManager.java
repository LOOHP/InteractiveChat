package com.loohp.interactivechat.objectholders;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageSender;
import com.loohp.interactivechat.objectholders.CooldownResult.CooldownOutcome;

public class PlaceholderCooldownManager {
	
	private Map<UUID, Long> universalTimestamps;
	private Map<ICPlaceholder, Map<UUID, Long>> placeholderTimestamps;
	
	public PlaceholderCooldownManager() {
		this.universalTimestamps = new ConcurrentHashMap<>();
		this.placeholderTimestamps = new ConcurrentHashMap<>();
		reloadPlaceholders();
	}
	
	public void reloadPlaceholders() {
		List<ICPlaceholder> placeholderList = InteractiveChatAPI.getICPlaceholderList();
		Iterator<ICPlaceholder> itr = placeholderTimestamps.keySet().iterator();
		while (itr.hasNext()) {
			ICPlaceholder placeholder = itr.next();
			if (!placeholderList.contains(placeholder)) {
				itr.remove();
			}
		}
		for (ICPlaceholder placeholder : placeholderList) {
			placeholderTimestamps.putIfAbsent(placeholder, new ConcurrentHashMap<>());
		}
	}
	
	public CooldownResult checkMessage(UUID uuid, String message) {
		long now = System.currentTimeMillis();
		List<Runnable> tasksIfSucessful = new LinkedList<>();
		if (InteractiveChat.universalCooldown > 0) {
			Long lastUniversal = universalTimestamps.get(uuid);
			if (lastUniversal != null && now - lastUniversal < InteractiveChat.universalCooldown) {
				return new CooldownResult(CooldownOutcome.DENY_UNIVERSAL, lastUniversal + InteractiveChat.universalCooldown, null);
			}
		}
		tasksIfSucessful.add(() -> setPlayerUniversalLastTimestamp(uuid, now));
		for (Entry<ICPlaceholder, Map<UUID, Long>> entry : placeholderTimestamps.entrySet()) {
			ICPlaceholder placeholder = entry.getKey();
			if ((placeholder.isCaseSensitive() && message.contains(placeholder.getKeyword())) || (!placeholder.isCaseSensitive() && message.toLowerCase().contains(placeholder.getKeyword().toLowerCase()))) {
				Map<UUID, Long> mapping = entry.getValue();
				if (placeholder.getCooldown() > 0) {
					Long lastUsed = mapping.get(uuid);
					if (lastUsed != null && now - lastUsed < placeholder.getCooldown()) {
						return new CooldownResult(CooldownOutcome.DENY_PLACEHOLDER, lastUsed + placeholder.getCooldown(), placeholder);
					}
				}
				tasksIfSucessful.add(() -> setPlayerPlaceholderLastTimestamp(uuid, placeholder, now));
			}
		}
		tasksIfSucessful.forEach(each -> each.run());
		return new CooldownResult(CooldownOutcome.ALLOW, -1, null);
	}
	
	public long getPlayerUniversalLastTimestamp(UUID uuid) {
		Long time = universalTimestamps.get(uuid);
		return time == null ? -1 : time;
	}
	
	public void setPlayerUniversalLastTimestamp(UUID uuid, long time) {
		if (InteractiveChat.bungeecordMode && InteractiveChat.universalCooldown > 0) {
			try {
				BungeeMessageSender.sendPlayerUniversalCooldown(uuid, time);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		setPlayerUniversalLastTimestampRaw(uuid, time);
	}
	
	@Deprecated
	public void setPlayerUniversalLastTimestampRaw(UUID uuid, long time) {
		universalTimestamps.put(uuid, time);
	}
	
	public long getPlayerPlaceholderLastTimestamp(UUID uuid, ICPlaceholder placeholder) {
		Map<UUID, Long> mapping = placeholderTimestamps.get(placeholder);
		if (mapping == null) {
			return -1;
		}
		Long time = mapping.get(uuid);
		return time == null ? -1 : time;
	}
	
	public void setPlayerPlaceholderLastTimestamp(UUID uuid, ICPlaceholder placeholder, long time) {
		if (InteractiveChat.bungeecordMode && placeholder.getCooldown() > 0) {
			try {
				BungeeMessageSender.sendPlayerPlaceholderCooldown(uuid, placeholder, time);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		setPlayerPlaceholderLastTimestampRaw(uuid, placeholder, time);
	}
	
	@Deprecated
	public void setPlayerPlaceholderLastTimestampRaw(UUID uuid, ICPlaceholder placeholder, long time) {
		Map<UUID, Long> mapping = placeholderTimestamps.get(placeholder);
		if (mapping == null) {
			return;
		}
		mapping.put(uuid, time);
	}
	
	public boolean isPlaceholderOnCooldownAt(UUID uuid, ICPlaceholder placeholder, long time) {
		long universalLastTimestamp = getPlayerUniversalLastTimestamp(uuid);
		if (universalLastTimestamp >= 0 && InteractiveChat.universalCooldown > 0 && time - universalLastTimestamp < InteractiveChat.universalCooldown) {
			return true;
		}
		long placeholderLastTimestamp = getPlayerPlaceholderLastTimestamp(uuid, placeholder);
		if (placeholderLastTimestamp >= 0 && placeholder.getCooldown() > 0 && time - placeholderLastTimestamp < placeholder.getCooldown()) {
			return true;
		}
		return false;
	}

}
