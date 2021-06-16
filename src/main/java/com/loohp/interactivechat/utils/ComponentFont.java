package com.loohp.interactivechat.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;

public class ComponentFont {
	
	public static final Pattern FONT_TAG_PATTERN = Pattern.compile("(?i)(?:(?<!\\\\)(\\\\)\\\\|(?<!\\\\))\\[font=([a-z:0-9]*)\\]");
	public static final Pattern FONT_TAG_ESCAPE = Pattern.compile("(?i)\\\\(\\[font=[a-z:0-9]*\\])");
	
	public static Component parseFont(Component component) {
		component = ComponentFlattening.flatten(component);
		List<Component> children = new ArrayList<>(component.children());
		Key currentFont = null;
		for (int i = 0; i < children.size(); i++) {
			Component child = children.get(i);
			if (child instanceof TextComponent) {
				TextComponent text = (TextComponent) child.style(child.style().toBuilder().merge(Style.style().font(currentFont).build()).build());
				Component parsed = parseTags(text.content(), text.style());
				List<Component> converted = ComponentFlattening.flatten(parsed).children();
				text = text.children(converted).content("");
				currentFont = converted.get(converted.size() - 1).style().font();
				children.set(i, text);
			}
		}
		return ComponentCompacting.optimize(component.children(children));
	}
	
	private static Component parseTags(String content, Style style) {
		Component component = Component.text("");
		Matcher matcher = FONT_TAG_PATTERN.matcher(content);
		int start = 0;
		while (matcher.find()) {
			String escape = matcher.group(1);
			String font = matcher.group(2);
			Key key = font.isEmpty() ? null : Key.key(font);
			int end = matcher.start();
			String section = content.substring(start, end);
			if (escape != null) {
				section += escape;
			}
			component = component.append(Component.text(section).style(style));
			style = style.font(key);
			start = matcher.end();
		}
		String section = content.substring(start);
		component = component.append(Component.text(section).style(style));
		component = ComponentFlattening.flatten(ComponentCompacting.optimize(component));
		
		List<Component> children = new ArrayList<>(component.children());
		for (int i = 0; i < children.size(); i++) {
			Component child = children.get(i);
			if (child instanceof TextComponent) {
				TextComponent textComponent = (TextComponent) child;
				matcher = FONT_TAG_ESCAPE.matcher(textComponent.content());
				StringBuilder sb = new StringBuilder();
				while (matcher.find()) {
					String escaped = matcher.group(1);
					matcher.appendReplacement(sb, escaped);
				}
				matcher.appendTail(sb);
				
				textComponent = textComponent.content(sb.toString());
				children.set(i, textComponent);
			}
		}
		
		return ComponentCompacting.optimize(component.children(children)); 
	}

}
