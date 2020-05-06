package com.loohp.interactivechat.Utils;

import java.util.Iterator;
import java.util.List;

import com.loohp.interactivechat.InteractiveChat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatColorFilter {
	
	public static String removeUselessColorCodes(String string) {
		return string.replaceAll("(§[0-9a-fl-or])*(?=§[0-9a-fr])", "");
	}
	
	public static String filterIllegalColorCodes(String string) {
		return string.replaceAll("§[g-j,p,q,s-z,A-Z,\\-!$%^&*()_+|~=`{}\\[\\]:\";'<>?,.\\/\\\\]", "§r");
	}
	
	public static BaseComponent filterExtraColorCodes(BaseComponent baseComponent) {
		if (baseComponent instanceof TextComponent) {
    		TextComponent text = (TextComponent) baseComponent;
    		if (text.getText().matches("^(§[0-9,a-f,l-o,r])*$")) {
    			text.setText("");
    		}
    	}
		
		return baseComponent;
	}
	
	public static List<BaseComponent> filterUselessColorCodes(List<BaseComponent> baseComponent) {
		if (InteractiveChat.FilterUselessColorCodes) {
		    Iterator<BaseComponent> itr = baseComponent.iterator();
		    while (itr.hasNext()) {
		    	BaseComponent base = itr.next();
		    	if (base instanceof TextComponent) {
		    		TextComponent text = (TextComponent) base;
		    		if (text.getText().matches("^(§[0-9,a-f,l-o,r])*$")) {
		    			itr.remove();
		    		} else {
		    			text.setText(ChatColorFilter.removeUselessColorCodes(text.getText()));
		    		}
		    	}
		    }
	    }
		
		return baseComponent;
	}

}
