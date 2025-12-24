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

package com.loohp.interactivechat.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComponentStyling {

    private static final Pattern LEGACY_PATTERN = Pattern.compile("§x(?:§[0-9a-fA-F]){6}|§[0-9a-fklmnor]");

    public static Component parseMiniMessage(String input) {
        Matcher matcher = LEGACY_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            ChatColor chatColor = ChatColorUtils.getColor(matcher.group());
            if (chatColor != null && ChatColorUtils.isColor(chatColor)) {
                TextColor textColor = ColorUtils.toTextColor(chatColor);
                matcher.appendReplacement(sb, "<" + textColor + ">");
            } else {
                String name = chatColor == null ? "reset" : chatColor.getName();
                matcher.appendReplacement(sb, "<" + name + ">");
            }
        }
        matcher.appendTail(sb);
        return MiniMessage.miniMessage().deserialize(sb.toString());
    }

    public static Component stripColor(Component component) {
        component = component.color(null);
        List<Component> children = new ArrayList<>(component.children());
        children.replaceAll(each -> ComponentStyling.stripColor(each));
        return component.children(children);
    }

    public static List<Component> splitAtLineBreaks(Component component) {
        List<Component> filtered = new ArrayList<>();
        component = ComponentFlattening.flatten(component);
        List<Component> currentChildren = new ArrayList<>();
        for (Component child : component.children()) {
            if (child instanceof TextComponent) {
                TextComponent textComponent = (TextComponent) child;
                String[] parts = textComponent.content().split("\\R", -1);
                if (parts.length > 1) {
                    currentChildren.add(textComponent.content(parts[0]));
                    filtered.add(ComponentCompacting.optimize(component.children(currentChildren)));
                    for (int i = 1; i < parts.length - 1; i++) {
                        filtered.add(ComponentCompacting.optimize(component.children(Collections.emptyList()).append(textComponent.content(parts[i]))));
                    }
                    currentChildren = new ArrayList<>();
                    currentChildren.add(textComponent.content(parts[parts.length - 1]));
                } else {
                    currentChildren.add(child);
                }
            } else {
                currentChildren.add(child);
            }
        }
        if (!currentChildren.isEmpty()) {
            filtered.add(ComponentCompacting.optimize(component.children(currentChildren)));
        }
        return filtered;
    }

    public static TextColor getFirstColor(Component component) {
        ChatColor chatColor = ChatColorUtils.getColor(ChatColorUtils.getFirstColors(BaseComponent.toLegacyText(ComponentSerializer.parse(InteractiveChatComponentSerializer.gson().serialize(component)))));
        return chatColor == null ? null : ColorUtils.toTextColor(chatColor);
    }

    public static Component stripEvents(Component component) {
        List<Component> components = new ArrayList<>(ComponentFlattening.flatten(component).children());
        components.replaceAll(part -> part.hoverEvent(null).clickEvent(null));
        return ComponentCompacting.optimize(Component.empty().children(components));
    }

}
