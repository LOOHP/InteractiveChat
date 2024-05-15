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

import org.apache.commons.text.translate.UnicodeUnescaper;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomStringUtils {

    private static final String[] META_CHARACTERS = new String[] {"\\", "^", "$", "{", "}", "[", "]", "(", ")", ".", "*", "+", "?", "|", "<", ">", "-", "&", "%", "\"", "'"};
    private static final String[] REPLACE_ALL_META_CHARACTERS = new String[] {"$"};
    private static final String REPLACE_REGEX = "(?<!\\\\)\\$%s|\\\\\\\\\\$%s";
    private static final String REPLACE_ESCAPE_REGEX = "(?<!\\\\)\\\\\\$";
    private static final UnicodeUnescaper UNESCAPE_UNICODE = new UnicodeUnescaper();

    public static String applyReplacementRegex(String str, MatchResult result, int groupOffset) {
        str = str.replaceAll(REPLACE_REGEX.replace("%s", "0"), escapeReplaceAllMetaCharacters(result.group()));
        for (int i = groupOffset + 1; i <= result.groupCount(); i++) {
            String replacement = result.group(i);
            if (replacement == null) {
                replacement = "";
            }
            str = str.replaceAll(REPLACE_REGEX.replace("%s", String.valueOf(i - groupOffset)), escapeReplaceAllMetaCharacters(replacement));
        }
        return str.replaceAll(REPLACE_ESCAPE_REGEX, "\\$");
    }

    public static Set<Character> getCharacterSet(String str) {
        Set<Character> unique = new HashSet<>();
        for (char c : str.toCharArray()) {
            unique.add(c);
        }
        return unique;
    }

    public static String replaceRespectColor(String str, String find, String replace) {
        for (int i = 0; i < str.length(); i++) {
            String after = str.substring(i);
            if (after.startsWith(find)) {
                String afterAfter = after.substring(find.length());
                String before = str.substring(0, i);
                str = before + replace + ChatColorUtils.getLastColors(before) + afterAfter;
                i += replace.length();
            }
        }
        return str;
    }

    public static String replaceRespectColorCaseInsensitive(String str, String find, String replace) {
        for (int i = 0; i < str.length(); i++) {
            String after = str.substring(i);
            if (after.toLowerCase().startsWith(find.toLowerCase())) {
                String afterAfter = after.substring(find.length());
                String before = str.substring(0, i);
                str = before + replace + ChatColorUtils.getLastColors(before) + afterAfter;
                i += replace.length();
            }
        }
        return str;
    }

    public static String replaceFromTo(String stringToReplace, int from, int to, String withString) {
        StringBuilder sb = new StringBuilder(stringToReplace);
        sb.delete(from, to);
        sb.insert(from, withString);
        return sb.toString();
    }

    public static List<String> getAllMatches(String regex, String str) {
        List<String> allMatches = new LinkedList<String>();
        Matcher m = Pattern.compile(regex).matcher(str);
        while (m.find()) {
            allMatches.add(m.group());
        }
        return allMatches;
    }

    public static int ordinalIndexOf(String str, String substr, int n) {
        int pos = str.indexOf(substr);
        while (--n > 0 && pos != -1) {
            pos = str.indexOf(substr, pos + 1);
        }
        return pos;
    }

    public static int occurrencesOfSubstring(String str, String findStr) {
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {
            lastIndex = str.indexOf(findStr, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += findStr.length();
            }
        }
        return count;
    }

    public static double similarity(String s1, String s2) {
        String longer = s1;
        String shorter = s2;
        if (s1.length() < s2.length()) {
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0;
        }
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) {
                costs[s2.length()] = lastValue;
            }
        }
        return costs[s2.length()];
    }

    public static String escapeReplaceAllMetaCharacters(String inputString) {
        for (int i = 0; i < REPLACE_ALL_META_CHARACTERS.length; i++) {
            if (inputString.contains(REPLACE_ALL_META_CHARACTERS[i])) {
                inputString = inputString.replace(REPLACE_ALL_META_CHARACTERS[i], "\\" + REPLACE_ALL_META_CHARACTERS[i]);
            }
        }
        return inputString;
    }

    public static String escapeMetaCharacters(String inputString) {
        for (int i = 0; i < META_CHARACTERS.length; i++) {
            if (inputString.contains(META_CHARACTERS[i])) {
                inputString = inputString.replace(META_CHARACTERS[i], "\\" + META_CHARACTERS[i]);
            }
        }
        return inputString;
    }

    public static String[] splitStringEvery(String s, int interval) {
        int arrayLength = (int) Math.ceil(((s.length() / (double) interval)));
        String[] result = new String[arrayLength];

        int j = 0;
        int lastIndex = result.length - 1;
        for (int i = 0; i < lastIndex; i++) {
            result[i] = s.substring(j, j + interval);
            j += interval;
        } //Add the last bit
        result[lastIndex] = s.substring(j);

        return result;
    }

    public static String getIgnoreColorCodeRegex(String input) {
        return input.replaceAll("(?<!^)(?=(?<!\u00a7.).)(?=(?<!\u00a7).)(?=(?<!\\\\).)", "(\u00a7.)*?");
    }

    public static String insert(String bag, String marble, int index) {
        String bagBegin = bag.substring(0, index);
        String bagEnd = bag.substring(index);
        return bagBegin + marble + bagEnd;
    }

    public static String clearPluginRGBTags(String str) {
        Matcher matcher = ChatColorUtils.COLOR_TAG_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String escape = matcher.group(1);
            String replacement = escape == null ? "" : escape;
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String clearPluginFontTags(String str) {
        Matcher matcher = ComponentFont.FONT_TAG_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String escape = matcher.group(1);
            String replacement = escape == null ? "" : escape;
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String unescapeUnicode(String str) {
        return UNESCAPE_UNICODE.translate(str);
    }

}
