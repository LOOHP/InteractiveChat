package com.loohp.interactivechat.platform.packets;

import com.loohp.interactivechat.listeners.packet.MessagePacketHandler;
import com.loohp.interactivechat.utils.ChatComponentType;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.loohp.interactivechat.listeners.packet.MessagePacketHandler.PacketAccessorResult;

public abstract class PlatformPlayServerUnifiedChatMessagePacket<Packet> extends PlatformPacket<Packet> {

    protected final MessagePacketHandler<?, Packet> messagePacketHandler;

    public PlatformPlayServerUnifiedChatMessagePacket(Packet handle, MessagePacketHandler<?, Packet> messagePacketHandler) {
        super(handle);
        this.messagePacketHandler = messagePacketHandler;
    }

    @Override
    public abstract PlatformPlayServerUnifiedChatMessagePacket<Packet> shallowClone();

    public MessagePacketHandler<?, Packet> getMessagePacketHandler() {
        return messagePacketHandler;
    }

    public abstract PacketAccessorResult read(Player player);

    public abstract void write(Component component, ChatComponentType type, int field, UUID sender);

}
