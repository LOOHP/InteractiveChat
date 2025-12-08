package com.loohp.interactivechat.platform.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.listeners.packet.MessagePacketHandler;
import com.loohp.interactivechat.platform.PlatformPacketEventListener;
import com.loohp.interactivechat.platform.PlatformPacketListenerPriority;
import com.loohp.interactivechat.platform.PlatformPacketListenerProvider;
import com.loohp.interactivechat.platform.packets.PlatformConfigurationClientClientInformationPacket;
import com.loohp.interactivechat.platform.packets.PlatformHandshakeClientSetProtocolPacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayClientChatCommandPacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayClientChatPacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerServerDataPacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerTabCompletePacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerUnifiedChatMessagePacket;
import com.loohp.interactivechat.platform.packets.PlatformStatusServerServerInfoPacket;
import com.loohp.interactivechat.platform.protocollib.packets.ProtocolLibConfigurationClientClientInformationPacket;
import com.loohp.interactivechat.platform.protocollib.packets.ProtocolLibHandshakeClientSetProtocolPacket;
import com.loohp.interactivechat.platform.protocollib.packets.ProtocolLibPlayClientChatCommandPacket;
import com.loohp.interactivechat.platform.protocollib.packets.ProtocolLibPlayClientChatPacket;
import com.loohp.interactivechat.platform.protocollib.packets.ProtocolLibPlayServerServerDataPacket;
import com.loohp.interactivechat.platform.protocollib.packets.ProtocolLibPlayServerTabCompletePacket;
import com.loohp.interactivechat.platform.protocollib.packets.ProtocolLibPlayServerUnifiedChatMessagePacket;
import com.loohp.interactivechat.platform.protocollib.packets.ProtocolLibStatusServerServerInfoPacket;
import com.loohp.interactivechat.utils.MCVersion;
import org.bukkit.plugin.Plugin;

public class ProtocolLibPacketListenerProvider implements PlatformPacketListenerProvider<PacketEvent, PacketContainer> {

    private static ListenerPriority c(PlatformPacketListenerPriority priority) {
        switch (priority) {
            case LOWEST:
                return ListenerPriority.LOWEST;
            case LOW:
                return ListenerPriority.LOW;
            case NORMAL:
                return ListenerPriority.NORMAL;
            case HIGH:
                return ListenerPriority.HIGH;
            case HIGHEST:
                return ListenerPriority.HIGHEST;
            case MONITOR:
                return ListenerPriority.MONITOR;
        }
        throw new IllegalArgumentException("Unknown priority " + priority.name());
    }

    private final ProtocolLibPlatform platform;

    public ProtocolLibPacketListenerProvider(ProtocolLibPlatform platform) {
        this.platform = platform;
    }

    @Override
    public void listenToHandshakeClientSetProtocol(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, PacketContainer, PlatformHandshakeClientSetProtocolPacket<PacketContainer>> listener) {
        platform.getProtocolManager().addPacketListener(new PacketAdapter(PacketAdapter.params().optionAsync().listenerPriority(c(priority)).plugin(plugin).types(PacketType.Handshake.Client.SET_PROTOCOL)) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                listener.handle(new ProtocolLibPacketEvent<>(event, packet -> new ProtocolLibHandshakeClientSetProtocolPacket(packet)));
            }
        });
    }

    @Override
    public void listenToStatusServerServerInfo(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, PacketContainer, PlatformStatusServerServerInfoPacket<PacketContainer>> listener) {
        platform.getProtocolManager().addPacketListener(new PacketAdapter(PacketAdapter.params().optionAsync().listenerPriority(c(priority)).plugin(plugin).types(PacketType.Status.Server.SERVER_INFO)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                listener.handle(new ProtocolLibPacketEvent<>(event, packet -> new ProtocolLibStatusServerServerInfoPacket(packet)));
            }
        });
    }

    @Override
    public void listenToConfigurationClientClientInformation(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, PacketContainer, PlatformConfigurationClientClientInformationPacket<PacketContainer>> listener) {
        PacketAdapter.AdapterParameteters params = PacketAdapter.params().listenerPriority(c(priority)).plugin(plugin);
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_20_2)) {
            params.types(PacketType.Configuration.Client.CLIENT_INFORMATION);
        } else {
            params.types(PacketType.Play.Client.SETTINGS);
        }
        platform.getProtocolManager().addPacketListener(new PacketAdapter(params) {
            @Override
            public void onPacketSending(PacketEvent event) {
                // do nothing
            }
            @Override
            public void onPacketReceiving(PacketEvent event) {
                listener.handle(new ProtocolLibPacketEvent<>(event, packet -> new ProtocolLibConfigurationClientClientInformationPacket(packet)));
            }
        });
    }

    @Override
    public void listenToPlayServerTabComplete(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, PacketContainer, PlatformPlayServerTabCompletePacket<PacketContainer>> listener) {
        platform.getProtocolManager().addPacketListener(new PacketAdapter(PacketAdapter.params().optionAsync().plugin(plugin).listenerPriority(c(priority)).types(PacketType.Play.Server.TAB_COMPLETE)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                listener.handle(new ProtocolLibPacketEvent<>(event, packet -> new ProtocolLibPlayServerTabCompletePacket(packet)));
            }
        });
    }

    @Override
    public void listenToPlayClientChat(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, PacketContainer, PlatformPlayClientChatPacket<PacketContainer>> listener) {
        platform.getProtocolManager().addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(plugin).listenerPriority(c(priority)).types(PacketType.Play.Client.CHAT)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                // Do nothing
            }
            @Override
            public void onPacketReceiving(PacketEvent event) {
                listener.handle(new ProtocolLibPacketEvent<>(event, packet -> new ProtocolLibPlayClientChatPacket(packet)));
            }
        });
    }

    @Override
    public void listenToPlayChatCommand(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, PacketContainer, PlatformPlayClientChatCommandPacket<PacketContainer>> listener) {
        PacketType[] packetTypes;
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_20_5)) {
            packetTypes = new PacketType[] {PacketType.Play.Client.CHAT_COMMAND_SIGNED, PacketType.Play.Client.CHAT_COMMAND};
        } else {
            packetTypes = new PacketType[] {PacketType.Play.Client.CHAT_COMMAND};
        }
        platform.getProtocolManager().addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(plugin).listenerPriority(c(priority)).types(packetTypes)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                // Do nothing
            }
            @Override
            public void onPacketReceiving(PacketEvent event) {
                listener.handle(new ProtocolLibPacketEvent<>(event, packet -> new ProtocolLibPlayClientChatCommandPacket(packet)));
            }
        });
    }

    @Override
    public void listenToPlayServerServerData(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, PacketContainer, PlatformPlayServerServerDataPacket<PacketContainer>> listener) {
        platform.getProtocolManager().addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(plugin).listenerPriority(c(priority)).types(PacketType.Play.Server.SERVER_DATA)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                listener.handle(new ProtocolLibPacketEvent<>(event, packet -> new ProtocolLibPlayServerServerDataPacket(packet)));
            }
        });
    }

    @Override
    public void listenToPlayServerUnifiedChatMessage(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<PacketEvent, PacketContainer, PlatformPlayServerUnifiedChatMessagePacket<PacketContainer>> listener) {
        PacketAdapter.AdapterParameteters params = PacketAdapter.params().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.MONITOR).types(ProtocolLibOutMessagePacketHelper.PACKET_HANDLERS.keySet());
        platform.getProtocolManager().addPacketListener(new PacketAdapter(params) {
            @Override
            public void onPacketSending(PacketEvent event) {
                MessagePacketHandler<PacketEvent, PacketContainer> handler = ProtocolLibOutMessagePacketHelper.PACKET_HANDLERS.get(event.getPacketType());
                listener.handle(new ProtocolLibPacketEvent<>(event, packet -> new ProtocolLibPlayServerUnifiedChatMessagePacket(packet, handler)));
            }
        });
    }
}
