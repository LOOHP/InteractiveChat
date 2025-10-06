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

import com.loohp.interactivechat.nms.NMS;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public enum ChatComponentType {

    IChatBaseComponent(".*(?:net\\.minecraft\\..*\\.IChatBaseComponent|net\\.minecraft\\.network\\.chat\\.Component).*", object -> {
        return InteractiveChatComponentSerializer.gson().deserialize(NMS.getInstance().serializeChatComponent(object));
    }, (component, legacyRGB) -> {
        return NMS.getInstance().deserializeChatComponent(legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component));
    }, object -> {
        return NMS.getInstance().serializeChatComponent(object);
    }, component -> {
        return true;
    }),

    BaseComponentArray(".*\\[Lnet\\.md_5\\.bungee\\.api\\.chat\\.BaseComponent.*", object -> {
        return InteractiveChatComponentSerializer.gson().deserialize(ComponentSerializer.toString((BaseComponent[]) object));
    }, (component, legacyRGB) -> {
        return ComponentSerializer.parse(legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component));
    }, object -> {
        return ComponentSerializer.toString((BaseComponent[]) object);
    }, component -> {
        return true;
    }),

    NativeAdventureComponent(".*net\\.kyori\\.adventure\\.text\\.Component.*", object -> {
        return NativeAdventureConverter.componentFromNative(object);
    }, (component, legacyRGB) -> {
        return NativeAdventureConverter.componentToNative(component, legacyRGB);
    }, object -> {
        return NativeAdventureConverter.jsonStringFromNative(object);
    }, component -> {
        return NativeAdventureConverter.canHandle(component);
    }),

    AdventureComponent(".*com\\.loohp\\.interactivechat\\.libs\\.net\\.kyori\\.adventure\\.text\\.Component.*", object -> {
        return (Component) object;
    }, (component, legacyRGB) -> {
        return component;
    }, object -> {
        return InteractiveChatComponentSerializer.gson().serialize((Component) object);
    }, component -> {
        return true;
    }),

    JsonString(".*java\\.lang\\.String.*", object -> {
        return InteractiveChatComponentSerializer.gson().deserialize((String) object);
    }, (component, legacyRGB) -> {
        return legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
    }, object -> {
        return (String) object;
    }, component -> {
        return true;
    });

    private static final ConcurrentHashMap<Object, String> SERIALIZE_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Component> DESERIALIZE_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Object> DESERIALIZE_NMS_CACHE = new ConcurrentHashMap<>();

    private static final List<ChatComponentType> BY_PRIORITY = Collections.unmodifiableList(Arrays.asList(AdventureComponent, NativeAdventureComponent, JsonString, BaseComponentArray, IChatBaseComponent));

    public static List<ChatComponentType> byPriority() {
        return BY_PRIORITY;
    }

    private final String regex;
    private final Function<Object, Component> converterFrom;
    private final BiFunction<Component, Boolean, Object> converterTo;
    private final Function<Object, String> toJsonString;
    private final Predicate<Component> canHandle;

    ChatComponentType(String regex, Function<Object, Component> converterFrom, BiFunction<Component, Boolean, Object> converterTo, Function<Object, String> toString, Predicate<Component> canHandle) {
        this.regex = regex;
        this.converterFrom = converterFrom;
        this.converterTo = converterTo;
        this.toJsonString = toString;
        this.canHandle = canHandle;
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

    public boolean canHandle(Component component) {
        return canHandle.test(component);
    }

    public String toString(Object object) {
        return toJsonString(object);
    }

    public static void clearCache() {
        SERIALIZE_CACHE.clear();
        DESERIALIZE_CACHE.clear();
        DESERIALIZE_NMS_CACHE.clear();
    }

    public static int getCacheSize() {
        return SERIALIZE_CACHE.size() + DESERIALIZE_CACHE.size() + DESERIALIZE_NMS_CACHE.size();
    }

}