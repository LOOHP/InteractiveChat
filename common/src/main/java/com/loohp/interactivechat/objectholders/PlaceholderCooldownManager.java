/*
 * This file is part of InteractiveChat4.
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

package com.loohp.interactivechat.objectholders;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageSender;
import com.loohp.interactivechat.objectholders.CooldownResult.CooldownOutcome;
import com.loohp.interactivechat.utils.PlayerUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlaceholderCooldownManager {

    private final Map<UUID, Long> universalTimestamps;
    private final Map<UUID, Map<UUID, Long>> placeholderTimestamps;

    public PlaceholderCooldownManager() {
        this.universalTimestamps = new ConcurrentHashMap<>();
        this.placeholderTimestamps = new ConcurrentHashMap<>();
        reloadPlaceholders();
    }

    public void reloadPlaceholders() {
        List<ICPlaceholder> placeholderList = InteractiveChatAPI.getICPlaceholderList();
        Iterator<UUID> itr = placeholderTimestamps.keySet().iterator();
        while (itr.hasNext()) {
            UUID internalId = itr.next();
            if (!placeholderList.stream().anyMatch(each -> each.getInternalId().equals(internalId))) {
                itr.remove();
            }
        }
        for (ICPlaceholder placeholder : placeholderList) {
            placeholderTimestamps.putIfAbsent(placeholder.getInternalId(), new ConcurrentHashMap<>());
        }
    }

    public CooldownResult checkMessage(UUID uuid, String message) {
        long now = System.currentTimeMillis();
        if (PlayerUtils.hasPermission(uuid, "interactivechat.cooldown.bypass", false, 200)) {
            return new CooldownResult(CooldownOutcome.ALLOW_BYPASS, now, -1, null);
        }
        List<Runnable> tasksIfSucessful = new LinkedList<>();
        boolean first = true;
        for (Entry<UUID, Map<UUID, Long>> entry : placeholderTimestamps.entrySet()) {
            UUID internalId = entry.getKey();
            ICPlaceholder placeholder = InteractiveChat.placeholderList.get(internalId);
            if (placeholder != null) {
                if (placeholder.getKeyword().matcher(message).find()) {
                    if (first) {
                        first = false;
                        if (InteractiveChat.universalCooldown > 0) {
                            Long lastUniversal = universalTimestamps.get(uuid);
                            if (lastUniversal != null && now - lastUniversal < InteractiveChat.universalCooldown) {
                                return new CooldownResult(CooldownOutcome.DENY_UNIVERSAL, now, lastUniversal + InteractiveChat.universalCooldown, null);
                            }
                        }
                        tasksIfSucessful.add(() -> setPlayerUniversalLastTimestamp(uuid, now));
                    }
                    Map<UUID, Long> mapping = entry.getValue();
                    if (placeholder.getCooldown() > 0) {
                        Long lastUsed = mapping.get(uuid);
                        if (lastUsed != null && now - lastUsed < placeholder.getCooldown()) {
                            return new CooldownResult(CooldownOutcome.DENY_PLACEHOLDER, now, lastUsed + placeholder.getCooldown(), placeholder);
                        }
                    }
                    tasksIfSucessful.add(() -> setPlayerPlaceholderLastTimestamp(uuid, placeholder, now));
                }
            }
        }
        tasksIfSucessful.forEach(each -> each.run());
        return new CooldownResult(CooldownOutcome.ALLOW, now, -1, null);
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
        Map<UUID, Long> mapping = placeholderTimestamps.get(placeholder.getInternalId());
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
        Map<UUID, Long> mapping = placeholderTimestamps.get(placeholder.getInternalId());
        if (mapping == null) {
            return;
        }
        mapping.put(uuid, time);
    }

    public boolean isPlaceholderOnCooldownAt(UUID uuid, ICPlaceholder placeholder, long time) {
        if (PlayerUtils.hasPermission(uuid, "interactivechat.cooldown.bypass", false, 200)) {
            return false;
        }
        long universalLastTimestamp = getPlayerUniversalLastTimestamp(uuid);
        if (universalLastTimestamp >= 0 && InteractiveChat.universalCooldown > 0 && time - universalLastTimestamp < InteractiveChat.universalCooldown && universalLastTimestamp < time) {
            return true;
        }
        long placeholderLastTimestamp = getPlayerPlaceholderLastTimestamp(uuid, placeholder);
        return placeholderLastTimestamp >= 0 && placeholder.getCooldown() > 0 && time - placeholderLastTimestamp < placeholder.getCooldown() && placeholderLastTimestamp < time;
    }

}
