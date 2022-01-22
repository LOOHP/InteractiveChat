package com.loohp.interactivechat.objectholders;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

public class OutboundPacket {

    private final Player reciever;
    private final PacketContainer packet;

    public OutboundPacket(Player reciever, PacketContainer packet) {
        this.reciever = reciever;
        this.packet = packet;
    }

    public Player getReciever() {
        return reciever;
    }

    public PacketContainer getPacket() {
        return packet;
    }

}
