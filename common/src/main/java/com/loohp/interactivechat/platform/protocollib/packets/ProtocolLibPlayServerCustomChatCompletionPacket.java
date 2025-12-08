package com.loohp.interactivechat.platform.protocollib.packets;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerCustomChatCompletionPacket;

public class ProtocolLibPlayServerCustomChatCompletionPacket extends PlatformPlayServerCustomChatCompletionPacket<PacketContainer> {

    public ProtocolLibPlayServerCustomChatCompletionPacket(PacketContainer handle) {
        super(handle);
    }

    @Override
    public ProtocolLibPlayServerCustomChatCompletionPacket shallowClone() {
        return new ProtocolLibPlayServerCustomChatCompletionPacket(handle.shallowClone());
    }

}
