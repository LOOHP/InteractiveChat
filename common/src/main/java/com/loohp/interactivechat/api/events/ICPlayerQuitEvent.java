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

package com.loohp.interactivechat.api.events;

import com.loohp.interactivechat.objectholders.ICPlayer;
import org.bukkit.event.HandlerList;

/**
 * Called when a icplayer is quits
 *
 * @author LOOHP
 */
public class ICPlayerQuitEvent extends ICPlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public ICPlayerQuitEvent(ICPlayer player, boolean isRemote) {
        super(player, isRemote);
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
