package com.loohp.interactivechat.platform.packets;

public abstract class PlatformPlayServerServerDataPacket<Packet> extends PlatformPacket<Packet> {

    public PlatformPlayServerServerDataPacket(Packet handle) {
        super(handle);
    }

    @Override
    public abstract PlatformPlayServerServerDataPacket<Packet> shallowClone();

    public abstract void setServerUnsignedStatus(boolean status);
}
