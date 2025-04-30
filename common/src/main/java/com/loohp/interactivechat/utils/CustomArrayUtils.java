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

import java.lang.reflect.Array;
import java.util.Arrays;

public class CustomArrayUtils {

    public static byte[][] divideArray(byte[] source, int chunkSize) {
        int length = (int) Math.ceil(source.length / (double) chunkSize);
        if (length <= 1) {
            return new byte[][] {source};
        }
        byte[][] ret = new byte[length][];
        int start = 0;
        for (int i = 0; i < ret.length; i++) {
            int end = start + chunkSize;
            ret[i] = Arrays.copyOfRange(source, start, Math.min(end, source.length));
            start += chunkSize;
        }
        return ret;
    }

    public static boolean allNull(Object src) {
        if (src == null) {
            return true;
        }
        if (!src.getClass().isArray()) {
            return false;
        }
        for (int i = 0; i < Array.getLength(src); i++) {
            if (Array.get(src, i) != null) {
                return false;
            }
        }
        return true;
    }

    public static boolean anyNull(Object src) {
        if (src == null) {
            return true;
        }
        if (!src.getClass().isArray()) {
            return false;
        }
        for (int i = 0; i < Array.getLength(src); i++) {
            if (Array.get(src, i) == null) {
                return true;
            }
        }
        return false;
    }

}
