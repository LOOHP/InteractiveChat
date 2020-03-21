package com.loohp.interactivechat.Utils;

import java.util.ArrayList;
import java.util.List;

import com.loohp.interactivechat.InteractiveChat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class CustomStringUtils {
	
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
	        	TextComponent text = new TextComponent(noExtra.toPlainText());
 	        	if (InteractiveChat.version.contains("legacy") && !InteractiveChat.version.equals("1.12") && !InteractiveChat.version.equals("1.11")) {
 	        		text = copyFormatting(text, noExtra);
 	        	} else {
 	        		text.copyFormatting(noExtra);
 	        	}	        	
	        	list.add(text);
	        	list.addAll(loadExtras(each.getExtra())); // Calls same method again.
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
