package com.loohp.interactivechat.modules;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.loohp.interactivechat.objectholders.ProcessSenderResult;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentReplacing;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ProcessCommands {
	
	public static final Pattern PATTERN_0 = Pattern.compile("(?:<cmd=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}):(.*)>)");
	public static final Pattern PATTERN_1 = Pattern.compile("(?:<cmd=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})>)");
	
	public static final Pattern COLOR_IGNORE_PATTERN_0 = Pattern.compile("(?:(?:§.)*<(?:§.)*c(?:§.)*m(?:§.)*d(?:§.)*=((?:(?:§.)*[0-9a-f]){8}(?:§.)*-(?:(?:§.)*[0-9a-f]){4}(?:§.)*-(?:(?:§.)*[0-9a-f]){4}(?:§.)*-(?:(?:§.)*[0-9a-f]){4}(?:§.)*-(?:(?:§.)*[0-9a-f]){12})(?:§.)*>)");
	public static final Pattern COLOR_IGNORE_PATTERN_1 = Pattern.compile("(?:(?:§.)*<(?:§.)*c(?:§.)*m(?:§.)*d(?:§.)*=((?:(?:§.)*[0-9a-f]){8}(?:§.)*-(?:(?:§.)*[0-9a-f]){4}(?:§.)*-(?:(?:§.)*[0-9a-f]){4}(?:§.)*-(?:(?:§.)*[0-9a-f]){4}(?:§.)*-(?:(?:§.)*[0-9a-f]){12}):(.*)(?:§.)*>)");
	
	public static ProcessSenderResult process(Component component) {
		UUID uuid = null;
		String replacement = "";
		String text = PlainTextComponentSerializer.plainText().serialize(component);
		Matcher matcher0 = PATTERN_0.matcher(text);
		if (matcher0.find()) {
			uuid = UUID.fromString(matcher0.group(1));
			replacement = ChatColorUtils.stripColor(matcher0.group(2));
		}
		component = ComponentReplacing.replace(component, PATTERN_0.pattern(), Component.text(replacement));
		if (uuid == null) {
			Matcher matcher1 = PATTERN_1.matcher(text);
			if (matcher1.find()) {
				uuid = UUID.fromString(matcher1.group(1));
			}
		}
		component = ComponentReplacing.replace(component, PATTERN_1.pattern(), Component.empty());
		return new ProcessSenderResult(component, uuid);
	}
	
}
