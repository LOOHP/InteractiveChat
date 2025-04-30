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

package com.loohp.interactivechat.utils;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;

public final class ByteBuddyFactory {
    private static final ByteBuddyFactory INSTANCE = new ByteBuddyFactory();

    private ByteBuddyFactory() {
    }

    public static ByteBuddyFactory getInstance() {
        return INSTANCE;
    }

    public <T> DynamicType.Builder.MethodDefinition.ImplementationDefinition.Optional<T> createSubclass(Class<T> clz, ConstructorStrategy.Default constructorStrategy) {
        return new ByteBuddy()
                .subclass(clz, constructorStrategy)
                .implement(GeneratedByteBuddy.class);
    }

    public static final class GeneratedByteBuddy {
    }
}
