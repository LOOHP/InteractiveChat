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

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class TimeUtils {

    public static final DecimalFormat FORMAT = new DecimalFormat("00");

    public static String getReadableTimeBetween(long from, long to) {
        LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochMilli(from), ZoneId.systemDefault());
        LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(to), ZoneId.systemDefault());
        long hrs = ChronoUnit.HOURS.between(start, now);
        long mins = ChronoUnit.MINUTES.between(start, now);
        long secs = ChronoUnit.SECONDS.between(start, now);
        return (hrs == 0 ? "" : (hrs + ":")) + FORMAT.format(mins % 60) + ":" + FORMAT.format(secs % 60);
    }

}
