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
import com.loohp.interactivechat.ObjectHolders.MentionPair;
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
	
	public static BaseComponent process(BaseComponent basecomponent, Player beenpinged, Player sender, String messageKey, long unix, boolean async) {
		if (InteractiveChat.mentionPair.containsKey(beenpinged.getUniqueId())) {
			MentionPair pair = InteractiveChat.mentionPair.get(beenpinged.getUniqueId());
    		if (pair.getSender().equals(sender.getUniqueId())) {
    			Player reciever = beenpinged;
    			
    			String title = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(sender, ConfigManager.getConfig().getString("Chat.MentionedTitle")));
				String subtitle = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(sender, ConfigManager.getConfig().getString("Chat.KnownPlayerMentionSubtitle")));
				Sound sound = null;
				if (SoundUtils.isValid(ConfigManager.getConfig().getString("Chat.MentionedSound"))) {
					sound = Sound.valueOf(ConfigManager.getConfig().getString("Chat.MentionedSound"));
				} else {
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Invalid Sound: " + ConfigManager.getConfig().getString("Chat.MentionedSound"));
				}
				
				boolean inCooldown = true;
				if (InteractiveChat.mentionCooldown.get(reciever) < unix) {
					inCooldown = false;
				}
				PlayerMentionPlayerEvent mentionEvent = new PlayerMentionPlayerEvent(async, reciever, sender.getUniqueId(), title, subtitle, sound, inCooldown);
				Bukkit.getPluginManager().callEvent(mentionEvent);
				if (!mentionEvent.isCancelled()) {
					
					int time = (int) Math.round(ConfigManager.getConfig().getDouble("Chat.MentionedTitleDuration") * 20);
					if (InteractiveChat.version.isOld()) {
						OldTitleSender.sendTitle(reciever, title, subtitle, time);
					} else {
						reciever.sendTitle(title, subtitle, 10, time, 20);
					}
					if (sound != null) {
						reciever.playSound(reciever.getLocation(), sound, 3.0F, 1.0F);
					}
					
					List<String> names = new ArrayList<String>();
					names.add(ChatColor.stripColor(reciever.getName()));
					if (!names.contains(ChatColor.stripColor(reciever.getDisplayName()))) {
						names.add(ChatColor.stripColor(reciever.getDisplayName()));
					}
					if (InteractiveChat.EssentialsHook) {
						if (InteractiveChat.essenNick.containsKey(reciever)) {
							names.add(ChatColor.stripColor(InteractiveChat.essenNick.get(reciever)));
						}
					}
					
					for (String name : names) {
						basecomponent = processPlayer(name, reciever, sender, basecomponent, messageKey, unix);
					}
					
					InteractiveChat.mentionCooldown.put(reciever, unix + 3000);
					pair.remove();
				}
    		}
		}
		return basecomponent;
	}
	
	@SuppressWarnings("deprecation")
	public static BaseComponent processPlayer(String placeholder, Player reciever, Player sender, BaseComponent basecomponent, String messageKey, long unix) {
		List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
		List<BaseComponent> newlist = new ArrayList<BaseComponent>();
		for (BaseComponent base : basecomponentlist) {
			if (!(base instanceof TextComponent)) {
				newlist.add(base);
			} else {
				TextComponent textcomponent = (TextComponent) base;
				String text = textcomponent.getText();
				String regex = CustomStringUtils.getIgnoreColorCodeRegex(CustomStringUtils.escapeMetaCharacters(placeholder));
				
				if (!text.matches("(?i).*" + regex + ".*")) {
					newlist.add(textcomponent);
					continue;
				}
				
				List<String> trim = new LinkedList<String>(Arrays.asList(text.split(regex, -1)));
				if (trim.get(trim.size() - 1).equals("")) {
					trim.remove(trim.size() - 1);
				}
				for (int i = 0; i < trim.size(); i++) {
					TextComponent before = new TextComponent(textcomponent);
					before.setText(trim.get(i));
					newlist.add(before);
					if ((trim.size() - 1) > i || text.matches(".*" + regex + "$")) {		    
						TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', InteractiveChat.mentionHightlight.replace("{MentionedPlayer}", placeholder)));
						message = (TextComponent) CustomStringUtils.copyFormattingEventsNoReplace(message, (BaseComponent) before);
						String hover = ChatColor.translateAlternateColorCodes('&', InteractiveChat.mentionHover.replace("{Sender}", sender.getDisplayName()).replace("{Reciever}", reciever.getDisplayName()));
						message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
						
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
