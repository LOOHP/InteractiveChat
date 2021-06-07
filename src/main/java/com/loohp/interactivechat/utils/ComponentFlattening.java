package com.loohp.interactivechat.utils;

import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style.Merge;

public class ComponentFlattening {
	
	public static Component flatten(Component component) {
		return Component.empty().children(getChildren(component));
	}
	
	private static List<Component> getChildren(Component component) {
		List<Component> list = new ArrayList<>();
		list.add(component.children(new ArrayList<>()));
		for (Component child : component.children()) {
			child = child.style(child.style().toBuilder().merge(component.style(), Merge.Strategy.IF_ABSENT_ON_TARGET).build());
			list.addAll(getChildren(child));
		}
		return list;
	}

}
