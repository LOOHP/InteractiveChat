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

package com.loohp.interactivechat.hooks.dynmap;

import com.loohp.interactivechat.modules.ProcessExternalMessage;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.dynmap.Client;
import org.dynmap.DynmapCore;
import org.dynmap.common.DynmapListenerManager.ChatEventListener;
import org.dynmap.common.DynmapPlayer;

public class DynmapCoreChatListener implements ChatEventListener {

    private final DynmapCore core;

    public DynmapCoreChatListener(DynmapCore core) {
        this.core = core;
    }

    @Override
    public void chatEvent(DynmapPlayer p, String msg) {
        if (core.disable_chat_to_web) {
            return;
        }
        if (core.mapManager != null) {
            Player bukkitplayer = Bukkit.getPlayer(p.getUUID());
            if (bukkitplayer == null) {
                msg = msg.replaceAll(Registry.ID_PATTERN.pattern(), "");
                core.mapManager.pushUpdate(new Client.ChatMessage("player", "", p.getDisplayName(), msg, p.getName()));
            } else {
                try {
                    String component = InteractiveChatComponentSerializer.gson().serialize(LegacyComponentSerializer.legacySection().deserialize(msg));
                    String processed = PlainTextComponentSerializer.plainText().serialize(InteractiveChatComponentSerializer.gson().deserialize(ProcessExternalMessage.processAndRespond(bukkitplayer, component, false)));
                    core.mapManager.pushUpdate(new Client.ChatMessage("player", "", p.getDisplayName(), processed, p.getName()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
