package com.loohp.interactivechat.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.loohp.interactivechat.InteractiveChat;

import de.myzelyam.api.vanish.VanishAPI;

public class VanishUtils {
	
	public static boolean isVanished(Player player) {
		if (InteractiveChat.vanishHook) {
			if (VanishAPI.isInvisible(player)) {
				return true;
			}
		}
		if (InteractiveChat.cmiHook) {
			CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
			if (user.isVanished()) {
				return true;
			}
		}
		if (InteractiveChat.essentialsHook) {
			Essentials ess3 = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
			User user = ess3.getUser(player);
			if (user.isVanished()) {
				return true;
			}
		}
		return false;
	}

}
