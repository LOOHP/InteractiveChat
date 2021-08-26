package com.loohp.interactivechat.objectholders;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public abstract class ICPlaceholder {
	
	protected String keyword;
	protected boolean caseSensitive;
	protected String description;
	protected String permission;
	protected long cooldown;
	protected UUID internalId;
	
	public ICPlaceholder(String keyword, boolean caseSensitive, String description, String permission, long cooldown) {
		this.keyword = keyword;
		this.caseSensitive = caseSensitive;
		this.description = description;
		this.permission = permission;
		this.cooldown = cooldown;
		this.internalId = UUID.nameUUIDFromBytes(keyword.toLowerCase().getBytes(StandardCharsets.UTF_8));
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
	
	public UUID getInternalId() {
		return internalId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((internalId == null) ? 0 : internalId.hashCode());
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
		if (internalId == null) {
			if (other.internalId != null) {
				return false;
			}
		} else if (!internalId.equals(other.internalId)) {
			return false;
		}
		return true;
	}

}
