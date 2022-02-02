package com.loohp.interactivechat.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.events.PostPacketComponentProcessEvent;
import com.loohp.interactivechat.api.events.PreChatPacketSendEvent;
import com.loohp.interactivechat.api.events.PrePacketComponentProcessEvent;
import com.loohp.interactivechat.data.PlayerDataManager.PlayerData;
import com.loohp.interactivechat.hooks.venturechat.VentureChatInjection;
import com.loohp.interactivechat.modules.CommandsDisplay;
import com.loohp.interactivechat.modules.CustomPlaceholderDisplay;
import com.loohp.interactivechat.modules.EnderchestDisplay;
import com.loohp.interactivechat.modules.HoverableItemDisplay;
import com.loohp.interactivechat.modules.InventoryDisplay;
import com.loohp.interactivechat.modules.ItemDisplay;
import com.loohp.interactivechat.modules.MentionDisplay;
import com.loohp.interactivechat.modules.PlayernameDisplay;
import com.loohp.interactivechat.modules.ProcessAccurateSender;
import com.loohp.interactivechat.modules.ProcessCommands;
import com.loohp.interactivechat.modules.SenderFinder;
import com.loohp.interactivechat.objectholders.AsyncChatSendingExecutor;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.objectholders.ProcessSenderResult;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.ChatComponentType;
import com.loohp.interactivechat.utils.ComponentFont;
import com.loohp.interactivechat.utils.ComponentModernizing;
import com.loohp.interactivechat.utils.ComponentStyling;
import com.loohp.interactivechat.utils.CustomArrayUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.JsonUtils;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.PlayerUtils;
import com.loohp.interactivechat.utils.PlayerUtils.ColorSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OutChatPacket implements Listener {

    private static final AsyncChatSendingExecutor service;
    private static int chatFieldsSize;

    static {
        PacketContainer packet = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.CHAT);
        List<String> matches = Stream.of(ChatComponentType.byPriority()).map(each -> each.getMatchingRegex()).collect(Collectors.toList());

        for (chatFieldsSize = 1; chatFieldsSize < packet.getModifier().size(); chatFieldsSize++) {
            String clazz = packet.getModifier().getField(chatFieldsSize).getType().getName();
            if (matches.stream().noneMatch(each -> clazz.matches(each))) {
                chatFieldsSize--;
                break;
            }
        }

        service = new AsyncChatSendingExecutor(() -> (long) (InteractiveChat.bungeecordMode ? InteractiveChat.remoteDelay : 0) + 2000, 5000);
    }

    public static AsyncChatSendingExecutor getAsyncChatSendingExecutor() {
        return service;
    }

    public static void chatMessageListener() {
        InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.MONITOR).types(PacketType.Play.Server.CHAT)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.isPlayerTemporary() || !event.isFiltered() || event.isCancelled() || !event.getPacketType().equals(PacketType.Play.Server.CHAT)) {
                    return;
                }

                if (InteractiveChat.ventureChatHook) {
                    VentureChatInjection.firePacketListener(event);
                }

                InteractiveChat.messagesCounter.getAndIncrement();

                PacketContainer packetOriginal = event.getPacket();

                if (!InteractiveChat.version.isLegacy() || InteractiveChat.version.equals(MCVersion.V1_12)) {
                    ChatType type = packetOriginal.getChatTypes().read(0);
                    if (type == null || type.equals(ChatType.GAME_INFO)) {
                        return;
                    }
                } else {
                    byte type = packetOriginal.getBytes().read(0);
                    if (type == (byte) 2) {
                        return;
                    }
                }

                event.setReadOnly(false);
                event.setCancelled(true);
                event.setReadOnly(false);

                Player reciever = event.getPlayer();
                PacketContainer packet = packetOriginal.deepClone();

                UUID messageUUID = UUID.randomUUID();

                service.execute(() -> {
                    processPacket(reciever, packet, messageUUID, event.isFiltered());
                }, reciever, messageUUID);
            }
        });
    }

    private static void processPacket(Player reciever, PacketContainer packet, UUID messageUUID, boolean isFiltered) {
        PacketContainer originalPacket = packet.deepClone();
        try {
            Component component = null;
            ChatComponentType type = null;
            int field = -1;

            search:
            for (ChatComponentType t : ChatComponentType.byPriority()) {
                for (int i = 0; i < packet.getModifier().size(); i++) {
                    if (!CustomArrayUtils.allNull(packet.getModifier().read(i)) && packet.getModifier().getField(i).getType().getName().matches(t.getMatchingRegex())) {
                        try {
                            component = t.convertFrom(packet.getModifier().read(i));
                        } catch (Throwable e) {
                            System.err.println(t.toString(packet.getModifier().read(i)));
                            e.printStackTrace();
                            service.send(packet, reciever, messageUUID);
                            return;
                        }
                        field = i;
                        type = t;
                        break search;
                    }
                }
            }
            if (field < 0 || type == null || component == null) {
                service.send(packet, reciever, messageUUID);
                return;
            }

            String legacyText = LegacyComponentSerializer.legacySection().serializeOr(component, "");
            try {
                if (legacyText.equals("") || InteractiveChat.messageToIgnore.stream().anyMatch(each -> legacyText.matches(each))) {
                    service.send(packet, reciever, messageUUID);
                    return;
                }
            } catch (Exception e) {
                service.send(packet, reciever, messageUUID);
                return;
            }

            if (InteractiveChat.version.isOld() && JsonUtils.containsKey(InteractiveChatComponentSerializer.gson().serialize(component), "translate")) {
                service.send(packet, reciever, messageUUID);
                return;
            }

            @SuppressWarnings("unused")
            boolean isCommand = false;
            @SuppressWarnings("unused")
            boolean isChat = false;

            Optional<ICPlayer> sender = Optional.empty();
            String rawMessageKey = PlainTextComponentSerializer.plainText().serializeOr(component, "");

            InteractiveChat.keyTime.putIfAbsent(rawMessageKey, System.currentTimeMillis());

            Long timeKey = InteractiveChat.keyTime.get(rawMessageKey);
            long unix = timeKey == null ? System.currentTimeMillis() : timeKey;

            ProcessSenderResult commandSender = ProcessCommands.process(component);
            if (commandSender.getSender() != null) {
                ICPlayer icplayer = ICPlayerFactory.getICPlayer(commandSender.getSender());
                if (icplayer != null) {
                    sender = Optional.of(icplayer);
                    isCommand = true;
                }
            }
            ProcessSenderResult chatSender = null;
            if (!sender.isPresent()) {
                if (InteractiveChat.useAccurateSenderFinder) {
                    chatSender = ProcessAccurateSender.process(component);
                    if (chatSender.getSender() != null) {
                        ICPlayer icplayer = ICPlayerFactory.getICPlayer(chatSender.getSender());
                        if (icplayer != null) {
                            sender = Optional.of(icplayer);
                            isChat = true;
                        }
                    }
                }
            }
            if (!sender.isPresent()) {
                sender = SenderFinder.getSender(component, rawMessageKey);
            }

            if (sender.isPresent() && !sender.get().isLocal()) {
                if (isFiltered) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(InteractiveChat.plugin, () -> {
                        service.execute(() -> {
                            processPacket(reciever, packet, messageUUID, false);
                        }, reciever, messageUUID);
                    }, (int) Math.ceil((double) InteractiveChat.remoteDelay / 50));
                    return;
                }
            }
            component = commandSender.getComponent();
            if (chatSender != null) {
                component = chatSender.getComponent();
            }
            if (sender.isPresent()) {
                InteractiveChat.keyPlayer.put(rawMessageKey, sender.get());
            }

            component = ComponentModernizing.modernize(component);

            UUID preEventSenderUUID = sender.isPresent() ? sender.get().getUniqueId() : null;
            PrePacketComponentProcessEvent preEvent = new PrePacketComponentProcessEvent(true, reciever, component, preEventSenderUUID);
            Bukkit.getPluginManager().callEvent(preEvent);
            if (preEvent.getSender() != null) {
                Player newsender = Bukkit.getPlayer(preEvent.getSender());
                if (newsender != null) {
                    sender = Optional.of(ICPlayerFactory.getICPlayer(newsender));
                }
            }
            component = preEvent.getComponent();

            component = component.replaceText(TextReplacementConfig.builder().match(Registry.ID_PATTERN).replacement("").build());

            if (InteractiveChat.translateHoverableItems && InteractiveChat.itemGUI) {
                component = HoverableItemDisplay.process(component, reciever);
            }

            if (InteractiveChat.usePlayerName) {
                component = PlayernameDisplay.process(component, sender, reciever, unix);
            }

            if (InteractiveChat.allowMention && sender.isPresent()) {
                PlayerData data = InteractiveChat.playerDataManager.getPlayerData(reciever);
                if (data == null || !data.isMentionDisabled()) {
                    component = MentionDisplay.process(component, reciever, sender.get(), unix, true);
                }
            }

            if (InteractiveChat.useItem) {
                component = ItemDisplay.process(component, sender, reciever, unix);
            }

            if (InteractiveChat.useInventory) {
                component = InventoryDisplay.process(component, sender, reciever, unix);
            }

            if (InteractiveChat.useEnder) {
                component = EnderchestDisplay.process(component, sender, reciever, unix);
            }

            component = CustomPlaceholderDisplay.process(component, sender, reciever, InteractiveChat.placeholderList.values(), unix);

            if (InteractiveChat.clickableCommands) {
                component = CommandsDisplay.process(component);
            }

            if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16) && InteractiveChat.fontTags) {
                if (!sender.isPresent() || (sender.isPresent() && PlayerUtils.hasPermission(sender.get().getUniqueId(), "interactivechat.customfont.translate", true, 250))) {
                    component = ComponentFont.parseFont(component);
                }
            }

            if (PlayerUtils.getColorSettings(reciever).equals(ColorSettings.OFF)) {
                component = ComponentStyling.stripColor(component);
            }

            PostPacketComponentProcessEvent postEvent = new PostPacketComponentProcessEvent(true, reciever, component, preEventSenderUUID);
            Bukkit.getPluginManager().callEvent(postEvent);
            component = postEvent.getComponent();

            boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
            String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
            boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;

            //Bukkit.getConsoleSender().sendMessage(json.replace(ChatColor.COLOR_CHAR, '$'));

            try {
                packet.getModifier().write(field, type.convertTo(component, legacyRGB));
            } catch (Throwable e) {
                for (int i = 0; i < chatFieldsSize; i++) {
                    packet.getModifier().write(i, null);
                }
                packet.getChatComponents().write(0, WrappedChatComponent.fromJson(json));
            }

            UUID postEventSenderUUID = sender.isPresent() ? sender.get().getUniqueId() : new UUID(0, 0);
            if (packet.getUUIDs().size() > 0) {
                packet.getUUIDs().write(0, postEventSenderUUID);
            }
            PreChatPacketSendEvent sendEvent = new PreChatPacketSendEvent(true, reciever, packet, postEventSenderUUID, originalPacket, InteractiveChat.sendOriginalIfTooLong, longerThanMaxLength);
            Bukkit.getPluginManager().callEvent(sendEvent);

            Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
                InteractiveChat.keyTime.remove(rawMessageKey);
                InteractiveChat.keyPlayer.remove(rawMessageKey);
            }, 10);

            if (sendEvent.isCancelled()) {
                if (sendEvent.sendOriginalIfCancelled()) {
                    PacketContainer originalPacketModified = sendEvent.getOriginal();
                    service.send(originalPacketModified, reciever, messageUUID);
                    return;
                } else {
                    if (longerThanMaxLength && InteractiveChat.cancelledMessage) {
                        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractiveChat] " + ChatColor.RED + "Cancelled a chat packet bounded to " + reciever.getName() + " that is " + json.length() + " characters long (Longer than maximum allowed in a chat packet) [THIS IS NOT A BUG]");
                    }
                }
                service.discard(reciever.getUniqueId(), messageUUID);
                return;
            }
            service.send(packet, reciever, messageUUID);
        } catch (Exception e) {
            e.printStackTrace();
            service.send(originalPacket, reciever, messageUUID);
        }
    }

}
