package com.loohp.interactivechat.utils;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.loohp.interactivechat.nms.NMS;

public class WrappedChatComponentUtils {

    public static WrappedChatComponent fromJson(String json) {
        return WrappedChatComponent.fromHandle(NMS.getInstance().deserializeChatComponent(json));
    }

    public static String toJson(WrappedChatComponent component) {
        return NMS.getInstance().serializeChatComponent(component.getHandle());
    }

}
