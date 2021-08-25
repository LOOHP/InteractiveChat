package com.loohp.interactivechat.hooks.luckperms;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.PlayerUtils;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.user.UserDataRecalculateEvent;

public class LuckPermsEvents {
	
	private Set<UUID> scheduledReset;
	
	public LuckPermsEvents(InteractiveChat plugin) {
		scheduledReset = Collections.synchronizedSet(new HashSet<>());
		LuckPermsProvider.get().getEventBus().subscribe(plugin, UserDataRecalculateEvent.class, event -> {
			UUID uuid = event.getUser().getUniqueId();
			if (!scheduledReset.contains(uuid)) {
				scheduledReset.add(uuid);
				Bukkit.getScheduler().runTaskLater(plugin, () -> {
					PlayerUtils.resetPermissionCache(uuid);
					scheduledReset.remove(uuid);
				}, 1);
			}
		});
	}

}
