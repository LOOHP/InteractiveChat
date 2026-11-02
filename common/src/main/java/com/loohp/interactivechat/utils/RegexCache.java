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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * small thread-safe cache of compiled regex Patterns, so hot paths that receive a regex
 * as a String (ComponentReplacing, MessagesToIgnore, ...) do not call Pattern.compile on every call.
 * Pattern is immutable and thread-safe. The cache is bounded: when it grows past MAX_SIZE it is
 * cleared, which only costs a recompile (some callers build regexes from player names/nicknames).
 * An invalid regex throws the same PatternSyntaxException as Pattern.compile and is not cached.
 */
public class RegexCache {

    private static final int MAX_SIZE = 2048;
    private static final Map<String, Pattern> CACHE = new ConcurrentHashMap<>();

    public static Pattern get(String regex) {
        Pattern pattern = CACHE.get(regex);
        if (pattern != null) {
            return pattern;
        }
        pattern = Pattern.compile(regex);
        if (CACHE.size() >= MAX_SIZE) {
            CACHE.clear();
        }
        CACHE.put(regex, pattern);
        return pattern;
    }

    public static void clear() {
        CACHE.clear();
    }

}
