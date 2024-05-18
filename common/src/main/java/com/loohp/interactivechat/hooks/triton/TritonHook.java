/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2024. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2024. Contributors
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

package com.loohp.interactivechat.hooks.triton;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.rexcantor64.triton.api.Triton;
import com.rexcantor64.triton.api.TritonAPI;
import com.rexcantor64.triton.api.config.FeatureSyntax;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.UUID;

public class TritonHook {

    public static Component parseLanguageChat(UUID player, Component component) {
        Triton triton = TritonAPI.getInstance();
        String language;
        if (player == null) {
            language = triton.getLanguageManager().getMainLanguage().getLanguageId();
        } else {
            language = triton.getPlayerManager().get(player).getLanguageId();
        }
        FeatureSyntax syntax = triton.getConf().getChatSyntax();
        String json;
        if (InteractiveChat.version.isLegacyRGB()) {
            json = InteractiveChatComponentSerializer.legacyGson().serialize(component);
        } else {
            json = InteractiveChatComponentSerializer.gson().serialize(component);
        }
        BaseComponent[] components = triton.getLanguageParser().parseComponent(language, syntax, ComponentSerializer.parse(json));
        if (InteractiveChat.version.isLegacyRGB()) {
            return InteractiveChatComponentSerializer.legacyGson().deserialize(ComponentSerializer.toString(components));
        } else {
            return InteractiveChatComponentSerializer.gson().deserialize(ComponentSerializer.toString(components));
        }
    }

}
