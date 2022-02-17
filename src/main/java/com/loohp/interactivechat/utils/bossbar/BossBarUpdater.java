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
import com.loohp.interactivechat.utils.MCVersion;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Flag;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Set;

public abstract class BossBarUpdater implements BossBar.Listener, AutoCloseable {

    public static BossBarUpdater update(BossBar bossbar, Player... players) {
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
            return new BossBarUpdaterV1_16Impl(bossbar, players);
        } else if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_9)) {
            return new BossBarUpdaterV1_9Impl(bossbar, players);
        } else {
            throw new UnsupportedOperationException("Bossbars are not supported on this version of Minecraft.");
        }
    }
    protected BossBar bossbar;
    protected Player[] players;
    protected boolean isValid;

    public BossBarUpdater(BossBar bossbar, Player... players) {
        bossbar.addListener(this);
        this.bossbar = bossbar;
        this.players = players;
        this.isValid = true;
    }

    @Override
    public synchronized void close() {
        bossbar.removeListener(this);
        isValid = false;
    }

    public boolean isValid() {
        return isValid;
    }

    public BossBar getBossBar() {
        return bossbar;
    }

    @Override
    public abstract void bossBarNameChanged(BossBar bar, Component oldName, Component newName);

    @Override
    public abstract void bossBarProgressChanged(BossBar bar, float oldProgress, float newProgress);

    @Override
    public abstract void bossBarColorChanged(BossBar bar, Color oldColor, Color newColor);

    @Override
    public abstract void bossBarOverlayChanged(BossBar bar, Overlay oldOverlay, Overlay newOverlay);

    @Override
    public abstract void bossBarFlagsChanged(BossBar bar, Set<Flag> flagsAdded, Set<Flag> flagsRemoved);

}
