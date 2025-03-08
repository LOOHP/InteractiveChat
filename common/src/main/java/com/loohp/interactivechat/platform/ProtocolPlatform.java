package com.loohp.interactivechat.platform;

import com.loohp.interactivechat.objectholders.CustomTabCompletionAction;
import org.bukkit.entity.Player;

import java.util.List;

public interface ProtocolPlatform {

    void initialise();

    void sendTabCompletionPacket(Player player, CustomTabCompletionAction action, List<String> list);

    void dispose();

    boolean hasChatSigning();

}
