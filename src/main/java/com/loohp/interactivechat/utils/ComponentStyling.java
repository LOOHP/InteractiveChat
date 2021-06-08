package com.loohp.interactivechat.utils;

import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ComponentStyling {
	
	public static Component stripColor(Component component) {
		component = component.color(null);
		List<Component> children = new ArrayList<>(component.children());
		for (int i = 0; i < children.size(); i++) {
			children.set(i, stripColor(children.get(i)));
		}
		return component.children(children);
	}
	
	public static TextColor getFirstColor(Component component) {
		ChatColor chatColor = ChatColorUtils.getColor(ChatColorUtils.getFirstColors(BaseComponent.toLegacyText(ComponentSerializer.parse(InteractiveChatComponentSerializer.gson().serialize(component)))));
		return chatColor == null ? null : ColorUtils.toTextColor(chatColor);
	}

}
