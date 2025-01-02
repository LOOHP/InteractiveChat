/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
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

package com.loohp.interactivechat.bungeemessaging;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.api.InteractiveChatAPI.SharedType;
import com.loohp.interactivechat.api.events.ProxyCustomDataRecievedEvent;
import com.loohp.interactivechat.data.PlayerDataManager.PlayerData;
import com.loohp.interactivechat.modules.ProcessExternalMessage;
import com.loohp.interactivechat.objectholders.BuiltInPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ClickEventAction;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderClickEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderHoverEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderReplaceText;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ParsePlayer;
import com.loohp.interactivechat.objectholders.ICInventoryHolder;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.objectholders.MentionPair;
import com.loohp.interactivechat.objectholders.SignedMessageModificationData;
import com.loohp.interactivechat.objectholders.ValueTrios;
import com.loohp.interactivechat.utils.CustomArrayUtils;
import com.loohp.interactivechat.utils.DataTypeIO;
import com.loohp.interactivechat.utils.InventoryUtils;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class BungeeMessageListener implements PluginMessageListener {

    private final InteractiveChat plugin;
    private final Map<Integer, byte[][]> incoming;
    private final Map<UUID, CompletableFuture<?>> toComplete = new ConcurrentHashMap<>();

    public BungeeMessageListener(InteractiveChat instance) {
        plugin = instance;
        Cache<Integer, byte[][]> incomingCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();
        incoming = incomingCache.asMap();
    }

    public void addToComplete(UUID uuid, CompletableFuture<?> future) {
        toComplete.put(uuid, future);
        InteractiveChat.plugin.getScheduler().runLaterAsync(() -> {
            CompletableFuture<?> f = toComplete.remove(uuid);
            if (f != null && !f.isDone() && !f.isCompletedExceptionally() && !f.isCancelled()) {
                f.completeExceptionally(new TimeoutException("The proxy did not respond in time"));
            }
        }, 400);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onPluginMessageReceived(String channel, Player pluginMessagingPlayer, byte[] bytes) {
        if (!channel.equals("interchat:main")) {
            return;
        }

        InteractiveChat.plugin.getScheduler().runAsync((task) -> {
            try {
                ByteArrayDataInput in = ByteStreams.newDataInput(bytes);

                int packetNumber = in.readInt();
                int packetChunkIndex = in.readInt();
                int packetChunkSize = in.readInt();
                int packetId = in.readShort();
                byte[] data = new byte[bytes.length - 14];
                in.readFully(data);

                byte[][] chunks = incoming.remove(packetNumber);
                if (chunks == null) {
                    chunks = new byte[packetChunkSize][];
                }
                if (chunks.length != packetChunkSize) {
                    byte[][] adjusted = new byte[packetChunkSize][];
                    System.arraycopy(chunks, 0, adjusted, 0, adjusted.length);
                    chunks = adjusted;
                }
                if (packetChunkIndex >= 0 && packetChunkIndex < chunks.length) {
                    chunks[packetChunkIndex] = data;
                }
                if (CustomArrayUtils.anyNull(chunks)) {
                    incoming.put(packetNumber, chunks);
                    return;
                }
                data = new byte[Arrays.stream(chunks).mapToInt(a -> a.length).sum()];
                for (int i = 0, pos = 0; i < chunks.length; i++) {
                    byte[] chunk = chunks[i];
                    System.arraycopy(chunk, 0, data, pos, chunk.length);
                    pos += chunk.length;
                }

                if (InteractiveChat.pluginMessagePacketVerbose) {
                    Bukkit.getConsoleSender().sendMessage("IC Inbound - ID " + packetId + " via " + pluginMessagingPlayer.getName());
                }
                ByteArrayDataInput input = ByteStreams.newDataInput(data);

                switch (packetId) {
                    case 0x00:
                        int playerAmount = input.readInt();
                        Set<UUID> localUUID = Bukkit.getOnlinePlayers().stream().map(each -> each.getUniqueId()).collect(Collectors.toSet());
                        Set<UUID> current = new HashSet<>(ICPlayerFactory.getRemoteUUIDs());
                        Set<UUID> newSet = new HashSet<>();
                        for (int i = 0; i < playerAmount; i++) {
                            String server = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                            UUID uuid = DataTypeIO.readUUID(input);
                            String name = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                            ICPlayer player = ICPlayerFactory.getICPlayer(uuid);
                            if (player != null) {
                                if (!player.getRemoteServer().equals(server)) {
                                    player.setRemoteServer(server);
                                }
                            }
                            if (!localUUID.contains(uuid) && !ICPlayerFactory.getRemoteUUIDs().contains(uuid)) {
                                ICPlayerFactory.createOrUpdateRemoteICPlayer(server, name, uuid, true, 0, 0, Bukkit.createInventory(ICInventoryHolder.INSTANCE, 45), Bukkit.createInventory(ICInventoryHolder.INSTANCE, InventoryUtils.getDefaultEnderChestSize()), false);
                            }
                            newSet.add(uuid);
                        }
                        current.removeAll(newSet);
                        for (UUID uuid : current) {
                            ICPlayerFactory.removeRemoteICPlayer(uuid);
                        }
                        for (UUID uuid : localUUID) {
                            ICPlayerFactory.removeRemoteICPlayer(uuid);
                        }
                        break;
                    case 0x01:
                        int delay = input.readInt();
                        short itemStackScheme = input.readShort();
                        short inventoryScheme = input.readShort();
                        InteractiveChat.remoteDelay = delay;
                        BungeeMessageSender.itemStackScheme = itemStackScheme;
                        BungeeMessageSender.inventoryScheme = inventoryScheme;
                        break;
                    case 0x02:
                        UUID sender = DataTypeIO.readUUID(input);
                        UUID receiver = DataTypeIO.readUUID(input);
                        InteractiveChat.mentionPair.add(new MentionPair(sender, receiver));
                        break;
                    case 0x03:
                        UUID uuid = DataTypeIO.readUUID(input);
                        ICPlayer player = ICPlayerFactory.getICPlayer(uuid);
                        if (player == null) {
                            break;
                        }
                        boolean rightHanded = input.readBoolean();
                        player.setRemoteRightHanded(rightHanded);
                        int selectedSlot = input.readByte();
                        player.setRemoteSelectedSlot(selectedSlot);
                        int level = input.readInt();
                        player.setRemoteExperienceLevel(level);
                        int size = input.readByte();
                        ItemStack[] equipment = new ItemStack[size];
                        for (int i = 0; i < equipment.length; i++) {
                            equipment[i] = DataTypeIO.readItemStack(input, StandardCharsets.UTF_8);
                        }
                        player.getEquipment().setHelmet(equipment[0]);
                        player.getEquipment().setChestplate(equipment[1]);
                        player.getEquipment().setLeggings(equipment[2]);
                        player.getEquipment().setBoots(equipment[3]);
                        if (InteractiveChat.version.isOld()) {
                            player.getEquipment().setItemInHand(equipment[4]);
                        } else {
                            player.getEquipment().setItemInMainHand(equipment[4]);
                            player.getEquipment().setItemInOffHand(equipment[5]);
                        }
                        break;
                    case 0x04:
                        UUID uuid1 = DataTypeIO.readUUID(input);
                        ICPlayer player1 = ICPlayerFactory.getICPlayer(uuid1);
                        if (player1 == null) {
                            break;
                        }
                        boolean rightHanded1 = input.readBoolean();
                        player1.setRemoteRightHanded(rightHanded1);
                        int selectedSlot1 = input.readByte();
                        player1.setRemoteSelectedSlot(selectedSlot1);
                        int level1 = input.readInt();
                        player1.setRemoteExperienceLevel(level1);
                        int type = input.readByte();
                        if (type == 0) {
                            player1.setRemoteInventory(DataTypeIO.readInventory(input, StandardCharsets.UTF_8, null));
                        } else {
                            player1.setRemoteEnderChest(DataTypeIO.readInventory(input, StandardCharsets.UTF_8, null));
                        }
                        break;
                    case 0x05:
                        UUID uuid2 = DataTypeIO.readUUID(input);
                        ICPlayer player2 = ICPlayerFactory.getICPlayer(uuid2);
                        if (player2 == null) {
                            break;
                        }
                        int size1 = input.readInt();
                        for (int i = 0; i < size1; i++) {
                            String placeholder = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                            String text = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                            player2.getRemotePlaceholdersMapping().put(placeholder, text);
                        }
                        break;
                    case 0x06:
                        String message = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                        UUID uuid3 = DataTypeIO.readUUID(input);
                        ICPlayer player3 = ICPlayerFactory.getICPlayer(uuid3);
                        if (player3 == null) {
                            break;
                        }
                        InteractiveChat.messages.put(message, uuid3);
                        InteractiveChat.plugin.getScheduler().runLater((inner) -> InteractiveChat.messages.remove(message), 60);
                        break;
                    case 0x07:
                        int cooldownType = input.readByte();
                        switch (cooldownType) {
                            case 0:
                                UUID uuid4 = DataTypeIO.readUUID(input);
                                long time = input.readLong();
                                InteractiveChat.placeholderCooldownManager.setPlayerUniversalLastTimestampRaw(uuid4, time);
                                break;
                            case 1:
                                uuid4 = DataTypeIO.readUUID(input);
                                UUID internalId = DataTypeIO.readUUID(input);
                                time = input.readLong();
                                Optional<ICPlaceholder> optPlaceholder = InteractiveChat.placeholderList.values().stream().filter(each -> each.getInternalId().equals(internalId)).findFirst();
                                if (optPlaceholder.isPresent()) {
                                    InteractiveChat.placeholderCooldownManager.setPlayerPlaceholderLastTimestampRaw(uuid4, optPlaceholder.get(), time);
                                }
                                break;
                        }
                        break;
                    case 0x08:
                        UUID messageId = DataTypeIO.readUUID(input);
                        UUID uuid5 = DataTypeIO.readUUID(input);
                        Player bukkitplayer1 = Bukkit.getPlayer(uuid5);
                        if (bukkitplayer1 == null) {
                            break;
                        }
                        String component = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                        boolean preview = input.readBoolean();
                        String processed = ProcessExternalMessage.processAndRespond(bukkitplayer1, component, preview);
                        BungeeMessageSender.respondProcessedMessage(System.currentTimeMillis(), processed, messageId);
                        break;
                    case 0x09:
                        String server = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                        int size2 = input.readInt();
                        List<ICPlaceholder> list = new ArrayList<>(size2);
                        for (int i = 0; i < size2; i++) {
                            boolean isBuiltIn = input.readBoolean();
                            if (isBuiltIn) {
                                String keyword = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                String name = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                String description = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                String permission = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                long cooldown = input.readLong();
                                list.add(new BuiltInPlaceholder(Pattern.compile(keyword), name, description, permission, cooldown));
                            } else {
                                String key = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                ParsePlayer parseplayer = ParsePlayer.fromOrder(input.readByte());
                                String placeholder = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                boolean parseKeyword = input.readBoolean();
                                long cooldown = input.readLong();
                                boolean hoverEnabled = input.readBoolean();
                                String hoverText = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                boolean clickEnabled = input.readBoolean();
                                String clickAction = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                String clickValue = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                boolean replaceEnabled = input.readBoolean();
                                String replaceText = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                String name = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                String description = DataTypeIO.readString(input, StandardCharsets.UTF_8);

                                list.add(new CustomPlaceholder(key, parseplayer, Pattern.compile(placeholder), parseKeyword, cooldown, new CustomPlaceholderHoverEvent(hoverEnabled, hoverText), new CustomPlaceholderClickEvent(clickEnabled, clickEnabled ? ClickEventAction.valueOf(clickAction) : null, clickValue), new CustomPlaceholderReplaceText(replaceEnabled, replaceText), name, description));
                            }
                        }
                        InteractiveChat.remotePlaceholderList.put(server, list);
                        break;
                    case 0x0A:
                        BungeeMessageSender.resetAndForwardPlaceholderList(System.currentTimeMillis(), InteractiveChat.placeholderList.values());
                        break;
                    case 0x0B:
                        int id = input.readInt();
                        UUID playerUUID = DataTypeIO.readUUID(input);
                        String permission = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                        Player player5 = Bukkit.getPlayer(playerUUID);
                        BungeeMessageSender.permissionCheckResponse(System.currentTimeMillis(), id, player5 != null && player5.hasPermission(permission));
                        break;
                    case 0x0D:
                        UUID playerUUID1 = DataTypeIO.readUUID(input);
                        PlayerData pd = InteractiveChat.playerDataManager.getPlayerData(playerUUID1);
                        if (pd != null) {
                            pd.reload();
                        }
                        break;
                    case 0x0E:
                        SharedType sharedType = SharedType.fromValue(input.readByte());
                        String sha1 = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                        Inventory inventory = DataTypeIO.readInventory(input, StandardCharsets.UTF_8, null);
                        InteractiveChatAPI.addInventoryToItemShareList(sharedType, sha1, inventory);
                        break;
                    case 0x0F:
                        int requestType = input.readByte();
                        UUID playerUUID2 = DataTypeIO.readUUID(input);
                        Player player6 = Bukkit.getPlayer(playerUUID2);
                        if (player6 != null) {
                            ICPlayer player7 = ICPlayerFactory.getICPlayer(player6);
                            switch (requestType) {
                                case 0:
                                    BungeeMessageSender.forwardInventory(System.currentTimeMillis(), player7.getUniqueId(), player7.isRightHanded(), player7.getSelectedSlot(), player7.getExperienceLevel(), null, player7.getInventory());
                                    break;
                                case 1:
                                    BungeeMessageSender.forwardEnderchest(System.currentTimeMillis(), player7.getUniqueId(), player7.isRightHanded(), player7.getSelectedSlot(), player7.getExperienceLevel(), null, player7.getEnderChest());
                                    break;
                            }
                        }
                        break;
                    case 0x10:
                        UUID requestUUID = DataTypeIO.readUUID(input);
                        int requestType2 = input.readByte();
                        //noinspection SwitchStatementWithTooFewBranches
                        switch (requestType2) {
                            case 0:
                                List<ValueTrios<UUID, String, Integer>> playerlist = new ArrayList<>();
                                int playerListSize = input.readInt();
                                for (int i = 0; i < playerListSize; i++) {
                                    playerlist.add(new ValueTrios<>(DataTypeIO.readUUID(input), DataTypeIO.readString(input, StandardCharsets.UTF_8), input.readInt()));
                                }
                                @SuppressWarnings("unchecked")
                                CompletableFuture<List<ValueTrios<UUID, String, Integer>>> future = (CompletableFuture<List<ValueTrios<UUID, String, Integer>>>) toComplete.remove(requestUUID);
                                if (future != null) {
                                    future.complete(playerlist);
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    case 0x11:
                        UUID playerUUID3 = DataTypeIO.readUUID(input);
                        int nicknameSize = input.readInt();
                        Set<String> remoteNicknames = new HashSet<>();
                        for (int i = 0; i < nicknameSize; i++) {
                            remoteNicknames.add(DataTypeIO.readString(input, StandardCharsets.UTF_8));
                        }
                        ICPlayer icPlayer = ICPlayerFactory.getICPlayer(playerUUID3);
                        if (icPlayer != null) {
                            icPlayer.setRemoteNicknames(remoteNicknames);
                        }
                        break;
                    case 0x12:
                        UUID playerUUID4 = DataTypeIO.readUUID(input);
                        String placeholders = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                        ICPlayer icPlayer1 = ICPlayerFactory.getICPlayer(playerUUID4);
                        if (icPlayer1 != null && icPlayer1.isLocal()) {
                            PlaceholderParser.parse(icPlayer1, placeholders);
                        }
                        break;
                    case 0x13:
                        UUID senderUUID = DataTypeIO.readUUID(input);
                        String originalMessage = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                        String modifiedMessage = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                        long time = input.readLong();
                        ICPlayer senderPlayer = ICPlayerFactory.getICPlayer(senderUUID);
                        if (senderPlayer != null && senderPlayer.isLocal()) {
                            List<SignedMessageModificationData> modData = InteractiveChat.signedMessageModificationData.get(senderUUID);
                            if (modData == null) {
                                InteractiveChat.signedMessageModificationData.putIfAbsent(senderUUID, Collections.synchronizedList(new LinkedList<>()));
                                modData = InteractiveChat.signedMessageModificationData.get(senderUUID);
                            }
                            modData.add(new SignedMessageModificationData(senderUUID, time, originalMessage, modifiedMessage));
                        }
                        break;
                    case 0x14:
                        int size3 = input.readInt();
                        for (int i = 0; i < size3; i++) {
                            UUID player4 = DataTypeIO.readUUID(input);
                            boolean vanished = input.readBoolean();
                            ICPlayer icPlayer2 = ICPlayerFactory.getICPlayer(player4);
                            if (icPlayer2 != null) {
                                icPlayer2.setRemoteVanished(vanished);
                            }
                        }
                        break;
                    case 0x15:
                        UUID playerUUID5 = DataTypeIO.readUUID(input);
                        String command = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                        Player player4 = Bukkit.getPlayer(playerUUID5);
                        if (player4 != null) {
                            InteractiveChat.plugin.getScheduler().runNextTick((inner) -> PlayerUtils.dispatchCommandAsPlayer(player4, command));
                        }
                        break;
                    case 0xFF:
                        String customChannel = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                        int dataLength = input.readInt();
                        byte[] customData = new byte[dataLength];
                        input.readFully(customData);
                        ProxyCustomDataRecievedEvent dataEvent = new ProxyCustomDataRecievedEvent(customChannel, customData);
                        Bukkit.getPluginManager().callEvent(dataEvent);
                        break;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

}
