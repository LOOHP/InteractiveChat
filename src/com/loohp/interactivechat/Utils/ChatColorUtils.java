package com.loohp.interactivechat.Utils;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

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
  
}
