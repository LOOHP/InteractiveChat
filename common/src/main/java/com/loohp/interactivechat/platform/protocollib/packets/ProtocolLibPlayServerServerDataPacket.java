package com.loohp.interactivechat.platform.protocollib.packets;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerServerDataPacket;
import com.loohp.interactivechat.utils.MCVersion;

public class ProtocolLibPlayServerServerDataPacket extends PlatformPlayServerServerDataPacket<PacketContainer> {

    public ProtocolLibPlayServerServerDataPacket(PacketContainer handle) {
        super(handle);
    }

    @Override
    public ProtocolLibPlayServerServerDataPacket shallowClone() {
        return new ProtocolLibPlayServerServerDataPacket(handle.shallowClone());
    }

    @Override
    public void setServerUnsignedStatus(boolean status) {
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19_3)) {
            if (handle.getBooleans().size() > 0) {
                handle.getBooleans().write(0, status);
            }
        } else {
            if (handle.getBooleans().size() > 1) {
                handle.getBooleans().write(1, status);
            }
        }
    }

}
