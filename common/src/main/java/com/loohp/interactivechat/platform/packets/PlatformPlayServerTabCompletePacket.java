package com.loohp.interactivechat.platform.packets;

import com.loohp.interactivechat.objectholders.CommandSuggestion;

public abstract class PlatformPlayServerTabCompletePacket<Packet> extends PlatformPacket<Packet> {

    public PlatformPlayServerTabCompletePacket(Packet handle) {
        super(handle);
    }

    @Override
    public abstract PlatformPlayServerTabCompletePacket<Packet> shallowClone();

    public abstract CommandSuggestion<?> getCommandSuggestions();

    public abstract void setPacket(PlatformPlayServerTabCompletePacket<?> packet);
}
