/*
 * This file is part of InteractiveChat-Abstraction.
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

public class CommandSuggestion<T> {

    public static <T> CommandSuggestion<T> of(int position, T suggestion) {
        return new CommandSuggestion<>(position, suggestion);
    }

    private final int position;
    private final T suggestion;

    private CommandSuggestion(int position, T suggestion) {
        this.position = position;
        this.suggestion = suggestion;
    }

    public int getPosition() {
        return position;
    }

    public T getSuggestion() {
        return suggestion;
    }

    public <E> E getSuggestion(Class<E> suggestionClass) {
        return suggestionClass.cast(suggestion);
    }

}
