package com.loohp.interactivechat.utils;

import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.transformation.TransformationType;

public class ComponentFont {
	
	private static final MiniMessage MINIMESSAGE_FONT_PARSER = MiniMessage.builder().removeDefaultTransformations().transformation(TransformationType.FONT).build();
	
	public static Component parseMiniMessageFont(Component component) {
		component = ComponentFlattening.flatten(component);
		List<Component> children = new ArrayList<>(component.children());
		Key currentFont = null;
		for (int i = 0; i < children.size(); i++) {
			Component child = children.get(i);
			if (child instanceof TextComponent) {
				TextComponent text = (TextComponent) child.style(child.style().toBuilder().merge(Style.style().font(currentFont).build()).build());
				List<Component> converted = ComponentFlattening.flatten(MINIMESSAGE_FONT_PARSER.deserialize(text.content())).children();
				text = text.children(converted).content("");
				currentFont = converted.get(converted.size() - 1).style().font();
				children.set(i, text);
			}
		}
		return ComponentCompacting.optimize(component, null);
	}

}
