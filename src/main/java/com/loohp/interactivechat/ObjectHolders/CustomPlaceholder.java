package com.loohp.interactivechat.ObjectHolders;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.chat.ClickEvent;

public class CustomPlaceholder extends ICPlaceholder {
	
	private int position;
	private ParsePlayer parsePlayer;
	private List<String> aliases;
	private boolean parseKeyword;
	private long cooldown;
	private CustomPlaceholderHoverEvent hover;
	private CustomPlaceholderClickEvent click;
	private CustomPlaceholderReplaceText replace;
	
	public CustomPlaceholder(int position, ParsePlayer parsePlayer, String keyword, List<String> aliases, boolean parseKeyword, boolean caseSensitive, long cooldown, CustomPlaceholderHoverEvent hover, CustomPlaceholderClickEvent click, CustomPlaceholderReplaceText replace, String description) {
		super(keyword, caseSensitive, description, true);
		this.position = position;
		this.parsePlayer = parsePlayer;
		this.aliases = aliases;
		this.parseKeyword = parseKeyword;
		this.cooldown = cooldown;
		this.hover = hover;
		this.click = click;
		this.replace = replace;
	}
	
	@Deprecated
	public int getPosition() {
		return position;
	}
	
	@Override
	public String getPermission() {
		return "interactivechat.module.custom." + position;
	}
	
	public ParsePlayer getParsePlayer() {
		return parsePlayer;
	}
	
	public List<String> getAliases() {
		return new ArrayList<String>(aliases);
	}
	
	public boolean getParseKeyword() {
		return parseKeyword;
	}
	
	public long getCooldown() {
		return cooldown;
	}
	
	public CustomPlaceholderHoverEvent getHover() {
		return hover;
	}
	
	public CustomPlaceholderClickEvent getClick() {
		return click;
	}
	
	public CustomPlaceholderReplaceText getReplace() {
		return replace;
	}
	
	public enum ParsePlayer {
		VIEWER("viewer", 0),
		SENDER("sender", 1);
		
		String name;
		int ord;
		
		ParsePlayer(String name, int ord) {
			this.name = name;
			this.ord = ord;
		}
		
		public int getOrder() {
			return ord;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public static ParsePlayer fromString(String name) {
			for (ParsePlayer parsePlayer : values()) {
				if (name.equalsIgnoreCase(parsePlayer.toString())) {
					return parsePlayer;
				}
			}
			return VIEWER;
		}
		
		public static ParsePlayer fromOrder(int order) {
			for (ParsePlayer parsePlayer : values()) {
				if (order == parsePlayer.getOrder()) {
					return parsePlayer;
				}
			}
			return VIEWER;
		}
	}
	
	public static class CustomPlaceholderHoverEvent {
		boolean enabled;
		String text;
		
		public CustomPlaceholderHoverEvent(boolean enabled, String text) {
			this.enabled = enabled;
			this.text = text;
		}
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public String getText() {
			return text;
		}
	}
	
	public static class CustomPlaceholderClickEvent {
		boolean enabled;
		ClickEvent.Action action;
		String value;
		
		public CustomPlaceholderClickEvent(boolean enabled, ClickEvent.Action action, String value) {
			this.enabled = enabled;
			this.action = action;
			this.value = value;
		}
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public ClickEvent.Action getAction() {
			return action;
		}
		
		public String getValue() {
			return value;
		}
	}
	
	public static class CustomPlaceholderReplaceText {
		boolean enabled;
		String replaceText;
		
		public CustomPlaceholderReplaceText(boolean enabled, String replaceText) {
			this.enabled = enabled;
			this.replaceText = replaceText;
		}
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public String getReplaceText() {
			return replaceText;
		}
	}

}
