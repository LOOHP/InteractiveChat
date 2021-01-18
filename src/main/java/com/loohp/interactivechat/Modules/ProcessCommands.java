package com.loohp.interactivechat.Modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.ObjectHolders.CommandPlaceholderInfo;
import com.loohp.interactivechat.ObjectHolders.ProcessCommandsResult;
import com.loohp.interactivechat.Utils.CustomStringUtils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class ProcessCommands {
	
	public static ProcessCommandsResult process(BaseComponent basecomponent) {
		
		UUID sender = null;
		List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
		List<BaseComponent> newlist = new ArrayList<BaseComponent>();
		for (BaseComponent base : basecomponentlist) {
			if (!(base instanceof TextComponent)) {
				newlist.add(base);
			} else {
				TextComponent textcomponent = (TextComponent) base;
				String text = textcomponent.getText();
				boolean contains = false;
				for (Entry<String, CommandPlaceholderInfo> entry : InteractiveChat.commandPlaceholderMatch.entrySet()) {
					if (text.contains(entry.getKey())) {
						String newText = text.replace(entry.getKey(), entry.getValue().getPlaceholder());
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
		return new ProcessCommandsResult(product, sender);
	}
}
