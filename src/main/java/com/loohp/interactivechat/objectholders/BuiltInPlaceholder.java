package com.loohp.interactivechat.objectholders;

import java.util.regex.Pattern;

public class BuiltInPlaceholder extends ICPlaceholder {
	
	public BuiltInPlaceholder(Pattern keyword, String name, String description, String permission, long cooldown) {
		super(keyword, name, description, permission, cooldown);
	}
	
	@Override
	public boolean isBuildIn() {
		return true;
	}

}
