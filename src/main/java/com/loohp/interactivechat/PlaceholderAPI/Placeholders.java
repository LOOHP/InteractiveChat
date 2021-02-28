package com.loohp.interactivechat.PlaceholderAPI;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class Placeholders extends PlaceholderExpansion {

	@Override
	public String getAuthor() {
		return String.join(", ", InteractiveChat.plugin.getDescription().getAuthors());
	}

	@Override
	public String getIdentifier() {
		return "interactivechat";
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}
	
	@Override
    public String onRequest(OfflinePlayer offlineplayer, String identifier) {
  
        if (identifier.equals("mentiontoggle")) {
        	if (offlineplayer.isOnline()) {
        		Player player = offlineplayer.getPlayer();
        		boolean toggle = InteractiveChat.playerDataManager.getPlayerData(player).isMentionDisabled();
        		return toggle ? "disabled" : "enabled";
        	} else {
        		boolean toggle = InteractiveChat.playerDataManager.getOfflineUnmodifiablePlayerData(offlineplayer.getUniqueId()).isMentionDisabled();
        		return toggle ? "disabled" : "enabled";
        	}
        }

        return null;
    }

}
