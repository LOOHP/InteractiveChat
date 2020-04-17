package com.loohp.interactivechat.Utils;

import java.util.ArrayList;
import java.util.List;

import com.loohp.interactivechat.InteractiveChat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class CustomStringUtils {
	
	public static double similarity(String s1, String s2) {
		String longer = s1;
		String shorter = s2;
		if (s1.length() < s2.length()) {
		    longer = s2; shorter = s1;
		}
		int longerLength = longer.length();
		if (longerLength == 0) {
			return 1.0;
		}
		return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
	}
	
	public static int editDistance(String s1, String s2) {
	    s1 = s1.toLowerCase();
	    s2 = s2.toLowerCase();

	    int[] costs = new int[s2.length() + 1];
	    for (int i = 0; i <= s1.length(); i++) {
	        int lastValue = i;
	        for (int j = 0; j <= s2.length(); j++) {
	        if (i == 0) {
	        	costs[j] = j;
	        } else {
	            if (j > 0) {
	            	int newValue = costs[j - 1];
	            	if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
	            		newValue = Math.min(Math.min(newValue, lastValue),costs[j]) + 1;
	                }
	                costs[j - 1] = lastValue;
	                lastValue = newValue;
	            }
	        }
	    }
	    if (i > 0)
	        costs[s2.length()] = lastValue;
	    }
	    return costs[s2.length()];
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
	
	public static String[] splitStringEvery(String s, int interval) {
	    int arrayLength = (int) Math.ceil(((s.length() / (double)interval)));
	    String[] result = new String[arrayLength];

	    int j = 0;
	    int lastIndex = result.length - 1;
	    for (int i = 0; i < lastIndex; i++) {
	        result[i] = s.substring(j, j + interval);
	        j += interval;
	    } //Add the last bit
	    result[lastIndex] = s.substring(j);

	    return result;
	}
	
	public static List<BaseComponent> loadExtras(BaseComponent basecomponent) {
		List<BaseComponent> list = new ArrayList<BaseComponent>();
		list.add(basecomponent);
		return loadExtras(list);
	}
	
	public static List<BaseComponent> loadExtras(List<BaseComponent> baseComp) {
		List<BaseComponent> list = new ArrayList<BaseComponent>();
			
	    for (BaseComponent each : baseComp) {
	        if (each.getExtra() == null) {
	        	list.add(each);
	        } else if (each.getExtra().isEmpty()) {
	        	list.add(each);
	        } else {
	        	BaseComponent noExtra = each.duplicate();
	        	noExtra.getExtra().clear();
	        	TextComponent text = new TextComponent(noExtra.toLegacyText());
 	        	if (InteractiveChat.version.contains("legacy") && !InteractiveChat.version.equals("1.12") && !InteractiveChat.version.equals("1.11")) {
 	        		text = copyFormatting(text, noExtra);
 	        	} else {
 	        		text.copyFormatting(noExtra);
 	        	}	        	
	        	list.add(text);
	        	for (BaseComponent extra : loadExtras(each.getExtra())) {
	        		extra = copyFormattingNoReplace(extra, noExtra);
	        		list.add(extra);
	        	}
	        }
	    }
	    return list;
	}
	
	public static TextComponent copyFormattingEventsNoReplace(TextComponent set, BaseComponent get) {
		if (set.getClickEvent() == null) {
			set.setClickEvent(get.getClickEvent());
		}
		if (set.getHoverEvent() == null) {
			set.setHoverEvent(get.getHoverEvent());
		}
		return set;
	}
	
	public static TextComponent copyFormattingNoReplace(TextComponent set, BaseComponent get) {
		if (set.getClickEvent() == null) {
			set.setClickEvent(get.getClickEvent());
		}
		if (set.getHoverEvent() == null) {
			set.setHoverEvent(get.getHoverEvent());
		}
		if (set.isBoldRaw() == null) {
			set.setBold(get.isBoldRaw());
		}
		if (set.getColorRaw() == null) {
			set.setColor(get.getColorRaw());
		}
		if (set.isItalicRaw() == null) {
			set.setItalic(get.isItalicRaw());
		}
		if (set.isObfuscatedRaw() == null) {
			set.setObfuscated(get.isObfuscatedRaw());
		}
		if (set.isStrikethroughRaw() == null) {
			set.setStrikethrough(get.isStrikethroughRaw());
		}
		if (set.isUnderlinedRaw() == null) {
			set.setUnderlined(get.isUnderlinedRaw());
		}
		if (!InteractiveChat.version.contains("OLD")) {
			if (set.getInsertion() == null) {
				set.setInsertion(get.getInsertion());
			}
		}
		return set;
	}
	
	public static BaseComponent copyFormattingNoReplace(BaseComponent set, BaseComponent get) {
		if (set.getClickEvent() == null) {
			set.setClickEvent(get.getClickEvent());
		}
		if (set.getHoverEvent() == null) {
			set.setHoverEvent(get.getHoverEvent());
		}
		if (set.isBoldRaw() == null) {
			set.setBold(get.isBoldRaw());
		}
		if (set.getColorRaw() == null) {
			set.setColor(get.getColorRaw());
		}
		if (set.isItalicRaw() == null) {
			set.setItalic(get.isItalicRaw());
		}
		if (set.isObfuscatedRaw() == null) {
			set.setObfuscated(get.isObfuscatedRaw());
		}
		if (set.isStrikethroughRaw() == null) {
			set.setStrikethrough(get.isStrikethroughRaw());
		}
		if (set.isUnderlinedRaw() == null) {
			set.setUnderlined(get.isUnderlinedRaw());
		}
		if (!InteractiveChat.version.contains("OLD")) {
			if (set.getInsertion() == null) {
				set.setInsertion(get.getInsertion());
			}
		}
		return set;
	}
	
	public static BaseComponent copyFormattingEventsNoReplace(BaseComponent set, TextComponent get) {
		if (set.getClickEvent() == null) {
			set.setClickEvent(get.getClickEvent());
		}
		if (set.getHoverEvent() == null) {
			set.setHoverEvent(get.getHoverEvent());
		}
		return set;
	}
	
	public static TextComponent copyFormatting(TextComponent set, BaseComponent get) {
		set.setBold(get.isBold());
		set.setClickEvent(get.getClickEvent());
		set.setColor(get.getColor());
		set.setHoverEvent(get.getHoverEvent());
		if (!InteractiveChat.version.contains("OLD")) {
			set.setInsertion(get.getInsertion());
		}
		set.setItalic(get.isItalic());
		set.setObfuscated(get.isObfuscated());
		set.setStrikethrough(get.isStrikethrough());
		set.setUnderlined(get.isUnderlined());
		return set;
	}
	
	public static String insert(String bag, String marble, int index) {
	    String bagBegin = bag.substring(0,index);
	    String bagEnd = bag.substring(index);
	    return bagBegin + marble + bagEnd;
	}
}
