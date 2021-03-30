package com.loohp.interactivechat.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.loohp.interactivechat.InteractiveChat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class CustomStringUtils {
	
	public static String replaceRespectColor(String str, String find, String replace) {
		for (int i = 0; i < str.length(); i++) {
			String after = str.substring(i);
			if (after.startsWith(find)) {
				String afterAfter = after.substring(find.length());
				String before = str.substring(0, i);
				str = before + replace + ChatColorUtils.getLastColors(before) + afterAfter;
				i += replace.length();
			}
		}
		return str;
	}
	
	public static String replaceRespectColorCaseInsensitive(String str, String find, String replace) {
		for (int i = 0; i < str.length(); i++) {
			String after = str.substring(i);
			if (after.toLowerCase().startsWith(find.toLowerCase())) {
				String afterAfter = after.substring(find.length());
				String before = str.substring(0, i);
				str = before + replace + ChatColorUtils.getLastColors(before) + afterAfter;
				i += replace.length();
			}
		}
		return str;
	}
	
	public static String replaceFromTo(String stringToReplace, int from, int to, String withString) {
		StringBuilder sb = new StringBuilder(stringToReplace);
		sb.delete(from, to);
		sb.insert(from, withString);
		return sb.toString();
	}
	
	public static List<String> getAllMatches(String regex, String str) {
		List<String> allMatches = new LinkedList<String>();
		Matcher m = Pattern.compile(regex).matcher(str);
		while (m.find()) {
		   allMatches.add(m.group());
		}
		return allMatches;
	}
	
	public static int ordinalIndexOf(String str, String substr, int n) {
	    int pos = str.indexOf(substr);
	    while (--n > 0 && pos != -1) {
	        pos = str.indexOf(substr, pos + 1);
	    }
	    return pos;
	}
	
	public static int occurrencesOfSubstring(String str, String findStr) {
		int lastIndex = 0;
		int count = 0;

		while(lastIndex != -1) {
		    lastIndex = str.indexOf(findStr,lastIndex);
		    if(lastIndex != -1) {
		        count ++;
		        lastIndex += findStr.length();
		    }
		}
		return count;
	}
	
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
	        if (inputString.contains(metaCharacters[i])) {
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
	
	public static String getIgnoreColorCodeRegex(String input) {
		return input.replaceAll("(?<!^)(?=(?<!\u00a7.).)(?=(?<!\u00a7).)(?=(?<!\\\\).)", "(\u00a7.)*?");
	}
	
	public static List<BaseComponent> loadExtras(BaseComponent basecomponent) {
		List<BaseComponent> list = new ArrayList<BaseComponent>();
		list.add(basecomponent);
		return loadExtras(list);
	}
	
	public static List<BaseComponent> loadExtras(List<BaseComponent> baseComp) {
		List<BaseComponent> list = new ArrayList<BaseComponent>();
			
		for (BaseComponent each : baseComp) {
	        if (each.getExtra() == null || each.getExtra().isEmpty()) {
	        	if (each instanceof TextComponent) {
		        	TextComponent text = new TextComponent(ChatColorUtils.addColorToEachWord(each.toLegacyText(), each.getColor() != null ? each.getColor().toString() : ""));
		        	if (InteractiveChat.version.isLegacy()) {
	 	        		text = (TextComponent) copyFormatting(text, each);
	 	        	} else {
	 	        		text.copyFormatting(each);
	 	        	}
	 	        	list.add(text);	 	        	
	        	} else {
	        		list.add(each);
	        	}
	        } else {
	        	BaseComponent noExtra = each.duplicate();
	        	noExtra.getExtra().clear();
	        	TextComponent text = each instanceof TextComponent ? new TextComponent(ChatColorUtils.addColorToEachWord(noExtra.toLegacyText(), each.getColor() != null ? each.getColor().toString() : "")) : null;
	        	if (each instanceof TextComponent || text != null) {
	        		if (InteractiveChat.version.isLegacy()) {
	 	        		text = (TextComponent) copyFormatting(text, noExtra);
	 	        	} else {
	 	        		text.copyFormatting(noExtra);
	 	        	}
	        	} else {
	        		list.add(noExtra);
	        	}
	        	for (BaseComponent extra : loadExtras(each.getExtra())) {
	        		if (InteractiveChat.version.isLegacy()) {
	        			extra = copyFormattingNoReplace(extra, noExtra);
	 	        	} else {
	 	        		extra.copyFormatting(noExtra, false);
	 	        	}	       
	        		if (extra instanceof TextComponent && text != null && ChatComponentUtils.areEventsSimilar(extra, text) && ChatComponentUtils.areFontsSimilar(extra, text)) {
	        			TextComponent extraNoExtra = (TextComponent) extra.duplicate();
	        			if (extraNoExtra.getExtra() != null) {
	        				extraNoExtra.getExtra().clear();
	        			}
	        			text.setText(text.getText() + ChatColorUtils.addColorToEachWord(extraNoExtra.toLegacyText(), ChatColorUtils.getLastColors(text.getText())));
	        			//Bukkit.getConsoleSender().sendMessage(text.getText().replace("\u00a7", "&"));
	        		} else if (!(extra instanceof TextComponent)) {
	        			if (text != null) {
	        				list.add(text);
	        			}
	        			list.add(extra);
	        			text = null;
	        		} else {
	        			if (text != null) {
	        				list.add(text);
	        			}
	        			BaseComponent extraNoExtra = extra.duplicate();
	        			if (extraNoExtra.getExtra() != null) {
	        				extraNoExtra.getExtra().clear();
	        			}
	    	        	text = new TextComponent(ChatColorUtils.addColorToEachWord(extraNoExtra.toLegacyText(), extra.getColor() != null ? extra.getColor().toString() : ""));
	    	        	if (InteractiveChat.version.isLegacy() && !InteractiveChat.version.equals(MCVersion.V1_12)) {
	     	        		text = (TextComponent) copyFormatting(text, extraNoExtra);
	     	        	} else {
	     	        		text.copyFormatting(extraNoExtra);
	     	        	}
	        		}
	        	}
	        	if (text != null) {
	        		list.add(text);
	        	}
	        }
	    }
	    
	    return list;
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
		if (!InteractiveChat.version.isOld()) {
			if (set.getInsertion() == null) {
				set.setInsertion(get.getInsertion());
			}
		}
		if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
			if (set.getFontRaw() == null) {
				set.setFont(get.getFontRaw());
			}
		}
		return set;
	}
	
	public static BaseComponent copyFormattingEventsNoReplace(BaseComponent set, BaseComponent get) {
		if (set.getClickEvent() == null) {
			set.setClickEvent(get.getClickEvent());
		}
		if (set.getHoverEvent() == null) {
			set.setHoverEvent(get.getHoverEvent());
		}
		return set;
	}
	
	public static BaseComponent copyFormatting(BaseComponent set, BaseComponent get) {
		set.setBold(get.isBold());
		set.setClickEvent(get.getClickEvent());
		set.setColor(get.getColor());
		set.setHoverEvent(get.getHoverEvent());
		if (!InteractiveChat.version.isOld()) {
			set.setInsertion(get.getInsertion());
		}
		set.setItalic(get.isItalic());
		set.setObfuscated(get.isObfuscated());
		set.setStrikethrough(get.isStrikethrough());
		set.setUnderlined(get.isUnderlined());
		if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
			set.setFont(get.getFontRaw());
		}
		return set;
	}
	
	public static String insert(String bag, String marble, int index) {
	    String bagBegin = bag.substring(0,index);
	    String bagEnd = bag.substring(index);
	    return bagBegin + marble + bagEnd;
	}
}
