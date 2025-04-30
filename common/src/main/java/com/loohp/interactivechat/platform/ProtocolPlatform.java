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

package com.loohp.interactivechat.platform;

import com.loohp.interactivechat.objectholders.CustomTabCompletionAction;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

public interface ProtocolPlatform {

    Plugin getRegisteredPlugin();

    void initialize();

    void onBungeecordModeEnabled();

    void sendTabCompletionPacket(Player player, CustomTabCompletionAction action, List<String> list);

    void sendUnprocessedChatMessage(CommandSender sender, UUID uuid, Component component);

    boolean hasChatSigning();

    int getProtocolVersion(Player player);

    Player newTemporaryPlayer(String name, UUID uuid);

}
