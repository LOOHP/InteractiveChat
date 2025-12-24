package com.loohp.interactivechat.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import static com.loohp.interactivechat.utils.CustomStringUtils.*;

public class ComponentUtils {

    public static boolean isEmpty(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component).isEmpty();
    }

    public static boolean contains(Component component, CharSequence text) {
        return PlainTextComponentSerializer.plainText().serialize(component).contains(text);
    }

    public static boolean contains(String source, Component component) {
        return source.contains(PlainTextComponentSerializer.plainText().serialize(component));
    }

    public static Component applyReplacementRegex(Component component, ComponentReplacing.ComponentMatchResult result, int groupOffset) {
        component = ComponentReplacing.replace(component, REPLACE_REGEX.replace("%s", "0"), escapeReplaceAllMetaCharacters(result.componentGroup()));
        for (int i = groupOffset + 1; i <= result.groupCount(); i++) {
            Component replacement = result.componentGroup(i);
            if (replacement == null) {
                replacement = Component.empty();
            }
            component = ComponentReplacing.replace(component, REPLACE_REGEX.replace("%s", String.valueOf(i - groupOffset)), escapeReplaceAllMetaCharacters(replacement));
        }
        return ComponentReplacing.replace(component, REPLACE_ESCAPE_REGEX, Component.text("\\$"));
    }

    public static Component escapeReplaceAllMetaCharacters(Component component) {
        for (String metaCharacter : REPLACE_ALL_META_CHARACTERS) {
            component = component.replaceText(TextReplacementConfig.builder().matchLiteral(metaCharacter).replacement(Component.text("\\" + metaCharacter)).build());
        }
        return component;
    }

    public static Component escapeMetaCharacters(Component component) {
        for (String metaCharacter : META_CHARACTERS) {
            component = component.replaceText(TextReplacementConfig.builder().matchLiteral(metaCharacter).replacement(Component.text("\\" + metaCharacter)).build());
        }
        return component;
    }

}
