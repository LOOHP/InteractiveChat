package com.loohp.interactivechat.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ChatComponentUtils;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class OutTabCompletePacket {
	
	private static Map<String, UUID> playernames = new HashMap<>();
	
	public static void tabCompleteListener() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(InteractiveChat.plugin, () -> {
			if (InteractiveChat.useTooltipOnTab) {
				Map<String, UUID> playernames = new HashMap<>();
				for (Player player : Bukkit.getOnlinePlayers()) {
	    			playernames.put(ChatColorUtils.stripColor(player.getName()), player.getUniqueId());
	    			if (!player.getName().equals(player.getDisplayName())) {
	    				playernames.put(ChatColorUtils.stripColor(player.getDisplayName()), player.getUniqueId());
	    			}
					List<String> names = InteractiveChatAPI.getNicknames(player.getUniqueId());
					for (String name : names) {
						playernames.put(ChatColorUtils.stripColor(name), player.getUniqueId());
					}
				}
				synchronized (InteractiveChat.remotePlayers) {
					for (Entry<UUID, ICPlayer> entry : InteractiveChat.remotePlayers.entrySet()) {
						playernames.put(ChatColorUtils.stripColor(entry.getValue().getName()), entry.getKey());
					}
				}
				Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> OutTabCompletePacket.playernames = playernames);
			}
		}, 0, 100);
		
		InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().optionAsync().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.HIGHEST).types(PacketType.Play.Server.TAB_COMPLETE)) {
			@Override
			public void onPacketSending(PacketEvent event) {
				if (!event.isFiltered() || event.isCancelled() || !event.getPacketType().equals(PacketType.Play.Server.TAB_COMPLETE) || event.isPlayerTemporary()) {
		    		return;
		    	}
				
				PacketContainer packet = event.getPacket();
				Player tabCompleter = event.getPlayer();
				Suggestions suggestions = (Suggestions) packet.getModifier().read(1);
				StringRange range = suggestions.getRange();
				
				List<Suggestion> matches = suggestions.getList();
				List<Suggestion> newMatches = new ArrayList<>();
				for (Suggestion suggestion : matches) {
					String text = suggestion.getText();
					int pos = text.indexOf("\0");
					if (pos < 0) {
						if (InteractiveChat.useTooltipOnTab) {
							ICPlayer icplayer = null;
							for (Entry<String, UUID> entry : playernames.entrySet()) {
								if (entry.getKey().equalsIgnoreCase(text)) {
									icplayer = InteractiveChat.remotePlayers.get(entry.getValue());
									if (icplayer == null) {
										icplayer = new ICPlayer(Bukkit.getPlayer(entry.getValue()));
									}
									break;
								}
							}
							if (icplayer != null) {
								TextComponent playertext = new TextComponent(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(icplayer, InteractiveChat.tabTooltip)));
								newMatches.add(new Suggestion(range, text, (Message) WrappedChatComponent.fromJson(ComponentSerializer.toString(ChatComponentUtils.cleanUpLegacyText(playertext, tabCompleter))).getHandle()));
							} else {
								newMatches.add(suggestion);
							}
						} else {
							newMatches.add(suggestion);
						}
					} else {
						String tooltip = text.substring(pos + 1);
						text = text.substring(0, pos);
						newMatches.add(new Suggestion(range, text, (Message) WrappedChatComponent.fromJson(tooltip).getHandle()));
					}
				}
				
				packet.getModifier().write(1, new Suggestions(range, newMatches));
			}
		});
	}

}
