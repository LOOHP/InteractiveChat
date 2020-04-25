package com.loohp.interactivechat.ObjectHolders;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;

public class ProcessCommandsReturn {
	
	BaseComponent basecomponent;
	Player sender;
	
	public ProcessCommandsReturn(BaseComponent basecomponent, Player sender) {
		this.basecomponent = basecomponent;
		this.sender = sender;
	}
	
	public BaseComponent getBaseComponent() {
		return basecomponent;
	}
	
	public Player getSender() {
		return sender;
	}
}
