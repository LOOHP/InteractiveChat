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

package com.loohp.interactivechat.api.events;

import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Optional;
import java.util.UUID;

/**
 * This event is called before the plugin plays the title and sound to the
 * player who is mentioned. The sound will be
 * null if you didn't set a sound in the config. Set the Sound to null if you do
 * not want to play any sound.
 *
 * @author LOOHP
 */
public class PlayerMentionPlayerEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    private final Player reciever;
    private final UUID sender;
    private String title;
    private String subtitle;
    private String actionbar;
    private String toast;
    private Optional<BossBar> bossbar;
    private Sound sound;
    private boolean silent;
    private boolean cancel;

    public PlayerMentionPlayerEvent(boolean async, Player receiver, UUID sender, String title, String subtitle, String actionbar, String toast, Optional<BossBar> bossbar, Sound sound, boolean silent, boolean cancel) {
        super(async);
        this.reciever = receiver;
        this.sender = sender;
        this.title = title;
        this.subtitle = subtitle;
        this.actionbar = actionbar;
        this.toast = toast;
        this.bossbar = bossbar;
        this.sound = sound;
        this.silent = silent;
        this.cancel = cancel;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public Player getReceiver() {
        return reciever;
    }

    public UUID getSender() {
        return sender;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getActionbar() {
        return actionbar;
    }

    public void setActionbar(String actionbar) {
        this.actionbar = actionbar;
    }

    public String getToast() {
        return toast;
    }

    public void setToast(String toast) {
        this.toast = toast;
    }

    public Optional<BossBar> getBossBar() {
        return bossbar;
    }

    public void setBossBar(Optional<BossBar> bossbar) {
        this.bossbar = bossbar;
    }

    public Sound getMentionSound() {
        return sound;
    }

    public void setMentionSound(Sound sound) {
        this.sound = sound;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
