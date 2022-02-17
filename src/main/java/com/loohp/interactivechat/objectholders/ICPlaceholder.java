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

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

public abstract class ICPlaceholder {

    protected Pattern keyword;
    protected String name;
    protected String description;
    protected String permission;
    protected long cooldown;
    protected UUID internalId;

    public ICPlaceholder(Pattern keyword, String name, String description, String permission, long cooldown) {
        this.keyword = keyword;
        this.name = name;
        this.description = description;
        this.permission = permission;
        this.cooldown = cooldown;
        this.internalId = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
    }

    public abstract boolean isBuildIn();

    public String getPermission() {
        return permission;
    }

    public Pattern getKeyword() {
        return keyword;
    }

    public long getCooldown() {
        return cooldown;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
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
