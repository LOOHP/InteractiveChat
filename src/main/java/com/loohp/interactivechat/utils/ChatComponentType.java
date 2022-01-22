package com.loohp.interactivechat.utils;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum ChatComponentType {

    IChatBaseComponent(".*net\\.minecraft\\..*\\.IChatBaseComponent.*", object -> {
        return InteractiveChatComponentSerializer.gson().deserialize(WrappedChatComponent.fromHandle(object).getJson());
    }, (component, legacyRGB) -> {
        return WrappedChatComponent.fromJson(legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component)).getHandle();
    }, object -> {
        return WrappedChatComponent.fromHandle(object).getJson();
    }),

    BaseComponentArray(".*\\[Lnet\\.md_5\\.bungee\\.api\\.chat\\.BaseComponent.*", object -> {
        return InteractiveChatComponentSerializer.gson().deserialize(ComponentSerializer.toString((BaseComponent[]) object));
    }, (component, legacyRGB) -> {
        return ComponentSerializer.parse(legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component));
    }, object -> {
        return ComponentSerializer.toString((BaseComponent[]) object);
    }),

    NativeAdventureComponent(".*net\\.kyori\\.adventure\\.text\\.Component.*", object -> {
        return NativeAdventureConverter.componentFromNative(object);
    }, (component, legacyRGB) -> {
        return NativeAdventureConverter.componentToNative(component, legacyRGB);
    }, object -> {
        return NativeAdventureConverter.jsonStringFromNative(object);
    }),

    AdventureComponent(".*com\\.loohp\\.interactivechat\\.libs\\.net\\.kyori\\.adventure\\.text\\.Component.*", object -> {
        return (Component) object;
    }, (component, legacyRGB) -> {
        return component;
    }, object -> {
        return InteractiveChatComponentSerializer.gson().serialize((Component) object);
    });

    private static final ChatComponentType[] BY_PRIORITY = new ChatComponentType[] {AdventureComponent, NativeAdventureComponent, BaseComponentArray, IChatBaseComponent};
    private final String regex;
    private final Function<Object, Component> converterFrom;
    private final BiFunction<Component, Boolean, Object> converterTo;
    private final Function<Object, String> toString;

    ChatComponentType(String regex, Function<Object, Component> converterFrom, BiFunction<Component, Boolean, Object> converterTo, Function<Object, String> toString) {
        this.regex = regex;
        this.converterFrom = converterFrom;
        this.converterTo = converterTo;
        this.toString = toString;
    }

    public static ChatComponentType[] byPriority() {
        return Arrays.copyOf(BY_PRIORITY, BY_PRIORITY.length);
    }

    public String getMatchingRegex() {
        return regex;
    }

    public Component convertFrom(Object object) {
        if (object == null) {
            return null;
        }
        return converterFrom.apply(object);
    }

    public Object convertTo(Component component, boolean legacyRGB) {
        if (component == null) {
            return null;
        }
        return converterTo.apply(component, legacyRGB);
    }

    public String toString(Object object) {
        if (object == null) {
            return null;
        }
        return toString.apply(object);
    }

}