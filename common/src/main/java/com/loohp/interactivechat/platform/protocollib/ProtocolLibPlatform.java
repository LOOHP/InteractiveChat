package com.loohp.interactivechat.platform.protocollib;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.loohp.interactivechat.listeners.packet.protocollib.PLibClientSettingPacket;
import com.loohp.interactivechat.listeners.packet.protocollib.PLibOutMessagePacket;
import com.loohp.interactivechat.listeners.packet.protocollib.PLibOutTabCompletePacket;
import com.loohp.interactivechat.listeners.packet.protocollib.PLibRedispatchSignedPacket;
import com.loohp.interactivechat.nms.NMS;
import com.loohp.interactivechat.objectholders.CustomTabCompletionAction;
import com.loohp.interactivechat.platform.ProtocolPlatform;
import com.loohp.interactivechat.utils.MCVersion;
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

        PLibOutMessagePacket.messageListeners();
        if (version.isNewerOrEqualTo(MCVersion.V1_19)) {
            PLibRedispatchSignedPacket.packetListener();
        }

        if (!version.isLegacy()) {
            PLibOutTabCompletePacket.tabCompleteListener();
        }

        PLibClientSettingPacket.clientSettingsListener();
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
