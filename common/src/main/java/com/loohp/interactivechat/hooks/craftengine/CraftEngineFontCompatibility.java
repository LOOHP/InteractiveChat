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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactivechat.hooks.craftengine;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.ComponentCompacting;
import com.loohp.interactivechat.utils.ComponentFlattening;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.momirealms.craftengine.core.font.BitmapImage;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class CraftEngineFontCompatibility {

    private static final String PLUGIN_NAME = "CraftEngine";
    private static final long CACHE_TTL = TimeUnit.SECONDS.toMillis(30);

    private static volatile long lastLoad = 0L;
    private static volatile Map<Integer, Key> cachedCodepointFonts = Collections.emptyMap();

    private CraftEngineFontCompatibility() {
    }

    public static void clearCache() {
        synchronized (CraftEngineFontCompatibility.class) {
            lastLoad = 0L;
            cachedCodepointFonts = Collections.emptyMap();
        }
    }

    public static Component process(Component component) {
        if (component == null) {
            return null;
        }

        if (!InteractiveChat.craftEngineFontCompatibility || InteractiveChat.craftEngineFontCompatibilityFonts.isEmpty()) {
            return component;
        }

        Map<Integer, Key> codepointFonts = getCodepointFonts();
        if (codepointFonts.isEmpty()) {
            return component;
        }

        Component flattened = ComponentFlattening.flatten(component);
        List<Component> children = new ArrayList<>();
        boolean changed = false;

        for (Component child : flattened.children()) {
            if (child instanceof TextComponent) {
                TextRepairResult result = repairText((TextComponent) child, codepointFonts);
                children.addAll(result.components);
                changed |= result.changed;
            } else {
                children.add(child);
            }
        }

        if (!changed) {
            return component;
        }

        return ComponentCompacting.optimize(flattened.children(children));
    }

    private static TextRepairResult repairText(TextComponent text, Map<Integer, Key> codepointFonts) {
        String content = text.content();

        if (content.isEmpty() || text.font() != null) {
            return TextRepairResult.unchanged(text);
        }

        List<Component> components = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        Key currentFont = null;
        boolean changed = false;

        for (int index = 0; index < content.length();) {
            int codepoint = content.codePointAt(index);
            String value = content.substring(index, index + Character.charCount(codepoint));

            Key font = shouldRepairCodepoint(codepoint) ? codepointFonts.get(codepoint) : null;

            if (!Objects.equals(currentFont, font)) {
                flushRun(components, current, text.style(), currentFont);
                currentFont = font;
            }

            if (font != null) {
                changed = true;
            }

            current.append(value);
            index += Character.charCount(codepoint);
        }

        flushRun(components, current, text.style(), currentFont);

        if (!changed) {
            return TextRepairResult.unchanged(text);
        }

        return new TextRepairResult(true, components);
    }

    private static boolean shouldRepairCodepoint(int codepoint) {
        if (Character.isWhitespace(codepoint)) {
            return false;
        }

        /*
         * Do not touch normal ASCII.
         * This avoids URLs, version strings, tags, normal names, etc.
         */
        return codepoint > 0x7F;
    }

    private static void flushRun(List<Component> components, StringBuilder current, Style baseStyle, Key font) {
        if (current.length() == 0) {
            return;
        }

        Style style = font == null ? baseStyle : baseStyle.font(font);
        components.add(Component.text(current.toString()).style(style));
        current.setLength(0);
    }

    private static Map<Integer, Key> getCodepointFonts() {
        long now = System.currentTimeMillis();
        if (now - lastLoad < CACHE_TTL) {
            return cachedCodepointFonts;
        }

        synchronized (CraftEngineFontCompatibility.class) {
            now = System.currentTimeMillis();
            if (now - lastLoad < CACHE_TTL) {
                return cachedCodepointFonts;
            }

            cachedCodepointFonts = loadCodepointFonts();
            lastLoad = now;
            return cachedCodepointFonts;
        }
    }

    private static Map<Integer, Key> loadCodepointFonts() {
        if (!InteractiveChat.craftEngineFontCompatibility || InteractiveChat.craftEngineFontCompatibilityFonts.isEmpty()) {
            return Collections.emptyMap();
        }

        if (!Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME)) {
            return Collections.emptyMap();
        }

        try {
            Map<Integer, Key> codepointFonts = new HashMap<>();

            for (BitmapImage bitmapImage : CraftEngine.instance().fontManager().loadedBitmapImages().values()) {
                String fontString = bitmapImage.font().toString().toLowerCase(Locale.ROOT);

                if (!InteractiveChat.craftEngineFontCompatibilityFonts.contains(fontString)) {
                    continue;
                }

                Key fontKey = Key.key(fontString);
                int[][] codepointGrid = bitmapImage.codepointGrid();

                for (int[] row : codepointGrid) {
                    for (int codepoint : row) {
                        if (codepoint > 0 && shouldRepairCodepoint(codepoint)) {
                            codepointFonts.putIfAbsent(codepoint, fontKey);
                        }
                    }
                }
            }

            return codepointFonts;
        } catch (Throwable ignored) {
            return Collections.emptyMap();
        }
    }

    private static final class TextRepairResult {
        private final boolean changed;
        private final List<Component> components;

        private TextRepairResult(boolean changed, List<Component> components) {
            this.changed = changed;
            this.components = components;
        }

        private static TextRepairResult unchanged(Component component) {
            return new TextRepairResult(false, Collections.singletonList(component));
        }
    }
}