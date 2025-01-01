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

import com.comphenix.net.bytebuddy.description.method.MethodDescription;
import com.comphenix.net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import com.comphenix.net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy.Default;
import com.comphenix.net.bytebuddy.implementation.MethodDelegation;
import com.comphenix.net.bytebuddy.implementation.bind.annotation.AllArguments;
import com.comphenix.net.bytebuddy.implementation.bind.annotation.Origin;
import com.comphenix.net.bytebuddy.implementation.bind.annotation.RuntimeType;
import com.comphenix.net.bytebuddy.implementation.bind.annotation.This;
import com.comphenix.net.bytebuddy.matcher.ElementMatcher;
import com.comphenix.net.bytebuddy.matcher.ElementMatchers;
import com.comphenix.protocol.utility.ByteBuddyFactory;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public abstract class DummyPlayer implements Player {

    private static final Constructor<? extends DummyPlayer> CONSTRUCTOR = setupProxyPlayerConstructor(true);

    private static Constructor<? extends DummyPlayer> setupProxyPlayerConstructor(boolean init) {
        if (init) {
            try {
                return setupProxyPlayerConstructor(false);
            } catch (Throwable ignore) {
            }
        }

        MethodDelegation implementation = MethodDelegation.to(new Object() {
            @RuntimeType
            public Object delegate(@This Object obj, @Origin Method method, @AllArguments Object... args) {
                throw new UnsupportedOperationException("This operation is unsupported for DummyPlayer");
            }
        });
        ElementMatcher.Junction<MethodDescription> callbackFilter = ElementMatchers.not(ElementMatchers.isAbstract());

        try {
            return ByteBuddyFactory.getInstance()
                    .createSubclass(DummyPlayer.class, Default.IMITATE_SUPER_CLASS)
                    .name(DummyPlayer.class.getPackage().getName() + ".DummyPlayerInvocationHandler")
                    .implement(Player.class)
                    .method(callbackFilter)
                    .intercept(implementation)
                    .make()
                    .load(DummyPlayer.class.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded()
                    .getDeclaredConstructor(String.class, UUID.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to find DummyPlayer constructor!", e);
        }
    }

    public static DummyPlayer newInstance(String name, UUID uuid) {
        try {
            return CONSTRUCTOR.newInstance(name, uuid);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private final String name;
    private final UUID uuid;

    public DummyPlayer(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

}
