/*
 * This file is part of InteractiveChat.
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

import com.loohp.interactivechat.objectholders.ValuePairs;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComponentFont {

    public static final Pattern FONT_TAG_PATTERN = Pattern.compile("(?i)(?:(?<!\\\\)(\\\\)\\\\|(?<!\\\\))\\[font=([a-z:0-9]*)\\]");
    public static final Pattern FONT_TAG_ESCAPE = Pattern.compile("(?i)\\\\(\\[font=[a-z:0-9]*\\])");

    public static Component parseFont(Component component) {
        component = ComponentFlattening.flatten(component);
        List<Component> children = new ArrayList<>();
        Key currentFont = null;
        for (Component child : component.children()) {
            if (child instanceof TextComponent) {
                TextComponent text = (TextComponent) child;
                Style style = text.style();
                if (style.font() == null) {
                    style = style.font(currentFont);
                }
                ValuePairs<Component, Key> result = parseTags(text.content(), style);
                Component parsed = result.getFirst();
                List<Component> sections = ComponentFlattening.flatten(parsed).children();
                children.addAll(sections);
                currentFont = result.getSecond();
            } else {
                if (child.font() == null) {
                    children.add(child.font(currentFont));
                } else {
                    children.add(child);
                }
            }
        }
        return ComponentCompacting.optimize(component.children(children));
    }

    private static ValuePairs<Component, Key> parseTags(String content, Style style) {
        Component component = Component.empty();
        Key lastTagKey = null;
        Matcher matcher = FONT_TAG_PATTERN.matcher(content);
        int start = 0;
        while (matcher.find()) {
            String escape = matcher.group(1);
            String font = matcher.group(2);
            lastTagKey = font.isEmpty() ? null : Key.key(font);
            int end = matcher.start();
            String section = content.substring(start, end);
            if (escape != null) {
                section += escape;
            }
            component = component.append(Component.text(section).style(style));
            style = style.font(lastTagKey);
            start = matcher.end();
        }
        String section = content.substring(start);
        component = component.append(Component.text(section).style(style));
        component = ComponentFlattening.flatten(ComponentCompacting.optimize(component));

        List<Component> children = new ArrayList<>(component.children());
        for (int i = 0; i < children.size(); i++) {
            Component child = children.get(i);
            if (child instanceof TextComponent) {
                TextComponent textComponent = (TextComponent) child;
                matcher = FONT_TAG_ESCAPE.matcher(textComponent.content());
                StringBuffer sb = new StringBuffer();
                while (matcher.find()) {
                    String escaped = matcher.group(1);
                    matcher.appendReplacement(sb, escaped);
                }
                matcher.appendTail(sb);

                textComponent = textComponent.content(sb.toString());
                children.set(i, textComponent);
            }
        }

        return new ValuePairs<>(ComponentCompacting.optimize(component.children(children)), lastTagKey);
    }

}
