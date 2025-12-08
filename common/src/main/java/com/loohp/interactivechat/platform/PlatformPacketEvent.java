package com.loohp.interactivechat.platform;

import com.loohp.interactivechat.platform.packets.PlatformPacket;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface PlatformPacketEvent<PacketEvent, Packet, PlatformPacketTyped extends PlatformPacket<Packet>> {

    PacketEvent getHandle();

    PlatformPacketTyped getPacket();

    Player getPlayer();

    boolean isPlayerTemporary();

    UUID getPlayerUniqueId();

    Object getIdentityObject();

    boolean isCancelled();

    void setCancelled(boolean cancelled);

    boolean isReadOnly();

    void setReadOnly(boolean readOnly);

    boolean isFiltered();

}
