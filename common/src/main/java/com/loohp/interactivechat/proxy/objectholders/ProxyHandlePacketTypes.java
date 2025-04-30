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

package com.loohp.interactivechat.proxy.objectholders;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ProxyHandlePacketTypes {

    public static final ProxyHandlePacketTypes ALL = new ProxyHandlePacketTypes(EnumSet.allOf(ProxyPacketType.class));

    public static ProxyHandlePacketTypes fromStringList(List<String> types) {
        Set<ProxyPacketType> set = EnumSet.noneOf(ProxyPacketType.class);
        for (String type : types) {
            set.add(ProxyPacketType.valueOf(type.toUpperCase()));
        }
        return new ProxyHandlePacketTypes(set);
    }

    private final Set<ProxyPacketType> types;

    public ProxyHandlePacketTypes(Set<ProxyPacketType> types) {
        this.types = Collections.unmodifiableSet(types);
    }

    public Set<ProxyPacketType> getTypes() {
        return types;
    }

    public boolean hasType(ProxyPacketType type) {
        return types.contains(type);
    }

    public enum ProxyPacketType {

        CHAT, SYSTEM_CHAT, ACTIONBAR, TITLE;

    }
}
