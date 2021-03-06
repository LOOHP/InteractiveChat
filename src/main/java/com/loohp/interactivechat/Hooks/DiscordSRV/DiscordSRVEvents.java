package com.loohp.interactivechat.Hooks.DiscordSRV;

import com.loohp.interactivechat.InteractiveChat;

import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;

public class DiscordSRVEvents {
	
	private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}";

	@Subscribe(priority = ListenerPriority.LOWEST)
	public void onGameToDiscord(GameChatMessagePreProcessEvent event) {
		if (InteractiveChat.useAccurateSenderFinder) {
			String message = event.getMessage();
			event.setMessage(message.replaceAll("<cmd=" + UUID_REGEX + ">", "").replaceAll("<chat=" + UUID_REGEX + ">", ""));
		}
	}

}
