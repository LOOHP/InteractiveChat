package com.loohp.interactivechat.platform.protocollib.packets;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.loohp.interactivechat.platform.packets.PlatformStatusServerServerInfoPacket;

public class ProtocolLibStatusServerServerInfoPacket extends PlatformStatusServerServerInfoPacket<PacketContainer> {

    public ProtocolLibStatusServerServerInfoPacket(PacketContainer handle) {
        super(handle);
    }

    @Override
    public ProtocolLibStatusServerServerInfoPacket shallowClone() {
        return new ProtocolLibStatusServerServerInfoPacket(handle.shallowClone());
    }

    @Override
    public String getMotd() {
        WrappedServerPing response = handle.getServerPings().read(0);
        if (response != null) {
            return response.getMotD().getJson();
        }
        return null;
    }

    @Override
    public void setMotd(String message) {
        WrappedServerPing response = handle.getServerPings().read(0);
        if (response != null) {
            response.setMotD(message);
            handle.getServerPings().write(0, response);
        }
    }
}
