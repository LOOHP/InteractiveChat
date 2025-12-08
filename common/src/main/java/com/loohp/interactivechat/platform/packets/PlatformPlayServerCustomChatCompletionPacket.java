package com.loohp.interactivechat.platform.packets;

public abstract class PlatformPlayServerCustomChatCompletionPacket<Packet> extends PlatformPacket<Packet> {

    public PlatformPlayServerCustomChatCompletionPacket(Packet handle) {
        super(handle);
    }

    @Override
    public abstract PlatformPlayServerCustomChatCompletionPacket<Packet> shallowClone();

}
