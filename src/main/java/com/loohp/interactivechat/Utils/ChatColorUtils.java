package com.loohp.interactivechat.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.loohp.interactivechat.InteractiveChat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public class ChatColorUtils {
	
	private static Set<Character> colors = new HashSet<Character>();
	private static Pattern colorFormating = Pattern.compile("(?=(?<!\\\\)|(?<=\\\\\\\\))\\[[^\\]]*?color=#[0-9a-fA-F]{6}[^\\[]*?\\]");
	private static Pattern colorEscape = Pattern.compile("\\\\\\[ *?color=#[0-9a-fA-F]{6} *?\\]");
	
	private static String validColorHex = "^#[0-9a-fA-F]{6}$";
	
	static {
		colors.add('0');
		colors.add('1');
		colors.add('2');
		colors.add('3');
		colors.add('4');
		colors.add('5');
		colors.add('6');
		colors.add('7');
		colors.add('8');
		colors.add('9');
		colors.add('a');
		colors.add('b');
		colors.add('c');
		colors.add('d');
		colors.add('e');
		colors.add('f');
		colors.add('k');
		colors.add('l');
		colors.add('m');
		colors.add('n');
		colors.add('o');
		colors.add('r');
	}
	
	public static String stripColor(String string) {
		return string.replaceAll("§[0-9A-Fa-fk-orx]", "");
	}
	
	public static String filterIllegalColorCodes(String string) {
		return InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16) ? string.replaceAll("§[^0-9A-Fa-fk-orx]", "") : string.replaceAll("§[^0-9a-fk-or]", "");
	}
	
    public static String getLastColors(String input) {
        String result = "";
        
        for (int i = input.length() - 1; i > 0; i--) {
        	if (input.charAt(i - 1) == '§') {
        		String color = String.valueOf(input.charAt(i - 1)) + String.valueOf(input.charAt(i));
        		if ((i - 13) >= 0 && input.charAt(i - 12) == 'x' && input.charAt(i - 13) == '§') {
            		color = input.substring(i - 13, i + 1);
            		i -= 13;
            	}
        		if (isLegal(color)) {
        			result = color + result;
        			if (color.charAt(1) == 'x' || isColor(ChatColor.getByChar(input.charAt(i))) || ChatColor.getByChar(input.charAt(i)).equals(ChatColor.RESET)) {
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
        	if (input.charAt(i - 1) == '§' && input.charAt(i) == 'x' && input.length() > i + 13) {
        		color = input.substring(i - 1, i + 13);
        		i += 13;
        	}
        	if (isLegal(color)) {
	        	if (!found) {
	        		found = true;
	        		result = color;
	        	} else if (color.charAt(1) == 'x' || isColor(ChatColor.getByChar(color.charAt(1)))) {
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
    	if (color.matches("§[0-9a-fk-or]")) {
    		return true;
    	}
    	if (color.matches("§x(§[0-9a-fA-F]){6}")) {
    		return true;
    	}
    	return false;
    }
    
    public static BaseComponent applyColor(BaseComponent basecomponent, String color) {
    	if (color.length() >= 2 && color.charAt(1) != 'r') {
	    	if (color.length() == 2) {
	    		basecomponent.setColor(ChatColor.getByChar(color.charAt(1)));
	    	} else {
	    		if (color.charAt(1) == 'x') {
	    			String hex = "#" + String.valueOf(color.charAt(3)) + String.valueOf(color.charAt(5)) + String.valueOf(color.charAt(7)) + String.valueOf(color.charAt(9)) + String.valueOf(color.charAt(11)) + String.valueOf(color.charAt(13));
	    			basecomponent.setColor(ChatColor.of(hex));
	    		} else {
	    			basecomponent.setColor(ChatColor.getByChar(color.charAt(1)));
	    		}
	    		for (int i = 3; i < color.length(); i = i + 2) {
	    			char cha = color.charAt(i);
	    			if (cha == 'k' || cha == 'l' || cha == 'm' || cha == 'n' || cha == 'o') { 
		    			if (ChatColor.getByChar(cha).equals(ChatColor.BOLD)) {
							basecomponent.setBold(true);
			    		} else if (ChatColor.getByChar(cha).equals(ChatColor.ITALIC)) {
							basecomponent.setItalic(true);
			    		} else if (ChatColor.getByChar(cha).equals(ChatColor.MAGIC)) {
							basecomponent.setObfuscated(true);
			    		} else if (ChatColor.getByChar(cha).equals(ChatColor.STRIKETHROUGH)) {
							basecomponent.setStrikethrough(true);
			    		} else if (ChatColor.getByChar(cha).equals(ChatColor.UNDERLINE)) {
							basecomponent.setUnderlined(true);
						}
	    			}
	    		}
	    	}
    	}
    	return basecomponent;
    }
    
    public static String addColorToEachWord(String text, String leadingColor) {
    	StringBuilder sb = new StringBuilder();
    	text = leadingColor + text;
    	do {
    		int pos = text.indexOf(" ") + 1;
    		pos = pos <= 0 ? text.length() : pos;
    		String before = leadingColor + text.substring(0, pos);
    		//Bukkit.getConsoleSender().sendMessage(leadingColor.replace("§", "&") + " " + text.replace("§", "&") + " " + before.replace("§", "&"));
    		sb.append(before);
    		text = text.substring(pos);
    		leadingColor = getLastColors(before);
    	} while (text.length() > 0 && !text.equals(leadingColor));
    	return sb.toString();
    }
    
    public static String hexToColorCode(String hex) {
    	if (hex == null) {
    		return hex;
    	}
    	
    	int pos = hex.indexOf("#");
    	if (!hex.matches(validColorHex) || pos < 0 || hex.length() < (pos + 7)) {
    		return "§x§F§F§F§F§F§F";
    	}
    	return "§x§" + String.valueOf(hex.charAt(1)) + "§" + String.valueOf(hex.charAt(2)) + "§" + String.valueOf(hex.charAt(3)) + "§" + String.valueOf(hex.charAt(4)) + "§" + String.valueOf(hex.charAt(5)) + "§" + String.valueOf(hex.charAt(6));
    }
    
    public static String translatePluginColorFormatting(String text) {
    	while (true) {
    		Matcher matcher = colorFormating.matcher(text);
    		
    		if (matcher.find()) {
	    	    String foramtedColor = matcher.group().toLowerCase();
	    	    int start = matcher.start();
	    	    int pos = foramtedColor.indexOf("color");
	    	    int absPos = text.indexOf("color", start);
	    	    int end = matcher.end();
	    	    
	    	    if (pos < 0) {
	    	    	continue;
	    	    }
	
	    	    String colorCode = hexToColorCode(foramtedColor.substring(pos + 6, pos + 13));
	    	    
	    	    StringBuilder sb = new StringBuilder(text);
	    	    sb.insert(end, colorCode);
	    	    
	    	    sb.delete(absPos, absPos + 13);

	    	    while (sb.charAt(absPos) == ',' || sb.charAt(absPos) == ' ') {
	    	    	sb.deleteCharAt(absPos);
	    	    }
	    	    
	    	    while (sb.charAt(absPos - 1) == ',' || sb.charAt(absPos - 1) == ' ') {
	    	    	sb.deleteCharAt(absPos - 1);
	    	    	absPos--;
	    	    }
	    	    
	    	    if (sb.charAt(absPos) == ']' && sb.charAt(absPos - 1) == '[') {
	    	    	sb.deleteCharAt(absPos - 1);
	    	    	sb.deleteCharAt(absPos - 1);
	    	    	
	    	    	if (absPos > 2 && sb.charAt(absPos - 2) == '\\' && sb.charAt(absPos - 3) == '\\') {
		    	    	sb.deleteCharAt(absPos - 2);
		    	    }
	    	    }	    	  
	    	    
	    	    text = sb.toString();	    	    
    		} else {
    			break;
    		}
    	}
    	
    	while (true) {
    		Matcher matcher = colorEscape.matcher(text);  		
    		if (matcher.find()) {
	    	    StringBuilder sb = new StringBuilder(text);
	    	    sb.deleteCharAt(matcher.start());
	    	    text = sb.toString();	    	    
    		} else {
    			break;
    		}
    	}

    	return text;
    }
    
    public static String translateAlternateColorCodes(char code, String text) {    	
		if (text == null) {
			return text;
		}
		
		if (text.length() < 2) {
        	return text;
        }
		
		if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
    		text = translatePluginColorFormatting(text);
    	}
        
        for (int i = 0; i < text.length() - 1; i++) {
        	if (text.charAt(i) == code) {
        		if (text.charAt(i + 1) == 'x' && text.length() > (i + 14)) {
        			String section = text.substring(i, i + 14);
        			String translated = section.replace(code, '§');
        			text = text.replace(section, translated);
        		} else if (colors.contains(text.charAt(i + 1))) {
        			StringBuilder sb = new StringBuilder(text);
        			sb.setCharAt(i, '§');
        			text = sb.toString();
        		}
        	}
        }

        return text;
    }
  
}
