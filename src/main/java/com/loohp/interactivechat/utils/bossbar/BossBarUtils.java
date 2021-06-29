package com.loohp.interactivechat.utils.bossbar;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.loohp.interactivechat.InteractiveChat;

import net.kyori.adventure.bossbar.BossBar;

public class BossBarUtils {
	
	public static void countdownBossBar(BossBarUpdater updater, int ticks, int removeDelay) {
		new BukkitRunnable() {
			int tick = 0;
			@Override
			public void run() {
				tick++;
				BossBar bossbar = updater.getBossBar();
				float current = 1 - (float) tick / (float) ticks;
				bossbar.progress(Math.max(current, 0));
				if (current < 0) {
					this.cancel();
					Bukkit.getScheduler().runTaskLaterAsynchronously(InteractiveChat.plugin, () -> updater.close(), removeDelay);
				}
			}
		}.runTaskTimerAsynchronously(InteractiveChat.plugin, 0, 1);
	}

}
