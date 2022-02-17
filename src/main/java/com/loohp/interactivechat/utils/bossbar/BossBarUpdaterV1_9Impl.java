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

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Flag;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import java.util.Set;

public class BossBarUpdaterV1_9Impl extends BossBarUpdater {

    private static BarColor toBukkit(BossBar.Color color) {
        switch (color) {
            case BLUE:
                return BarColor.BLUE;
            case GREEN:
                return BarColor.GREEN;
            case PINK:
                return BarColor.PINK;
            case RED:
                return BarColor.RED;
            case WHITE:
                return BarColor.WHITE;
            case YELLOW:
                return BarColor.YELLOW;
            case PURPLE:
            default:
                return BarColor.PURPLE;
        }
    }

    private static BarStyle toBukkit(BossBar.Overlay overlay) {
        switch (overlay) {
            case NOTCHED_10:
                return BarStyle.SEGMENTED_10;
            case NOTCHED_12:
                return BarStyle.SEGMENTED_12;
            case NOTCHED_20:
                return BarStyle.SEGMENTED_20;
            case NOTCHED_6:
                return BarStyle.SEGMENTED_6;
            case PROGRESS:
            default:
                return BarStyle.SOLID;
        }
    }

    private static BarFlag toBukkit(BossBar.Flag flag) {
        switch (flag) {
            case CREATE_WORLD_FOG:
                return BarFlag.CREATE_FOG;
            case DARKEN_SCREEN:
                return BarFlag.DARKEN_SKY;
            case PLAY_BOSS_MUSIC:
            default:
                return BarFlag.PLAY_BOSS_MUSIC;
        }
    }
    protected org.bukkit.boss.BossBar bukkitBossbar;

    public BossBarUpdaterV1_9Impl(BossBar bossbar, Player[] players) {
        super(bossbar, players);
        BarFlag[] flags = bossbar.flags().stream().map(each -> toBukkit(each)).toArray(size -> new BarFlag[size]);
        bukkitBossbar = Bukkit.createBossBar(LegacyComponentSerializer.legacySection().serialize(bossbar.name()), toBukkit(bossbar.color()), toBukkit(bossbar.overlay()), flags);
        for (Player player : players) {
            bukkitBossbar.addPlayer(player);
        }
    }

    @Override
    public synchronized void close() {
        bukkitBossbar.removeAll();
        super.close();
    }

    @Override
    public void bossBarNameChanged(BossBar bar, Component oldName, Component newName) {
        bukkitBossbar.setTitle(LegacyComponentSerializer.legacySection().serialize(newName));
    }

    @Override
    public void bossBarProgressChanged(BossBar bar, float oldProgress, float newProgress) {
        bukkitBossbar.setProgress(newProgress);
    }

    @Override
    public void bossBarColorChanged(BossBar bar, Color oldColor, Color newColor) {
        bukkitBossbar.setColor(toBukkit(newColor));
    }

    @Override
    public void bossBarOverlayChanged(BossBar bar, Overlay oldOverlay, Overlay newOverlay) {
        bukkitBossbar.setStyle(toBukkit(newOverlay));
    }

    @Override
    public void bossBarFlagsChanged(BossBar bar, Set<Flag> flagsAdded, Set<Flag> flagsRemoved) {
        for (Flag flag : flagsRemoved) {
            bukkitBossbar.removeFlag(toBukkit(flag));
        }
        for (Flag flag : flagsAdded) {
            bukkitBossbar.addFlag(toBukkit(flag));
        }
    }

}
