package com.loohp.interactivechat.objectholders;

public class BuiltInPlaceholder extends ICPlaceholder {
	
	public BuiltInPlaceholder(String keyword, boolean caseSensitive, String description, String permission, long cooldown) {
		super(keyword, caseSensitive, description, permission, cooldown);
	}
	
	public boolean isBuildIn() {
		return true;
	}

}
