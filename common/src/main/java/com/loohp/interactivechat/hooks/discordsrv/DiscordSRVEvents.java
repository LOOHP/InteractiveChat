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

package com.loohp.interactivechat.hooks.discordsrv;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentFont;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;
import github.scarsz.discordsrv.api.events.VentureChatMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class DiscordSRVEvents {

    public static Component process(Component component) {
        net.kyori.adventure.text.Component icComponent = convert(component);
        icComponent = ComponentReplacing.replace(icComponent, Registry.ID_PATTERN.pattern(), false, (result, matchedComponents) -> {
            String placeholder = result.group(4);
            String replacement = placeholder == null ? "" : Registry.ID_UNESCAPE_PATTERN.matcher(placeholder).replaceAll(">");
            return LegacyComponentSerializer.legacySection().deserialize(replacement);
        });
        if (InteractiveChat.chatControlRedHook) {
            icComponent = ComponentReplacing.replace(icComponent, Registry.CHR_ID_PATTERN.pattern(), false, (result, matchedComponents) -> {
                String placeholder = result.group(4);
                String replacement = placeholder == null ? "" : Registry.ID_UNESCAPE_PATTERN.matcher(placeholder).replaceAll(">");
                return LegacyComponentSerializer.legacySection().deserialize(replacement);
            });
        }
        icComponent = ComponentReplacing.replace(icComponent, Registry.MENTION_TAG_CONVERTER.getReversePattern().pattern(), true, ((result, components) -> {
            return LegacyComponentSerializer.legacySection().deserialize(result.group(2));
        }));
        icComponent = icComponent.replaceText(TextReplacementConfig.builder().match(ChatColorUtils.COLOR_TAG_PATTERN).replacement((result, builder) -> {
            String escape = result.group(1);
            String replacement = escape == null ? "" : escape;
            return builder.content(replacement);
        }).build());
        if (InteractiveChat.fontTags) {
            icComponent = icComponent.replaceText(TextReplacementConfig.builder().match(ComponentFont.FONT_TAG_PATTERN).replacement((result, builder) -> {
                String escape = result.group(2);
                String replacement = escape == null ? "" : escape;
                return builder.content(replacement);
            }).build());
        }
        return convert(icComponent);
    }

    public static Component convert(net.kyori.adventure.text.Component component) {
        return GsonComponentSerializer.gson().deserialize(InteractiveChatComponentSerializer.gson().serialize(component));
    }

    public static net.kyori.adventure.text.Component convert(Component component) {
        return InteractiveChatComponentSerializer.gson().deserialize(GsonComponentSerializer.gson().serialize(component));
    }

    @Subscribe(priority = ListenerPriority.LOWEST)
    public void onGameToDiscord(GameChatMessagePreProcessEvent event) {
        Component component = event.getMessageComponent();
        event.setMessageComponent(process(component));
    }

    @Subscribe(priority = ListenerPriority.LOWEST)
    public void onVentureChatBungeeToDiscord(VentureChatMessagePreProcessEvent event) {
        Component component = event.getMessageComponent();
        event.setMessageComponent(process(component));
    }
}
