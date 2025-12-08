package com.loohp.interactivechat.platform;

import com.loohp.interactivechat.platform.packets.PlatformPacket;

@FunctionalInterface
public interface PlatformPacketEventListener<PacketEvent, Packet, PlatformPacketTyped extends PlatformPacket<Packet>> {

    void handle(PlatformPacketEvent<PacketEvent, Packet, PlatformPacketTyped> event);

}
