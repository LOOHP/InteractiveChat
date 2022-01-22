package com.loohp.interactivechat.modules;

import com.loohp.interactivechat.objectholders.ProcessSenderResult;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import net.kyori.adventure.text.Component;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessAccurateSender {

    public static final Pattern PATTERN = Pattern.compile("(?:<chat=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})>)");

    public static final Pattern COLOR_IGNORE_PATTERN = Pattern.compile("(?:(?:§.)*<(?:§.)*c(?:§.)*h(?:§.)*a(?:§.)*t(?:§.)*=((?:(?:§.)*[0-9a-f]){8}(?:§.)*-(?:(?:§.)*[0-9a-f]){4}(?:§.)*-(?:(?:§.)*[0-9a-f]){4}(?:§.)*-(?:(?:§.)*[0-9a-f]){4}(?:§.)*-(?:(?:§.)*[0-9a-f]){12})(?:§.)*>)");

    public static ProcessSenderResult process(Component component) {
        String text = InteractiveChatComponentSerializer.plainText().serialize(component);
        UUID uuid = find(text);
        component = ComponentReplacing.replace(component, PATTERN.pattern(), Component.empty());
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
