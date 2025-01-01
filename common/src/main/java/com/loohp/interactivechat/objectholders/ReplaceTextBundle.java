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

package com.loohp.interactivechat.objectholders;

import java.util.Objects;

public class ReplaceTextBundle implements Comparable<ReplaceTextBundle> {

    private final String placeholder;
    private final ICPlayer player;
    private final String replaceText;

    public ReplaceTextBundle(String placeholder, ICPlayer player, String replaceText) {
        this.placeholder = placeholder;
        this.player = player;
        this.replaceText = replaceText;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public ICPlayer getPlayer() {
        return player;
    }

    public String getReplaceText() {
        return replaceText;
    }

    @Override
    public int compareTo(ReplaceTextBundle anotherReplaceTextBundle) {
        int compare = Integer.compare(placeholder.length(), anotherReplaceTextBundle.placeholder.length());
        if (compare != 0) {
            return compare;
        } else {
            return Integer.compare(replaceText.length(), anotherReplaceTextBundle.replaceText.length());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReplaceTextBundle that = (ReplaceTextBundle) o;
        return placeholder.equals(that.placeholder) && player.equals(that.player) && replaceText.equals(that.replaceText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeholder, player, replaceText);
    }

}
