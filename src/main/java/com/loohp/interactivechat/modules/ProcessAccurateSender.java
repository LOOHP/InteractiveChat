package com.loohp.interactivechat.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.objectholders.ProcessSenderResult;
import com.loohp.interactivechat.objectholders.SenderPlaceholderInfo;
import com.loohp.interactivechat.utils.CustomStringUtils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class ProcessAccurateSender {
	
	public static ProcessSenderResult process(BaseComponent basecomponent) {
		UUID sender = null;
		List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
		List<BaseComponent> newlist = new ArrayList<>();
		for (BaseComponent base : basecomponentlist) {
			if (!(base instanceof TextComponent)) {
				newlist.add(base);
			} else {
				TextComponent textcomponent = (TextComponent) base;
				String text = textcomponent.getText();
				boolean contains = false;
				for (Entry<String, SenderPlaceholderInfo> entry : InteractiveChat.senderPlaceholderMatch.entrySet()) {
					if (text.contains(entry.getKey())) {
						String newText = text.replace(entry.getKey(), "");
						textcomponent.setText(newText);
						newlist.add(textcomponent);
						sender = entry.getValue().getSender();
						contains = true;
						break;
					}
				}
				if (!contains) {
					newlist.add(textcomponent);
				}
			}
		}
		
		TextComponent product = new TextComponent("");
		for (int i = 0; i < newlist.size(); i++) {
			BaseComponent each = newlist.get(i);
			product.addExtra(each);
		}
		return new ProcessSenderResult(product, sender);
	}
	
	public static UUID find(String text) {
		UUID sender = null;
		for (Entry<String, SenderPlaceholderInfo> entry : InteractiveChat.senderPlaceholderMatch.entrySet()) {
			if (text.contains(entry.getKey())) {
				sender = entry.getValue().getSender();
				break;
			}
		}
		return sender;
	}
}
