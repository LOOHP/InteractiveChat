package com.loohp.interactivechat.listeners.packet.packetevents;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientSettings;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.listeners.packet.ClientSettingsHandler;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.PlayerUtils;
import org.bukkit.entity.Player;

public class PEClientSettingsPacket implements PacketListener {

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Configuration.Client.CLIENT_SETTINGS) return;

        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_20_2)) {
            WrapperConfigClientSettings packet = new WrapperConfigClientSettings(event);

            Player player = event.getPlayer();
            boolean colorSettings = packet.isChatColors();
            boolean originalColorSettings = PlayerUtils.canChatColor(player);

            ClientSettingsHandler.handlePacketReceiving(colorSettings, originalColorSettings, player);
        }
    }
}
