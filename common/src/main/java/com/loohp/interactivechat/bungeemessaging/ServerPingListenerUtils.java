package com.loohp.interactivechat.bungeemessaging;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.registry.Registry;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerPingListenerUtils {

    public static final Map<Player, Long> REQUESTS = new ConcurrentHashMap<>();
    public static String json;

    static {
        JSONObject obj = new JSONObject();
        obj.put("present", true);
        obj.put("version", InteractiveChat.plugin.getDescription().getVersion());
        obj.put("minecraftVersion", InteractiveChat.version.getNumber());
        obj.put("exactMinecraftVersion", InteractiveChat.exactMinecraftVersion);
        obj.put("protocol", Registry.PLUGIN_MESSAGING_PROTOCOL_VERSION);
        json = obj.toJSONString();
    }
}
