package com.loohp.interactivechat.platform.protocollib;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.loohp.interactivechat.platform.PlatformPacketEvent;
import com.loohp.interactivechat.platform.packets.PlatformPacket;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Function;

public class ProtocolLibPacketEvent<PlatformPacketTyped extends PlatformPacket<PacketContainer>> implements PlatformPacketEvent<PacketEvent, PacketContainer, PlatformPacketTyped> {

    private final PacketEvent handle;
    private final Function<PacketContainer, PlatformPacketTyped> converter;

    public ProtocolLibPacketEvent(PacketEvent handle, Function<PacketContainer, PlatformPacketTyped> converter) {
        this.handle = handle;
        this.converter = converter;
    }

    @Override
    public PacketEvent getHandle() {
        return handle;
    }

    @Override
    public PlatformPacketTyped getPacket() {
        return converter.apply(handle.getPacket());
    }

    @Override
    public Player getPlayer() {
        return handle.getPlayer();
    }

    @Override
    public boolean isPlayerTemporary() {
        return handle.isPlayerTemporary();
    }

    @Override
    public UUID getPlayerUniqueId() {
        return handle.getPlayer().getUniqueId();
    }

    @Override
    public Object getIdentityObject() {
        return handle.getPlayer();
    }

    @Override
    public boolean isCancelled() {
        return handle.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        handle.setCancelled(cancelled);
    }

    @Override
    public boolean isReadOnly() {
        return handle.isReadOnly();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        handle.setReadOnly(readOnly);
    }

    @Override
    public boolean isFiltered() {
        return handle.isFiltered();
    }


}
