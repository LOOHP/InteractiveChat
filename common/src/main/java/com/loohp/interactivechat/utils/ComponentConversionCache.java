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
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * a broadcast reaches every player as the same JSON, and IC turns it into the same adventure component
 * (and back into the same JSON) once per receiver. Adventure components and strings are immutable, so the results
 * can be shared. NMS components are mutable, so they are only used as a short-lived key (see fromNms), never as a
 * cached value.
 */
public class ComponentConversionCache {

    private static final int MAX_SIZE = 1024;

    private static final Map<String, Component> DESERIALIZED = new ConcurrentHashMap<>();
    private static final Map<Component, String> SERIALIZED = new ConcurrentHashMap<>();
    private static final Map<Component, String> SERIALIZED_LEGACY = new ConcurrentHashMap<>();

    // A broadcast hands the same NMS component instance to every receiver's packet, and serializing it (localized
    // codec, then JSON) is the expensive half of reading a message. Keyed by instance and locale, and only trusted for
    // a second, so a plugin that changes a component it already sent cannot get the old text back later.
    private static final long NMS_MILLIS = 1000;
    private static final Map<NmsKey, TimedComponent> FROM_NMS = new ConcurrentHashMap<>();

    private static final LongAdder HITS = new LongAdder();
    private static final LongAdder MISSES = new LongAdder();

    public static Component fromNms(Object handle, Player player) {
        NmsKey key = new NmsKey(handle, player == null ? "" : player.getLocale());
        long now = System.currentTimeMillis();
        TimedComponent cached = FROM_NMS.get(key);
        if (cached != null && now - cached.time < NMS_MILLIS) {
            HITS.increment();
            return cached.component;
        }
        Component component = deserialize(NMS.getInstance().serializeChatComponent(handle, player));
        put(FROM_NMS, key, new TimedComponent(component, now));
        return component;
    }

    public static Component deserialize(String json) {
        Component component = DESERIALIZED.get(json);
        if (component != null) {
            HITS.increment();
            return component;
        }
        MISSES.increment();
        component = InteractiveChatComponentSerializer.gson().deserialize(json);
        put(DESERIALIZED, json, component);
        return component;
    }

    public static String serialize(Component component, boolean legacyRGB) {
        Map<Component, String> cache = legacyRGB ? SERIALIZED_LEGACY : SERIALIZED;
        String json = cache.get(component);
        if (json != null) {
            HITS.increment();
            return json;
        }
        MISSES.increment();
        json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
        put(cache, component, json);
        return json;
    }

    private static <K, V> void put(Map<K, V> cache, K key, V value) {
        if (cache.size() >= MAX_SIZE) {
            cache.clear();
        }
        cache.put(key, value);
    }

    public static void clear() {
        DESERIALIZED.clear();
        SERIALIZED.clear();
        SERIALIZED_LEGACY.clear();
        FROM_NMS.clear();
    }

    public static String statsAndReset() {
        long hits = HITS.sumThenReset();
        long misses = MISSES.sumThenReset();
        long total = hits + misses;
        return "conversion cache hits: " + hits + "/" + total + (total == 0 ? "" : " (" + (hits * 100 / total) + " %)");
    }

    /**
     * Map key that compares by instance.
     */
    public static final class IdentityKey {

        private final Object object;

        public IdentityKey(Object object) {
            this.object = object;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof IdentityKey && ((IdentityKey) other).object == object;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(object);
        }

    }

    private static final class NmsKey {

        private final Object handle;
        private final String locale;

        private NmsKey(Object handle, String locale) {
            this.handle = handle;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof NmsKey)) {
                return false;
            }
            NmsKey key = (NmsKey) other;
            return key.handle == handle && key.locale.equals(locale);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(handle) * 31 + locale.hashCode();
        }

    }

    private static final class TimedComponent {

        private final Component component;
        private final long time;

        private TimedComponent(Component component, long time) {
            this.component = component;
            this.time = time;
        }

    }

}
