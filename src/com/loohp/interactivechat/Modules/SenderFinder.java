package com.loohp.interactivechat.Modules;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.Utils.ChatColorUtils;
import com.loohp.interactivechat.Utils.CustomStringUtils;

import net.md_5.bungee.api.chat.BaseComponent;

public class SenderFinder {
	
	private static ConcurrentHashMap<String, UUID> messages = InteractiveChat.messages;
	
	public static Optional<Player> getSender(BaseComponent basecomponent, String messageKey) {
		Player keyPlayer = InteractiveChat.keyPlayer.get(messageKey);
		if (keyPlayer != null) {
			return Optional.of(keyPlayer);
		}
		
		String chat = basecomponent.toPlainText();
		
		for (Entry<String, UUID> entry : messages.entrySet()) {
			String msg = entry.getKey();
			if (chat.contains(msg)) {
				Player player = Bukkit.getPlayer(entry.getValue());
				Bukkit.getScheduler().runTaskLaterAsynchronously(InteractiveChat.plugin, () -> messages.remove(entry.getKey()), 5);
				if (player != null) {
					return Optional.of(player);
				}
			}
		}
		
		String mostsimular = null;
		double currentsim = 0.5;
		for (Entry<String, UUID> entry : messages.entrySet()) {
			String msg = entry.getKey();
			double sim = CustomStringUtils.similarity(chat, msg);
			if (sim > currentsim) {
				mostsimular = msg;
				currentsim = sim;
			}
		}
		
		if (mostsimular != null) {
			UUID uuid = messages.get(mostsimular);
			String finalmostsimular = mostsimular;
			Bukkit.getScheduler().runTaskLaterAsynchronously(InteractiveChat.plugin, () -> messages.remove(finalmostsimular), 5);
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				return Optional.of(player);
			}
		}
		
		HashMap<String, Player> names = new HashMap<String, Player>();
		Bukkit.getOnlinePlayers().forEach((each) -> {
			names.put(ChatColorUtils.stripColor(each.getName()), each);
			if (!ChatColorUtils.stripColor(each.getName()).equals(ChatColorUtils.stripColor(each.getDisplayName()))) {
				names.put(ChatColorUtils.stripColor(each.getDisplayName()), each);
			}
		});
		if (InteractiveChat.EssentialsHook) {
			InteractiveChat.essenNick.forEach((player, name) -> names.put(ChatColorUtils.stripColor(name), player));
		}
		
		Player currentplayer = null;
		int currentpos = 99999;
		for (Entry<String, Player> entry : names.entrySet()) {
			int pos = chat.toLowerCase().indexOf(entry.getKey().toLowerCase());
			if (pos >= 0 && pos < currentpos) {
				currentpos = pos;
				currentplayer = entry.getValue();
			}
		}
		
		if (currentplayer != null) {
			return Optional.of(currentplayer);
		}

		return Optional.empty();
	}

}
