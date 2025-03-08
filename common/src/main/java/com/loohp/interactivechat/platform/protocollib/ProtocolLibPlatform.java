package com.loohp.interactivechat.platform.protocollib;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.listeners.PaperChatEvents;
import com.loohp.interactivechat.listeners.packet.protocollib.OutMessagePacket;
import com.loohp.interactivechat.listeners.packet.protocollib.OutTabCompletePacket;
import com.loohp.interactivechat.listeners.packet.protocollib.RedispatchSignedPacket;
import com.loohp.interactivechat.nms.NMS;
import com.loohp.interactivechat.objectholders.CustomTabCompletionAction;
import com.loohp.interactivechat.objectholders.ModernChatCompletionTask;
import com.loohp.interactivechat.platform.ProtocolPlatform;
import com.loohp.interactivechat.utils.MCVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.loohp.interactivechat.InteractiveChat.version;

public class ProtocolLibPlatform implements ProtocolPlatform {

    @Nullable
    public static ProtocolManager protocolManager;

    @Override
    public boolean hasChatSigning() {
        return MinecraftVersion.getCurrentVersion().compareTo(new MinecraftVersion(1, 19, 1)) >= 0;
    }

    @Override
    public void initialise() {
        protocolManager = ProtocolLibrary.getProtocolManager();

        OutMessagePacket.messageListeners();
        if (version.isNewerOrEqualTo(MCVersion.V1_19)) {
            RedispatchSignedPacket.packetListener();
        }

        if (!version.isLegacy()) {
            OutTabCompletePacket.tabCompleteListener();
        }
    }

    @Override
    public void sendTabCompletionPacket(Player player, CustomTabCompletionAction action, List<String> list) {
        PacketContainer chatCompletionPacket1 = NMS.getInstance().createCustomTabCompletionPacket(action, list);
        protocolManager.sendServerPacket(player, chatCompletionPacket1);
    }

    @Override
    public void dispose() {

    }
}
