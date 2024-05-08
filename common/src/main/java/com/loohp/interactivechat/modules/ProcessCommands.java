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

package com.loohp.interactivechat.modules;

import com.loohp.interactivechat.objectholders.ProcessSenderResult;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import net.kyori.adventure.text.Component;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessCommands {

    public static final Pattern PATTERN_0 = Pattern.compile("(?:<cmd=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}):(.*?):>)");
    public static final Pattern PATTERN_1 = Pattern.compile("(?:<cmd=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})>)");

    public static final Pattern COLOR_IGNORE_PATTERN_0 = Pattern.compile("(?:(?:§.)*<(?:§.)*c(?:§.)*m(?:§.)*d(?:§.)*=((?:(?:§.)*[0-9a-f]){8}(?:§.)*-(?:(?:§.)*[0-9a-f]){4}(?:§.)*-(?:(?:§.)*[0-9a-f]){4}(?:§.)*-(?:(?:§.)*[0-9a-f]){4}(?:§.)*-(?:(?:§.)*[0-9a-f]){12})(?:§.)*>)");
    public static final Pattern COLOR_IGNORE_PATTERN_1 = Pattern.compile("(?:(?:§.)*<(?:§.)*c(?:§.)*m(?:§.)*d(?:§.)*=((?:(?:§.)*[0-9a-f]){8}(?:§.)*-(?:(?:§.)*[0-9a-f]){4}(?:§.)*-(?:(?:§.)*[0-9a-f]){4}(?:§.)*-(?:(?:§.)*[0-9a-f]){4}(?:§.)*-(?:(?:§.)*[0-9a-f]){12}):(.*)(?:§.)*>)");

    public static ProcessSenderResult process(Component component) {
        UUID uuid = null;
        String text = InteractiveChatComponentSerializer.plainText().serialize(component);
        Matcher matcher0 = PATTERN_0.matcher(text);
        if (matcher0.find()) {
            uuid = UUID.fromString(matcher0.group(1));
        }
        component = ComponentReplacing.replace(component, PATTERN_0.pattern(), false, (result, matchedComponents) -> {
            Component replacement = result.componentGroup(2);
            if (replacement == null) {
                return Component.empty();
            } else {
                return ComponentReplacing.replace(replacement, Registry.ID_UNESCAPE_PATTERN.pattern(), Component.text(">"));
            }
        });
        if (uuid == null) {
            Matcher matcher1 = PATTERN_1.matcher(text);
            if (matcher1.find()) {
                uuid = UUID.fromString(matcher1.group(1));
            }
        }
        component = ComponentReplacing.replace(component, PATTERN_1.pattern(), Component.empty());
        return new ProcessSenderResult(component, uuid);
    }

}
