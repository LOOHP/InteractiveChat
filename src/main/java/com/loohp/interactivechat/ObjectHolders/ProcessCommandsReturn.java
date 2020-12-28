package com.loohp.interactivechat.ObjectHolders;

import java.util.UUID;

import net.md_5.bungee.api.chat.BaseComponent;

public class ProcessCommandsReturn {
	
	BaseComponent basecomponent;
	UUID sender;
	
	public ProcessCommandsReturn(BaseComponent basecomponent, UUID sender) {
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
