package com.loohp.interactivechat.objectholders;

import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketContainer;

public class OutboundPacket {
	
	private Player reciever;
	private PacketContainer packet;
	
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
