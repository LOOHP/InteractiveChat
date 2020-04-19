package com.loohp.interactivechat.Modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.loohp.interactivechat.ConfigManager;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.Utils.CustomStringUtils;
import com.loohp.interactivechat.Utils.JsonUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class CustomPlaceholderDisplay {
	
	private static HashMap<Player, HashMap<String, Long>> placeholderCooldowns = InteractiveChat.placeholderCooldowns;
	private static HashMap<Player, Long> universalCooldowns = InteractiveChat.universalCooldowns;
	
	public static BaseComponent process(BaseComponent basecomponent, Optional<Player> optplayer, Player reciever, String messageKey, long unix) {
		for (int customNo = 1; ConfigManager.getConfig().contains("CustomPlaceholders." + String.valueOf(customNo)); customNo++) {
			ConfigurationSection s = ConfigManager.getConfig().getConfigurationSection("CustomPlaceholders." + String.valueOf(customNo));
			Player parseplayer = (s.getString("ParsePlayer").equalsIgnoreCase("sender") && optplayer.isPresent()) ? optplayer.get() : reciever;
			boolean casesensitive = s.getBoolean("CaseSensitive");
			String placeholder = s.getString("Text");
			placeholder = (s.getBoolean("ParseKeyword")) ? PlaceholderAPI.setPlaceholders(parseplayer, placeholder) : placeholder;
			long cooldown = s.getLong("Cooldown") * 1000;
			boolean hoverEnabled = s.getBoolean("Hover.Enable");
			String hoverText = String.join("\n", s.getStringList("Hover.Text"));
			boolean clickEnabled = s.getBoolean("Click.Enable");
			String clickAction = s.getString("Click.Action").toUpperCase();
			String clickValue = s.getString("Click.Value");
			boolean replaceEnabled = s.getBoolean("Replace.Enable");
			String replaceText = s.getString("Replace.ReplaceText");
			
			basecomponent = processCustomPlaceholder(parseplayer, casesensitive, placeholder, cooldown, hoverEnabled, hoverText, clickEnabled, clickAction, clickValue, replaceEnabled, replaceText, basecomponent, optplayer, messageKey, unix);
		}
		
		return basecomponent;
	}
	
	public static BaseComponent processCustomPlaceholder(Player parseplayer, boolean casesensitive, String placeholder, long cooldown, boolean hoverEnabled, String hoverText, boolean clickEnabled, String clickAction, String clickValue, boolean replaceEnabled, String replaceText, BaseComponent basecomponent, Optional<Player> optplayer, String messageKey, long unix) {
		boolean contain = (casesensitive) ? (basecomponent.toPlainText().contains(placeholder)) : (basecomponent.toPlainText().toLowerCase().contains(placeholder.toLowerCase()));
		if (!InteractiveChat.cooldownbypass.get(unix).contains(placeholder) && contain) {
			if (optplayer.isPresent()) {
				Player player = optplayer.get();
				Long uc = universalCooldowns.get(player);
				if (uc != null) {
					if (uc > unix) {
						return basecomponent;
					}
				}
				
				if (!placeholderCooldowns.containsKey(player)) {
					placeholderCooldowns.put(player, new HashMap<String, Long>());
				}
				HashMap<String, Long> spmap = placeholderCooldowns.get(player);
				if (spmap.containsKey(placeholder)) {
					if (spmap.get(placeholder) > unix) {
						if (!player.hasPermission("interactivechat.cooldown.bypass")) {
							return basecomponent;
						}
					}
				}
				spmap.put(placeholder, unix + cooldown);
			}
			InteractiveChat.cooldownbypass.get(unix).add(placeholder);
			InteractiveChat.cooldownbypass.put(unix, InteractiveChat.cooldownbypass.get(unix));
		}
		
		List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
		List<BaseComponent> newlist = new ArrayList<BaseComponent>();
		for (BaseComponent base : basecomponentlist) {
			TextComponent textcomponent = (TextComponent) base;
			String text = textcomponent.getText();
			if (casesensitive) {
				if (!text.contains(placeholder)) {
					newlist.add(textcomponent);
					continue;
				}
			} else {
				if (!text.toLowerCase().contains(placeholder.toLowerCase())) {
					newlist.add(textcomponent);
					continue;
				}
			}
			
			String regex = casesensitive ? CustomStringUtils.escapeMetaCharacters(placeholder) : "(?i)(" + CustomStringUtils.escapeMetaCharacters(placeholder) + ")";
			List<String> trim = new LinkedList<String>(Arrays.asList(text.split(regex, -1)));
			if (trim.get(trim.size() - 1).equals("")) {
				trim.remove(trim.size() - 1);
			}
			for (int i = 0; i < trim.size(); i++) {
				TextComponent before = (TextComponent) textcomponent.duplicate();
				before.setText(trim.get(i));
				newlist.add(before);
				
				boolean endwith = casesensitive ? text.endsWith(placeholder) : text.toLowerCase().endsWith(placeholder.toLowerCase());
				if ((trim.size() - 1) > i || endwith) {
					if (trim.get(i).endsWith("\\") && !trim.get(i).endsWith("\\\\")) {
						TextComponent message = new TextComponent(placeholder);
						((TextComponent) newlist.get(newlist.size() - 1)).setText(trim.get(i).substring(0, trim.get(i).length() - 1));
						newlist.add(message);
					} else {
						if (trim.get(i).endsWith("\\\\")) {
							((TextComponent) newlist.get(newlist.size() - 1)).setText(trim.get(i).substring(0, trim.get(i).length() - 1));
						}
						Player player = parseplayer;
						
						String textComp = placeholder;
						if (replaceEnabled) {
							textComp = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, replaceText));
						}
						Bukkit.getConsoleSender().sendMessage(textComp);
						BaseComponent[] bcJson = ComponentSerializer.parse(JsonUtils.toJSON(textComp));
		            	List<BaseComponent> baseJson = new ArrayList<BaseComponent>();
		            	baseJson = CustomStringUtils.loadExtras(Arrays.asList(bcJson));
		            	
		            	for (BaseComponent baseComponent : baseJson) {
		            		TextComponent message = (TextComponent) baseComponent;
		            		if (hoverEnabled) {
								message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PlaceholderAPI.setPlaceholders(player, hoverText)).create()));
							}
							
							if (clickEnabled) {
								String clicktext = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, clickValue));
								message.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(clickAction), clicktext));
							}
							
							newlist.add(message);
		            	}
					}
				}
			}
		}
		
		TextComponent product = (TextComponent) newlist.get(0);
		for (int i = 1; i < newlist.size(); i++) {
			BaseComponent each = newlist.get(i);
			product.addExtra(each);
		}
		return product;
	}

}
