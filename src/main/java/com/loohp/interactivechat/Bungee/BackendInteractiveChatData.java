package com.loohp.interactivechat.Bungee;

import com.loohp.interactivechat.Utils.MCVersion;

public class BackendInteractiveChatData {

	private String server;
	private boolean isOnline;
	
	private boolean hasInteractiveChat;
	private String version;
	private int ping;
	private MCVersion minecraftVersion;
	private String exactMinecraftVersion;
	private int protocol;
	
	protected BackendInteractiveChatData(String server, boolean isOnline, boolean hasInteractiveChat, String version, MCVersion minecraftVersion, String exactMinecraftVersion, int ping, int protocol) {
		this.server = server;
		this.hasInteractiveChat = hasInteractiveChat;
		this.version = version;
		this.minecraftVersion = minecraftVersion;
		this.exactMinecraftVersion = exactMinecraftVersion;
		this.ping = ping;
		this.protocol = protocol;
		this.isOnline = isOnline;
	}
	
	public String getServer() {
		return server;
	}
	
	public boolean isOnline() {
		return isOnline;
	}
	
	protected void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
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
	
	public int getProtocolVersion() {
		return protocol;
	}
	
	protected void setProtocolVersion(int protocol) {
		this.protocol = protocol;
	}

}
