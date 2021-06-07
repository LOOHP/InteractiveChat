package com.loohp.interactivechat.objectholders;

import java.util.UUID;

import net.kyori.adventure.text.Component;

public class ProcessSenderResult {
	
	private Component component;
	private UUID sender;
	
	public ProcessSenderResult(Component component, UUID sender) {
		this.component = component;
		this.sender = sender;
	}
	
	public Component getComponent() {
		return component;
	}
	
	public UUID getSender() {
		return sender;
	}
}
