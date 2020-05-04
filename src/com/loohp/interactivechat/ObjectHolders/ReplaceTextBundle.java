package com.loohp.interactivechat.ObjectHolders;

import org.bukkit.entity.Player;

public class ReplaceTextBundle {
	
	String placeholder;
	Player player;
	String replaceText;
	
	public ReplaceTextBundle(String placeholder, Player player, String replaceText) {
		this.placeholder = placeholder;
		this.player = player;
		this.replaceText = replaceText;
	}
	
	public String getPlaceholder() {
		return placeholder;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public String getReplaceText() {
		return replaceText;
	}

}
