package com.loohp.interactivechat.Utils;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.ObjectHolders.ICPlaceholder;

import net.md_5.bungee.api.ChatColor;

public class MessageUtils {

	public static String preprocessMessage(String message) {
		for (ICPlaceholder icplaceholder : InteractiveChat.placeholderList) {
			String placeholder = icplaceholder.getKeyword();
			if ((icplaceholder.isCaseSensitive() && ChatColor.stripColor(message).contains(placeholder)) || (!icplaceholder.isCaseSensitive() && ChatColor.stripColor(message.toLowerCase()).contains(placeholder.toLowerCase()))) {
				String regex = CustomStringUtils.escapeMetaCharacters(placeholder);
				regex = regex.replaceAll("\\\\?.", "(?:§.)?(?:$0)");
				message = message.replaceAll(regex, placeholder);
			}
		}
		for (String placeholder : InteractiveChat.aliasesMapping.keySet()) {
			if (ChatColor.stripColor(message).matches(".*" + placeholder + ".*")) {
				String regex = CustomStringUtils.escapeMetaCharacters(placeholder);
				regex = regex.replaceAll("\\\\?.", "(?:§.)?(?:$0)");
				message = message.replaceAll(regex, placeholder);
			}
			message = message.replaceAll(placeholder, InteractiveChat.aliasesMapping.get(placeholder));
		}
		return message;
	}

}
