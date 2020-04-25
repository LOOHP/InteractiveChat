package com.loohp.interactivechat.Modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.ObjectHolders.CommandPlaceholderGroup;
import com.loohp.interactivechat.ObjectHolders.ProcessCommandsReturn;
import com.loohp.interactivechat.Utils.CustomStringUtils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class ProcessCommands {
	
	public static ProcessCommandsReturn process(BaseComponent basecomponent) {
		
		Player sender = null;
		List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
		List<BaseComponent> newlist = new ArrayList<BaseComponent>();
		for (BaseComponent base : basecomponentlist) {
			TextComponent textcomponent = (TextComponent) base;
			String text = textcomponent.getText();
			boolean contains = false;
			for (Entry<String, CommandPlaceholderGroup> entry : InteractiveChat.commandPlaceholderMatch.entrySet()) {
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
		
		TextComponent product = (TextComponent) newlist.get(0);
		for (int i = 1; i < newlist.size(); i++) {
			BaseComponent each = newlist.get(i);
			product.addExtra(each);
		}
		return new ProcessCommandsReturn(product, sender);
	}
}
