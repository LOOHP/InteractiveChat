package com.loohp.interactivechat.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.Style.Merge;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ComponentStyling {
	
	private static final Map<TextDecoration, TextDecoration.State> DECORATIONS_ALL_NOT_SET = new HashMap<>();
	
	static {
		for (TextDecoration decoration : TextDecoration.values()) {
			DECORATIONS_ALL_NOT_SET.put(decoration, TextDecoration.State.NOT_SET);
		}
	}
	
	public static Style colorOverrideMerge(Style from, Style to, Merge.Strategy strategy) {
		if (to.color() != null) {
			return to.merge(from.decorations(DECORATIONS_ALL_NOT_SET), strategy);
		} else {
			return to.merge(from, strategy);
		}
	}
	
	public static Component stripColor(Component component) {
		component = component.color(null);
		List<Component> children = new ArrayList<>(component.children());
		for (int i = 0; i < children.size(); i++) {
			children.set(i, stripColor(children.get(i)));
		}
		return component.children(children);
	}
	
	public static List<Component> splitAtLineBreaks(Component component) {
		List<Component> filtered = new ArrayList<>();
		component = ComponentFlattening.flatten(component);
		List<Component> currentChildrens = new ArrayList<>();
		for (Component child : component.children()) {
			if (child instanceof TextComponent) {
				TextComponent textComponent = (TextComponent) child;
				String[] parts = textComponent.content().split("\n", -1);
				if (parts.length > 1) {
					currentChildrens.add(textComponent.content(parts[0]));
					filtered.add(ComponentCompacting.optimize(component.children(currentChildrens)));
					for (int i = 1; i < parts.length - 1; i++) {
						filtered.add(ComponentCompacting.optimize(component.children(Collections.emptyList()).append(textComponent.content(parts[i]))));
					}
					currentChildrens = new ArrayList<>();
					currentChildrens.add(textComponent.content(parts[parts.length - 1]));
				} else {
					currentChildrens.add(child);
				}
			} else {
				currentChildrens.add(child);
			}
		}
		if (currentChildrens.size() > 0) {
			filtered.add(ComponentCompacting.optimize(component.children(currentChildrens)));
		}
		return filtered;
	}
	
	public static TextColor getFirstColor(Component component) {
		ChatColor chatColor = ChatColorUtils.getColor(ChatColorUtils.getFirstColors(BaseComponent.toLegacyText(ComponentSerializer.parse(InteractiveChatComponentSerializer.gson().serialize(component)))));
		return chatColor == null ? null : ColorUtils.toTextColor(chatColor);
	}

}
