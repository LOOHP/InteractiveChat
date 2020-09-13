package com.loohp.interactivechat.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import com.loohp.interactivechat.ObjectHolders.PlayerWrapper;
import com.loohp.interactivechat.PluginMessaging.BungeeMessageSender;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderParser {
	
	public static String parse(PlayerWrapper player, String str) {
		if (player.isLocal()) {
			for (Entry<String, String> entry : getAllPlaceholdersContained(player.getLocalPlayer(), str).entrySet()) {
				try {
					BungeeMessageSender.forwardPlaceholders(player.getUniqueId(), entry.getKey(), entry.getValue());
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
		Set<PlaceholderExpansion> expansions = PlaceholderAPI.getExpansions();
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
