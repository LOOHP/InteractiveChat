package com.loohp.interactivechat.proxy.objectholders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.loohp.interactivechat.objectholders.ICPlaceholder;

public class ProxyPlayerCooldownManager {
	
	private Map<UUID, Long> universalTimestamps;
	private Map<UUID, Map<UUID, Long>> placeholderTimestamps;
	
	public ProxyPlayerCooldownManager(Collection<ICPlaceholder> keywords) {
		this.universalTimestamps = new ConcurrentHashMap<>();
		this.placeholderTimestamps = new ConcurrentHashMap<>();
		reloadPlaceholders(keywords);
	}
	
	public void reloadPlaceholders(Collection<ICPlaceholder> placeholders) {
		List<ICPlaceholder> placeholderList = new ArrayList<>(placeholders);
		Iterator<UUID> itr = placeholderTimestamps.keySet().iterator();
		while (itr.hasNext()) {
			UUID internalId = itr.next();
			if (placeholderList.stream().noneMatch(each -> each.getInternalId().equals(internalId))) {
				itr.remove();
			}
		}
		for (ICPlaceholder keyword : placeholderList) {
			placeholderTimestamps.putIfAbsent(keyword.getInternalId(), new ConcurrentHashMap<>());
		}
	}
	
	public long getPlayerUniversalLastTimestamp(UUID uuid) {
		Long time = universalTimestamps.get(uuid);
		return time == null ? -1 : time;
	}
	
	public void setPlayerUniversalLastTimestamp(UUID uuid, long time) {
		universalTimestamps.put(uuid, time);
	}
	
	public long getPlayerPlaceholderLastTimestamp(UUID uuid, UUID internalId) {
		Map<UUID, Long> mapping = placeholderTimestamps.get(internalId);
		if (mapping == null) {
			return -1;
		}
		Long time = mapping.get(uuid);
		return time == null ? -1 : time;
	}
	
	public void setPlayerPlaceholderLastTimestamp(UUID uuid, UUID internalId, long time) {
		Map<UUID, Long> mapping = placeholderTimestamps.get(internalId);
		if (mapping == null) {
			return;
		}
		mapping.put(uuid, time);
	}

}
