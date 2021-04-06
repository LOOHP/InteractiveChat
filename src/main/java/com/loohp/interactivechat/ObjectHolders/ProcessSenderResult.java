package com.loohp.interactivechat.objectholders;

import java.util.UUID;

import net.md_5.bungee.api.chat.BaseComponent;

public class ProcessSenderResult {
	
	private BaseComponent basecomponent;
	private UUID sender;
	
	public ProcessSenderResult(BaseComponent basecomponent, UUID sender) {
		this.basecomponent = basecomponent;
		this.sender = sender;
	}
	
	public BaseComponent getBaseComponent() {
		return basecomponent;
	}
	
	public UUID getSender() {
		return sender;
	}
}
