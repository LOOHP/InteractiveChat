package com.loohp.interactivechat.hooks.discordsrv;

import com.loohp.interactivechat.registry.Registry;

import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.TextReplacementConfig;

public class DiscordSRVEvents {

	@Subscribe(priority = ListenerPriority.LOWEST)
	public void onGameToDiscord(GameChatMessagePreProcessEvent event) {
		event.setMessageComponent(event.getMessageComponent().replaceText(TextReplacementConfig.builder().match(Registry.ID_PATTERN).replacement("").build()));
	}

}
