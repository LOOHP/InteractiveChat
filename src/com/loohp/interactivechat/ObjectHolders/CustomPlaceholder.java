package com.loohp.interactivechat.ObjectHolders;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.chat.ClickEvent;

public class CustomPlaceholder extends ICPlaceholder {
	
	int position;
	ParsePlayer parsePlayer;
	List<String> aliases;
	boolean parseKeyword;
	long cooldown;
	CustomPlaceholderHoverEvent hover;
	CustomPlaceholderClickEvent click;
	CustomPlaceholderReplaceText replace;
	
	public CustomPlaceholder(int position, ParsePlayer parsePlayer, String keyword, List<String> aliases, boolean parseKeyword, boolean caseSensitive, long cooldown, CustomPlaceholderHoverEvent hover, CustomPlaceholderClickEvent click, CustomPlaceholderReplaceText replace) {
		super(keyword, caseSensitive);
		this.position = position;
		this.parsePlayer = parsePlayer;
		this.aliases = aliases;
		this.parseKeyword = parseKeyword;
		this.cooldown = cooldown;
		this.hover = hover;
		this.click = click;
		this.replace = replace;
	}
	
	public int getPosition() {
		return position;
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
		VIEWER("viewer"),
		SENDER("sender");
		
		String name;
		
		ParsePlayer(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public static ParsePlayer fromString(String name) {
			if (name.equalsIgnoreCase("sender")) {
				return SENDER;
			}
			return SENDER;
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
