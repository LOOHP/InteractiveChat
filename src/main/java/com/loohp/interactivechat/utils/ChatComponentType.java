package com.loohp.interactivechat.utils;

import java.util.Arrays;
import java.util.function.Function;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.loohp.interactivechat.registry.Registry;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public enum ChatComponentType {

	IChatBaseComponent(".*net\\.minecraft\\.server\\..*\\.IChatBaseComponent.*", object -> {
		return Registry.ADVENTURE_GSON_SERIALIZER.deserialize(WrappedChatComponent.fromHandle(object).getJson());
	}, component -> {
		return WrappedChatComponent.fromJson(Registry.ADVENTURE_GSON_SERIALIZER.serialize(component)).getHandle();
	}), 
	
	BaseComponentArray(".*\\[Lnet\\.md_5\\.bungee\\.api\\.chat\\.BaseComponent.*", object -> {
		return Registry.ADVENTURE_GSON_SERIALIZER.deserialize(ComponentSerializer.toString((BaseComponent[]) object));
	}, component -> {
		return ComponentSerializer.parse(Registry.ADVENTURE_GSON_SERIALIZER.serialize(component));
	}), 
	
	AdventureComponent(".*net\\.kyori\\.adventure\\.text\\.Component.*", object -> {
		return (Component) object;
	}, component -> {
		return component;
	});

	private static final ChatComponentType[] BY_PRIORITY = new ChatComponentType[] {AdventureComponent, BaseComponentArray, IChatBaseComponent};
	private String regex;
	private Function<Object, Component> converterFrom;
	private Function<Component, Object> converterTo;

	ChatComponentType(String regex, Function<Object, Component> converterFrom, Function<Component, Object> converterTo) {
		this.regex = regex;
		this.converterFrom = converterFrom;
		this.converterTo = converterTo;
	}

	public String getMatchingRegex() {
		return regex;
	}

	public Component convertFrom(Object object) {
		return converterFrom.apply(object);
	}

	public Object convertTo(Component component) {
		return converterTo.apply(component);
	}

	public static ChatComponentType[] byPriority() {
		return Arrays.copyOf(BY_PRIORITY, BY_PRIORITY.length);
	}
	
}