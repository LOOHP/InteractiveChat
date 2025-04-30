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

import java.util.regex.MatchResult;

public class OffsetMatchResult implements MatchResult {

    public static MatchResult wrap(MatchResult matchResult, int groupOffset) {
        return new OffsetMatchResult(matchResult, groupOffset);
    }

    private MatchResult backingResult;
    private int groupOffset;

    private OffsetMatchResult(MatchResult backingResult, int groupOffset) {
        this.backingResult = backingResult;
        this.groupOffset = groupOffset;
    }

    @Override
    public int start() {
        return backingResult.start();
    }

    @Override
    public int start(int group) {
        return backingResult.start(getOffsetGroup(group));
    }

    @Override
    public int end() {
        return backingResult.end();
    }

    @Override
    public int end(int group) {
        return backingResult.end(getOffsetGroup(group));
    }

    @Override
    public String group() {
        return backingResult.group();
    }

    @Override
    public String group(int group) {
        return backingResult.group(getOffsetGroup(group));
    }

    @Override
    public int groupCount() {
        return Math.max(0, backingResult.groupCount() - groupOffset);
    }

    public int getOffsetGroup(int group) {
        return group == 0 ? 0 : group + groupOffset;
    }

}
