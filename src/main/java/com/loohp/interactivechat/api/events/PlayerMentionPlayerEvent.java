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
 * null if you did't set a sound in the config. Set the Sound to null if you do
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
    private boolean cancel;

    public PlayerMentionPlayerEvent(boolean async, Player reciever, UUID sender, String title, String subtitle, String actionbar, String toast, Optional<BossBar> bossbar, Sound sound, boolean cancel) {
        super(async);
        this.reciever = reciever;
        this.sender = sender;
        this.title = title;
        this.subtitle = subtitle;
        this.actionbar = actionbar;
        this.toast = toast;
        this.bossbar = bossbar;
        this.sound = sound;
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

    public Player getReciver() {
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

    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
