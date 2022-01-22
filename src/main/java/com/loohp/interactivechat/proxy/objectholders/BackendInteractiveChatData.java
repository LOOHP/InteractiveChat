package com.loohp.interactivechat.proxy.objectholders;

import com.loohp.interactivechat.utils.MCVersion;

public class BackendInteractiveChatData {

    private final String server;
    private boolean isOnline;

    private boolean hasInteractiveChat;
    private String version;
    private int ping;
    private MCVersion minecraftVersion;
    private String exactMinecraftVersion;
    private int protocol;

    @Deprecated
    public BackendInteractiveChatData(String server, boolean isOnline, boolean hasInteractiveChat, String version, MCVersion minecraftVersion, String exactMinecraftVersion, int ping, int protocol) {
        this.server = server;
        this.hasInteractiveChat = hasInteractiveChat;
        this.version = version;
        this.minecraftVersion = minecraftVersion;
        this.exactMinecraftVersion = exactMinecraftVersion;
        this.ping = ping;
        this.protocol = protocol;
        this.isOnline = isOnline;
    }

    public String getServer() {
        return server;
    }

    public boolean isOnline() {
        return isOnline;
    }

    @Deprecated
    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public boolean hasInteractiveChat() {
        return hasInteractiveChat;
    }

    @Deprecated
    public void setInteractiveChat(boolean hasInteractiveChat) {
        this.hasInteractiveChat = hasInteractiveChat;
    }

    public String getVersion() {
        return version;
    }

    @Deprecated
    public void setVersion(String version) {
        this.version = version;
    }

    public int getPing() {
        return ping;
    }

    @Deprecated
    public void setPing(int ping) {
        this.ping = ping;
    }

    public MCVersion getMinecraftVersion() {
        return minecraftVersion;
    }

    @Deprecated
    public void setMinecraftVersion(MCVersion minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public String getExactMinecraftVersion() {
        return exactMinecraftVersion;
    }

    @Deprecated
    public void setExactMinecraftVersion(String exactMinecraftVersion) {
        this.exactMinecraftVersion = exactMinecraftVersion;
    }

    public int getProtocolVersion() {
        return protocol;
    }

    @Deprecated
    public void setProtocolVersion(int protocol) {
        this.protocol = protocol;
    }

}
