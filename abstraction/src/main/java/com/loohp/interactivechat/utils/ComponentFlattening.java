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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style.Merge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComponentFlattening {

    public static Component flatten(Component component) {
        return Component.empty().children(getChildren(component));
    }

    private static List<Component> getChildren(Component component) {
        List<Component> list = new ArrayList<>();
        list.add(component.children(Collections.emptyList()));
        for (Component child : component.children()) {
            child = child.style(child.style().merge(component.style(), Merge.Strategy.IF_ABSENT_ON_TARGET));
            list.addAll(getChildren(child));
        }
        return list;
    }

}
