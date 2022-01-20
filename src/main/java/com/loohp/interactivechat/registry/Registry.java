package com.loohp.interactivechat.registry;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Registry {
	
	public static final String PLUGIN_MESSAGING_PROTOCOL_IDENTIFIER = "InterativeChatBungeePing";
	public static final int PLUGIN_MESSAGING_PROTOCOL_VERSION = 8;
	
	public static final int INTERACTIVE_CHAT_DISCORD_SRV_ADDON_COMPATIBLE_VERSION = 15;
	
	public static final String CANCELLED_IDENTIFIER = "<Event-Cancelled-eda06945-39b7-4235-95bc-4cf38e063de4>";
	public static final Pattern ID_PATTERN = Pattern.compile("(?:<(cmd|chat)=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})(:(.*))?>)");
	
	public static final int MINECRAFT_1_16_PROTOCOL_VERSION = 735;
	
	public static final Set<Integer> PROXY_PASSTHROUGH_RELAY_PACKETS;
	static {
		Set<Integer> set = new HashSet<>();
		set.add(0x00);
		set.add(0x01);
		set.add(0x02);
		set.add(0x03);
		set.add(0x04);
		set.add(0x05);
		set.add(0x06);
		set.add(0x0E);
		set.add(0x0F);
		set.add(0xFF);
		PROXY_PASSTHROUGH_RELAY_PACKETS = Collections.unmodifiableSet(set);
	}
	
}
