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

public class CooldownResult {

    private final CooldownOutcome outcome;
    private final long now;
    private final long cooldownExpireTime;
    private final ICPlaceholder placeholder;

    public CooldownResult(CooldownOutcome outcome, long now, long cooldownExpireTime, ICPlaceholder placeholder) {
        this.outcome = outcome;
        this.now = now;
        this.cooldownExpireTime = cooldownExpireTime;
        this.placeholder = placeholder;
    }

    public CooldownOutcome getOutcome() {
        return outcome;
    }

    public long getTimeNow() {
        return now;
    }

    public long getCooldownExpireTime() {
        return cooldownExpireTime;
    }

    public ICPlaceholder getPlaceholder() {
        return placeholder;
    }

    public enum CooldownOutcome {

        ALLOW(true),
        ALLOW_BYPASS(true),
        DENY_PLACEHOLDER(false),
        DENY_UNIVERSAL(false);

        private final boolean allowed;

        CooldownOutcome(boolean allowed) {
            this.allowed = allowed;
        }

        public boolean isAllowed() {
            return allowed;
        }

    }

}
