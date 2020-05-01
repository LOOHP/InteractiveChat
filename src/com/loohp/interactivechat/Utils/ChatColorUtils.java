package com.loohp.interactivechat.Utils;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public class ChatColorUtils {
	
    public static String getLastColors(String input) {
        String result = "";
        
        for (int i = input.length() - 1; i > 0; i--) {
        	if (input.charAt(i - 1) == '§') {
        		String color = String.valueOf(input.charAt(i - 1)) + String.valueOf(input.charAt(i));
        		if (isLegal(color)) {
        			result = color + result;
        			if (isColor(ChatColor.getByChar(input.charAt(i))) || ChatColor.getByChar(input.charAt(i)).equals(ChatColor.RESET)) {
        				break;
        			}
        		}
        	}
        }

        return result;
    }
    
    public static boolean isColor(ChatColor color) {
    	List<ChatColor> format = new ArrayList<ChatColor>();
    	format.add(ChatColor.MAGIC);
    	format.add(ChatColor.BOLD);
    	format.add(ChatColor.ITALIC);
    	format.add(ChatColor.UNDERLINE);
    	format.add(ChatColor.STRIKETHROUGH);
    	if (format.contains(color) || color.equals(ChatColor.RESET)) {
    		return false;
    	}
    	return true;
    }
    
    public static boolean isLegal(String color) {
    	if (color.matches("§[g-j,p,q,s-z,A-Z,\\-!$%^&*()_+|~=`{}\\[\\]:\\\";'<>?,.\\/\\\\]")) {
    		return false;
    	}
    	return true;
    }
    
    public static BaseComponent applyColor(BaseComponent basecomponent, String color) {
    	if (color.length() >= 2 && color.charAt(1) != 'r') {
	    	if (color.length() == 2) {
	    		basecomponent.setColor(ChatColor.getByChar(color.charAt(1)));
	    	} else if (color.length() == 4) {
	    		basecomponent.setColor(ChatColor.getByChar(color.charAt(1)));
	    		switch (ChatColor.getByChar(color.charAt(3))) {
				case BOLD:
					basecomponent.setBold(true);
					break;
				case ITALIC:
					basecomponent.setItalic(true);
					break;
				case MAGIC:
					basecomponent.setObfuscated(true);
					break;
				case STRIKETHROUGH:
					basecomponent.setStrikethrough(true);
					break;
				case UNDERLINE:
					basecomponent.setUnderlined(true);
					break;
				default:
					break;
	    		}
	    	}
    	}
    	return basecomponent;
    }
  
}
