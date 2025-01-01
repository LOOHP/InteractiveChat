/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactivechat.proxy.objectholders;

import com.loohp.interactivechat.objectholders.ICPlaceholder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyPlayerCooldownManager {

    private final Map<UUID, Long> universalTimestamps;
    private final Map<UUID, Map<UUID, Long>> placeholderTimestamps;

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
