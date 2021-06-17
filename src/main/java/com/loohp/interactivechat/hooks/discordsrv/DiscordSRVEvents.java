package com.loohp.interactivechat.hooks.discordsrv;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentFont;

import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.TextReplacementConfig;

public class DiscordSRVEvents {

	@Subscribe(priority = ListenerPriority.LOWEST)
	public void onGameToDiscord(GameChatMessagePreProcessEvent event) {
		Component component = event.getMessageComponent().replaceText(TextReplacementConfig.builder().match(Registry.ID_PATTERN).replacement("").build()).replaceText(TextReplacementConfig.builder().match(ChatColorUtils.COLOR_TAG_PATTERN).replacement((result, builder) -> {
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
		event.setMessageComponent(component);
	}

}
