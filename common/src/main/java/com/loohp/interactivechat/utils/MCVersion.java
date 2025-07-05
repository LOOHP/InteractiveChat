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

import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum MCVersion {

    V1_21_7("1.21.7", "1_21_R5", 33),
    V1_21_6("1.21.6", "1_21_R5", 32),
    V1_21_5("1.21.5", "1_21_R4", 31),
    V1_21_4("1.21.4", "1_21_R3", 30),
    V1_21_3("1.21.3", "1_21_R2", 29),
    V1_21_2("1.21.2", "1_21_R2", 28),
    V1_21_1("1.21.1", "1_21_R1", 27),
    V1_21("1.21", "1_21_R1", 26),
    V1_20_6("1.20.6", "1_20_R4", 25),
    V1_20_5("1.20.5", "1_20_R4", 24),
    V1_20_3("1.20.3", "1_20_R3", 23),
    V1_20_2("1.20.2", "1_20_R2", 22),
    V1_20("1.20", "1_20_R1", 21),
    V1_19_4("1.19.4", "1_19_R3", 20),
    V1_19_3("1.19.3", "1_19_R2", 19),
    V1_19("1.19", "1_19_R1", 18),
    V1_18_2("1.18.2", "1_18_R2", 17),
    V1_18("1.18", "1_18_R1", 16),
    V1_17("1.17", "1_17_R1", 15),
    V1_16_4("1.16.4", "1_16_R3", 14),
    V1_16_2("1.16.2", "1_16_R2", 13),
    V1_16("1.16", "1_16_R1", 12),
    V1_15("1.15", "1_15_R1", 11),
    V1_14("1.14", "1_14_R1", 10),
    V1_13_1("1.13.1", "1_13_R2", 9),
    V1_13("1.13", "1_13_R1", 8),
    V1_12("1.12", "1_12_R1", 7),
    V1_11("1.11", "1_11_R1", 6),
    V1_10("1.10", "1_10_R1", 5),
    V1_9_4("1.9.4", "1_9_R2", 4),
    V1_9("1.9", "1_9_R1", 3),
    V1_8_4("1.8.4", "1_8_R3", 2),
    V1_8_3("1.8.3", "1_8_R2", 1),
    V1_8("1.8", "1_8_R1", 0),
    UNSUPPORTED("Unsupported", null, -1);

    public static final MCVersion MINIMUM_SUPPORTED_VERSION = V1_8;

    private static final MCVersion[] SUPPORTED_VALUES = Arrays.stream(values()).filter(v -> v.isSupported()).toArray(MCVersion[]::new);

    public static MCVersion resolve() {
        MCVersion version = fromVersion(Bukkit.getVersion());
        if (version.isSupported()) {
            return version;
        }
        return fromPackageName(Bukkit.getServer().getClass().getPackage().getName());
    }

    public static MCVersion fromVersion(String bukkitVersion) {
        Pattern versionPattern = Pattern.compile("(?i)\\(MC:? ([0-9]+\\.[0-9]+(?:\\.[0-9]+)?)\\)");
        Matcher matcher = versionPattern.matcher(bukkitVersion);
        if (matcher.find()) {
            String minecraftVersion = matcher.group(1);
            for (MCVersion version : SUPPORTED_VALUES) {
                if (minecraftVersion.equals(version.getMinecraftVersion())) {
                    return version;
                }
            }
        }
        return UNSUPPORTED;
    }

    public static MCVersion fromPackageName(String packageName) {
        for (MCVersion version : SUPPORTED_VALUES) {
            if (packageName.contains(version.getPackageName())) {
                return version;
            }
        }
        return UNSUPPORTED;
    }

    public static MCVersion fromNumber(int number) {
        for (MCVersion version : SUPPORTED_VALUES) {
            if (version.shortNum == number) {
                return version;
            }
        }
        return UNSUPPORTED;
    }

    private final String name;
    private final String packageName;
    private final int shortNum;

    MCVersion(String name, String packageName, int shortNum) {
        this.name = name;
        this.packageName = packageName;
        this.shortNum = shortNum;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getMinecraftVersion() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getNumber() {
        return shortNum;
    }

    public int compareWith(MCVersion version) {
        return this.shortNum - version.shortNum;
    }

    public boolean isOlderThan(MCVersion version) {
        return compareWith(version) < 0;
    }

    public boolean isOlderOrEqualTo(MCVersion version) {
        return compareWith(version) <= 0;
    }

    public boolean isNewerThan(MCVersion version) {
        return compareWith(version) > 0;
    }

    public boolean isNewerOrEqualTo(MCVersion version) {
        return compareWith(version) >= 0;
    }

    public boolean isBetweenInclusively(MCVersion v1, MCVersion v2) {
        int difference = v1.compareWith(v2);
        if (difference == 0) {
            return this.equals(v1);
        } else if (difference < 0) {
            return this.isNewerOrEqualTo(v1) && this.isOlderOrEqualTo(v2);
        } else {
            return this.isNewerOrEqualTo(v2) && this.isOlderOrEqualTo(v1);
        }
    }

    public boolean isLegacy() {
        return isOlderOrEqualTo(V1_12);
    }

    public boolean isOld() {
        return isOlderOrEqualTo(V1_8_4);
    }

    public boolean isSupported() {
        return this.shortNum >= MINIMUM_SUPPORTED_VERSION.shortNum;
    }

    public boolean isLegacyRGB() {
        return isOlderThan(V1_16);
    }

}