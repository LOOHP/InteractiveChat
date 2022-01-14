package com.loohp.interactivechat.objectholders;

import java.util.regex.Pattern;

public class CustomPlaceholder extends ICPlaceholder {
	
	public static final String CUSTOM_PLACEHOLDER_PERMISSION = "interactivechat.module.custom.";
	
	private int position;
	private ParsePlayer parsePlayer;
	private boolean parseKeyword;
	private CustomPlaceholderHoverEvent hover;
	private CustomPlaceholderClickEvent click;
	private CustomPlaceholderReplaceText replace;
	
	public CustomPlaceholder(int position, ParsePlayer parsePlayer, Pattern keyword, boolean parseKeyword, long cooldown, CustomPlaceholderHoverEvent hover, CustomPlaceholderClickEvent click, CustomPlaceholderReplaceText replace, String name, String description) {
		super(keyword, name, description, CUSTOM_PLACEHOLDER_PERMISSION + position, cooldown);
		this.position = position;
		this.parsePlayer = parsePlayer;
		this.parseKeyword = parseKeyword;
		this.hover = hover;
		this.click = click;
		this.replace = replace;
	}
	
	@Override
	public boolean isBuildIn() {
		return false;
	}
	
	@Deprecated
	public int getPosition() {
		return position;
	}
	
	public ParsePlayer getParsePlayer() {
		return parsePlayer;
	}
	
	public boolean getParseKeyword() {
		return parseKeyword;
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (enabled ? 1231 : 1237);
			result = prime * result + ((text == null) ? 0 : text.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			CustomPlaceholderHoverEvent other = (CustomPlaceholderHoverEvent) obj;
			if (enabled != other.enabled) {
				return false;
			}
			if (text == null) {
				if (other.text != null) {
					return false;
				}
			} else if (!text.equals(other.text)) {
				return false;
			}
			return true;
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((action == null) ? 0 : action.hashCode());
			result = prime * result + (enabled ? 1231 : 1237);
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			CustomPlaceholderClickEvent other = (CustomPlaceholderClickEvent) obj;
			if (action != other.action) {
				return false;
			}
			if (enabled != other.enabled) {
				return false;
			}
			if (value == null) {
				if (other.value != null) {
					return false;
				}
			} else if (!value.equals(other.value)) {
				return false;
			}
			return true;
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (enabled ? 1231 : 1237);
			result = prime * result + ((replaceText == null) ? 0 : replaceText.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			CustomPlaceholderReplaceText other = (CustomPlaceholderReplaceText) obj;
			if (enabled != other.enabled) {
				return false;
			}
			if (replaceText == null) {
				if (other.replaceText != null) {
					return false;
				}
			} else if (!replaceText.equals(other.replaceText)) {
				return false;
			}
			return true;
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
