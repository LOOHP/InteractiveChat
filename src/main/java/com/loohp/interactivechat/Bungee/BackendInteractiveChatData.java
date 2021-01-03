package com.loohp.interactivechat.Bungee;

public class BackendInteractiveChatData {

	private String server;
	private boolean hasInteractiveChat;
	private String version;
	private int ping;
	
	protected BackendInteractiveChatData(String server, boolean hasInteractiveChat, String version, int ping) {
		this.server = server;
		this.hasInteractiveChat = hasInteractiveChat;
		this.version = version;
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

}
