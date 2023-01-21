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

package com.loohp.interactivechat.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.Style.Merge;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ComponentModernizing {

    public static final Pattern COLOR_CODE_PATTERN = Pattern.compile("\u00a7.");

    public static Component modernize(Component component) {
        component = ComponentFlattening.flatten(component);
        List<Component> children = new ArrayList<>(component.children());
        for (int i = 0; i < children.size(); i++) {
            Component child = children.get(i);
            if (child instanceof TextComponent) {
                TextComponent text = (TextComponent) child;
                String content = text.content();
                TextComponent modern = LegacyComponentSerializer.legacySection().deserialize(content);
                String modernContent = COLOR_CODE_PATTERN.matcher(modern.content()).replaceAll("");
                modern = modern.content(modernContent);
                modern = modern.style(modern.style().merge(text.style(), Merge.Strategy.IF_ABSENT_ON_TARGET));
                children.set(i, modern);
            } else if (child instanceof TranslatableComponent) {
                TranslatableComponent trans = (TranslatableComponent) child;
                List<Component> withs = new ArrayList<>(trans.args());
                withs.replaceAll(arg -> modernize(arg));
                trans = trans.args(withs);
                children.set(i, trans);
            }
        }
        return ComponentCompacting.optimize(component.children(children));
    }

}
