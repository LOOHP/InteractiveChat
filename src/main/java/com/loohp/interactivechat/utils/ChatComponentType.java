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

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum ChatComponentType {

    IChatBaseComponent(".*net\\.minecraft\\..*\\.IChatBaseComponent.*", object -> {
        return InteractiveChatComponentSerializer.gson().deserialize(WrappedChatComponent.fromHandle(object).getJson());
    }, (component, legacyRGB) -> {
        return WrappedChatComponent.fromJson(legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component)).getHandle();
    }, object -> {
        return WrappedChatComponent.fromHandle(object).getJson();
    }),

    BaseComponentArray(".*\\[Lnet\\.md_5\\.bungee\\.api\\.chat\\.BaseComponent.*", object -> {
        return InteractiveChatComponentSerializer.gson().deserialize(ComponentSerializer.toString((BaseComponent[]) object));
    }, (component, legacyRGB) -> {
        return ComponentSerializer.parse(legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component));
    }, object -> {
        return ComponentSerializer.toString((BaseComponent[]) object);
    }),

    NativeAdventureComponent(".*net\\.kyori\\.adventure\\.text\\.Component.*", object -> {
        return NativeAdventureConverter.componentFromNative(object);
    }, (component, legacyRGB) -> {
        return NativeAdventureConverter.componentToNative(component, legacyRGB);
    }, object -> {
        return NativeAdventureConverter.jsonStringFromNative(object);
    }),

    AdventureComponent(".*com\\.loohp\\.interactivechat\\.libs\\.net\\.kyori\\.adventure\\.text\\.Component.*", object -> {
        return (Component) object;
    }, (component, legacyRGB) -> {
        return component;
    }, object -> {
        return InteractiveChatComponentSerializer.gson().serialize((Component) object);
    });

    private static final List<ChatComponentType> BY_PRIORITY = Collections.unmodifiableList(Arrays.asList(AdventureComponent, NativeAdventureComponent, BaseComponentArray, IChatBaseComponent));

    public static List<ChatComponentType> byPriority() {
        return BY_PRIORITY;
    }

    private final String regex;
    private final Function<Object, Component> converterFrom;
    private final BiFunction<Component, Boolean, Object> converterTo;
    private final Function<Object, String> toJsonString;

    ChatComponentType(String regex, Function<Object, Component> converterFrom, BiFunction<Component, Boolean, Object> converterTo, Function<Object, String> toString) {
        this.regex = regex;
        this.converterFrom = converterFrom;
        this.converterTo = converterTo;
        this.toJsonString = toString;
    }

    public String getMatchingRegex() {
        return regex;
    }

    public Component convertFrom(Object object) {
        if (object == null) {
            return null;
        }
        return converterFrom.apply(object);
    }

    public Object convertTo(Component component, boolean legacyRGB) {
        if (component == null) {
            return null;
        }
        return converterTo.apply(component, legacyRGB);
    }

    public String toJsonString(Object object) {
        if (object == null) {
            return null;
        }
        return toJsonString.apply(object);
    }

    public String toString(Object object) {
        return toJsonString(object);
    }

}