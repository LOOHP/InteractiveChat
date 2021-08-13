package com.loohp.interactivechat.objectholders;

public abstract class ICPlaceholder {
	
	protected String keyword;
	protected boolean caseSensitive;
	protected String description;
	protected String permission;
	protected long cooldown;
	
	public ICPlaceholder(String keyword, boolean caseSensitive, String description, String permission, long cooldown) {
		this.keyword = keyword;
		this.caseSensitive = caseSensitive;
		this.description = description;
		this.permission = permission;
		this.cooldown = cooldown;
	}
	
	public abstract boolean isBuildIn();
	
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
