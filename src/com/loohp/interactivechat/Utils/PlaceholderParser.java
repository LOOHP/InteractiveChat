package com.loohp.interactivechat.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.ObjectHolders.PlayerWrapper;
import com.loohp.interactivechat.ObjectHolders.ValuePairs;
import com.loohp.interactivechat.PluginMessaging.BungeeMessageSender;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderParser {
	
	public static String parse(PlayerWrapper player, String str) {
		if (player.isLocal()) {
			if (InteractiveChat.bungeecordMode) {
				List<ValuePairs<String, String>> pairs = new ArrayList<>();
				for (Entry<String, String> entry : getAllPlaceholdersContained(player.getLocalPlayer(), str).entrySet()) {
					pairs.add(new ValuePairs<>(entry.getKey(), entry.getValue()));
				}
				try {
					BungeeMessageSender.forwardPlaceholders(player.getUniqueId(), pairs);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return PlaceholderAPI.setPlaceholders(player.getLocalPlayer(), str);
		} else {
			for (Entry<String, String> entry : player.getRemotePlaceholdersMapping().entrySet()) {
				str = str.replace(entry.getKey(), entry.getValue());
			}
			return str;
		}
	}
	
	public static Map<String, String> getAllPlaceholdersContained(Player player, String str) {
		Collection<PlaceholderExpansion> expansions = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().getExpansions();
		Set<String> identifier = expansions.parallelStream().map(each -> each.getIdentifier()).collect(Collectors.toSet());
		Map<String, String> matchingPlaceholders = new HashMap<>();
		for (String word : str.split(" ")) {
			if (identifier.parallelStream().anyMatch(each -> word.startsWith("%" + each))) {
				matchingPlaceholders.put(word, PlaceholderAPI.setPlaceholders(player, word));
			}
		}
		return matchingPlaceholders;
	}

}
