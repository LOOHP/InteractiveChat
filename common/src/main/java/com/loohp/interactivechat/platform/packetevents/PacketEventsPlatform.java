package com.loohp.interactivechat.platform.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTabComplete;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.listeners.packet.packetevents.PEOutMessagePacket;
import com.loohp.interactivechat.objectholders.CustomTabCompletionAction;
import com.loohp.interactivechat.platform.ProtocolPlatform;
import com.loohp.interactivechat.utils.MCVersion;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PacketEventsPlatform implements ProtocolPlatform {
    @Override
    public void initialise() {
        new PEOutMessagePacket().messageListeners();
    }

    @Override
    public void sendTabCompletionPacket(Player player, CustomTabCompletionAction action, List<String> list) {
        List<WrapperPlayServerTabComplete.CommandMatch> suggestions = new ArrayList<>();
        for (String cmd : list) {
            suggestions.add(new WrapperPlayServerTabComplete.CommandMatch(cmd, null));
        }

        WrapperPlayServerTabComplete packet = new WrapperPlayServerTabComplete(
                null,
                new WrapperPlayServerTabComplete.CommandRange(0, list.size()),
                suggestions
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean hasChatSigning() {
        return InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19);
    }
}
