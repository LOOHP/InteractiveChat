package com.loohp.interactivechat.platform.packets;

public abstract class PlatformConfigurationClientClientInformationPacket<Packet> extends PlatformPacket<Packet> {

    public PlatformConfigurationClientClientInformationPacket(Packet handle) {
        super(handle);
    }

    @Override
    public abstract PlatformConfigurationClientClientInformationPacket<Packet> shallowClone();

    public abstract boolean getColorSettings();
}
