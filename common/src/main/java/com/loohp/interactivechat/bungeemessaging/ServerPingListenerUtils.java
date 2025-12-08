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

package com.loohp.interactivechat.bungeemessaging;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.registry.Registry;
import org.json.simple.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class ServerPingListenerUtils {

    public static final Map<Object, Long> REQUESTS = new ConcurrentHashMap<>();
    public static final String MOTD_JSON;

    static {
        JSONObject obj = new JSONObject();
        obj.put("present", true);
        obj.put("version", InteractiveChat.plugin.getDescription().getVersion());
        obj.put("minecraftVersion", InteractiveChat.version.getNumber());
        obj.put("exactMinecraftVersion", InteractiveChat.exactMinecraftVersion);
        obj.put("protocol", Registry.PLUGIN_MESSAGING_PROTOCOL_VERSION);
        MOTD_JSON = obj.toJSONString();
    }
}
