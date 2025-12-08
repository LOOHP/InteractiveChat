package com.loohp.interactivechat.platform.protocollib.packets;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.platform.packets.PlatformPlayClientChatCommandPacket;
import com.loohp.interactivechat.utils.ModernChatSigningUtils;

public class ProtocolLibPlayClientChatCommandPacket extends PlatformPlayClientChatCommandPacket<PacketContainer> {

    public ProtocolLibPlayClientChatCommandPacket(PacketContainer handle) {
        super(handle);
    }

    @Override
    public ProtocolLibPlayClientChatCommandPacket shallowClone() {
        return new ProtocolLibPlayClientChatCommandPacket(handle.shallowClone());
    }

    @Override
    public boolean hasArgumentSignatureEntries() {
        if (handle.getModifier().size() > 3) {
            Object argumentSignature = handle.getModifier().read(3);
            return ModernChatSigningUtils.isArgumentSignatureClass(argumentSignature) && !ModernChatSigningUtils.getArgumentSignatureEntries(argumentSignature).isEmpty();
        }
        return false;
    }

    @Override
    public String getCommand() {
        return handle.getStrings().read(0);
    }

}
