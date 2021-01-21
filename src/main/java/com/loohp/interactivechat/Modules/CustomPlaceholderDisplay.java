package com.loohp.interactivechat.Modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.ParsePlayer;
import com.loohp.interactivechat.ObjectHolders.ICPlaceholder;
import com.loohp.interactivechat.ObjectHolders.PlayerWrapper;
import com.loohp.interactivechat.ObjectHolders.WebData;
import com.loohp.interactivechat.Utils.ChatColorUtils;
import com.loohp.interactivechat.Utils.CustomStringUtils;
import com.loohp.interactivechat.Utils.PlaceholderParser;
import com.loohp.interactivechat.Utils.PlayerUtils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CustomPlaceholderDisplay {
	
	private static Map<UUID, Map<String, Long>> placeholderCooldowns = InteractiveChat.placeholderCooldowns;
	private static Map<UUID, Long> universalCooldowns = InteractiveChat.universalCooldowns;
	
	public static BaseComponent process(BaseComponent basecomponent, Optional<PlayerWrapper> optplayer, Player reciever, String messageKey, List<ICPlaceholder> placeholderList, long unix) {
		for (int i = 0; i < placeholderList.size(); i++) {
			
			ICPlaceholder icplaceholder = placeholderList.get(i);
			if (icplaceholder.isBuildIn()) {
				continue;
			}
			CustomPlaceholder cp = icplaceholder.getCustomPlaceholder().get();
			
			PlayerWrapper parseplayer = (cp.getParsePlayer().equals(ParsePlayer.SENDER) && optplayer.isPresent()) ? optplayer.get() : new PlayerWrapper(reciever);
			boolean casesensitive = cp.isCaseSensitive();
			
			if (InteractiveChat.useCustomPlaceholderPermissions && optplayer.isPresent()) {
				PlayerWrapper sender = optplayer.get();
				if (!PlayerUtils.hasPermission(sender.getUniqueId(), "interactivechat.module.custom." + cp.getPosition(), true, 5)) {
					continue;
				}
			}
			
			String placeholder = cp.getKeyword();
			placeholder = (cp.getParseKeyword()) ? PlaceholderParser.parse(parseplayer, placeholder) : placeholder;
			long cooldown = cp.getCooldown();
			boolean hoverEnabled = cp.getHover().isEnabled();
			String hoverText = cp.getHover().getText();
			boolean clickEnabled = cp.getClick().isEnabled();
			Action clickAction = cp.getClick().getAction();
			String clickValue = cp.getClick().getValue();
			boolean replaceEnabled = cp.getReplace().isEnabled();
			String replaceText = cp.getReplace().getReplaceText();
			
			basecomponent = processCustomPlaceholder(parseplayer, casesensitive, placeholder, cooldown, hoverEnabled, hoverText, clickEnabled, clickAction, clickValue, replaceEnabled, replaceText, basecomponent, optplayer, messageKey, unix);
		}
		
		if (InteractiveChat.t) {
			for (CustomPlaceholder cp : WebData.getInstance().getSpecialPlaceholders()) {
				PlayerWrapper parseplayer = (cp.getParsePlayer().equals(ParsePlayer.SENDER) && optplayer.isPresent()) ? optplayer.get() : new PlayerWrapper(reciever);
				boolean casesensitive = cp.isCaseSensitive();			
				String placeholder = cp.getKeyword();
				placeholder = (cp.getParseKeyword()) ? PlaceholderParser.parse(parseplayer, placeholder) : placeholder;
				long cooldown = cp.getCooldown();
				boolean hoverEnabled = cp.getHover().isEnabled();
				String hoverText = cp.getHover().getText();
				boolean clickEnabled = cp.getClick().isEnabled();
				Action clickAction = cp.getClick().getAction();
				String clickValue = cp.getClick().getValue();
				boolean replaceEnabled = cp.getReplace().isEnabled();
				String replaceText = cp.getReplace().getReplaceText();
				
				basecomponent = processCustomPlaceholder(parseplayer, casesensitive, placeholder, cooldown, hoverEnabled, hoverText, clickEnabled, clickAction, clickValue, replaceEnabled, replaceText, basecomponent, optplayer, messageKey, unix);
			}
		}
			
		return basecomponent;
	}
	
	@SuppressWarnings("deprecation")
	public static BaseComponent processCustomPlaceholder(PlayerWrapper parseplayer, boolean casesensitive, String placeholder, long cooldown, boolean hoverEnabled, String hoverText, boolean clickEnabled, Action clickAction, String clickValue, boolean replaceEnabled, String replaceText, BaseComponent basecomponent, Optional<PlayerWrapper> optplayer, String messageKey, long unix) {
		boolean contain = (casesensitive) ? (basecomponent.toPlainText().contains(placeholder)) : (basecomponent.toPlainText().toLowerCase().contains(placeholder.toLowerCase()));
		if (!InteractiveChat.cooldownbypass.get(unix).contains(placeholder) && contain) {
			if (optplayer.isPresent()) {
				PlayerWrapper player = optplayer.get();
				Long uc = universalCooldowns.get(player.getUniqueId());
				if (uc != null) {
					if (uc > unix) {
						return basecomponent;
					}
				}
				
				if (!placeholderCooldowns.containsKey(player.getUniqueId())) {
					placeholderCooldowns.put(player.getUniqueId(), new ConcurrentHashMap<String, Long>());
				}
				Map<String, Long> spmap = placeholderCooldowns.get(player.getUniqueId());
				if (spmap.containsKey(placeholder)) {
					if (spmap.get(placeholder) > unix) {
						if (!PlayerUtils.hasPermission(player.getUniqueId(), "interactivechat.cooldown.bypass", false, 5)) {
							return basecomponent;
						}
					}
				}
				spmap.put(placeholder, unix + cooldown);
				InteractiveChat.universalCooldowns.put(player.getUniqueId(), unix + InteractiveChat.universalCooldown);
			}
			InteractiveChat.cooldownbypass.get(unix).add(placeholder);
			InteractiveChat.cooldownbypass.put(unix, InteractiveChat.cooldownbypass.get(unix));
		}
		
		List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
		List<BaseComponent> newlist = new ArrayList<BaseComponent>();
		for (BaseComponent base : basecomponentlist) {
			if (!(base instanceof TextComponent)) {
				newlist.add(base);
			} else {
				TextComponent textcomponent = (TextComponent) base;
				String text = textcomponent.getText();
				if (casesensitive) {
					if (!ChatColorUtils.stripColor(text).contains(ChatColorUtils.stripColor(placeholder))) {
						newlist.add(textcomponent);
						continue;
					}
				} else {
					if (!ChatColorUtils.stripColor(text).toLowerCase().contains(ChatColorUtils.stripColor(placeholder).toLowerCase())) {
						newlist.add(textcomponent);
						continue;
					}
				}
				
				String regex = casesensitive ? "(?<!§)" + CustomStringUtils.getIgnoreColorCodeRegex(CustomStringUtils.escapeMetaCharacters(placeholder)) : "(?i)(?<!§)(" + CustomStringUtils.getIgnoreColorCodeRegex(CustomStringUtils.escapeMetaCharacters(placeholder)) + ")";
				List<String> trim = new LinkedList<String>(Arrays.asList(text.split(regex, -1)));
				if (trim.get(trim.size() - 1).equals("")) {
					trim.remove(trim.size() - 1);
				}

				String lastColor = "";
				
				for (int i = 0; i < trim.size(); i++) {
					TextComponent before = new TextComponent(textcomponent);
					before.setText(lastColor + trim.get(i));
					newlist.add(before);
					lastColor = ChatColorUtils.getLastColors(before.getText());
					
					boolean endwith = casesensitive ? text.matches(".*" + regex + "$") : text.toLowerCase().matches(".*" + regex.toLowerCase() + "$");
					if ((trim.size() - 1) > i || endwith) {
						if (trim.get(i).endsWith("\\") && !trim.get(i).endsWith("\\\\")) {
							String color = ChatColorUtils.getLastColors(newlist.get(newlist.size() - 1).toLegacyText());
							TextComponent message = new TextComponent(placeholder);
							message = (TextComponent) ChatColorUtils.applyColor(message, color);
							((TextComponent) newlist.get(newlist.size() - 1)).setText(trim.get(i).substring(0, trim.get(i).length() - 1));
							newlist.add(message);
						} else {
							if (trim.get(i).endsWith("\\\\")) {
								((TextComponent) newlist.get(newlist.size() - 1)).setText(trim.get(i).substring(0, trim.get(i).length() - 1));
							}
							PlayerWrapper player = parseplayer;
							
							String textComp = placeholder;
							if (replaceEnabled) {
								textComp = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, replaceText));
							}
							BaseComponent[] bcJson = TextComponent.fromLegacyText(textComp);
			            	List<BaseComponent> baseJson = new ArrayList<BaseComponent>();
			            	baseJson = CustomStringUtils.loadExtras(Arrays.asList(bcJson));
			            	
			            	for (BaseComponent baseComponent : baseJson) {
			            		TextComponent message = (TextComponent) baseComponent;
			            		if (hoverEnabled) {
									message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, hoverText))).create()));
								}
								
								if (clickEnabled) {
									String clicktext = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, clickValue));
									message.setClickEvent(new ClickEvent(clickAction, clicktext));
								}
								
								newlist.add(message);
			            	}
						}
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
