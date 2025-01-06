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
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ComponentCompacting {

    public static Component optimize(Component component) {
        return optimizeEvents(component).compact();
    }

    public static Component optimizeEvents(Component component) {
        component = ComponentFlattening.flatten(component);
        List<Component> children = component.children();
        if (children.isEmpty()) {
            return component;
        }
        List<Component> optimized = new ArrayList<>();
        HoverEvent<?> hoverEvent = null;
        ClickEvent clickEvent = null;
        Component currentComponent = Component.empty().hoverEvent(hoverEvent).clickEvent(clickEvent);
        for (Component child : children) {
            if ((Objects.equals(child.hoverEvent(), hoverEvent) && Objects.equals(child.clickEvent(), clickEvent)) || (child instanceof TextComponent && ((TextComponent) child).content().isEmpty())) {
                currentComponent = currentComponent.append(child.hoverEvent(null).clickEvent(null));
            } else {
                optimized.add(currentComponent);
                hoverEvent = child.hoverEvent();
                clickEvent = child.clickEvent();
                currentComponent = Component.empty().hoverEvent(hoverEvent).clickEvent(clickEvent).append(child.hoverEvent(null).clickEvent(null));
            }
        }
        optimized.add(currentComponent);

        return component.children(optimized);
    }

}