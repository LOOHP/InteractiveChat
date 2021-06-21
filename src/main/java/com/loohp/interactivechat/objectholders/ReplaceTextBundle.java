package com.loohp.interactivechat.objectholders;

public class ReplaceTextBundle implements Comparable<ReplaceTextBundle> {
	
	private String placeholder;
	private ICPlayer player;
	private String replaceText;
	
	public ReplaceTextBundle(String placeholder, ICPlayer player, String replaceText) {
		this.placeholder = placeholder;
		this.player = player;
		this.replaceText = replaceText;
	}
	
	public String getPlaceholder() {
		return placeholder;
	}
	
	public ICPlayer getPlayer() {
		return player;
	}
	
	public String getReplaceText() {
		return replaceText;
	}

	@Override
	public int compareTo(ReplaceTextBundle anotherReplaceTextBundle) {
		int compare = Integer.valueOf(placeholder.length()).compareTo(anotherReplaceTextBundle.placeholder.length());
		if (compare != 0) {
			return compare;
		} else {
			return Integer.valueOf(replaceText.length()).compareTo(anotherReplaceTextBundle.replaceText.length());
		}
	}

}
