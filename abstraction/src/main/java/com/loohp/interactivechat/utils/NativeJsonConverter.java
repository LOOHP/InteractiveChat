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

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NativeJsonConverter {

    private static final String NATIVE_PACKAGE = new String(new char[] {'c', 'o', 'm', '.', 'g', 'o', 'o', 'g', 'l', 'e', '.', 'g', 's', 'o', 'n'});

    public static final Gson GSON = new Gson();

    private static final Object nativeGSON;
    private static final Class<?> nativeGsonElementClass;
    private static final Method nativeGSONToJsonMethod;
    private static final Method nativeGSONFromJsonMethod;

    static {
        try {
            Class<?> nativeGsonClass = Class.forName(NATIVE_PACKAGE + ".Gson");
            nativeGsonElementClass = Class.forName(NATIVE_PACKAGE + ".JsonElement");
            nativeGSON = nativeGsonClass.getConstructor().newInstance();
            nativeGSONToJsonMethod = nativeGsonClass.getMethod("toJson", nativeGsonElementClass);
            nativeGSONFromJsonMethod = nativeGsonClass.getMethod("fromJson", String.class, Class.class);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonElement fromNative(Object nativeJsonElement) {
        try {
            String json = (String) nativeGSONToJsonMethod.invoke(nativeGSON, nativeJsonElement);
            return GSON.fromJson(json, JsonElement.class);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object toNative(JsonElement jsonElement) {
        try {
            String json = GSON.toJson(jsonElement);
            return nativeGSONFromJsonMethod.invoke(nativeGSON, json, nativeGsonElementClass);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
