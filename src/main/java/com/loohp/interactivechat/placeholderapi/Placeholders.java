package com.loohp.interactivechat.placeholderapi;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.data.PlayerDataManager.PlayerData;

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
    public boolean persist() {
        return true;
    }
    
    @Override
    public String getRequiredPlugin() {
        return InteractiveChat.plugin.getName();
    }
	
	@Override
    public String onRequest(OfflinePlayer offlineplayer, String identifier) {
  
        if (identifier.equals("mentiontoggle")) {
        	PlayerData pd;
        	if (offlineplayer.isOnline()) {
        		Player player = offlineplayer.getPlayer();
        		pd = InteractiveChat.playerDataManager.getPlayerData(player);
        	} else {
        		pd = InteractiveChat.database.getPlayerInfo(offlineplayer.getUniqueId());
        	}
        	return pd == null ? "enabled" : (pd.isMentionDisabled() ? "disabled" : "enabled");
        }
        
        if (identifier.equals("invdisplaylayout")) {
        	PlayerData pd;
        	if (offlineplayer.isOnline()) {
        		Player player = offlineplayer.getPlayer();
        		pd = InteractiveChat.playerDataManager.getPlayerData(player);
        	} else {
        		pd = InteractiveChat.database.getPlayerInfo(offlineplayer.getUniqueId());
        	}
        	return (pd == null ? InteractiveChat.invDisplayLayout : pd.getInventoryDisplayLayout()) + "";
        }

        return null;
    }

}
