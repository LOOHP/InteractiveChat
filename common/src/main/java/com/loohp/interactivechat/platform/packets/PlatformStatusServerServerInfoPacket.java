package com.loohp.interactivechat.platform.packets;

public abstract class PlatformStatusServerServerInfoPacket<Packet> extends PlatformPacket<Packet> {

    public PlatformStatusServerServerInfoPacket(Packet handle) {
        super(handle);
    }

    @Override
    public abstract PlatformStatusServerServerInfoPacket<Packet> shallowClone();

    public abstract String getMotd();

    public abstract void setMotd(String message);
}
