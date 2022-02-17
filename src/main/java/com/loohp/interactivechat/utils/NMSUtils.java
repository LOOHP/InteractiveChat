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

import org.bukkit.Bukkit;

public class NMSUtils {

    public static Class<?> getNMSClass(String path, String... paths) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        ClassNotFoundException error = null;
        try {
            return Class.forName(path.replace("%s", version));
        } catch (ClassNotFoundException e) {
            error = e;
        }
        for (String classpath : paths) {
            try {
                return Class.forName(classpath.replace("%s", version));
            } catch (ClassNotFoundException e) {
                error = e;
            }
        }
        throw error;
    }

}
