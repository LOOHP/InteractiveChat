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

package com.loohp.interactivechat.objectholders;

import java.util.regex.Pattern;

public class CustomPlaceholder extends ICPlaceholder {

    public static final String CUSTOM_PLACEHOLDER_PERMISSION = "interactivechat.module.custom.";

    private final String key;
    private final ParsePlayer parsePlayer;
    private final boolean parseKeyword;
    private final CustomPlaceholderHoverEvent hover;
    private final CustomPlaceholderClickEvent click;
    private final CustomPlaceholderReplaceText replace;

    public CustomPlaceholder(String key, ParsePlayer parsePlayer, Pattern keyword, boolean parseKeyword, long cooldown, CustomPlaceholderHoverEvent hover, CustomPlaceholderClickEvent click, CustomPlaceholderReplaceText replace, String name, String description) {
        super(keyword, name, description, CUSTOM_PLACEHOLDER_PERMISSION + key, cooldown);
        this.key = key;
        this.parsePlayer = parsePlayer;
        this.parseKeyword = parseKeyword;
        this.hover = hover;
        this.click = click;
        this.replace = replace;
    }

    @Override
    public boolean isBuildIn() {
        return false;
    }

    public String getKey() {
        return key;
    }

    public ParsePlayer getParsePlayer() {
        return parsePlayer;
    }

    public boolean getParseKeyword() {
        return parseKeyword;
    }

    public CustomPlaceholderHoverEvent getHover() {
        return hover;
    }

    public CustomPlaceholderClickEvent getClick() {
        return click;
    }

    public CustomPlaceholderReplaceText getReplace() {
        return replace;
    }

    public enum ParsePlayer {

        VIEWER("viewer", 0),
        SENDER("sender", 1);

        private static final ParsePlayer[] VALUES = values();

        public static ParsePlayer fromString(String name) {
            for (ParsePlayer parsePlayer : VALUES) {
                if (name.equalsIgnoreCase(parsePlayer.toString())) {
                    return parsePlayer;
                }
            }
            return VIEWER;
        }

        public static ParsePlayer fromOrder(int order) {
            for (ParsePlayer parsePlayer : VALUES) {
                if (order == parsePlayer.getOrder()) {
                    return parsePlayer;
                }
            }
            return VIEWER;
        }

        private final String name;
        private final int ord;

        ParsePlayer(String name, int ord) {
            this.name = name;
            this.ord = ord;
        }

        public int getOrder() {
            return ord;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum ClickEventAction {

        OPEN_URL,
        OPEN_FILE,
        RUN_COMMAND,
        SUGGEST_COMMAND,
        CHANGE_PAGE,
        COPY_TO_CLIPBOARD;

    }

    public static class CustomPlaceholderHoverEvent {

        private final boolean enabled;
        private final String text;

        public CustomPlaceholderHoverEvent(boolean enabled, String text) {
            this.enabled = enabled;
            this.text = text;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getText() {
            return text;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (enabled ? 1231 : 1237);
            result = prime * result + ((text == null) ? 0 : text.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            CustomPlaceholderHoverEvent other = (CustomPlaceholderHoverEvent) obj;
            if (enabled != other.enabled) {
                return false;
            }
            if (text == null) {
                return other.text == null;
            } else {
                return text.equals(other.text);
            }
        }

    }

    public static class CustomPlaceholderClickEvent {

        private final boolean enabled;
        private final ClickEventAction action;
        private final String value;

        public CustomPlaceholderClickEvent(boolean enabled, ClickEventAction action, String value) {
            this.enabled = enabled;
            this.action = action;
            this.value = value;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public ClickEventAction getAction() {
            return action;
        }

        public String getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((action == null) ? 0 : action.hashCode());
            result = prime * result + (enabled ? 1231 : 1237);
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            CustomPlaceholderClickEvent other = (CustomPlaceholderClickEvent) obj;
            if (action != other.action) {
                return false;
            }
            if (enabled != other.enabled) {
                return false;
            }
            if (value == null) {
                return other.value == null;
            } else {
                return value.equals(other.value);
            }
        }

    }

    public static class CustomPlaceholderReplaceText {

        private final boolean enabled;
        private final String replaceText;

        public CustomPlaceholderReplaceText(boolean enabled, String replaceText) {
            this.enabled = enabled;
            this.replaceText = replaceText;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getReplaceText() {
            return replaceText;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (enabled ? 1231 : 1237);
            result = prime * result + ((replaceText == null) ? 0 : replaceText.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            CustomPlaceholderReplaceText other = (CustomPlaceholderReplaceText) obj;
            if (enabled != other.enabled) {
                return false;
            }
            if (replaceText == null) {
                return other.replaceText == null;
            } else {
                return replaceText.equals(other.replaceText);
            }
        }

    }

}
