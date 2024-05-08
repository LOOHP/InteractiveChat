/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
