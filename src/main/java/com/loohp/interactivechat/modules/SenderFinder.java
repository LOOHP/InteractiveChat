package com.loohp.interactivechat.modules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.CustomStringUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class SenderFinder {
	
	public static Optional<ICPlayer> getSender(Component component, String messageKey) {
		ICPlayer keyPlayer = InteractiveChat.keyPlayer.get(messageKey);
		if (keyPlayer != null) {
			return Optional.of(keyPlayer);
		}
		
		String chat = PlainTextComponentSerializer.plainText().serialize(component);
		
		for (Entry<String, UUID> entry : InteractiveChat.messages.entrySet()) {
			String msg = entry.getKey();
			if (chat.contains(msg)) {
				UUID uuid = entry.getValue();
				Player player = Bukkit.getPlayer(uuid);
				Bukkit.getScheduler().runTaskLaterAsynchronously(InteractiveChat.plugin, () -> InteractiveChat.messages.remove(msg), 5);
				if (player != null) {
					return Optional.of(new ICPlayer(player));
				}
				ICPlayer wplayer = InteractiveChat.remotePlayers.get(entry.getValue());
				if (wplayer != null) {
					return Optional.of(wplayer);
				}
			}
		}
		
		String mostsimular = null;
		double currentsim = 0.5;
		for (Entry<String, UUID> entry : InteractiveChat.messages.entrySet()) {
			String msg = entry.getKey();
			double sim = CustomStringUtils.similarity(chat, msg);
			if (sim > currentsim) {
				mostsimular = msg;
				currentsim = sim;
			}
		}
		
		if (mostsimular != null) {
			UUID uuid = InteractiveChat.messages.get(mostsimular);
			String finalmostsimular = mostsimular;
			Bukkit.getScheduler().runTaskLaterAsynchronously(InteractiveChat.plugin, () -> InteractiveChat.messages.remove(finalmostsimular), 5);
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				return Optional.of(new ICPlayer(player));
			}
			ICPlayer wplayer = InteractiveChat.remotePlayers.get(uuid);
			if (wplayer != null) {
				return Optional.of(wplayer);
			}
		}
		
		Map<String, UUID> names = new HashMap<>();
		Bukkit.getOnlinePlayers().forEach((each) -> {
			names.put(ChatColorUtils.stripColor(each.getName()), each.getUniqueId());
			if (!ChatColorUtils.stripColor(each.getName()).equals(ChatColorUtils.stripColor(each.getDisplayName()))) {
				names.put(ChatColorUtils.stripColor(each.getDisplayName()), each.getUniqueId());
			}
		});
		InteractiveChat.remotePlayers.entrySet().forEach(entry -> {
			names.put(ChatColorUtils.stripColor(entry.getValue().getDisplayName()), entry.getKey());
		});
		Bukkit.getOnlinePlayers().forEach(each -> {
			List<String> list = InteractiveChatAPI.getNicknames(each.getUniqueId());
			for (String name : list) {
				names.put(ChatColorUtils.stripColor(name), each.getUniqueId());
			}
		});
		
		UUID currentplayer = null;
		int currentpos = 99999;
		for (Entry<String, UUID> entry : names.entrySet()) {
			int pos = chat.toLowerCase().indexOf(entry.getKey().toLowerCase());
			if (pos >= 0 && pos < currentpos) {
				currentpos = pos;
				currentplayer = entry.getValue();
			}
		}
		
		if (currentplayer != null) {
			Player player = Bukkit.getPlayer(currentplayer);
			if (player != null) {
				return Optional.of(new ICPlayer(player));
			}
			ICPlayer wplayer = InteractiveChat.remotePlayers.get(currentplayer);
			if (wplayer != null) {
				return Optional.of(wplayer);
			}
		}

		return Optional.empty();
	}

}
