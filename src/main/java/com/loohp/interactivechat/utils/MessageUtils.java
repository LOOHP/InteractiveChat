package com.loohp.interactivechat.utils;

import java.util.Collection;
import java.util.Map;

import com.loohp.interactivechat.objectholders.ICPlaceholder;

public class MessageUtils {

	public static String preprocessMessage(String message, Collection<ICPlaceholder> placeholderList, Map<String, String> aliasesMapping) {
		for (ICPlaceholder icplaceholder : placeholderList) {
			String placeholder = icplaceholder.getKeyword();
			if ((icplaceholder.isCaseSensitive() && ChatColorUtils.stripColor(message).contains(placeholder)) || (!icplaceholder.isCaseSensitive() && ChatColorUtils.stripColor(message.toLowerCase()).contains(placeholder.toLowerCase()))) {
				String regex = CustomStringUtils.escapeMetaCharacters(placeholder);
				regex = regex.replaceAll("(?<!^|^\\\\)\\\\?.", "(?:\u00a7.)?(?:$0)");
				message = message.replaceAll(regex, CustomStringUtils.escapeReplaceAllMetaCharacters(placeholder));
			}
		}
		for (String placeholder : aliasesMapping.keySet()) {
			if (ChatColorUtils.stripColor(message).matches(".*" + placeholder + ".*")) {
				String regex = CustomStringUtils.escapeMetaCharacters(placeholder);
				regex = regex.replaceAll("(?<!^|^\\\\)\\\\?.", "(?:\u00a7.)?(?:$0)");
				message = message.replaceAll(regex, CustomStringUtils.escapeReplaceAllMetaCharacters(placeholder));
			}
			message = message.replaceAll(placeholder, CustomStringUtils.escapeReplaceAllMetaCharacters(aliasesMapping.get(placeholder)));
		}
		return message;
	}

}
