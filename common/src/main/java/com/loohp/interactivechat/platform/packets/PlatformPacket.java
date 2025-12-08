package com.loohp.interactivechat.platform.packets;

public abstract class PlatformPacket<Packet> {

    protected final Packet handle;

    public PlatformPacket(Packet handle) {
        this.handle = handle;
    }

    public Packet getHandle() {
        return handle;
    }

    public abstract PlatformPacket<Packet> shallowClone();

}
