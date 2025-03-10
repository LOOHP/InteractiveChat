package com.loohp.interactivechat.listeners.packet.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.chat.ChatType;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.events.PostPacketComponentProcessEvent;
import com.loohp.interactivechat.api.events.PreChatPacketSendEvent;
import com.loohp.interactivechat.api.events.PrePacketComponentProcessEvent;
import com.loohp.interactivechat.data.PlayerDataManager;
import com.loohp.interactivechat.hooks.triton.TritonHook;
import com.loohp.interactivechat.listeners.packet.MessagePacketHandler;
import com.loohp.interactivechat.modules.*;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.objectholders.ProcessSenderResult;
import com.loohp.interactivechat.platform.packetevents.PacketEventsAsyncChatSendingExecutor;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.loohp.interactivechat.listeners.packet.MessagePacketHandler.*;

public class PEOutMessagePacket implements PacketListener {

    private static final Map<PacketTypeCommon, PacketHandler> PACKET_HANDLERS = new HashMap<>();
    private static Method setDisguisedChatMessageMethod;
    private static Method setChatMessageContentMethod;
    private static Method setSystemChatMessageMethod;
    private static Method setTitleMethod;
    private static Method setTitleTextMethod;
    private static Method setSubtitleTextMethod;
    private static Method setActionBarTextMethod;

    static {
        try {
            // well.
            // Thanks to PE's AMAZING decision to shade Adventure AND NOT RELOCATE IT, we have to do reflection.
            // we then use even more reflection to turn IC's shaded (and relocated) components into the native adventure component (which PE would support),
            // otherwise you get a bunch of NoSuchMethodErrors because we can't supply our own shaded adventure components.
            setDisguisedChatMessageMethod = Arrays.stream(WrapperPlayServerDisguisedChat.class.getMethods())
                    .filter(each -> each.getName().equals("setMessage") && each.getParameterCount() == 1).findFirst().get();
            setChatMessageContentMethod = Arrays.stream(ChatMessage.class.getMethods())
                    .filter(each -> each.getName().equals("setChatContent") && each.getParameterCount() == 1).findFirst().get();
            setSystemChatMessageMethod = Arrays.stream(WrapperPlayServerSystemChatMessage.class.getMethods())
                    .filter(each -> each.getName().equals("setMessage") && each.getParameterCount() == 1).findFirst().get();
            setTitleMethod = Arrays.stream(WrapperPlayServerTitle.class.getMethods())
                    .filter(each -> each.getName().equals("setTitle") && each.getParameterCount() == 1).findFirst().get();
            setTitleTextMethod = Arrays.stream(WrapperPlayServerSetTitleText.class.getMethods())
                    .filter(each -> each.getName().equals("setTitle") && each.getParameterCount() == 1).findFirst().get();
            setSubtitleTextMethod = Arrays.stream(WrapperPlayServerSetTitleSubtitle.class.getMethods())
                    .filter(each -> each.getName().equals("setSubtitle") && each.getParameterCount() == 1).findFirst().get();
            setActionBarTextMethod = Arrays.stream(WrapperPlayServerActionBar.class.getMethods())
                    .filter(each -> each.getName().equals("setActionBarText") && each.getParameterCount() == 1).findFirst().get();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public PEOutMessagePacket() {
        initializePacketHandlers();
        SERVICE = new PacketEventsAsyncChatSendingExecutor(
                () -> (long) (InteractiveChat.bungeecordMode ? InteractiveChat.remoteDelay : 0) + 2000,
                5000
        );
    }

    private void initializePacketHandlers() {
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19_3)) {
            initializeModernPacketHandlers();
        }
        initializeCommonPacketHandlers();
    }

    private void initializeModernPacketHandlers() {
        PACKET_HANDLERS.put(PacketType.Play.Server.DISGUISED_CHAT, new PacketHandler(
                event -> InteractiveChat.chatListener,
                packet -> new PacketAccessorResult(
                        ((WrapperPlayServerDisguisedChat) packet).getMessage(),
                        ChatComponentType.AdventureComponent,
                        0,
                        false
                ),
                (packet, component, type, field, sender) -> {
                    try {
                        boolean legacyRGB = InteractiveChat.version.isLegacyRGB();

                        String json = legacyRGB ?
                                InteractiveChatComponentSerializer.legacyGson().serialize(component) :
                                InteractiveChatComponentSerializer.gson().serialize(component);
                        boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;

                        WrapperPlayServerDisguisedChat chatPacket = (WrapperPlayServerDisguisedChat) packet;
                        setDisguisedChatMessageMethod.invoke(chatPacket, type.convertTo(component, legacyRGB));

                        return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // This should never happen!
                    return null;
                }
        ));
    }

    private void initializeCommonPacketHandlers() {
        int chatFieldSize = getChatFieldsSize();
        PacketHandler modernTitleHandler = createModernTitleHandler();

        PACKET_HANDLERS.put(
                InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19) ?
                        PacketType.Play.Server.SYSTEM_CHAT_MESSAGE :
                        PacketType.Play.Server.CHAT_MESSAGE,
                new PacketHandler(
                        event -> {
                            if (event.getPacketType().equals(PacketType.Play.Server.CHAT_MESSAGE)) {
                                WrapperPlayServerChatMessage messagePacket = new WrapperPlayServerChatMessage(event);

                                ChatType type = messagePacket.getMessage().getType();

                                return type == null || type.equals(ChatTypes.GAME_INFO) ?
                                        InteractiveChat.titleListener :
                                        InteractiveChat.chatListener;
                            }

                            WrapperPlayServerSystemChatMessage messagePacket = new WrapperPlayServerSystemChatMessage(event);
                            return messagePacket.isOverlay() ?
                                    InteractiveChat.titleListener :
                                    InteractiveChat.chatListener;
                        },
                        packet -> {
                            Component component = packet.readComponent();
                            return new PacketAccessorResult(component, ChatComponentType.AdventureComponent, 0, false);
                        },
                        (packet, component, type, field, sender) -> {
                            boolean legacyRGB = InteractiveChat.version.isLegacyRGB();

                            String json = legacyRGB ?
                                    InteractiveChatComponentSerializer.legacyGson().serialize(component) :
                                    InteractiveChatComponentSerializer.gson().serialize(component);
                            boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;

                            if (type.canHandle(component)) {
                                try {
                                    if (packet instanceof WrapperPlayServerChatMessage) {
                                        WrapperPlayServerChatMessage chatMessage = (WrapperPlayServerChatMessage) packet;
                                        setChatMessageContentMethod.invoke(chatMessage.getMessage(), type.convertTo(component, legacyRGB));
                                    } else {
                                        WrapperPlayServerSystemChatMessage chatMessage = (WrapperPlayServerSystemChatMessage) packet;
                                        setSystemChatMessageMethod.invoke(chatMessage, type.convertTo(component, legacyRGB));
                                    }
                                } catch (Throwable e) {
                                    try {
                                        if (packet instanceof WrapperPlayServerChatMessage) {
                                            setChatMessageContentMethod.invoke(
                                                    ((WrapperPlayServerChatMessage) packet).getMessage(),
                                                    NativeAdventureConverter.componentToNative(component, legacyRGB)
                                            );
                                        } else {
                                            ((WrapperPlayServerSystemChatMessage) packet).setMessageJson(json);
                                        }
                                    } catch (Throwable ignored) {}
                                }
                            }


                            if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19_3)) {
                                if (sender != null) {
                                    if (packet.readUUID() != null) {
                                        packet.writeUUID(sender);
                                    }
                                }
                            } else {
                                if (sender == null) {
                                    sender = UUID_NIL;
                                }

                                if (packet.readUUID() != null) {
                                    packet.writeUUID(sender);
                                }
                            }

                            return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
                        }
                )
        );

        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_17)) {
            PACKET_HANDLERS.put(PacketType.Play.Server.SET_TITLE_TEXT, modernTitleHandler);
            PACKET_HANDLERS.put(PacketType.Play.Server.SET_TITLE_SUBTITLE, modernTitleHandler);
            PACKET_HANDLERS.put(PacketType.Play.Server.ACTION_BAR, modernTitleHandler);
        } else {
            PACKET_HANDLERS.put(
                    PacketType.Play.Server.TITLE,
                    new PacketHandler(
                            event -> {
                                WrapperPlayServerTitle.TitleAction action = ((WrapperPlayServerTitle) event.getLastUsedWrapper()).getAction();
                                return action != null && !action.equals(WrapperPlayServerTitle.TitleAction.RESET) &&
                                        !action.equals(WrapperPlayServerTitle.TitleAction.HIDE) && !action.equals(WrapperPlayServerTitle.TitleAction.SET_TIMES_AND_DISPLAY) && InteractiveChat.titleListener;
                            },
                            packet -> {
                                Component component = packet.readComponent();
                                return new PacketAccessorResult(component, ChatComponentType.AdventureComponent, 0, false);
                            },
                            (packet, component, type, field, sender) -> {
                                try {
                                    boolean legacyRGB = InteractiveChat.version.isLegacyRGB();

                                    String json = legacyRGB ?
                                            InteractiveChatComponentSerializer.legacyGson().serialize(component) :
                                            InteractiveChatComponentSerializer.gson().serialize(component);
                                    boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong &&
                                            json.length() > InteractiveChat.packetStringMaxLength;

                                    WrapperPlayServerTitle titlePacket = (WrapperPlayServerTitle) packet;
                                    setTitleMethod.invoke(titlePacket, type.convertTo(component, legacyRGB));

                                    if (sender == null) sender = UUID_NIL;

                                    return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                return null;
                            }
                    )
            );
        }
    }

    private PacketHandler createModernTitleHandler() {
        return new PacketHandler(
                event -> InteractiveChat.titleListener,
                packet -> {
                    Component component = packet.readComponent();
                    return new PacketAccessorResult(component, ChatComponentType.AdventureComponent, 0, false);
                },
                (packet, component, type, field, sender) -> {
                    try {
                        boolean legacyRGB = InteractiveChat.version.isLegacyRGB();

                        String json = legacyRGB ?
                                InteractiveChatComponentSerializer.legacyGson().serialize(component) :
                                InteractiveChatComponentSerializer.gson().serialize(component);
                        boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;

                        Object convertedType = type.convertTo(component, legacyRGB);

                        if (packet instanceof WrapperPlayServerSetTitleText) {
                            setTitleTextMethod.invoke(
                                    packet,
                                    convertedType
                            );
                        } else if (packet instanceof WrapperPlayServerSetTitleSubtitle) {
                            setSubtitleTextMethod.invoke(
                                    packet,
                                    convertedType
                            );
                        } else if (packet instanceof WrapperPlayServerActionBar) {
                            setActionBarTextMethod.invoke(
                                    packet,
                                    convertedType
                            );
                        }

                        return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return null;
                }
        );
    }

    private int getChatFieldsSize() {
        return InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_13) ? 262144 : 32767;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!PACKET_HANDLERS.containsKey(event.getPacketType())) return;

        handlePacketSending(event);
    }

    private void handlePacketSending(PacketSendEvent event) {
        try {
            if (event.isCancelled()) return;

            PacketHandler packetHandler = PACKET_HANDLERS.get(event.getPacketType());
            if (!packetHandler.getPreFilter().test(event)) return;

            InteractiveChat.messagesCounter.getAndIncrement();

            Player receiver = event.getPlayer();
            event.setCancelled(true);
            UUID messageUUID = UUID.randomUUID();
            ICPlayer determinedSender = packetHandler.getDeterminedSenderFunction().apply(event);

            SCHEDULING_SERVICE.execute(() -> {
                SERVICE.execute(() -> {
                    processPacket(receiver, determinedSender, event, messageUUID, false, packetHandler);
                }, receiver, messageUUID);
            });


        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void processPacket(Player receiver, ICPlayer determinedSender, PacketSendEvent event, UUID messageUUID, boolean isFiltered, PacketHandler packetHandler) {
        PacketSendEvent originalEvent = event.clone();
        PacketWrapper<?> originalWrapper = originalEvent.getLastUsedWrapper();
        PacketWrapper<?> packet = event.getLastUsedWrapper();

        try {
            if (packetHandler.getAccessor() == null) {
                SERVICE.send(packet, receiver, messageUUID);
                return;
            }

            PacketAccessorResult packetAccessorResult = packetHandler.getAccessor().apply(packet);
            Component component = packetAccessorResult.getComponent();
            ChatComponentType type = packetAccessorResult.getType();

            if (type == null || component == null) {
                SERVICE.send(packet, receiver, messageUUID);
                return;
            }

            component = ComponentModernizing.modernize(component);
            String legacyText = LegacyComponentSerializer.legacySection().serializeOr(component, "");
            try {
                if (legacyText.isEmpty() || InteractiveChat.messageToIgnore.stream().anyMatch(legacyText::matches)) {
                    SERVICE.send(packet, receiver, messageUUID);
                    return;
                }
            } catch (Exception e) {
                SERVICE.send(packet, receiver, messageUUID);
                return;
            }

            if (InteractiveChat.version.isOld() && JsonUtils.containsKey(InteractiveChatComponentSerializer.gson().serialize(component), "translate")) {
                SERVICE.send(packet, receiver, messageUUID);
                return;
            }

            Optional<ICPlayer> sender = Optional.ofNullable(determinedSender);
            String rawMessageKey = PlainTextComponentSerializer.plainText().serializeOr(component, "");
            InteractiveChat.keyTime.putIfAbsent(rawMessageKey, System.currentTimeMillis());
            Long timeKey = InteractiveChat.keyTime.get(rawMessageKey);
            long unix = timeKey == null ? System.currentTimeMillis() : timeKey;
            ProcessSenderResult commandSender = ProcessCommands.process(component);

            if (!sender.isPresent()) {
                if (commandSender.getSender() != null) {
                    ICPlayer icPlayer = ICPlayerFactory.getICPlayer(commandSender.getSender());
                    if (icPlayer != null) {
                        sender = Optional.of(icPlayer);
                    }
                }
            }

            ProcessSenderResult chatSender = null;
            if (!sender.isPresent()) {
                if (InteractiveChat.useAccurateSenderFinder) {
                    chatSender = ProcessAccurateSender.process(component);
                    if (chatSender.getSender() != null) {
                        ICPlayer icPlayer = ICPlayerFactory.getICPlayer(chatSender.getSender());
                        if (icPlayer != null) {
                            sender = Optional.of(icPlayer);
                        }
                    }
                }
            }

            if (!sender.isPresent() && !InteractiveChat.useAccurateSenderFinder) {
                sender = SenderFinder.getSender(component, rawMessageKey);
            }

            if (sender.isPresent() && !sender.get().isLocal()) {
                if (isFiltered) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(InteractiveChat.plugin, () -> {
                        SERVICE.execute(() -> {
                            processPacket(receiver, determinedSender, event, messageUUID, false, packetHandler);
                        }, receiver, messageUUID);
                    }, (int) Math.ceil((double) InteractiveChat.remoteDelay / 50) + InteractiveChat.extraProxiedPacketProcessingDelay);

                    return;
                }
            }

            component = commandSender.getComponent();
            if (chatSender != null) {
                component = chatSender.getComponent();
            }

            sender.ifPresent(icPlayer -> InteractiveChat.keyPlayer.put(rawMessageKey, icPlayer));
            component = ComponentReplacing.replace(component, Registry.ID_PATTERN.pattern(), Registry.ID_PATTERN_REPLACEMENT);
            UUID preEventSenderUUID = sender.map(ICPlayer::getUniqueId).orElse(null);

            PrePacketComponentProcessEvent preEvent = new PrePacketComponentProcessEvent(!Bukkit.isPrimaryThread(), receiver, component, preEventSenderUUID);
            Bukkit.getPluginManager().callEvent(preEvent);

            if (preEvent.getSender() != null) {
                Player newSender = Bukkit.getPlayer(preEvent.getSender());
                if (newSender != null) {
                    sender = Optional.of(ICPlayerFactory.getICPlayer(newSender));
                }
            }
            component = preEvent.getComponent();

            if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16) && InteractiveChat.fontTags) {
                if (!sender.isPresent() || PlayerUtils.hasPermission(sender.get().getUniqueId(), "interactivechat.customfont.translate", true, 250)) {
                    component = ComponentFont.parseFont(component);
                }
            }

            if (InteractiveChat.translateHoverableItems && InteractiveChat.itemGUI) {
                component = HoverableItemDisplay.process(component, receiver);
            }

            if (InteractiveChat.usePlayerName) {
                component = PlayernameDisplay.process(component, sender, receiver, unix);
            }

            if (InteractiveChat.allowMention && sender.isPresent()) {
                PlayerDataManager.PlayerData data = InteractiveChat.playerDataManager.getPlayerData(receiver);
                if (data == null || !data.isMentionDisabled()) {
                    component = MentionDisplay.process(component, receiver, sender.get(), unix, true);
                }
            }

            component = ComponentReplacing.replace(component, Registry.MENTION_TAG_CONVERTER.getReversePattern().pattern(), true, (result, components) -> {
                return LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.mentionHighlightOthers)).replaceText(TextReplacementConfig.builder().matchLiteral("{MentionedPlayer}").replacement(PlainTextComponentSerializer.plainText().deserialize(result.group(2))).build());
            });
            component = CustomPlaceholderDisplay.process(component, sender, receiver, InteractiveChat.placeholderList.values(), unix);

            if (InteractiveChat.useInventory) {
                component = InventoryDisplay.process(component, sender, receiver, packetAccessorResult.isPreview(), unix);
            }
            if (InteractiveChat.useEnder) {
                component = EnderchestDisplay.process(component, sender, receiver, packetAccessorResult.isPreview(), unix);
            }

            if (InteractiveChat.clickableCommands) {
                component = CommandsDisplay.process(component);
            }

            if (InteractiveChat.useItem) {
                component = ItemDisplay.process(component, sender, receiver, packetAccessorResult.isPreview(), unix);
            }

            if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16) && InteractiveChat.fontTags) {
                if (!sender.isPresent() || (sender.isPresent() && PlayerUtils.hasPermission(sender.get().getUniqueId(), "interactivechat.customfont.translate", true, 250))) {
                    component = ComponentFont.parseFont(component);
                }
            }

            if (!PlayerUtils.canChatColor(receiver)) {
                component = ComponentStyling.stripColor(component);
            }

            if (InteractiveChat.tritonHook) {
                component = TritonHook.parseLanguageChat(sender.map(ICPlayer::getUniqueId).orElse(null), component);
            }

            PostPacketComponentProcessEvent postEvent = new PostPacketComponentProcessEvent(true, receiver, component, preEventSenderUUID);
            Bukkit.getPluginManager().callEvent(postEvent);
            component = postEvent.getComponent();

            PacketWriterResult packetWriterResult = packetHandler.getWriter().apply(packet, component, type, packetAccessorResult.getField(), sender.map(ICPlayer::getUniqueId).orElse(null));
            event.markForReEncode(true);

            boolean longerThanMaxLength = packetWriterResult.isTooLong();
            UUID postEventSenderUUID = packetWriterResult.getSender();
            int jsonLength = packetWriterResult.getJsonLength();

            PreChatPacketSendEvent sendEvent = new PreChatPacketSendEvent(true, receiver, packet, component, postEventSenderUUID, originalWrapper, InteractiveChat.sendOriginalIfTooLong, longerThanMaxLength);
            Bukkit.getPluginManager().callEvent(sendEvent);

            Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
                InteractiveChat.keyTime.remove(rawMessageKey);
                InteractiveChat.keyPlayer.remove(rawMessageKey);
            }, 10);

            if (sendEvent.isCancelled()) {
                if (sendEvent.sendOriginalIfCancelled()) {
                    if (longerThanMaxLength && InteractiveChat.cancelledMessage) {
                        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractiveChat] " +
                                ChatColor.RED + "Cancelled a chat packet bounded to " + receiver.getName() +
                                " that is " + jsonLength + " characters long (Longer than maximum allowed in a chat packet), " +
                                "sending original unmodified message instead! [THIS IS NOT A BUG]");
                    }
                    PacketWrapper<?> originalPacketModified = (PacketWrapper<?>) sendEvent.getOriginal();
                    SERVICE.send(originalPacketModified, receiver, messageUUID);
                    return;
                }

                if (longerThanMaxLength && InteractiveChat.cancelledMessage) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractiveChat] " +
                            ChatColor.RED + "Cancelled a chat packet bounded to " + receiver.getName() +
                            " that is " + jsonLength + " characters long (Longer than maximum allowed in a chat packet) " +
                            "[THIS IS NOT A BUG]");
                }
                SERVICE.discard(receiver.getUniqueId(), messageUUID);
                return;
            }

            SERVICE.send(packet, receiver, messageUUID);
        } catch (Exception e) {
            e.printStackTrace();
            SERVICE.send(originalWrapper, receiver, messageUUID);
        }
    }

    public static class PacketHandler {
        private static final Function<PacketSendEvent, ICPlayer> UNDETERMINED_SENDER = event -> null;

        private final Predicate<PacketSendEvent> preFilter;
        private final Function<PacketSendEvent, ICPlayer> determinedSenderFunction;
        private final PacketAccessor accessor;
        private final PacketWriter writer;

        public PacketHandler(Predicate<PacketSendEvent> preFilter, Function<PacketSendEvent, ICPlayer> determinedSenderFunction,
                             PacketAccessor accessor, PacketWriter writer) {
            this.preFilter = preFilter;
            this.determinedSenderFunction = determinedSenderFunction;
            this.accessor = accessor;
            this.writer = writer;
        }

        public PacketHandler(Predicate<PacketSendEvent> preFilter, PacketAccessor accessor, PacketWriter writer) {
            this(preFilter, UNDETERMINED_SENDER, accessor, writer);
        }

        public Predicate<PacketSendEvent> getPreFilter() {
            return preFilter;
        }

        public Function<PacketSendEvent, ICPlayer> getDeterminedSenderFunction() {
            return determinedSenderFunction;
        }

        public PacketAccessor getAccessor() {
            return accessor;
        }

        public PacketWriter getWriter() {
            return writer;
        }
    }

    public interface PacketAccessor extends Function<PacketWrapper<?>, MessagePacketHandler.PacketAccessorResult> {
        @Override
        MessagePacketHandler.PacketAccessorResult apply(PacketWrapper<?> packet);
    }

    public interface PacketWriter {
        MessagePacketHandler.PacketWriterResult apply(PacketWrapper<?> packet, Component component, ChatComponentType type, int field, UUID sender);
    }
}
