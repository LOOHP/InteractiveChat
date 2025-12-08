package com.loohp.interactivechat.platform.protocollib.packets;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.listeners.packet.MessagePacketHandler;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerUnifiedChatMessagePacket;
import com.loohp.interactivechat.utils.ChatComponentType;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.loohp.interactivechat.listeners.packet.MessagePacketHandler.PacketAccessorResult;

public class ProtocolLibPlayServerUnifiedChatMessagePacket extends PlatformPlayServerUnifiedChatMessagePacket<PacketContainer> {

    public ProtocolLibPlayServerUnifiedChatMessagePacket(PacketContainer handle, MessagePacketHandler<?, PacketContainer> messagePacketHandler) {
        super(handle, messagePacketHandler);
    }

    @Override
    public ProtocolLibPlayServerUnifiedChatMessagePacket shallowClone() {
        return new ProtocolLibPlayServerUnifiedChatMessagePacket(handle.shallowClone(), messagePacketHandler);
    }

    @Override
    public PacketAccessorResult read(Player player) {
        return messagePacketHandler.getAccessor().apply(handle, player);
    }

    @Override
    public void write(Component component, ChatComponentType type, int field, UUID sender) {
        messagePacketHandler.getWriter().apply(handle, component, type, field, sender);
    }

}
