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
	
	private static final String PATTERN_PREFIX = "(?i)(?:(?<!\\\\)(\\\\)\\\\|(?<!\\\\))";
	private static final String PATTERN_COMMAND = "(/(?:[^\\\\%s]|(?:\\\\[\\\\%s]))*)";
	private static final String ESCAPING_PATTERN = "\\\\([\\\\%s])";
	
	private static final String ESCAPE_CLEARUP_PREFIX = "(?i)\\\\(";
	private static final String ESCAPE_CLEARUP_COMMAND = "/(?:[^\\\\%s]|(?:\\\\[\\\\%s]))*";
	private static final String ESCAPE_CLEARUP_SUFFIX = ")";
	
	private static final String JOINT_PATTERN = "(%c)|(%e)";
	
	public static Component process(Component component) {
		Set<Character> chars = CustomStringUtils.getCharacterSet(InteractiveChat.clickableCommandsFormat.replace("{Command}", ""));
		StringBuilder sb = new StringBuilder();
		for (Character c : chars) {
			sb.append(CustomStringUtils.escapeMetaCharacters(c.toString()));
		}
		String escapeChars = sb.toString();
		String commandMatchingPattern = PATTERN_PREFIX + CustomStringUtils.escapeMetaCharacters(InteractiveChat.clickableCommandsFormat.replace("{Command}", "\0\0\0")).replace("\0\0\0", PATTERN_COMMAND.replace("%s", escapeChars));
		String escapeMatchingPattern = ESCAPE_CLEARUP_PREFIX + CustomStringUtils.escapeMetaCharacters(InteractiveChat.clickableCommandsFormat.replace("{Command}", "\0\0\0")).replace("\0\0\0", ESCAPE_CLEARUP_COMMAND.replace("%s", escapeChars)) + ESCAPE_CLEARUP_SUFFIX;
		
		String pattern = JOINT_PATTERN.replace("%c", commandMatchingPattern).replace("%e", escapeMatchingPattern);
		
		return ComponentReplacing.replace(component, pattern, result -> {
			if (result.group(1) != null) {
				String escape = result.group(2);
				String command = result.group(3).replaceAll(ESCAPING_PATTERN.replace("%s", escapeChars), "$1");
				String componentText = InteractiveChat.clickableCommandsDisplay.replace("{Command}", command);
				if (escape != null) {
					componentText = escape + componentText;
				}
				Component commandComponent = LegacyComponentSerializer.legacySection().deserialize(componentText);
				commandComponent = commandComponent.clickEvent(ClickEvent.clickEvent(InteractiveChat.clickableCommandsAction, command));
				if (!InteractiveChat.clickableCommandsHoverText.isEmpty()) {
					commandComponent = commandComponent.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.clickableCommandsHoverText)));
				}
				return commandComponent;
			} else if (result.group(4) != null) {
				String escaped = result.group(5);
				return Component.text(escaped);
			} else {
				return Component.text(result.group());
			}
		});
	}

}
