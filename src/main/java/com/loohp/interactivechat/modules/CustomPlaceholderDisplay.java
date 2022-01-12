package com.loohp.interactivechat.modules;

import java.util.Collection;
import java.util.Optional;

import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ClickEventAction;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ParsePlayer;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.objectholders.WebData;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.CustomStringUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class CustomPlaceholderDisplay {
	
	public static Component process(Component component, Optional<ICPlayer> optplayer, Player reciever, Collection<ICPlaceholder> placeholderList, long unix) {
		for (ICPlaceholder icplaceholder : placeholderList) {
			if (icplaceholder.isBuildIn()) {
				continue;
			}
			CustomPlaceholder cp = (CustomPlaceholder) icplaceholder;
			
			ICPlayer parseplayer = (cp.getParsePlayer().equals(ParsePlayer.SENDER) && optplayer.isPresent()) ? optplayer.get() : ICPlayerFactory.getICPlayer(reciever);
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
			
			component = processCustomPlaceholder(parseplayer, casesensitive, placeholder, cooldown, hoverEnabled, hoverText, clickEnabled, clickAction, clickValue, replaceEnabled, replaceText, component, optplayer, unix);
		}
		
		if (InteractiveChat.t && WebData.getInstance() != null) {
			for (CustomPlaceholder cp : WebData.getInstance().getSpecialPlaceholders()) {
				ICPlayer parseplayer = (cp.getParsePlayer().equals(ParsePlayer.SENDER) && optplayer.isPresent()) ? optplayer.get() : ICPlayerFactory.getICPlayer(reciever);
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
				
				component = processCustomPlaceholder(parseplayer, casesensitive, placeholder, cooldown, hoverEnabled, hoverText, clickEnabled, clickAction, clickValue, replaceEnabled, replaceText, component, optplayer, unix);
			}
		}
			
		return component;
	}
	
	public static Component processCustomPlaceholder(ICPlayer player, boolean casesensitive, String placeholder, long cooldown, boolean hoverEnabled, String hoverText, boolean clickEnabled, ClickEventAction clickAction, String clickValue, boolean replaceEnabled, String replaceText, Component component, Optional<ICPlayer> optplayer, long unix) {
		String plain = InteractiveChatComponentSerializer.plainText().serialize(component);
		if (casesensitive ? plain.contains(placeholder) : plain.toLowerCase().contains(placeholder.toLowerCase())) {
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
		} else {
			return component;
		}
	}

}
