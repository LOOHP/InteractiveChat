package com.loohp.interactivechat.platform.protocollib.packets;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.nms.NMS;
import com.loohp.interactivechat.platform.packets.PlatformConfigurationClientClientInformationPacket;

public class ProtocolLibConfigurationClientClientInformationPacket extends PlatformConfigurationClientClientInformationPacket<PacketContainer> {

    public ProtocolLibConfigurationClientClientInformationPacket(PacketContainer handle) {
        super(handle);
    }

    @Override
    public ProtocolLibConfigurationClientClientInformationPacket shallowClone() {
        return new ProtocolLibConfigurationClientClientInformationPacket(handle.shallowClone());
    }

    @Override
    public boolean getColorSettings() {
        return NMS.getInstance().getColorSettingsFromClientInformationPacket(handle);
    }

}
