package com.loohp.interactivechat.ObjectHolders;

public class ReplaceTextBundle implements Comparable<ReplaceTextBundle> {
	
	private String placeholder;
	private PlayerWrapper player;
	private String replaceText;
	
	public ReplaceTextBundle(String placeholder, PlayerWrapper player, String replaceText) {
		this.placeholder = placeholder;
		this.player = player;
		this.replaceText = replaceText;
	}
	
	public String getPlaceholder() {
		return placeholder;
	}
	
	public PlayerWrapper getPlayer() {
		return player;
	}
	
	public String getReplaceText() {
		return replaceText;
	}

	@Override
	public int compareTo(ReplaceTextBundle anotherReplaceTextBundle) {
		String compareText = anotherReplaceTextBundle.getPlaceholder();
		int res = String.CASE_INSENSITIVE_ORDER.compare(placeholder, compareText);
        return (res != 0) ? res : placeholder.compareTo(compareText);
	}

}
