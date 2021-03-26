package com.loohp.interactivechat.ObjectHolders;

public class SharedDisplayTimeoutInfo {
	
	private String hash;
	private int type;
	private long timeout;
	
	public SharedDisplayTimeoutInfo(String hash, int type, long timeout) {
		this.hash = hash;
		this.type = type;
		this.timeout = timeout;
	}

	public String getHash() {
		return hash;
	}

	public int getType() {
		return type;
	}

	public long getTimeout() {
		return timeout;
	}

}
