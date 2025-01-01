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

package com.loohp.interactivechat.hooks.luckperms;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.PlayerUtils;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LuckPermsEvents {

    private final Set<UUID> scheduledReset;

    public LuckPermsEvents(InteractiveChat plugin) {
        scheduledReset = Collections.synchronizedSet(new HashSet<>());
        LuckPermsProvider.get().getEventBus().subscribe(plugin, UserDataRecalculateEvent.class, event -> {
            UUID uuid = event.getUser().getUniqueId();
            if (!scheduledReset.contains(uuid)) {
                scheduledReset.add(uuid);
                InteractiveChat.plugin.getScheduler().runLater(() -> {
                    PlayerUtils.resetPermissionCache(uuid);
                    scheduledReset.remove(uuid);
                }, 1);
            }
        });
    }

}
