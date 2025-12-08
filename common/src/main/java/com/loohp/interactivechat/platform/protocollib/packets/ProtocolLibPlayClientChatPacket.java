package com.loohp.interactivechat.platform.protocollib.packets;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.platform.packets.PlatformPlayClientChatPacket;

public class ProtocolLibPlayClientChatPacket extends PlatformPlayClientChatPacket<PacketContainer> {

    public ProtocolLibPlayClientChatPacket(PacketContainer handle) {
        super(handle);
    }

    @Override
    public ProtocolLibPlayClientChatPacket shallowClone() {
        return new ProtocolLibPlayClientChatPacket(handle.shallowClone());
    }

    @Override
    public String getMessage() {
        return handle.getStrings().read(0);
    }

}
