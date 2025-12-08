package com.loohp.interactivechat.platform.protocollib.packets;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.nms.NMS;
import com.loohp.interactivechat.objectholders.CommandSuggestion;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerTabCompletePacket;

public class ProtocolLibPlayServerTabCompletePacket extends PlatformPlayServerTabCompletePacket<PacketContainer> {

    public ProtocolLibPlayServerTabCompletePacket(PacketContainer handle) {
        super(handle);
    }

    @Override
    public ProtocolLibPlayServerTabCompletePacket shallowClone() {
        return new ProtocolLibPlayServerTabCompletePacket(handle.shallowClone());
    }

    @Override
    public CommandSuggestion<?> getCommandSuggestions() {
        return NMS.getInstance().readCommandSuggestionPacket(handle);
    }

    @Override
    public void setPacket(PlatformPlayServerTabCompletePacket<?> packet) {
        PacketContainer packetContainer = (PacketContainer) packet.getHandle();
        for (int i = 0; i < Math.min(handle.getModifier().size(), packetContainer.getModifier().size()); i++) {
            handle.getModifier().write(i, packetContainer.getModifier().read(i));
        }
    }

}
