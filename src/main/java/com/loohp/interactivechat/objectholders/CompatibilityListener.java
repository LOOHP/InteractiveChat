package com.loohp.interactivechat.objectholders;

import org.bukkit.event.EventPriority;

public class CompatibilityListener {
	
	private String plugin;
	private String clazz;
	private EventPriority priority;
	
	public CompatibilityListener(String plugin, String clazz, EventPriority priority) {
		this.plugin = plugin;
		this.clazz = clazz;
		this.priority = priority;
	}

	public String getPluginRegex() {
		return plugin;
	}

	public String getClassName() {
		return clazz;
	}

	public EventPriority getPriority() {
		return priority;
	}

}
