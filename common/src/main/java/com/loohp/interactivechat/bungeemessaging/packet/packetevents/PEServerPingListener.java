package com.loohp.interactivechat.bungeemessaging.packet.packetevents;

import com.comphenix.protocol.PacketType;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.wrapper.handshaking.client.WrapperHandshakingClientHandshake;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.loohp.interactivechat.registry.Registry;

import static com.loohp.interactivechat.bungeemessaging.ServerPingListenerUtils.*;

public class PEServerPingListener implements PacketListener {

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!event.getPacketType().equals(PacketType.Handshake.Client.SET_PROTOCOL)) return;

        WrapperHandshakingClientHandshake handshake = new WrapperHandshakingClientHandshake(event);

        String addr = handshake.getServerAddress();
        if (addr != null && addr.equals(Registry.PLUGIN_MESSAGING_PROTOCOL_IDENTIFIER)) {
            REQUESTS.put(event.getPlayer(), System.currentTimeMillis() + 5000);
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!event.getPacketType().equals(PacketType.Status.Server.SERVER_INFO)) return;

        WrapperStatusServerResponse response = new WrapperStatusServerResponse(event);
        if (REQUESTS.remove(event.getPlayer()) != null) {
            response.getComponent().addProperty("description", json);
        }

        event.setLastUsedWrapper(response);
    }

}
