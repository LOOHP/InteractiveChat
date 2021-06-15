package com.loohp.interactivechat.modules;

import java.util.ArrayList;
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
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.CustomStringUtils;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.SoundUtils;
import com.loohp.interactivechat.utils.TitleUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class MentionDisplay {
	
	public static Component process(Component component, Player beenpinged, ICPlayer sender, long unix, boolean async) {
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
					if (InteractiveChat.useBukkitDisplayName && !ChatColorUtils.stripColor(reciever.getName()).equals(ChatColorUtils.stripColor(reciever.getDisplayName()))) {
						names.add(ChatColorUtils.stripColor(reciever.getDisplayName()));
	    			}
					List<String> list = InteractiveChatAPI.getNicknames(reciever.getUniqueId());
					for (String name : list) {
						names.add(ChatColorUtils.stripColor(name));
					}
					names.add("here");
					names.add("everyone");
					
					for (String name : names) {
						component = processPlayer(InteractiveChat.mentionPrefix + name, reciever, sender, component, unix);
					}
					
					InteractiveChat.mentionCooldown.put(reciever, unix + 3000);
					pair.remove();
				}
    		}
		}
		return component;
	}
	
	public static Component processPlayer(String placeholder, Player reciever, ICPlayer sender, Component component, long unix) {
		String replacementText = ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.mentionHightlight.replace("{MentionedPlayer}", placeholder));
		Component replacement = LegacyComponentSerializer.legacySection().deserialize(replacementText);
		String hoverText = ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.mentionHover.replace("{Sender}", sender.getDisplayName()).replace("{Reciever}", reciever.getDisplayName()));
		HoverEvent<Component> hoverEvent = HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(hoverText));
		replacement = replacement.hoverEvent(hoverEvent);
		return ComponentReplacing.replace(component, CustomStringUtils.escapeMetaCharacters(placeholder), true, replacement);
	}

}
