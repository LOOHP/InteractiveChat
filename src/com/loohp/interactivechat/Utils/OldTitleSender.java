package com.loohp.interactivechat.Utils;

import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.NMS.V1_8_R1;
import com.loohp.interactivechat.NMS.V1_8_R2;
import com.loohp.interactivechat.NMS.V1_8_R3;

public class OldTitleSender {
	
	public static void sendTitle(Player player, String title, String subtitle, int time) {
		if (InteractiveChat.version.equals("OLDlegacy1.8")) {
			V1_8_R1.sendTitle(player, title, subtitle, time);
		} else if (InteractiveChat.version.equals("OLDlegacy1.8.3")) {
			V1_8_R2.sendTitle(player, title, subtitle, time);
		} else if (InteractiveChat.version.equals("OLDlegacy1.8.4")) {
			V1_8_R3.sendTitle(player, title, subtitle, time);
		}
	}

}
