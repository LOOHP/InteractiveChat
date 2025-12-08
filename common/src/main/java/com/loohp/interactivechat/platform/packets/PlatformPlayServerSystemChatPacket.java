package com.loohp.interactivechat.platform.packets;

public abstract class PlatformPlayServerSystemChatPacket<Packet> extends PlatformPacket<Packet> {

    public PlatformPlayServerSystemChatPacket(Packet handle) {
        super(handle);
    }

    @Override
    public abstract PlatformPlayServerSystemChatPacket<Packet> shallowClone();

}
