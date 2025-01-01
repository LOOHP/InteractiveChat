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

import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatColorUtils {

    public static final char COLOR_CHAR = '\u00a7';
    public static final Pattern COLOR_TAG_PATTERN = Pattern.compile("(?i)(?:(?<!\\\\)(\\\\)\\\\|(?<!\\\\))\\[color=(#[0-9a-fA-F]{6})\\]");
    public static final Pattern COLOR_TAG_ESCAPE = Pattern.compile("(?i)\\\\(\\[color=#[0-9a-fA-F]{6}\\])");
    public static final Pattern RGB_HEX_COLOR = Pattern.compile("#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])");
    public static final Pattern RGB_HEX_COLOR_1 = Pattern.compile("(?:&|" + COLOR_CHAR + ")x(?:&|" + COLOR_CHAR + ")([0-9a-fA-F])(?:&|" + COLOR_CHAR + ")([0-9a-fA-F])(?:&|" + COLOR_CHAR + ")([0-9a-fA-F])(?:&|" + COLOR_CHAR + ")([0-9a-fA-F])(?:&|" + COLOR_CHAR + ")([0-9a-fA-F])(?:&|" + COLOR_CHAR + ")([0-9a-fA-F])");
    public static final Pattern RGB_HEX_COLOR_2 = Pattern.compile("(?:&|" + COLOR_CHAR + ")#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])");
    public static final Pattern STANDARD_COLOR_CODE = Pattern.compile("&([0-9A-Fa-fk-or])");
    public static final Set<ChatColor> FORMATTING_CODES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ChatColor.MAGIC, ChatColor.BOLD, ChatColor.ITALIC, ChatColor.UNDERLINE, ChatColor.STRIKETHROUGH)));

    public static String stripColor(String string) {
        return string.replaceAll("\u00a7[0-9A-Fa-fk-orx]", "");
    }

    public static String filterIllegalColorCodes(String string) {
        return filterIllegalColorCodes(string, com.loohp.interactivechat.InteractiveChat.version.isLegacyRGB());
    }

    public static String filterIllegalColorCodes(String string, boolean legacyRGB) {
        return legacyRGB ? string.replaceAll("\u00a7[^0-9a-fk-or]", "") : string.replaceAll("\u00a7[^0-9A-Fa-fk-orx]", "");
    }

    public static String getLastColors(String input) {
        StringBuilder result = new StringBuilder();

        for (int i = input.length() - 1; i > 0; i--) {
            if (input.charAt(i - 1) == '\u00a7') {
                String color = String.valueOf(input.charAt(i - 1)) + input.charAt(i);
                if ((i - 13) >= 0 && input.charAt(i - 12) == 'x' && input.charAt(i - 13) == '\u00a7') {
                    color = input.substring(i - 13, i + 1);
                    i -= 13;
                }
                if (isLegal(color)) {
                    result.insert(0, color);
                    if (color.charAt(1) == 'x' || isColor(ChatColor.getByChar(input.charAt(i))) || ChatColor.getByChar(input.charAt(i)).equals(ChatColor.RESET)) {
                        break;
                    }
                }
            }
        }

        return result.toString();
    }

    public static String getFirstColors(String input) {
        StringBuilder result = new StringBuilder();
        boolean found = false;

        if (input.length() < 2) {
            return "";
        }

        int i = 1;
        String color = "";
        while (i < input.length()) {
            color = String.valueOf(input.charAt(i - 1)) + input.charAt(i);
            if (input.charAt(i - 1) == '\u00a7' && input.charAt(i) == 'x' && input.length() > i + 13) {
                color = input.substring(i - 1, i + 13);
                i += 13;
            }
            if (isLegal(color)) {
                if (!found) {
                    found = true;
                    result = new StringBuilder(color);
                } else if (color.charAt(1) == 'x' || isColor(ChatColor.getByChar(color.charAt(1)))) {
                    result = new StringBuilder(color);
                } else {
                    result.append(color);
                }
                i++;
            } else if (found) {
                break;
            }
            i++;
        }

        return result.toString();
    }

    public static boolean isColor(ChatColor color) {
        return !FORMATTING_CODES.contains(color) && !color.equals(ChatColor.RESET);
    }

    public static boolean isLegal(String color) {
        if (color.charAt(0) != '\u00a7') {
            return false;
        }
        if (color.matches("\u00a7[0-9a-fk-or]")) {
            return true;
        }
        return color.matches("\u00a7x(\u00a7[0-9a-fA-F]){6}");
    }

    public static ChatColor getColor(String color) {
        if (color.length() >= 2 && color.charAt(1) != 'r') {
            if (color.length() == 2) {
                return ChatColor.getByChar(color.charAt(1));
            } else {
                if (color.charAt(1) == 'x') {
                    String hex = "#" + color.charAt(3) + color.charAt(5) + color.charAt(7) + color.charAt(9) + color.charAt(11) + color.charAt(13);
                    return ChatColor.of(hex);
                } else {
                    return ChatColor.getByChar(color.charAt(1));
                }
            }
        }
        return null;
    }

    public static String addColorToEachWord(String text, String leadingColor) {
        StringBuilder sb = new StringBuilder();
        text = leadingColor + text;
        do {
            int pos = text.indexOf(" ") + 1;
            pos = pos <= 0 ? text.length() : pos;
            String before = leadingColor + text.substring(0, pos);
            //Bukkit.getConsoleSender().sendMessage(leadingColor.replace("\u00a7", "&") + " " + text.replace("\u00a7", "&") + " " + before.replace("\u00a7", "&"));
            sb.append(before);
            text = text.substring(pos);
            leadingColor = getLastColors(before);
        } while (text.length() > 0 && !text.equals(leadingColor));
        return sb.toString();
    }

    private static String hexToColorCode(String hex) {
        if (hex == null) {
            return hex;
        }
        Matcher matcher = RGB_HEX_COLOR.matcher(hex);
        if (matcher.matches()) {
            return COLOR_CHAR + "x" + COLOR_CHAR + matcher.group(1) + COLOR_CHAR + matcher.group(2) + COLOR_CHAR + matcher.group(3) + COLOR_CHAR + matcher.group(4) + COLOR_CHAR + matcher.group(5) + COLOR_CHAR + matcher.group(6);
        } else {
            return COLOR_CHAR + "x" + COLOR_CHAR + "f" + COLOR_CHAR + "f" + COLOR_CHAR + "f" + COLOR_CHAR + "f" + COLOR_CHAR + "f" + COLOR_CHAR + "f";
        }
    }

    public static String translateAlternateColorCodes(char code, String text) {
        return translateAlternateColorCodes(code, text, com.loohp.interactivechat.InteractiveChat.version.isLegacyRGB(), com.loohp.interactivechat.InteractiveChat.rgbTags, com.loohp.interactivechat.InteractiveChat.additionalRGBFormats);
    }

    public static String translateAlternateColorCodes(char code, String text, boolean legacyRGB, boolean rgbTags, List<Pattern> additionalRGBFormats) {
        if (text == null || text.length() < 2) {
            return text;
        }
        if (!legacyRGB) {
            if (rgbTags) {
                Matcher matcher = COLOR_TAG_PATTERN.matcher(text);
                StringBuffer sb = new StringBuffer();
                while (matcher.find()) {
                    String escape = matcher.group(1);
                    String color = matcher.group(2);
                    String replacement = hexToColorCode(color);
                    if (escape != null) {
                        replacement = escape + replacement;
                    }
                    matcher.appendReplacement(sb, replacement);
                }
                matcher.appendTail(sb);

                matcher = COLOR_TAG_ESCAPE.matcher(sb.toString());
                sb = new StringBuffer();
                while (matcher.find()) {
                    String escaped = matcher.group(1);
                    matcher.appendReplacement(sb, escaped);
                }
                matcher.appendTail(sb);

                text = sb.toString();
            }
            Matcher matcher = RGB_HEX_COLOR_1.matcher(text);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, COLOR_CHAR + "x" + COLOR_CHAR + matcher.group(1) + COLOR_CHAR + matcher.group(2) + COLOR_CHAR + matcher.group(3) + COLOR_CHAR + matcher.group(4) + COLOR_CHAR + matcher.group(5) + COLOR_CHAR + matcher.group(6));
            }
            matcher.appendTail(sb);
            matcher = RGB_HEX_COLOR_2.matcher(sb.toString());
            sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, COLOR_CHAR + "x" + COLOR_CHAR + matcher.group(1) + COLOR_CHAR + matcher.group(2) + COLOR_CHAR + matcher.group(3) + COLOR_CHAR + matcher.group(4) + COLOR_CHAR + matcher.group(5) + COLOR_CHAR + matcher.group(6));
            }
            matcher.appendTail(sb);
            text = sb.toString();

            outer:
            for (Pattern pattern : additionalRGBFormats) {
                matcher = pattern.matcher(text);
                sb = new StringBuffer();
                while (matcher.find()) {
                    if (matcher.groupCount() < 6) {
                        continue outer;
                    }
                    matcher.appendReplacement(sb, COLOR_CHAR + "x" + COLOR_CHAR + matcher.group(1) + COLOR_CHAR + matcher.group(2) + COLOR_CHAR + matcher.group(3) + COLOR_CHAR + matcher.group(4) + COLOR_CHAR + matcher.group(5) + COLOR_CHAR + matcher.group(6));
                }
                matcher.appendTail(sb);
                text = sb.toString();
            }
        }

        Matcher matcher = STANDARD_COLOR_CODE.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, COLOR_CHAR + matcher.group(1));
        }
        matcher.appendTail(sb);
        text = sb.toString();

        return text;
    }

    public static String escapeColorCharacters(char character, String str) {
        return str.replaceAll("(?i)(?<!\\\\)\\\\u00A7", character + "");
    }

    public static String unescapeColorCharacters(String str) {
        return str.replaceAll("(?i)(?<!\\\\)\\\\u00A7", COLOR_CHAR + "");
    }

}
