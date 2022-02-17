/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
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

import org.bukkit.event.EventPriority;

public class CompatibilityListener {

    private final String plugin;
    private final String clazz;
    private final EventPriority priority;

    public CompatibilityListener(String plugin, String clazz, EventPriority priority) {
        this.plugin = plugin;
        this.clazz = clazz;
        this.priority = priority;
    }

    public String getPluginRegex() {
        return plugin;
    }

    public String getClassName() {
        return clazz;
    }

    public EventPriority getPriority() {
        return priority;
    }

}
