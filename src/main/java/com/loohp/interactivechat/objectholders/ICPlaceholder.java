package com.loohp.interactivechat.objectholders;

import java.util.Optional;

public class ICPlaceholder {
	
	private boolean isCustomPlaceholder;
	private String keyword;
	private boolean caseSensitive;
	private String description;
	private String permission;
	private long cooldown;
	
	/**
	 * This constructor is used by {@link CustomPlaceholder}
	 */
	protected ICPlaceholder(String keyword, boolean caseSensitive, String description, long cooldown) {
		this.keyword = keyword;
		this.caseSensitive = caseSensitive;
		this.description = description;
		this.cooldown = cooldown;
		this.isCustomPlaceholder = true;
		this.permission = null;
	}
	
	public ICPlaceholder(String keyword, boolean caseSensitive, String description, String permission, long cooldown) {
		this.keyword = keyword;
		this.caseSensitive = caseSensitive;
		this.description = description;
		this.isCustomPlaceholder = false;
		this.permission = permission;
		this.cooldown = cooldown;
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
	
	public long getCooldown() {
		return cooldown;
	}
	
	public String getDescription() {
		return description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());
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
		ICPlaceholder other = (ICPlaceholder) obj;
		if (keyword == null) {
			if (other.keyword != null) {
				return false;
			}
		} else if (!keyword.equals(other.keyword)) {
			return false;
		}
		return true;
	}

}
