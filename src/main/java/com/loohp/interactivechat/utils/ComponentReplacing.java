package com.loohp.interactivechat.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.Style.Merge;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;

public class ComponentReplacing {
	
	public static final String ESCAPE_PREPEND_PATTERN = "(?:(?<=\\\\)(\\\\)|(?<!\\\\))";
	public static final String ESCAPE_PLACEHOLDER_PATTERN = "\\\\(%s)";
	
	private static class ComponentReplacingData {
		private String text;
		private List<Integer> pos;
		
		private ComponentReplacingData(String text, List<Integer> pos) {
			this.text = text;
			this.pos = pos;
		}
		
		public String getText() {
			return text;
		}
		
		public int getChildIndexAt(int pos) {
			return this.pos.get(pos);
		}
		
		public int getPosWithinChild(int pos) {
			int index = getChildIndexAt(pos);
			int first = this.pos.indexOf(index);
			return pos - first;
		}
	}
	
	public static Component replace(Component component, String regex, boolean escaping, Component replace) {
		return replace(component, regex, escaping, groups -> replace);
	}
	
	public static Component replace(Component component, String regex, boolean escaping, Function<String[], Component> replaceFunction) {
		String regexOriginal = regex;
		if (escaping) {
			regex = ESCAPE_PREPEND_PATTERN + regex;
		}
		component = ComponentFlattening.flatten(component);
		List<ComponentReplacingData> sections = getData(component);
		List<Component> children = new ArrayList<>(component.children());
		int offset = 0;
		Pattern pattern = Pattern.compile(regex);
		for (int i = 0; i < sections.size(); i++) {
			ComponentReplacingData data = sections.get(i);
			String text = data.getText();
			Matcher matcher = pattern.matcher(text);
			if (text.length() > offset && matcher.find(offset) && matcher.start() < matcher.end()) {
				int start = matcher.start();
				int end = matcher.end() - 1;
				int childIndexOfStart = data.getChildIndexAt(start);
				int childIndexOfEnd = data.getChildIndexAt(end);
				Style style = children.get(childIndexOfStart).style();
				int indexOfStartInStartChild = data.getPosWithinChild(start);
				int indexOfEndInEndChild = data.getPosWithinChild(end);
				int insertPos = indexOfStartInStartChild <= 0 ? childIndexOfStart : childIndexOfStart + 1;
				
				if (childIndexOfStart == childIndexOfEnd) {
					TextComponent textComponent = (TextComponent) children.get(childIndexOfStart);
					String content = textComponent.content();
					if (indexOfStartInStartChild == 0 && indexOfEndInEndChild == content.length() - 1) {
						children.remove(childIndexOfStart);
					} else if (indexOfStartInStartChild == 0) {
						String trailingContent = content.substring(indexOfEndInEndChild + 1);
						children.set(childIndexOfStart, textComponent.content(trailingContent));
					} else if (indexOfEndInEndChild == content.length() - 1) {
						String leadingContent = content.substring(0, indexOfStartInStartChild);
						children.set(childIndexOfStart, textComponent.content(leadingContent));
					} else {
						String leadingContent = content.substring(0, indexOfStartInStartChild);
						String trailingContent = content.substring(indexOfEndInEndChild + 1);
						children.set(childIndexOfStart, textComponent.content(leadingContent));
						children.add(insertPos, textComponent.content(trailingContent));
					}
				} else {
					for (; childIndexOfEnd > childIndexOfStart + 1; childIndexOfEnd--) {
						children.remove(childIndexOfEnd - 1);
					}
					TextComponent textComponentStart = (TextComponent) children.get(childIndexOfStart);
					TextComponent textComponentEnd = (TextComponent) children.get(childIndexOfEnd);
					String contentStart = textComponentStart.content();
					String contentEnd = textComponentEnd.content();
					if (indexOfStartInStartChild == 0 && indexOfEndInEndChild == contentEnd.length() - 1) {
						children.remove(childIndexOfEnd);
						children.remove(childIndexOfStart);
					} else if (indexOfStartInStartChild == 0) {
						String trailingContent = contentEnd.substring(indexOfEndInEndChild + 1);
						children.set(childIndexOfEnd, textComponentEnd.content(trailingContent));
						children.remove(childIndexOfStart);
					} else if (indexOfEndInEndChild == contentEnd.length() - 1) {
						String leadingContent = contentStart.substring(0, indexOfStartInStartChild);
						children.set(childIndexOfStart, textComponentStart.content(leadingContent));
						children.remove(childIndexOfEnd);
					} else {
						String leadingContent = contentStart.substring(0, indexOfStartInStartChild);
						String trailingContent = contentEnd.substring(indexOfEndInEndChild + 1);
						children.set(childIndexOfStart, textComponentStart.content(leadingContent));
						children.set(childIndexOfEnd, textComponentEnd.content(trailingContent));
					}
				}
				
				Component replace = replaceFunction.apply(getAllGroups(matcher));
				children.add(insertPos, replace.style(replace.style().merge(style, Merge.Strategy.IF_ABSENT_ON_TARGET)));
				component = ComponentCompacting.optimize(component.children(children));
				component = ComponentFlattening.flatten(component);
				children = new ArrayList<>(component.children());
				sections = getData(component);
				
				int sectionsInReplace = getData(ComponentFlattening.flatten(replace)).size();
				if (sectionsInReplace > 1) {
					offset = 0;
					if (sectionsInReplace > 2) {
						i += sectionsInReplace - 2;
					}
				} else {
					i--;
					offset = start + PlainComponentSerializer.plain().serialize(replace).length();
				}
			} else {
				offset = 0;
			}
		}
		
		component = ComponentCompacting.optimize(component.children(children));
		
		if (escaping) {
			component = replace(component, ESCAPE_PLACEHOLDER_PATTERN.replace("%s", regexOriginal), false, groups -> Component.text(groups[1]));
		}
		
		return component;
	}
	
	private static String[] getAllGroups(Matcher matcher) {
		String[] array = new String[matcher.groupCount() + 1];
		array[0] = matcher.group();
		for (int i = 1; i < array.length; i++) {
			array[i] = matcher.group(i);
		}
		return array;
	}
	
	private static List<ComponentReplacingData> getData(Component component) {
		List<ComponentReplacingData> sections = new ArrayList<>();
		String current = "";
		List<Integer> pos = new ArrayList<>();
		int i = -1;
		boolean lastIsTextComponent = false;
		for (Component child : component.children()) {
			i++;
			if (child instanceof TextComponent) {
				lastIsTextComponent = true;
				String content = ((TextComponent) child).content();
				current += content;
				for (int u = 0; u < content.length(); u++) {
					pos.add(i);
				}
			} else {
				if (lastIsTextComponent) {
					lastIsTextComponent = false;
					sections.add(new ComponentReplacingData(current, pos));
					current = "";
					pos = new ArrayList<>();
				}
			}
		}
		if (lastIsTextComponent) {
			sections.add(new ComponentReplacingData(current, pos));
		}
		return sections;
	}
}
