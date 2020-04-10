package com.loohp.interactivechat.Utils;

import com.loohp.interactivechat.InteractiveChat;

import net.md_5.bungee.api.ChatColor;

public class MessageUtils {
	
	public static String preprocessMessage(String message) {
		for (String placeholder : InteractiveChat.placeholderList) {
			if (ChatColor.stripColor(message).contains(placeholder)) {
				String regex = escapeMetaCharacters(placeholder);
				regex = regex.replaceAll("(\\\\?.)", "(?:§.)?(?:$0)");
				message = message.replaceAll("(" + regex + ")", placeholder);
			}
		}
		return message;
	}
	
	public static String escapeMetaCharacters(String inputString){
	    final String[] metaCharacters = {"\\","^","$","{","}","[","]","(",")",".","*","+","?","|","<",">","-","&","%"};

	    for (int i = 0; i < metaCharacters.length; i++){
	        if(inputString.contains(metaCharacters[i])){
	            inputString = inputString.replace(metaCharacters[i], "\\" + metaCharacters[i]);
	        }
	    }
	    return inputString;
	}

}
