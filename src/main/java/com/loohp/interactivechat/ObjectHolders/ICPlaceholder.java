package com.loohp.interactivechat.ObjectHolders;

import java.util.Optional;

public class ICPlaceholder {
	
	private final boolean isCustomPlaceholder;
	private String keyword;
	private boolean caseSensitive;
	private String description;
	private String permission;
	
	/*
	 * This constructor is used by CustomPlaceholder
	 */
	protected ICPlaceholder(String keyword, boolean caseSensitive, String description, boolean isCustomPlaceholder) {
		this.keyword = keyword;
		this.caseSensitive = caseSensitive;
		this.description = description;
		this.isCustomPlaceholder = isCustomPlaceholder;
		this.permission = null;
	}
	
	public ICPlaceholder(String keyword, boolean caseSensitive, String description, String permission) {
		this.keyword = keyword;
		this.caseSensitive = caseSensitive;
		this.description = description;
		this.isCustomPlaceholder = false;
		this.permission = permission;
	}
	
	public boolean isBuildIn() {
		return !isCustomPlaceholder;
	}
	
	public Optional<CustomPlaceholder> getCustomPlaceholder() {
		return isCustomPlaceholder ? Optional.of((CustomPlaceholder) this) : Optional.empty();
	}
	
	public String getPermission() {
		return permission;
	}
	
	public String getKeyword() {
		return keyword;
	}
	
	public boolean isCaseSensitive() {
		return caseSensitive;
	}
	
	public String getDescription() {
		return description;
	}

}
