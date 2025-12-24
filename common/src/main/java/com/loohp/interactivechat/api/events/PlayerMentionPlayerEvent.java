/*
 * This file is part of InteractiveChat4.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
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

import com.loohp.interactivechat.objectholders.Either;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
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

    private final Player receiver;
    private final UUID sender;
    private Component title;
    private Component subtitle;
    private Component actionbar;
    private Component toast;
    private Optional<BossBar> bossbar;
    private Either<Sound, String> sound;
    private boolean silent;
    private boolean cancel;

    public PlayerMentionPlayerEvent(boolean async, Player receiver, UUID sender, Component title, Component subtitle, Component actionbar, Component toast, Optional<BossBar> bossbar, Either<Sound, String> sound, boolean silent, boolean cancel) {
        super(async);
        this.receiver = receiver;
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
        return receiver;
    }

    public UUID getSender() {
        return sender;
    }

    public Component getTitle() {
        return title;
    }

    public void setTitle(Component title) {
        this.title = title;
    }

    public Component getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(Component subtitle) {
        this.subtitle = subtitle;
    }

    public Component getActionbar() {
        return actionbar;
    }

    public void setActionbar(Component actionbar) {
        this.actionbar = actionbar;
    }

    public Component getToast() {
        return toast;
    }

    public void setToast(Component toast) {
        this.toast = toast;
    }

    public Optional<BossBar> getBossBar() {
        return bossbar;
    }

    public void setBossBar(Optional<BossBar> bossbar) {
        this.bossbar = bossbar;
    }

    public Either<Sound, String> getMentionSound() {
        return sound;
    }

    public void setMentionSound(Either<Sound, String> sound) {
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
