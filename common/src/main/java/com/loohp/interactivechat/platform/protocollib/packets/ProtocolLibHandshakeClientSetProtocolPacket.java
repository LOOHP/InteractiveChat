package com.loohp.interactivechat.platform.protocollib.packets;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.platform.packets.PlatformHandshakeClientSetProtocolPacket;

public class ProtocolLibHandshakeClientSetProtocolPacket extends PlatformHandshakeClientSetProtocolPacket<PacketContainer> {

    public ProtocolLibHandshakeClientSetProtocolPacket(PacketContainer handle) {
        super(handle);
    }

    @Override
    public ProtocolLibHandshakeClientSetProtocolPacket shallowClone() {
        return new ProtocolLibHandshakeClientSetProtocolPacket(handle.shallowClone());
    }

    @Override
    public String getServerAddress() {
        return handle.getStrings().read(0);
    }
}
