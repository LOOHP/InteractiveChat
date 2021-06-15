package com.loohp.interactivechat.modules;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.loohp.interactivechat.objectholders.ProcessSenderResult;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ProcessCommands {
	
	private static final Pattern PATTERN_0 = Pattern.compile("(?:<cmd=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}):(.*)>)");
	private static final Pattern PATTERN_1 = Pattern.compile("(?:<cmd=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})>)");
	
	public static ProcessSenderResult process(Component component) {
		UUID uuid = null;
		String replacement = "";
		String text = PlainTextComponentSerializer.plainText().serialize(component);
		Matcher matcher0 = PATTERN_0.matcher(text);
		if (matcher0.find()) {
			uuid = UUID.fromString(matcher0.group(1));
			replacement = matcher0.group(2);
		}
		component = component.replaceText(TextReplacementConfig.builder().match(PATTERN_0).replacement(replacement).build());
		if (uuid == null) {
			Matcher matcher1 = PATTERN_1.matcher(text);
			if (matcher1.find()) {
				uuid = UUID.fromString(matcher1.group(1));
			}
		}
		component = component.replaceText(TextReplacementConfig.builder().match(PATTERN_1).replacement("").build());
		return new ProcessSenderResult(component, uuid);
	}
	
}
