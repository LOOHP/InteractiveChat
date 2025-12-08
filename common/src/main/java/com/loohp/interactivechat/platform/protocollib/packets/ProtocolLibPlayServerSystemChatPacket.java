package com.loohp.interactivechat.platform.protocollib.packets;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerSystemChatPacket;

public class ProtocolLibPlayServerSystemChatPacket extends PlatformPlayServerSystemChatPacket<PacketContainer> {

    public ProtocolLibPlayServerSystemChatPacket(PacketContainer handle) {
        super(handle);
    }

    @Override
    public ProtocolLibPlayServerSystemChatPacket shallowClone() {
        return new ProtocolLibPlayServerSystemChatPacket(handle.shallowClone());
    }

}
