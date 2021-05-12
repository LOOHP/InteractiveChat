package com.loohp.interactivechat.objectholders;

import java.util.ArrayList;
import java.util.List;

public class CustomPlaceholder extends ICPlaceholder {
	
	private static final String CUSTOM_PLACEHOLDER_PERMISSION = "interactivechat.module.custom.";
	
	private int position;
	private ParsePlayer parsePlayer;
	private List<String> aliases;
	private boolean parseKeyword;
	private long cooldown;
	private CustomPlaceholderHoverEvent hover;
	private CustomPlaceholderClickEvent click;
	private CustomPlaceholderReplaceText replace;
	
	public CustomPlaceholder(int position, ParsePlayer parsePlayer, String keyword, List<String> aliases, boolean parseKeyword, boolean caseSensitive, long cooldown, CustomPlaceholderHoverEvent hover, CustomPlaceholderClickEvent click, CustomPlaceholderReplaceText replace, String description) {
		super(keyword, caseSensitive, description);
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
		return CUSTOM_PLACEHOLDER_PERMISSION + position;
	}
	
	public ParsePlayer getParsePlayer() {
		return parsePlayer;
	}
	
	public List<String> getAliases() {
		return new ArrayList<>(aliases);
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
	
	public static enum ParsePlayer {
		
		VIEWER("viewer", 0),
		SENDER("sender", 1);
		
		private static final ParsePlayer[] VALUES = values();
		
		private String name;
		private int ord;
		
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
			for (ParsePlayer parsePlayer : VALUES) {
				if (name.equalsIgnoreCase(parsePlayer.toString())) {
					return parsePlayer;
				}
			}
			return VIEWER;
		}
		
		public static ParsePlayer fromOrder(int order) {
			for (ParsePlayer parsePlayer : VALUES) {
				if (order == parsePlayer.getOrder()) {
					return parsePlayer;
				}
			}
			return VIEWER;
		}
	}
	
	public static class CustomPlaceholderHoverEvent {
		
		private boolean enabled;
		private String text;
		
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
		
		private boolean enabled;
		private ClickEventAction action;
		private String value;
		
		public CustomPlaceholderClickEvent(boolean enabled, ClickEventAction action, String value) {
			this.enabled = enabled;
			this.action = action;
			this.value = value;
		}
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public ClickEventAction getAction() {
			return action;
		}
		
		public String getValue() {
			return value;
		}
	}
	
	public static class CustomPlaceholderReplaceText {
		
		private boolean enabled;
		private String replaceText;
		
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
	
	public static enum ClickEventAction {
        /**
         * Open a url at the path given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#value}.
         */
        OPEN_URL,
        /**
         * Open a file at the path given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#value}.
         */
        OPEN_FILE,
        /**
         * Run the command given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#value}.
         */
        RUN_COMMAND,
        /**
         * Inserts the string given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#value} into the player's
         * text box.
         */
        SUGGEST_COMMAND,
        /**
         * Change to the page number given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#value} in a book.
         */
        CHANGE_PAGE,
        /**
         * Copy the string given by
         * {@link net.md_5.bungee.api.chat.ClickEvent#value} into the player's
         * clipboard.
         */
        COPY_TO_CLIPBOARD;
	}

}
