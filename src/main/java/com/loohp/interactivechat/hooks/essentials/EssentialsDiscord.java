package com.loohp.interactivechat.hooks.essentials;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentFont;
import net.essentialsx.api.v2.events.discord.DiscordChatMessageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class EssentialsDiscord implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDiscordChatMessage(DiscordChatMessageEvent event) {
        Component component = LegacyComponentSerializer.legacySection().deserialize(event.getMessage());
        component = component.replaceText(TextReplacementConfig.builder().match(Registry.ID_PATTERN).replacement("").build()).replaceText(TextReplacementConfig.builder().match(ChatColorUtils.COLOR_TAG_PATTERN).replacement((result, builder) -> {
            String escape = result.group(1);
            String replacement = escape == null ? "" : escape;
            return builder.content(replacement);
        }).build());
        if (InteractiveChat.fontTags) {
            component = component.replaceText(TextReplacementConfig.builder().match(ComponentFont.FONT_TAG_PATTERN).replacement((result, builder) -> {
                String escape = result.group(1);
                String replacement = escape == null ? "" : escape;
                return builder.content(replacement);
            }).build());
        }
        event.setMessage(LegacyComponentSerializer.legacySection().serialize(component));
    }

}
