/*
 * This file is part of InteractiveChat4.
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

package com.loohp.interactivechat.proxy.objectholders;

public enum ChatPacketType {

    LEGACY_CHAT(false),
    CLIENT_CHAT(false),
    SYSTEM_CHAT(false),
    PLAYER_CHAT(false),
    CHAT_PREVIEW(true),
    LEGACY_TITLE(false),
    TITLE(false),
    SUBTITLE(false),
    ACTION_BAR(false);

    private final boolean preview;

    ChatPacketType(boolean preview) {
        this.preview = preview;
    }

    public boolean isPreview() {
        return preview;
    }
}
