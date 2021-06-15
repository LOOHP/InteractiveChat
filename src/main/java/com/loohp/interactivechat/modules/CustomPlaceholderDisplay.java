package com.loohp.interactivechat.modules;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ClickEventAction;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ParsePlayer;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.WebData;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.CustomStringUtils;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class CustomPlaceholderDisplay {
	
	private static Map<UUID, Map<String, Long>> placeholderCooldowns = InteractiveChat.placeholderCooldowns;
	private static Map<UUID, Long> universalCooldowns = InteractiveChat.universalCooldowns;
	
	public static Component process(Component component, Optional<ICPlayer> optplayer, Player reciever, List<ICPlaceholder> placeholderList, long unix) {
		return process(component, optplayer, reciever, placeholderList, unix, false);
	}
			
	
	public static Component process(Component component, Optional<ICPlayer> optplayer, Player reciever, List<ICPlaceholder> placeholderList, long unix, boolean withoutCooldown) {
		for (int i = 0; i < placeholderList.size(); i++) {
			
			ICPlaceholder icplaceholder = placeholderList.get(i);
			if (icplaceholder.isBuildIn()) {
				continue;
			}
			CustomPlaceholder cp = icplaceholder.getCustomPlaceholder().get();
			
			ICPlayer parseplayer = (cp.getParsePlayer().equals(ParsePlayer.SENDER) && optplayer.isPresent()) ? optplayer.get() : new ICPlayer(reciever);
			boolean casesensitive = cp.isCaseSensitive();
			
			if (InteractiveChat.useCustomPlaceholderPermissions && optplayer.isPresent()) {
				ICPlayer sender = optplayer.get();
				if (!PlayerUtils.hasPermission(sender.getUniqueId(), cp.getPermission(), true, 5)) {
					continue;
				}
			}
			
			String placeholder = cp.getKeyword();
			placeholder = (cp.getParseKeyword()) ? PlaceholderParser.parse(parseplayer, placeholder) : placeholder;
			long cooldown = cp.getCooldown();
			boolean hoverEnabled = cp.getHover().isEnabled();
			String hoverText = cp.getHover().getText();
			boolean clickEnabled = cp.getClick().isEnabled();
			ClickEventAction clickAction = cp.getClick().getAction();
			String clickValue = cp.getClick().getValue();
			boolean replaceEnabled = cp.getReplace().isEnabled();
			String replaceText = cp.getReplace().getReplaceText();
			
			if (withoutCooldown) {
				component = processCustomPlaceholderWithoutCooldown(parseplayer, casesensitive, placeholder, cooldown, hoverEnabled, hoverText, clickEnabled, clickAction, clickValue, replaceEnabled, replaceText, component, optplayer, unix);
			} else {
				component = processCustomPlaceholder(parseplayer, casesensitive, placeholder, cooldown, hoverEnabled, hoverText, clickEnabled, clickAction, clickValue, replaceEnabled, replaceText, component, optplayer, unix);
			}
		}
		
		if (InteractiveChat.t && WebData.getInstance() != null) {
			for (CustomPlaceholder cp : WebData.getInstance().getSpecialPlaceholders()) {
				ICPlayer parseplayer = (cp.getParsePlayer().equals(ParsePlayer.SENDER) && optplayer.isPresent()) ? optplayer.get() : new ICPlayer(reciever);
				boolean casesensitive = cp.isCaseSensitive();			
				String placeholder = cp.getKeyword();
				placeholder = (cp.getParseKeyword()) ? PlaceholderParser.parse(parseplayer, placeholder) : placeholder;
				long cooldown = cp.getCooldown();
				boolean hoverEnabled = cp.getHover().isEnabled();
				String hoverText = cp.getHover().getText();
				boolean clickEnabled = cp.getClick().isEnabled();
				ClickEventAction clickAction = cp.getClick().getAction();
				String clickValue = cp.getClick().getValue();
				boolean replaceEnabled = cp.getReplace().isEnabled();
				String replaceText = cp.getReplace().getReplaceText();
				
				if (withoutCooldown) {
					component = processCustomPlaceholderWithoutCooldown(parseplayer, casesensitive, placeholder, cooldown, hoverEnabled, hoverText, clickEnabled, clickAction, clickValue, replaceEnabled, replaceText, component, optplayer, unix);
				} else {
					component = processCustomPlaceholder(parseplayer, casesensitive, placeholder, cooldown, hoverEnabled, hoverText, clickEnabled, clickAction, clickValue, replaceEnabled, replaceText, component, optplayer, unix);
				}
			}
		}
			
		return component;
	}
	
	public static Component processCustomPlaceholder(ICPlayer parseplayer, boolean casesensitive, String placeholder, long cooldown, boolean hoverEnabled, String hoverText, boolean clickEnabled, ClickEventAction clickAction, String clickValue, boolean replaceEnabled, String replaceText, Component component, Optional<ICPlayer> optplayer, long unix) {
		String plain = PlainTextComponentSerializer.plainText().serialize(component);
		boolean contain = (casesensitive) ? (plain.contains(placeholder)) : (plain.toLowerCase().contains(placeholder.toLowerCase()));
		if (!InteractiveChat.cooldownbypass.get(unix).contains(placeholder) && contain) {
			if (optplayer.isPresent()) {
				ICPlayer player = optplayer.get();
				Long uc = universalCooldowns.get(player.getUniqueId());
				if (uc != null) {
					if (uc > unix) {
						return component;
					}
				}
				
				if (!placeholderCooldowns.containsKey(player.getUniqueId())) {
					placeholderCooldowns.put(player.getUniqueId(), new ConcurrentHashMap<String, Long>());
				}
				Map<String, Long> spmap = placeholderCooldowns.get(player.getUniqueId());
				if (spmap.containsKey(placeholder)) {
					if (spmap.get(placeholder) > unix) {
						if (!PlayerUtils.hasPermission(player.getUniqueId(), "interactivechat.cooldown.bypass", false, 5)) {
							return component;
						}
					}
				}
				spmap.put(placeholder, unix + cooldown);
				InteractiveChat.universalCooldowns.put(player.getUniqueId(), unix + InteractiveChat.universalCooldown);
			}
			InteractiveChat.cooldownbypass.get(unix).add(placeholder);
			InteractiveChat.cooldownbypass.put(unix, InteractiveChat.cooldownbypass.get(unix));
		}
		
		return processCustomPlaceholderWithoutCooldown(parseplayer, casesensitive, placeholder, cooldown, hoverEnabled, hoverText, clickEnabled, clickAction, clickValue, replaceEnabled, replaceText, component, optplayer, unix);
	}
	
	public static Component processCustomPlaceholderWithoutCooldown(ICPlayer player, boolean casesensitive, String placeholder, long cooldown, boolean hoverEnabled, String hoverText, boolean clickEnabled, ClickEventAction clickAction, String clickValue, boolean replaceEnabled, String replaceText, Component component, Optional<ICPlayer> optplayer, long unix) {
		String regex = casesensitive ? CustomStringUtils.escapeMetaCharacters(placeholder) : "(?i)" + CustomStringUtils.escapeMetaCharacters(placeholder);
		String componentText = placeholder;
		if (replaceEnabled) {
			componentText = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, replaceText));
		}
		
		Component placeholderComponent = LegacyComponentSerializer.legacySection().deserialize(componentText);
		if (hoverEnabled) {
			placeholderComponent = placeholderComponent.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, hoverText)))));
		}
		if (clickEnabled) {
			String clicktext = PlaceholderParser.parse(player, clickValue);
			placeholderComponent = placeholderComponent.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.valueOf(clickAction.name()), clicktext));
		}
		
		return ComponentReplacing.replace(component, regex, true, placeholderComponent);
	}

}
