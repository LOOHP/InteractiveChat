package com.loohp.interactivechat.utils;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;

public final class ByteBuddyFactory {
    private static final ByteBuddyFactory INSTANCE = new ByteBuddyFactory();

    public ByteBuddyFactory() {
    }

    public static ByteBuddyFactory i() {
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
