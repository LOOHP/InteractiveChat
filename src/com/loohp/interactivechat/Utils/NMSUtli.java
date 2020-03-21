package com.loohp.interactivechat.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.NMS.V1_10_R1;
import com.loohp.interactivechat.NMS.V1_11_R1;
import com.loohp.interactivechat.NMS.V1_12_R1;
import com.loohp.interactivechat.NMS.V1_13_R1;
import com.loohp.interactivechat.NMS.V1_13_R2;
import com.loohp.interactivechat.NMS.V1_14_R1;
import com.loohp.interactivechat.NMS.V1_15_R1;
import com.loohp.interactivechat.NMS.V1_8_R1;
import com.loohp.interactivechat.NMS.V1_8_R2;
import com.loohp.interactivechat.NMS.V1_8_R3;
import com.loohp.interactivechat.NMS.V1_9_R1;
import com.loohp.interactivechat.NMS.V1_9_R2;

public class NMSUtli {
	
	public static String getNMSItemStackJson(ItemStack item) {
		String itemJson = "";
		if (InteractiveChat.version.equals("1.15")) {
	    	itemJson = V1_15_R1.convertItemStackToJson(item);
	    } else if (InteractiveChat.version.equals("1.14")) {
	    	itemJson = V1_14_R1.convertItemStackToJson(item);
	    } else if (InteractiveChat.version.equals("1.13.1")) {
	    	itemJson = V1_13_R2.convertItemStackToJson(item);
	    } else if (InteractiveChat.version.equals("1.13")) {
	    	itemJson = V1_13_R1.convertItemStackToJson(item);
	    } else if (InteractiveChat.version.equals("legacy1.12")) {
	    	itemJson = V1_12_R1.convertItemStackToJson(item);
	    } else if (InteractiveChat.version.equals("legacy1.11")) {
	    	itemJson = V1_11_R1.convertItemStackToJson(item);
	    } else if (InteractiveChat.version.equals("legacy1.10")) {
	    	itemJson = V1_10_R1.convertItemStackToJson(item);
	    } else if (InteractiveChat.version.equals("legacy1.9.4")) {
	    	itemJson = V1_9_R2.convertItemStackToJson(item);
	    } else if (InteractiveChat.version.equals("legacy1.9")) {
	    	itemJson = V1_9_R1.convertItemStackToJson(item);
	    } else if (InteractiveChat.version.equals("OLDlegacy1.8.4")) {
	    	itemJson = V1_8_R3.convertItemStackToJson(item);
	    } else if (InteractiveChat.version.equals("OLDlegacy1.8.3")) {
	    	itemJson = V1_8_R2.convertItemStackToJson(item);
	    } else if (InteractiveChat.version.equals("OLDlegacy1.8")) {
	    	itemJson = V1_8_R1.convertItemStackToJson(item);
	    } else {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Unable to get Item JSON: This NMS version is not supported!");
	    	return null;
	    }
		return itemJson;
	}

}
