package com.loohp.interactivechat.platform;

import com.loohp.interactivechat.platform.packets.PlatformConfigurationClientClientInformationPacket;
import com.loohp.interactivechat.platform.packets.PlatformHandshakeClientSetProtocolPacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayClientChatCommandPacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayClientChatPacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerServerDataPacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerTabCompletePacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerUnifiedChatMessagePacket;
import com.loohp.interactivechat.platform.packets.PlatformStatusServerServerInfoPacket;
import org.bukkit.plugin.Plugin;

public interface PlatformPacketListenerProvider<PacketEvent, Packet> {

    void listenToHandshakeClientSetProtocol(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, Packet, PlatformHandshakeClientSetProtocolPacket<Packet>> listener);

    void listenToStatusServerServerInfo(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, Packet, PlatformStatusServerServerInfoPacket<Packet>> listener);

    void listenToConfigurationClientClientInformation(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, Packet, PlatformConfigurationClientClientInformationPacket<Packet>> listener);

    void listenToPlayServerTabComplete(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, Packet, PlatformPlayServerTabCompletePacket<Packet>> listener);

    void listenToPlayClientChat(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, Packet, PlatformPlayClientChatPacket<Packet>> listener);

    void listenToPlayChatCommand(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, Packet, PlatformPlayClientChatCommandPacket<Packet>> listener);

    void listenToPlayServerServerData(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, Packet, PlatformPlayServerServerDataPacket<Packet>> listener);

    void listenToPlayServerUnifiedChatMessage(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, Packet, PlatformPlayServerUnifiedChatMessagePacket<Packet>> listener);

}
