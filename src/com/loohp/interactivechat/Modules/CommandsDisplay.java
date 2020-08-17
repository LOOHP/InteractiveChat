package com.loohp.interactivechat.Modules;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.Utils.ChatColorUtils;
import com.loohp.interactivechat.Utils.CustomStringUtils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CommandsDisplay {
	
	public static BaseComponent process(BaseComponent basecomponent) {		
		List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
		List<BaseComponent> newlist = new ArrayList<BaseComponent>();
		
		boolean parsingCommand = false;
		int indexOfParsingStart = -1;
		for (int i = 0; i < basecomponentlist.size(); i++) {
			BaseComponent base = basecomponentlist.get(i);
			
			if (!parsingCommand && !(base instanceof TextComponent)) {
				newlist.add(base);
			} else {
				if (base instanceof TextComponent) {
					TextComponent textComp = (TextComponent) base;
					if (parsingCommand) {
						int end = textComp.getText().indexOf(InteractiveChat.clickableCommandsSuffix);
						if (i + 1 == basecomponentlist.size() || end >= 0) {
							end = end < 0 ? textComp.getText().length() : end;
							TextComponent before = new TextComponent(textComp);
							before.setText(before.getText().substring(0, end));
							StringBuilder cmd = new StringBuilder();
							List<BaseComponent> cmdCompList = new ArrayList<BaseComponent>();
							for (int u = indexOfParsingStart; u < i; u++) {
								BaseComponent part = basecomponentlist.get(u);
								Bukkit.getConsoleSender().sendMessage(ChatColorUtils.stripColor(part.toLegacyText()));
								cmdCompList.add(part);
								cmd.append(ChatColorUtils.stripColor(part.toLegacyText()));
							}
							cmdCompList.add(before);
							cmd.append(ChatColorUtils.stripColor(before.toLegacyText()));
							ClickEvent click = new ClickEvent(InteractiveChat.clickableCommandsAction, cmd.toString());
							for (BaseComponent each : cmdCompList) {
								each.setClickEvent(click);
								newlist.add(each);
							}
							textComp.setText(ChatColorUtils.getLastColors(before.getText()) + textComp.getText().substring(end + InteractiveChat.clickableCommandsSuffix.length()));
							parsingCommand = false;
						}
					}
					if (!parsingCommand) {
						int begin = textComp.getText().indexOf(InteractiveChat.clickableCommandsPrefix);
						if (begin >= 0) {
							String remaining = ChatColorUtils.stripColor(textComp.getText().substring(begin));
							if (remaining.length() > InteractiveChat.clickableCommandsPrefix.length() && remaining.charAt(InteractiveChat.clickableCommandsPrefix.length()) == '/') {
								parsingCommand = true;
								TextComponent before = new TextComponent(textComp);
								before.setText(before.getText().substring(0, begin));
								newlist.add(before);
								textComp.setText(ChatColorUtils.getLastColors(before.getText()) + textComp.getText().substring(begin + InteractiveChat.clickableCommandsPrefix.length()));
								basecomponentlist.add(i + 1, textComp);
								indexOfParsingStart = i + 1;
							}
						}
					}
					if (!parsingCommand) {
						newlist.add(base);
					}
				} else {
					if (i + 1 == basecomponentlist.size()) {
						StringBuilder cmd = new StringBuilder();
						List<BaseComponent> cmdCompList = new ArrayList<BaseComponent>();
						for (int u = indexOfParsingStart; u < i; u++) {
							BaseComponent part = basecomponentlist.get(u);
							cmdCompList.add(part);
							cmd.append(ChatColorUtils.stripColor(part.toLegacyText()));
						}
						cmdCompList.add(base);
						cmd.append(ChatColorUtils.stripColor(base.toLegacyText()));
						ClickEvent click = new ClickEvent(InteractiveChat.clickableCommandsAction, cmd.toString());
						for (BaseComponent each : cmdCompList) {
							each.setClickEvent(click);
							newlist.add(each);
						}
						parsingCommand = false;
					}
				}
			}
		}
		
		TextComponent product = new TextComponent("");
		for (int i = 0; i < newlist.size(); i++) {
			BaseComponent each = newlist.get(i);
			product.addExtra(each);
		}
		return product;	
	}

}
