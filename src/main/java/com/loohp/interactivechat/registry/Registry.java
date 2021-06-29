package com.loohp.interactivechat.registry;

import java.util.regex.Pattern;

public class Registry {
	
	public static final String PLUGIN_MESSAGING_PROTOCOL_IDENTIFIER = "InterativeChatBungeePing";
	public static final int PLUGIN_MESSAGING_PROTOCOL_VERSION = 5;
	
	public static final int INTERACTIVE_CHAT_DISCORD_SRV_ADDON_COMPATIBLE_VERSION = 4;
	
	public static final String CANCELLED_IDENTIFIER = "<Event-Cancelled-eda06945-39b7-4235-95bc-4cf38e063de4>";
	public static final Pattern ID_PATTERN = Pattern.compile("(?:<(cmd|chat)=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})(:(.*))?>)");
	
	public static final int MINECRAFT_1_16_PROTOCOL_VERSION = 735;
	
}
