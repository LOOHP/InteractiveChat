package com.loohp.interactivechat.Modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.Utils.ChatColorUtils;
import com.loohp.interactivechat.Utils.CustomStringUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class PlayernameDisplay {
	
	private static boolean casesensitive = InteractiveChat.usePlayerNameCaseSensitive;
	private static boolean hoverEnabled = InteractiveChat.usePlayerNameHoverEnable;
	private static String hoverText = InteractiveChat.usePlayerNameHoverText;
	private static boolean clickEnabled = InteractiveChat.usePlayerNameClickEnable;
	private static String clickAction = InteractiveChat.usePlayerNameClickAction;
	private static String clickValue = InteractiveChat.usePlayerNameClickValue;
	
	public static BaseComponent process(BaseComponent basecomponent, String messageKey, long unix) {
		HashMap<String, Player> names = new HashMap<String, Player>();
		Bukkit.getOnlinePlayers().forEach((each) -> {
			names.put(ChatColor.stripColor(each.getName()), each);
			if (!names.containsKey(ChatColor.stripColor(each.getDisplayName()))) {
				names.put(ChatColor.stripColor(each.getDisplayName()), each);
			}
		}); 
		if (InteractiveChat.EssentialsHook) {
			InteractiveChat.essenNick.forEach((player, name) -> names.put(ChatColor.stripColor(name), player));
		}
		
		for (Entry<String, Player> entry : names.entrySet()) {
			basecomponent = processPlayer(entry.getKey(), entry.getValue(), basecomponent, messageKey, unix);
		}
		return basecomponent;
	}
	
	public static BaseComponent processPlayer(String placeholder, Player player, BaseComponent basecomponent, String messageKey, long unix) {
		List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
		List<BaseComponent> newlist = new ArrayList<BaseComponent>();
		for (BaseComponent base : basecomponentlist) {
			if (!(base instanceof TextComponent)) {
				newlist.add(base);
			} else {
				TextComponent textcomponent = (TextComponent) base;
				String text = textcomponent.getText();
				String regex = casesensitive ? CustomStringUtils.getIgnoreColorCodeRegex(CustomStringUtils.escapeMetaCharacters(placeholder)) : "(?i)(" + CustomStringUtils.getIgnoreColorCodeRegex(CustomStringUtils.escapeMetaCharacters(placeholder)) + ")";
				
				if (!text.matches(".*" + regex + ".*")) {
					newlist.add(textcomponent);
					continue;
				}
				
				List<String> trim = new LinkedList<String>(Arrays.asList(text.split(regex, -1)));
				if (trim.get(trim.size() - 1).equals("")) {
					trim.remove(trim.size() - 1);
				}
				for (int i = 0; i < trim.size(); i++) {
					TextComponent before = (TextComponent) textcomponent.duplicate();
					before.setText(trim.get(i));
					newlist.add(before);
					
					if ((trim.size() - 1) > i || text.matches(".*" + regex + "$")) {
						String lastColor = ChatColorUtils.getLastColors(trim.get(i));
				    
						TextComponent message = new TextComponent(placeholder);
						message = CustomStringUtils.copyFormatting(message, before);
						message.setText(lastColor + message.getText());
						if (hoverEnabled) {
							String playertext = PlaceholderAPI.setPlaceholders(player, hoverText);
							message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(playertext).create()));
						}
						if (clickEnabled) {
							String playertext = PlaceholderAPI.setPlaceholders(player, clickValue);
							message.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(clickAction), playertext));
						}
						
						newlist.add(message);
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
