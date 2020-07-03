package com.loohp.interactivechat.ObjectHolders;

import java.util.Optional;

public class ICPlaceholder {
	
	String keyword;
	boolean caseSensitive; 
	
	public ICPlaceholder(String keyword, boolean caseSensitive) {
		this.keyword = keyword;
		this.caseSensitive = caseSensitive;
	}
	
	public boolean isBuildIn() {
		return !(this instanceof CustomPlaceholder);
	}
	
	public Optional<CustomPlaceholder> getCustomPlaceholder() {
		return isBuildIn() ? Optional.empty() : Optional.of((CustomPlaceholder) this);
	}
	
	public String getKeyword() {
		return keyword;
	}
	
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

}
