package com.loohp.interactivechat.modules;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.CustomStringUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class CommandsDisplay {
	
	private static final String PATTERN = "(\\/[^\\\\\\[\\]]*)";
	
	public static Component process(Component component) {
		return ComponentReplacing.replace(component, CustomStringUtils.escapeMetaCharacters(InteractiveChat.clickableCommandsFormat.replace("{Command}", "PATTERN")).replace("PATTERN", PATTERN), true, groups -> {
			String command = groups[groups.length - 1];
			Component commandComponent = LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.clickableCommandsDisplay.replace("{Command}", command));
			commandComponent = commandComponent.clickEvent(ClickEvent.clickEvent(InteractiveChat.clickableCommandsAction, command));
			if (!InteractiveChat.clickableCommandsHoverText.isEmpty()) {
				commandComponent = commandComponent.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.clickableCommandsHoverText)));
			}
			return commandComponent;
		});
	}

}
