package com.loohp.interactivechat.api.events;

import com.loohp.interactivechat.objectholders.ICPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This is the base class of all events related to parsing placeholders.
 *
 * @author LOOHP
 */
public class PlaceholderEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
    protected final ICPlayer sender;
    protected final Player receiver;
    protected final long timeSent;
    protected Component component;
    protected boolean isCancelled;

    public PlaceholderEvent(ICPlayer sender, Player receiver, Component component, long timeSent) {
        super(!Bukkit.isPrimaryThread());
        this.sender = sender;
        this.receiver = receiver;
        this.component = component;
        this.timeSent = timeSent;
        this.isCancelled = false;
    }

    public PlaceholderEvent(Player receiver, Component component, long timeSent) {
        this(null, receiver, component, timeSent);
    }

    public boolean hasSender() {
        return sender != null;
    }

    public ICPlayer getSender() {
        return sender;
    }

    public Player getReceiver() {
        return receiver;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public long getTimeSent() {
        return timeSent;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
