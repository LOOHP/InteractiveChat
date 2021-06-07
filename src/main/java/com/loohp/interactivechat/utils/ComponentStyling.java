package com.loohp.interactivechat.utils;

import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.text.Component;

public class ComponentStyling {
	
	public static Component stripColor(Component component) {
		component = component.color(null);
		List<Component> children = new ArrayList<>(component.children());
		for (int i = 0; i < children.size(); i++) {
			children.set(i, stripColor(children.get(i)));
		}
		return component.children(children);
	}

}
