package com.loohp.interactivechat.utils;

import java.util.UUID;

import org.bukkit.Bukkit;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.loohp.interactivechat.InteractiveChat;

import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;

public class IgnoreUtils {
	
	public static boolean isIgnore(UUID source, UUID target) {
		if (InteractiveChat.ventureChatHook) {
			MineverseChatPlayer venturePlayer = MineverseChatAPI.getMineverseChatPlayer(source);
			if (venturePlayer != null && venturePlayer.getIgnores().contains(target)) {
				return true;
			}
		}
		
		if (InteractiveChat.cmiHook) {
			CMIUser user = CMI.getInstance().getPlayerManager().getUser(source);
			if (user != null && user.isIgnoring(target)) {
				return true;
			}
		}
		
		if (InteractiveChat.essentialsHook) {
			Essentials ess3 = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
			User sourceUser = ess3.getUser(source);
			User targetUser = ess3.getUser(target);
			if (sourceUser != null && targetUser != null && sourceUser.isIgnoredPlayer(targetUser)) {
				return true;
			}
		}
		
		return false;
	}

}
