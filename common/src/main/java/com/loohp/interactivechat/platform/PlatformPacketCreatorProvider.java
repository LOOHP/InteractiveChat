package com.loohp.interactivechat.platform;

import com.loohp.interactivechat.objectholders.CustomTabCompletionAction;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerCustomChatCompletionPacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerSystemChatPacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerTabCompletePacket;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.UUID;

public interface PlatformPacketCreatorProvider<Packet> {

    PlatformPlayServerTabCompletePacket<Packet> createPlayServerTabCompletePacket(int id, Object suggestions);

    PlatformPlayServerCustomChatCompletionPacket<Packet> createPlayServerCustomChatCompletionPacket(CustomTabCompletionAction action, List<String> list);

    PlatformPlayServerSystemChatPacket<Packet> createPlayServerSystemChatPacket(UUID uuid, Component component);

}
