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

package com.loohp.interactivechat.modules;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.CustomStringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Set;

public class CommandsDisplay {

    private static final String PATTERN_PREFIX = "(?i)(?:(?<!\\\\)(\\\\)\\\\|(?<!\\\\))";
    private static final String PATTERN_COMMAND = "(/(?:[^\\\\%s]|(?:\\\\[\\\\%s]))*)";
    private static final String ESCAPING_PATTERN = "\\\\([\\\\%s])";

    private static final String ESCAPE_CLEARUP_PREFIX = "(?i)\\\\(";
    private static final String ESCAPE_CLEARUP_COMMAND = "/(?:[^\\\\%s]|(?:\\\\[\\\\%s]))*";
    private static final String ESCAPE_CLEARUP_SUFFIX = ")";

    private static final String JOINT_PATTERN = "(%c)|(%e)";

    public static Component process(Component component) {
        Set<Character> chars = CustomStringUtils.getCharacterSet(InteractiveChat.clickableCommandsFormat.replace("{Command}", ""));
        StringBuilder sb = new StringBuilder();
        for (Character c : chars) {
            sb.append(CustomStringUtils.escapeMetaCharacters(c.toString()));
        }
        String escapeChars = sb.toString();
        String commandMatchingPattern = PATTERN_PREFIX + CustomStringUtils.escapeMetaCharacters(InteractiveChat.clickableCommandsFormat.replace("{Command}", "\0\0\0")).replace("\0\0\0", PATTERN_COMMAND.replace("%s", escapeChars));
        String escapeMatchingPattern = ESCAPE_CLEARUP_PREFIX + CustomStringUtils.escapeMetaCharacters(InteractiveChat.clickableCommandsFormat.replace("{Command}", "\0\0\0")).replace("\0\0\0", ESCAPE_CLEARUP_COMMAND.replace("%s", escapeChars)) + ESCAPE_CLEARUP_SUFFIX;

        String pattern = JOINT_PATTERN.replace("%c", commandMatchingPattern).replace("%e", escapeMatchingPattern);

        return ComponentReplacing.replace(component, pattern, result -> {
            if (result.group(1) != null) {
                String escape = result.group(2);
                String command = result.group(3).replaceAll(ESCAPING_PATTERN.replace("%s", escapeChars), "$1");
                String componentText = InteractiveChat.clickableCommandsDisplay.replace("{Command}", command);
                if (escape != null) {
                    componentText = escape + componentText;
                }
                Component commandComponent = LegacyComponentSerializer.legacySection().deserialize(componentText);
                commandComponent = commandComponent.clickEvent(ClickEvent.clickEvent(InteractiveChat.clickableCommandsAction, command));
                if (!InteractiveChat.clickableCommandsHoverText.isEmpty()) {
                    commandComponent = commandComponent.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.clickableCommandsHoverText)));
                }
                return commandComponent;
            } else if (result.group(4) != null) {
                return result.componentGroup(5);
            } else {
                return result.componentGroup();
            }
        });
    }

}
