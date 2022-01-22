package com.loohp.interactivechat.api.events;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * This Event is called after the plugin had modified the
 * components of a chat packet. Sender will be null if the message is not send by a player or
 * the plugin is unable to find the sender of the message.
 *
 * @author LOOHP
 */
public class PostPacketComponentProcessEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player reciever;
    private Component component;
    private final UUID sender;

    public PostPacketComponentProcessEvent(boolean async, Player reciever, Component component, UUID sender) {
        super(async);
        this.reciever = reciever;
        this.component = component;
        this.sender = sender;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Player getReciver() {
        return reciever;
    }

    public UUID getSender() {
        return sender;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
