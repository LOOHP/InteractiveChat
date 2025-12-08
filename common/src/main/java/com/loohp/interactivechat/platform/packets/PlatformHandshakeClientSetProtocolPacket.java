package com.loohp.interactivechat.platform.packets;

public abstract class PlatformHandshakeClientSetProtocolPacket<Packet> extends PlatformPacket<Packet> {

    public PlatformHandshakeClientSetProtocolPacket(Packet handle) {
        super(handle);
    }

    @Override
    public abstract PlatformHandshakeClientSetProtocolPacket<Packet> shallowClone();

    public abstract String getServerAddress();
}
