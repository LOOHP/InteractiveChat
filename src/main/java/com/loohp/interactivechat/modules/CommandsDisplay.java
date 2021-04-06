package com.loohp.interactivechat.modules;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.CustomStringUtils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CommandsDisplay {
	
	@SuppressWarnings("deprecation")
	public static BaseComponent process(BaseComponent basecomponent) {		
		List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
		List<BaseComponent> newlist = new ArrayList<>();
		
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
							List<BaseComponent> cmdCompList = new ArrayList<>();
							
							String[] formmat = InteractiveChat.clickableCommandsFormat.split("\\{Command\\}");
							String prepend = formmat[0];
							String color = ChatColorUtils.getLastColors(prepend);
							String append = formmat[formmat.length - 1];
							
							for (int u = indexOfParsingStart; u < i; u++) {
								BaseComponent part = basecomponentlist.get(u);
								Bukkit.getConsoleSender().sendMessage(ChatColorUtils.stripColor(part.toLegacyText()));
								if (InteractiveChat.clickableCommandsEnforceColors) {
									if (part instanceof TextComponent) {
										((TextComponent) part).setText(color + ChatColorUtils.stripColor(((TextComponent) part).getText()));
									} else {
										part = ChatColorUtils.applyColor(part, color);
									}
								}
								cmdCompList.add(part);
								cmd.append(ChatColorUtils.stripColor(part.toLegacyText()));
							}
							if (InteractiveChat.clickableCommandsEnforceColors) {
								before.setText(color + ChatColorUtils.stripColor(before.getText()));
							}
							cmdCompList.add(before);
							//Bukkit.getConsoleSender().sendMessage(((TextComponent) before).getText().replace("\u00a7", "&"));
							cmd.append(ChatColorUtils.stripColor(before.toLegacyText()));
							
							HoverEvent hover = null;
							if (InteractiveChat.clickableCommandsHoverText != null) {
								hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent(InteractiveChat.clickableCommandsHoverText)});
							}
							ClickEvent click = new ClickEvent(InteractiveChat.clickableCommandsAction, cmd.toString());
							cmdCompList.add(0, new TextComponent(prepend));
							cmdCompList.add(new TextComponent(append));
							
							for (BaseComponent each : cmdCompList) {
								if (hover != null) {
									each.setHoverEvent(hover);
								}
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
							if (begin == 0 || textComp.getText().charAt(begin - 1) != '\\' || (begin > 1 && textComp.getText().charAt(begin - 1) == '\\' && textComp.getText().charAt(begin - 2) == '\\')) {
								String remaining = ChatColorUtils.stripColor(textComp.getText().substring(begin));
								if (remaining.length() > InteractiveChat.clickableCommandsPrefix.length() && remaining.charAt(InteractiveChat.clickableCommandsPrefix.length()) == '/') {
									parsingCommand = true;
									TextComponent before = new TextComponent(textComp);
									before.setText(before.getText().substring(0, begin));
									if (before.getText().endsWith("\\")) {
										before.setText(before.getText().substring(0, before.getText().length() - 1));
									}
									newlist.add(before);
									textComp.setText(ChatColorUtils.getLastColors(before.getText()) + textComp.getText().substring(begin + InteractiveChat.clickableCommandsPrefix.length()));
									basecomponentlist.add(i + 1, textComp);
									indexOfParsingStart = i + 1;
								}
							}
						}
					}
					if (!parsingCommand) {
						newlist.add(base);
					}
				} else {
					if (i + 1 == basecomponentlist.size()) {
						StringBuilder cmd = new StringBuilder();
						List<BaseComponent> cmdCompList = new ArrayList<>();
						String[] formmat = InteractiveChat.clickableCommandsFormat.split("\\{Command\\}");
						String prepend = formmat[0];
						String color = ChatColorUtils.getLastColors(prepend);
						String append = formmat[formmat.length - 1];
						
						for (int u = indexOfParsingStart; u <= i; u++) {
							BaseComponent part = basecomponentlist.get(u);
							Bukkit.getConsoleSender().sendMessage(ChatColorUtils.stripColor(part.toLegacyText()));
							if (InteractiveChat.clickableCommandsEnforceColors) {
								if (part instanceof TextComponent) {
									((TextComponent) part).setText(color + ChatColorUtils.stripColor(((TextComponent) part).getText()));
								} else {
									part = ChatColorUtils.applyColor(part, color);
								}
							}
							cmdCompList.add(part);
							cmd.append(ChatColorUtils.stripColor(part.toLegacyText()));
						}
						
						HoverEvent hover = null;
						if (InteractiveChat.clickableCommandsHoverText != null) {
							hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent(InteractiveChat.clickableCommandsHoverText)});
						}
						ClickEvent click = new ClickEvent(InteractiveChat.clickableCommandsAction, cmd.toString());
						cmdCompList.add(0, new TextComponent(prepend));
						cmdCompList.add(new TextComponent(append));
						
						for (BaseComponent each : cmdCompList) {
							if (hover != null) {
								each.setHoverEvent(hover);
							}
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
