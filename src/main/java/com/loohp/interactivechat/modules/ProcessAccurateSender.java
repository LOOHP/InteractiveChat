package com.loohp.interactivechat.modules;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.loohp.interactivechat.objectholders.ProcessSenderResult;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;

public class ProcessAccurateSender {
	
	private static final Pattern PATTERN = Pattern.compile("(?:<chat=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})>)");
	
	public static ProcessSenderResult process(Component component) {
		String text = PlainComponentSerializer.plain().serialize(component);
		UUID uuid = find(text);
		component = component.replaceText(TextReplacementConfig.builder().match(PATTERN).replacement("").build());
		return new ProcessSenderResult(component, uuid);
	}
	
	public static UUID find(String text) {
		UUID uuid = null;
		Matcher matcher = PATTERN.matcher(text);
		if (matcher.find()) {
			uuid = UUID.fromString(matcher.group(1));
		}
		return uuid;
	}
	
}
