package com.loohp.interactivechat.Utils;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.ObjectHolders.ICPlaceholder;

public class MessageUtils {

	public static String preprocessMessage(String message) {
		for (ICPlaceholder icplaceholder : InteractiveChat.placeholderList) {
			String placeholder = icplaceholder.getKeyword();
			if ((icplaceholder.isCaseSensitive() && ChatColorUtils.stripColor(message).contains(placeholder)) || (!icplaceholder.isCaseSensitive() && ChatColorUtils.stripColor(message.toLowerCase()).contains(placeholder.toLowerCase()))) {
				String regex = CustomStringUtils.escapeMetaCharacters(placeholder);
				regex = regex.replaceAll("\\\\?.", "(?:§.)?(?:$0)");
				message = message.replaceAll(regex, placeholder);
			}
		}
		for (String placeholder : InteractiveChat.aliasesMapping.keySet()) {
			if (ChatColorUtils.stripColor(message).matches(".*" + placeholder + ".*")) {
				String regex = CustomStringUtils.escapeMetaCharacters(placeholder);
				regex = regex.replaceAll("\\\\?.", "(?:§.)?(?:$0)");
				message = message.replaceAll(regex, placeholder);
			}
			message = message.replaceAll(placeholder, InteractiveChat.aliasesMapping.get(placeholder));
		}
		return message;
	}

}
