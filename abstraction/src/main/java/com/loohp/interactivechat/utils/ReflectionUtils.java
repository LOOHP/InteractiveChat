/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2024. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2024. Contributors
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Predicate;

public class ReflectionUtils {

    public static Field findDeclaredField(Class<?> clazz, Class<?> fieldType, String... names) throws NoSuchFieldException {
        NoSuchFieldException exception = null;
        for (String name : names) {
            try {
                Field field = clazz.getDeclaredField(name);
                if (field.getType().equals(fieldType)) {
                    return field;
                }
            } catch (NoSuchFieldException e) {
                exception = e;
            }
        }
        if (exception == null) {
            throw new NoSuchFieldException();
        } else {
            throw exception;
        }
    }

    public static Method findDeclaredMethod(Class<?> clazz, Class<?>[] parameters, String... names) throws NoSuchMethodException {
        NoSuchMethodException exception = null;
        for (String name : names) {
            try {
                return clazz.getDeclaredMethod(name, parameters);
            } catch (NoSuchMethodException e) {
                exception = e;
            }
        }
        if (exception == null) {
            throw new NoSuchMethodException();
        } else {
            throw exception;
        }
    }

}
