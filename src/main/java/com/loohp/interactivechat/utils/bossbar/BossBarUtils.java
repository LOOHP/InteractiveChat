/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactivechat.utils.bossbar;

import com.loohp.interactivechat.InteractiveChat;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

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
