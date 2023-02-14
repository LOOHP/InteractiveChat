package com.loohp.interactivechat.proxy.objectholders;

import java.util.List;

public class HandlePacket {

    private final boolean chat;
    private final boolean title;
    private final boolean systemChat;
    private final boolean actionBar;

    public HandlePacket(List<String> types) {
        this.actionBar = types.contains("ACTIONBAR");
        this.systemChat = types.contains("SYSTEM_CHAT");
        this.chat = types.contains("CHAT");
        this.title = types.contains("TITLE");
    }

    public HandlePacket() {
        this.actionBar = true;
        this.chat = true;
        this.systemChat = true;
        this.title = true;
    }

    public boolean isChat() {
        return chat;
    }

    public boolean isTitle() {
        return title;
    }

    public boolean isSystemChat() {
        return systemChat;
    }

    public boolean isActionBar() {
        return actionBar;
    }
}
