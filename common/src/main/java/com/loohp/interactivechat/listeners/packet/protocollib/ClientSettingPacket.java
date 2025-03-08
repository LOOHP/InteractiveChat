package com.loohp.interactivechat.listeners.packet.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.config.ConfigManager;
import com.loohp.interactivechat.listeners.packet.ClientSettingsHandler;
import com.loohp.interactivechat.nms.NMS;
import com.loohp.interactivechat.platform.protocollib.ProtocolLibPlatform;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ClientSettingPacket {

    public static void clientSettingsListener() {
        PacketAdapter.AdapterParameteters params = PacketAdapter.params()
                .listenerPriority(ListenerPriority.MONITOR)
                .plugin(InteractiveChat.plugin);

        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_20_2)) {
            params.types(PacketType.Configuration.Client.CLIENT_INFORMATION);
        } else {
            params.types(PacketType.Play.Client.SETTINGS);
        }

        ProtocolLibPlatform.protocolManager.addPacketListener(new PacketAdapter(params) {
            @Override
            public void onPacketSending(PacketEvent event) {
                // do nothing
            }

            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.isPlayerTemporary()) return;

                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();

                boolean colorSettings = NMS.getInstance().getColorSettingsFromClientInformationPacket(packet);
                boolean originalColorSettings = PlayerUtils.canChatColor(player);

                ClientSettingsHandler.handlePacketReceiving(colorSettings, originalColorSettings, player);
            }
        });
    }
}