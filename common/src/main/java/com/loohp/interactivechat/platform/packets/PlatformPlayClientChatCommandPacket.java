package com.loohp.interactivechat.platform.packets;

public abstract class PlatformPlayClientChatCommandPacket<Packet> extends PlatformPacket<Packet> {

    public PlatformPlayClientChatCommandPacket(Packet handle) {
        super(handle);
    }

    @Override
    public abstract PlatformPlayClientChatCommandPacket<Packet> shallowClone();

    public abstract boolean hasArgumentSignatureEntries();

    public abstract String getCommand();
}
