package com.loohp.interactivechat.api.events;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * This Event is called after the plugin had determinded the chat component in
 * the packet need modifying and before the plugin actually modifies the
 * components. Sender will be null if the message is not send by a player or
 * the plugin is unable to find the sender of the message.
 *
 * @author LOOHP
 */
public class PrePacketComponentProcessEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    private final Player reciever;
    private Component component;
    private UUID sender;

    public PrePacketComponentProcessEvent(boolean async, Player reciever, Component component, UUID sender) {
        super(async);
        this.reciever = reciever;
        this.component = component;
        this.sender = sender;
    }

    public Player getReciver() {
        return reciever;
    }

    public UUID getSender() {
        return sender;
    }

    public void setSender(UUID sender) {
        this.sender = sender;
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
