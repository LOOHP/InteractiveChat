package com.loohp.interactivechat.Modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.loohp.interactivechat.ConfigManager;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.API.Events.PlayerMentionPlayerEvent;
import com.loohp.interactivechat.Utils.CustomStringUtils;
import com.loohp.interactivechat.Utils.OldTitleSender;
import com.loohp.interactivechat.Utils.SoundUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class MentionDisplay {
	
	public static BaseComponent process(BaseComponent basecomponent, Player beenpinged, Player sender, String messageKey, long unix) {
		if (InteractiveChat.mentionPair.containsKey(beenpinged.getUniqueId())) {
    		if (InteractiveChat.mentionPair.get(beenpinged.getUniqueId()).equals(sender.getUniqueId())) {
    			Player player = beenpinged;
    			
    			String title = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(sender, ConfigManager.getConfig().getString("Chat.MentionedTitle")));
				String subtitle = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(sender, ConfigManager.getConfig().getString("Chat.KnownPlayerMentionSubtitle")));
				Sound sound = null;
				if (SoundUtils.isValid(ConfigManager.getConfig().getString("Chat.MentionedSound"))) {
					sound = Sound.valueOf(ConfigManager.getConfig().getString("Chat.MentionedSound"));
				} else {
					Bukkit.getConsoleSender().sendMessage("Invalid Sound: " + ConfigManager.getConfig().getString("Chat.MentionedSound"));
				}
				
				boolean inCooldown = true;
				if (InteractiveChat.mentionCooldown.get(player) < unix) {
					inCooldown = false;
				}
				PlayerMentionPlayerEvent mentionEvent = new PlayerMentionPlayerEvent(player, sender.getUniqueId(), title, subtitle, sound, inCooldown);
				Player reciever = mentionEvent.getReciver();
				InteractiveChat.mentionPair.put(reciever.getUniqueId(), sender.getUniqueId());
				if (!mentionEvent.isCancelled()) {
					
					int time = (int) Math.round(ConfigManager.getConfig().getDouble("Chat.MentionedTitleDuration") * 20);
					if (InteractiveChat.version.contains("OLD")) {
						OldTitleSender.sendTitle(player, title, subtitle, time);
					} else {
						reciever.sendTitle(title, subtitle, 10, time, 20);
					}
					if (sound != null) {
						reciever.playSound(player.getLocation(), sound, 3.0F, 1.0F);
					}
					
					List<String> names = new ArrayList<String>();
					names.add(reciever.getName());
					if (!names.contains(reciever.getName())) {
						names.add(reciever.getName());
					}
					if (InteractiveChat.ess3) {
						if (InteractiveChat.essenNick.containsKey(reciever)) {
							names.add(InteractiveChat.essenNick.get(reciever));
						}
					}
					
					for (String name : names) {
						basecomponent = processPlayer(name, reciever, sender, basecomponent, messageKey, unix);
					}
					
					InteractiveChat.mentionCooldown.put(reciever, unix + 3000);
					InteractiveChat.mentionPair.remove(reciever.getUniqueId());
				}
    		}
		}
		return basecomponent;
	}
	
	public static BaseComponent processPlayer(String placeholder, Player reciever, Player sender, BaseComponent basecomponent, String messageKey, long unix) {
		List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
		List<BaseComponent> newlist = new ArrayList<BaseComponent>();
		for (BaseComponent base : basecomponentlist) {
			TextComponent textcomponent = (TextComponent) base;
			String text = textcomponent.getText();
			if (!text.toLowerCase().contains(placeholder.toLowerCase())) {
				newlist.add(textcomponent);
				continue;
			}
			
			String regex = CustomStringUtils.escapeMetaCharacters(placeholder);
			List<String> trim = new LinkedList<String>(Arrays.asList(text.split(regex, -1)));
			if (trim.get(trim.size() - 1).equals("")) {
				trim.remove(trim.size() - 1);
			}
			for (int i = 0; i < trim.size(); i++) {
				TextComponent before = (TextComponent) textcomponent.duplicate();
				before.setText(trim.get(i));
				newlist.add(before);
				
				if ((trim.size() - 1) > i || text.endsWith(placeholder)) {		    
					TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', InteractiveChat.mentionHightlight.replace("{MentionedPlayer}", placeholder)));
					message = CustomStringUtils.copyFormattingEventsNoReplace(message, (BaseComponent) before);
					String hover = ChatColor.translateAlternateColorCodes('&', InteractiveChat.mentionHover.replace("{Sender}", sender.getDisplayName()).replace("{Reciever}", reciever.getDisplayName()));
					message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
					
					newlist.add(message);
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
