package com.loohp.interactivechat.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.loohp.interactivechat.ConfigManager;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.api.events.PlayerMentionPlayerEvent;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.MentionPair;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.CustomStringUtils;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.SoundUtils;
import com.loohp.interactivechat.utils.TitleUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class MentionDisplay {
	
	public static BaseComponent process(BaseComponent basecomponent, Player beenpinged, ICPlayer sender, long unix, boolean async) {
		if (InteractiveChat.mentionPair.containsKey(beenpinged.getUniqueId())) {
			MentionPair pair = InteractiveChat.mentionPair.get(beenpinged.getUniqueId());
    		if (pair.getSender().equals(sender.getUniqueId())) {
    			Player reciever = beenpinged;
    			
    			String title = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(sender, ConfigManager.getConfig().getString("Chat.MentionedTitle")));
				String subtitle = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(sender, ConfigManager.getConfig().getString("Chat.KnownPlayerMentionSubtitle")));
				String actionbar = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(sender, ConfigManager.getConfig().getString("Chat.KnownPlayerMentionActionbar")));
				
				String settings = ConfigManager.getConfig().getString("Chat.MentionedSound");
				Sound sound = null;
				float volume = 3.0F;
				float pitch = 1.0F;
				
				String[] settingsArgs = settings.split(":");
				if (settingsArgs.length == 3) {
					settings = settingsArgs[0];
					try {
						volume = Float.parseFloat(settingsArgs[1]);
					} catch (Exception ignore) {}
					try {
						pitch = Float.parseFloat(settingsArgs[2]);
					} catch (Exception ignore) {}
				} else if (settingsArgs.length > 0) {
					settings = settingsArgs[0];
				}
				
				sound = SoundUtils.parseSound(settings);
				if (sound == null) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Invalid Sound: " + settings);
				}
				
				boolean inCooldown = true;
				if (InteractiveChat.mentionCooldown.get(reciever) < unix) {
					inCooldown = false;
				}
				PlayerMentionPlayerEvent mentionEvent = new PlayerMentionPlayerEvent(async, reciever, sender.getUniqueId(), title, subtitle, actionbar, sound, inCooldown);
				Bukkit.getPluginManager().callEvent(mentionEvent);
				if (!mentionEvent.isCancelled()) {
					title = mentionEvent.getTitle();
					subtitle = mentionEvent.getSubtitle();
					actionbar = mentionEvent.getActionbar();
					
					int time = (int) Math.round(ConfigManager.getConfig().getDouble("Chat.MentionedTitleDuration") * 20);
					TitleUtils.sendTitle(reciever, title, subtitle, actionbar, 10, time, 20);
					if (sound != null) {
						reciever.playSound(reciever.getLocation(), sound, volume, pitch);
					}
					
					List<String> names = new ArrayList<>();
					names.add(ChatColorUtils.stripColor(reciever.getName()));
					List<String> list = InteractiveChatAPI.getNicknames(reciever.getUniqueId());
					for (String name : list) {
						names.add(ChatColorUtils.stripColor(name));
					}
					names.add("here");
					names.add("everyone");
					
					for (String name : names) {
						basecomponent = processPlayer(InteractiveChat.mentionPrefix + name, reciever, sender, basecomponent, unix);
					}
					
					InteractiveChat.mentionCooldown.put(reciever, unix + 3000);
					pair.remove();
				}
    		}
		}
		return basecomponent;
	}
	
	@SuppressWarnings("deprecation")
	public static BaseComponent processPlayer(String placeholder, Player reciever, ICPlayer sender, BaseComponent basecomponent, long unix) {
		List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
		List<BaseComponent> newlist = new ArrayList<>();
		for (BaseComponent base : basecomponentlist) {
			if (!(base instanceof TextComponent)) {
				newlist.add(base);
			} else {
				TextComponent textcomponent = (TextComponent) base;
				String text = textcomponent.getText();
				String regex = "(?i)(?<!\u00a7)" + CustomStringUtils.getIgnoreColorCodeRegex(CustomStringUtils.escapeMetaCharacters(placeholder));
				
				if (!text.matches(".*" + regex + ".*")) {
					newlist.add(textcomponent);
					continue;
				}
				
				List<String> trim = new LinkedList<>(Arrays.asList(text.split(regex, -1)));
				if (trim.get(trim.size() - 1).equals("")) {
					trim.remove(trim.size() - 1);
				}
				for (int i = 0; i < trim.size(); i++) {
					TextComponent before = new TextComponent(textcomponent);
					before.setText(trim.get(i));
					newlist.add(before);
					if ((trim.size() - 1) > i || text.matches(".*" + regex + "$")) {		    
						TextComponent message = new TextComponent(ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.mentionHightlight.replace("{MentionedPlayer}", placeholder)));
						message = (TextComponent) CustomStringUtils.copyFormattingEventsNoReplace(message, (BaseComponent) before);
						String hover = ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.mentionHover.replace("{Sender}", sender.getDisplayName()).replace("{Reciever}", reciever.getDisplayName()));
						message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent(hover)}));
						
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
