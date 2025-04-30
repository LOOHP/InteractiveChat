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

import com.loohp.interactivechat.utils.CustomStringUtils;

import java.util.regex.Pattern;

public class MentionTagConverter {

    private String style;
    private Pattern reverse;

    public MentionTagConverter(String style) {
        this.style = style;
        this.reverse = Pattern.compile(CustomStringUtils.escapeMetaCharacters(style).replace("\\%s", "(.*?)"));
    }

    public String convertToTag(String find, String input) {
        return input.replace(find, getTagStyle(find));
    }

    public String revertTags(String input) {
        return reverse.matcher(input).replaceAll("$1");
    }

    public boolean containsTags(String input) {
        return reverse.matcher(input).find();
    }

    public String getRawTagStyle() {
        return style;
    }

    public String getTagStyle(String find) {
        return style.replace("%s", find);
    }

    public Pattern getReversePattern() {
        return reverse;
    }

}
