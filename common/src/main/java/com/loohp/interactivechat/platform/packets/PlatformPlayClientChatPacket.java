package com.loohp.interactivechat.platform.packets;

public abstract class PlatformPlayClientChatPacket<Packet> extends PlatformPacket<Packet> {

    public PlatformPlayClientChatPacket(Packet handle) {
        super(handle);
    }

    @Override
    public abstract PlatformPlayClientChatPacket<Packet> shallowClone();

    public abstract String getMessage();
}
