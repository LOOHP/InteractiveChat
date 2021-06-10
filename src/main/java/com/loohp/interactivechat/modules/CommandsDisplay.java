package com.loohp.interactivechat.modules;

import java.util.Set;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.CustomStringUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class CommandsDisplay {
	
	private static final String COMMAND_MATCHING_PATTERN = "(\\/(?:[^\\\\%s]|\\\\[\\\\%s])*)";
	private static final String ESCAPING_PATTERN = "\\\\([\\\\%s])";
	
	public static Component process(Component component) {
		Set<Character> chars = CustomStringUtils.getCharacterSet(InteractiveChat.clickableCommandsFormat.replace("{Command}", ""));
		StringBuilder sb = new StringBuilder();
		for (Character c : chars) {
			sb.append(CustomStringUtils.escapeMetaCharacters(c.toString()));
		}
		String escapeChars = sb.toString();
		return ComponentReplacing.replace(component, CustomStringUtils.escapeMetaCharacters(InteractiveChat.clickableCommandsFormat.replace("{Command}", "\0\0\0")).replace("\0\0\0", COMMAND_MATCHING_PATTERN.replace("%s", escapeChars)), true, groups -> {
			String command = groups[groups.length - 1].replaceAll(ESCAPING_PATTERN.replace("%s", escapeChars), "$1");
			Component commandComponent = LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.clickableCommandsDisplay.replace("{Command}", command));
			commandComponent = commandComponent.clickEvent(ClickEvent.clickEvent(InteractiveChat.clickableCommandsAction, command));
			if (!InteractiveChat.clickableCommandsHoverText.isEmpty()) {
				commandComponent = commandComponent.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.clickableCommandsHoverText)));
			}
			return commandComponent;
		});
	}

}
