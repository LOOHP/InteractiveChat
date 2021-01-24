package com.loohp.interactivechat.Bungee;

import com.loohp.interactivechat.Utils.MCVersion;

public class BackendInteractiveChatData {

	private String server;
	private boolean hasInteractiveChat;
	private String version;
	private int ping;
	private MCVersion minecraftVersion;
	private String exactMinecraftVersion;
	
	protected BackendInteractiveChatData(String server, boolean hasInteractiveChat, String version, MCVersion minecraftVersion, String exactMinecraftVersion, int ping) {
		this.server = server;
		this.hasInteractiveChat = hasInteractiveChat;
		this.version = version;
		this.minecraftVersion = minecraftVersion;
		this.exactMinecraftVersion = exactMinecraftVersion;
		this.ping = ping;
	}
	
	public String getServer() {
		return server;
	}
	
	public boolean hasInteractiveChat() {
		return hasInteractiveChat;
	}
	
	protected void setInteractiveChat(boolean hasInteractiveChat) {
		this.hasInteractiveChat = hasInteractiveChat;
	}
	
	public String getVersion() {
		return version;
	}
	
	protected void setVersion(String version) {
		this.version = version;
	}
	
	public int getPing() {
		return ping;
	}
	
	protected void setPing(int ping) {
		this.ping = ping;
	}

	public MCVersion getMinecraftVersion() {
		return minecraftVersion;
	}

	protected void setMinecraftVersion(MCVersion minecraftVersion) {
		this.minecraftVersion = minecraftVersion;
	}

	public String getExactMinecraftVersion() {
		return exactMinecraftVersion;
	}

	protected void setExactMinecraftVersion(String exactMinecraftVersion) {
		this.exactMinecraftVersion = exactMinecraftVersion;
	}

}
