package com.loohp.interactivechat.proxy.objectholders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyPlayerCooldownManager {
	
	private Map<UUID, Long> universalTimestamps;
	private Map<String, Map<UUID, Long>> placeholderTimestamps;
	
	public ProxyPlayerCooldownManager(List<String> keywords) {
		this.universalTimestamps = new ConcurrentHashMap<>();
		this.placeholderTimestamps = new ConcurrentHashMap<>();
		reloadPlaceholders(keywords);
	}
	
	public void reloadPlaceholders(List<String> keywords) {
		List<String> keywordList = new ArrayList<>(keywords);
		Iterator<String> itr = placeholderTimestamps.keySet().iterator();
		while (itr.hasNext()) {
			String keyword = itr.next();
			if (!keywordList.contains(keyword)) {
				itr.remove();
			}
		}
		for (String keyword : keywordList) {
			placeholderTimestamps.putIfAbsent(keyword, new ConcurrentHashMap<>());
		}
	}
	
	public long getPlayerUniversalLastTimestamp(UUID uuid) {
		Long time = universalTimestamps.get(uuid);
		return time == null ? -1 : time;
	}
	
	public void setPlayerUniversalLastTimestamp(UUID uuid, long time) {
		universalTimestamps.put(uuid, time);
	}
	
	public long getPlayerPlaceholderLastTimestamp(UUID uuid, String keyword) {
		Map<UUID, Long> mapping = placeholderTimestamps.get(keyword);
		if (mapping == null) {
			return -1;
		}
		Long time = mapping.get(uuid);
		return time == null ? -1 : time;
	}
	
	public void setPlayerPlaceholderLastTimestamp(UUID uuid, String keyword, long time) {
		Map<UUID, Long> mapping = placeholderTimestamps.get(keyword);
		if (mapping == null) {
			return;
		}
		mapping.put(uuid, time);
	}

}
