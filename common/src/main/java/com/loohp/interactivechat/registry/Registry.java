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

package com.loohp.interactivechat.registry;

import com.loohp.interactivechat.objectholders.MentionTagConverter;
import com.loohp.interactivechat.utils.ComponentReplacing;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Registry {

    public static final String PLUGIN_MESSAGING_PROTOCOL_IDENTIFIER = "InteractiveChatBungeePing";
    public static final int PLUGIN_MESSAGING_PROTOCOL_VERSION = 20;

    public static final int INTERACTIVE_CHAT_DISCORD_SRV_ADDON_COMPATIBLE_VERSION = 47;

    public static final Pattern ID_PATTERN = Pattern.compile("(?:<(cmd|chat)=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})(:(.*?):)?>)");
    public static final Function<ComponentReplacing.ComponentMatchResult, Component> ID_PATTERN_REPLACEMENT = result -> result.group(4) == null ? Component.empty() : result.componentGroup(4);
    public static final Pattern CHR_ID_PATTERN = Pattern.compile("(?:<(cmd|chat)=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})(\\\\:(.*?)\\\\:)?>)");
    public static final Pattern ID_ESCAPE_PATTERN = Pattern.compile(">");
    public static final Pattern ID_UNESCAPE_PATTERN = Pattern.compile("\\\\>");

    public static final MentionTagConverter MENTION_TAG_CONVERTER = new MentionTagConverter("<IC^%s>");

    public static final int MINECRAFT_1_16_PROTOCOL_VERSION = 735;
    public static final int MINECRAFT_1_19_1_PROTOCOL_VERSION = 760;

    public static final Set<Integer> PROXY_PASSTHROUGH_RELAY_PACKETS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            0x00,
            0x01,
            0x02,
            0x03,
            0x04,
            0x05,
            0x06,
            0x0E,
            0x0F,
            0x11,
            0x12,
            0x14,
            0xFF
    )));

}
