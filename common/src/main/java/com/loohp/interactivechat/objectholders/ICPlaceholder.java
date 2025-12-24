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

package com.loohp.interactivechat.objectholders;

import net.kyori.adventure.text.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class ICPlaceholder {

    public static Pattern colorCodeIgnoredPattern(Pattern pattern) {
        String regex = pattern.pattern();
        if (regex.isEmpty()) {
            return pattern;
        }
        String firstChar = new String(Character.toChars(regex.codePointAt(0)));
        StringBuilder sb = new StringBuilder(firstChar);
        String inCharSet = null;
        if (firstChar.equals("[")) {
            inCharSet = "]";
        } else if (firstChar.equals("{")) {
            inCharSet = "}";
        }
        String lastChar = firstChar;
        boolean lastCharEscaped = false;
        for (int i = firstChar.length(); i < regex.length();) {
            String character = new String(Character.toChars(regex.codePointAt(i)));
            i += character.length();
            boolean shouldNotAppend = lastChar.equals("?") || inCharSet != null;
            if (!lastChar.equals("\\")) {
                if (character.equals(inCharSet)) {
                    if (inCharSet.equals("]")) {
                        sb.append("\u00A7");
                    }
                    inCharSet = null;
                } else {
                    switch (character) {
                        case "[": {
                            inCharSet = "]";
                            break;
                        }
                        case "{": {
                            inCharSet = "}";
                            shouldNotAppend = true;
                            break;
                        }
                        case "|": {
                            shouldNotAppend = true;
                            break;
                        }
                        case "<": {
                            if (lastChar.equals("?")) {
                                inCharSet = ">";
                                shouldNotAppend = true;
                            }
                            break;
                        }
                        default: {
                            try {
                                Pattern.compile(character);
                            } catch (PatternSyntaxException e) {
                                shouldNotAppend = true;
                            }
                            break;
                        }
                    }
                }
            }
            if (!shouldNotAppend) {
                try {
                    Pattern.compile(regex.substring(0, i - (lastChar.equals("\\") ? 2 : 1)) + regex.substring(i));
                } catch (PatternSyntaxException e) {
                    shouldNotAppend = true;
                }
            }
            if (!shouldNotAppend) {
                if (!lastCharEscaped && lastChar.equals("\\")) {
                    sb.insert(sb.length() - 1, "(?:\u00A7.)?");
                } else {
                    sb.append("(?:\u00A7.)?");
                }
            }
            sb.append(character);
            if (lastCharEscaped) {
                lastCharEscaped = false;
            } else if (lastChar.equals("\\")) {
                lastCharEscaped = true;
            }
            lastChar = character;
        }
        String result = sb.toString();
        try {
            return Pattern.compile(result);
        } catch (PatternSyntaxException e) {
            new RuntimeException("This error will NOT affect the functionally of this plugin, however, please still report this to InteractiveChat as we would like to study this! (Original Pattern: \"" + regex + "\")", e).printStackTrace();
            return pattern;
        }
    }

    protected Pattern keyword;
    protected String name;
    protected Component description;
    protected String permission;
    protected long cooldown;
    protected UUID internalId;

    private Pattern colorIgnoredKeyword;

    public ICPlaceholder(Pattern keyword, String name, Component description, String permission, long cooldown) {
        this.keyword = keyword;
        this.name = name;
        this.description = description;
        this.permission = permission;
        this.cooldown = cooldown;
        this.internalId = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
        this.colorIgnoredKeyword = colorCodeIgnoredPattern(keyword);
    }

    public abstract boolean isBuildIn();

    public String getPermission() {
        return permission;
    }

    public Pattern getRawKeyword() {
        return keyword;
    }

    public Pattern getKeyword() {
        return colorIgnoredKeyword;
    }

    public long getCooldown() {
        return cooldown;
    }

    public String getName() {
        return name;
    }

    public Component getDescription() {
        return description;
    }

    public UUID getInternalId() {
        return internalId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((internalId == null) ? 0 : internalId.hashCode());
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
        ICPlaceholder other = (ICPlaceholder) obj;
        if (internalId == null) {
            return other.internalId == null;
        } else {
            return internalId.equals(other.internalId);
        }
    }

}
