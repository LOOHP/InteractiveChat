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
    
    public static String getFirstColors(String input) {
        String result = "";
        boolean found = false;
        
        if (input.length() < 2) {
        	return "";
        }
        
        int i = 1;
        String color = "";
        while (i < input.length()) {
        	color = String.valueOf(input.charAt(i - 1)) + String.valueOf(input.charAt(i));
        	//Bukkit.getConsoleSender().sendMessage(color.replace("§", "&"));
        	if (isLegal(color)) {
	        	if (!found) {
	        		found = true;
	        		result = color;
	        	} else if (isColor(ChatColor.getByChar(color.charAt(1)))) {
	        		result = color;
	        	} else {
	        		result = result + color;
	        	}
	        	i++;
        	} else if (found) {
        		break;
        	}
        	i++;
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
    	if (color.charAt(0) != '§') {
    		return false;
    	}
    	if (color.matches("§[g-j,p,q,s-z,A-Z,\\-!$%^&*()_+|~=`{}\\[\\]:\\\";'<>?,.\\/\\\\]")) {
    		return false;
    	}
    	return true;
    }
    
    public static BaseComponent applyColor(BaseComponent basecomponent, String color) {
    	if (color.length() >= 2 && color.charAt(1) != 'r') {
	    	if (color.length() == 2) {
	    		basecomponent.setColor(ChatColor.getByChar(color.charAt(1)));
	    	} else {
	    		basecomponent.setColor(ChatColor.getByChar(color.charAt(1)));
	    		for (int i = 3; i < color.length(); i = i + 2) {
		    		switch (ChatColor.getByChar(color.charAt(i))) {
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
    	}
    	return basecomponent;
    }
    
    public static String addColorToEachWord(String text, String color) {
    	StringBuilder sb = new StringBuilder();
    	text = color + text;
    	do {
    		color = getFirstColors(text);
    		int pos = text.indexOf(" ") + 1;
    		pos = pos <= 0 ? text.length() : pos;
    		String before = text.substring(0, pos);
    		//Bukkit.getConsoleSender().sendMessage(color.replace("§", "&") + " " + text.replace("§", "&") + " " + before.replace("§", "&"));
    		sb.append(before);
    		text = color + text.substring(pos);
    	} while (text.length() > 0 && !text.equals(color));
    	return ChatColorFilter.removeUselessColorCodes(sb.toString());
    }
  
}
