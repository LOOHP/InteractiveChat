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

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI.SharedType;
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderClickEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderHoverEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderReplaceText;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.ValuePairs;
import com.loohp.interactivechat.objectholders.ValueTrios;
import com.loohp.interactivechat.utils.CustomArrayUtils;
import com.loohp.interactivechat.utils.DataTypeIO;
import com.loohp.interactivechat.utils.HashUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.regex.Pattern;

public class BungeeMessageSender {

    public static final Pattern VALID_CUSTOM_CHANNEL = Pattern.compile("[a-z]+:[a-z0-9_]+");
    private static final Random random = new Random();
    private static final ConcurrentSkipListMap<Long, Set<String>> sent = new ConcurrentSkipListMap<>();
    protected static short itemStackScheme = 0;
    protected static short inventoryScheme = 0;

    static {
        InteractiveChat.plugin.getScheduler().runTimerAsync((task) -> {
            int size = sent.size();
            for (int i = size; i > 500; i--) {
                sent.remove(sent.firstKey());
            }
        }, 1200, 1200);
    }

    public static int getItemStackScheme() {
        return itemStackScheme;
    }

    public static int getInventoryScheme() {
        return inventoryScheme;
    }

    public static boolean forwardData(long time, int packetId, byte[] data) throws Exception {
        long index = (time << 16) + packetId;
        String hash = HashUtils.createSha1String(new ByteArrayInputStream(data));

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        if (players.size() == 0) {
            return false;
        }
        Player player = players.stream().skip(random.nextInt(players.size())).findAny().orElse(null);
        if (player == null) {
            return false;
        }

        synchronized (sent) {
            Set<String> cacheData = sent.get(index);
            if (cacheData != null && cacheData.contains(hash)) {
                return false;
            }

            if (cacheData != null) {
                cacheData.add(hash);
            } else {
                Set<String> newSet = new HashSet<>();
                newSet.add(hash);
                sent.put(index, newSet);
            }
        }

        if (InteractiveChat.pluginMessagePacketVerbose) {
            Bukkit.getConsoleSender().sendMessage("IC Outbound - ID " + packetId + " via " + player.getName());
        }

        int packetNumber = random.nextInt();
        try {
            byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

            for (int i = 0; i < dataArray.length; i++) {
                byte[] chunk = dataArray[i];

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeInt(packetNumber); //random packet number
                out.writeInt(i); //packet chunk index
                out.writeInt(dataArray.length); //packet total chunks
                out.writeShort(packetId); //packet id

                out.write(chunk);
                player.sendPluginMessage(InteractiveChat.plugin, "interchat:main", out.toByteArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean forwardCustomData(long time, String channel, byte[] data) throws Exception {
        if (!VALID_CUSTOM_CHANNEL.matcher(channel).matches()) {
            throw new IllegalArgumentException("Channel name must satisfy this pattern " + VALID_CUSTOM_CHANNEL.pattern());
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        DataTypeIO.writeString(out, channel, StandardCharsets.UTF_8);
        out.writeInt(data.length);
        out.write(data);
        return forwardData(time, 0xFF, out.toByteArray());
    }

    public static boolean forwardMentionPair(long time, UUID sender, UUID receiver) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        DataTypeIO.writeUUID(out, sender);
        DataTypeIO.writeUUID(out, receiver);
        return forwardData(time, 0x02, out.toByteArray());
    }

    public static boolean forwardEquipment(long time, UUID player, boolean rightHanded, int selectedSlot, int level, ItemStack... equipment) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        DataTypeIO.writeUUID(out, player);
        out.writeBoolean(rightHanded);
        out.writeByte(selectedSlot);
        out.writeInt(level);
        out.writeByte(equipment.length);
        for (ItemStack itemStack : equipment) {
            DataTypeIO.writeItemStack(out, itemStackScheme, itemStack, StandardCharsets.UTF_8);
        }
        return forwardData(time, 0x03, out.toByteArray());
    }

    public static boolean forwardInventory(long time, UUID player, boolean rightHanded, int selectedSlot, int level, String title, Inventory inventory) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        DataTypeIO.writeUUID(out, player);
        out.writeBoolean(rightHanded);
        out.writeByte(selectedSlot);
        out.writeInt(level);
        out.writeByte(0);
        DataTypeIO.writeInventory(out, inventoryScheme, title, inventory, StandardCharsets.UTF_8);
        return forwardData(time, 0x04, out.toByteArray());
    }

    public static boolean forwardEnderchest(long time, UUID player, boolean rightHanded, int selectedSlot, int level, String title, Inventory enderchest) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        DataTypeIO.writeUUID(out, player);
        out.writeBoolean(rightHanded);
        out.writeByte(selectedSlot);
        out.writeInt(level);
        out.writeByte(1);
        DataTypeIO.writeInventory(out, inventoryScheme, title, enderchest, StandardCharsets.UTF_8);
        return forwardData(time, 0x04, out.toByteArray());
    }

    public static boolean forwardPlaceholders(long time, UUID player, List<ValuePairs<String, String>> pairs) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        DataTypeIO.writeUUID(out, player);
        out.writeInt(pairs.size());
        for (ValuePairs<String, String> pair : pairs) {
            DataTypeIO.writeString(out, pair.getFirst(), StandardCharsets.UTF_8);
            DataTypeIO.writeString(out, pair.getSecond(), StandardCharsets.UTF_8);
        }
        return forwardData(time, 0x05, out.toByteArray());
    }

    public static boolean addMessage(long time, String message, UUID player) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        DataTypeIO.writeString(out, message, StandardCharsets.UTF_8);
        DataTypeIO.writeUUID(out, player);
        return forwardData(time, 0x06, out.toByteArray());
    }

    public static boolean sendPlayerUniversalCooldown(UUID player, long time) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(0);
        DataTypeIO.writeUUID(out, player);
        out.writeLong(time);
        return forwardData(time, 0x07, out.toByteArray());
    }

    public static boolean sendPlayerPlaceholderCooldown(UUID player, ICPlaceholder placeholder, long time) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(1);
        DataTypeIO.writeUUID(out, player);
        DataTypeIO.writeUUID(out, placeholder.getInternalId());
        out.writeLong(time);
        return forwardData(time, 0x07, out.toByteArray());
    }

    public static boolean respondProcessedMessage(long time, String component, UUID messageId) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        DataTypeIO.writeUUID(out, messageId);
        DataTypeIO.writeString(out, component, StandardCharsets.UTF_8);
        return forwardData(time, 0x08, out.toByteArray());
    }

    public static boolean reloadBungeeConfig(long time) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        return forwardData(time, 0x09, out.toByteArray());
    }

    public static boolean permissionCheckResponse(long time, int id, boolean value) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(id);
        out.writeBoolean(value);
        return forwardData(time, 0x0B, out.toByteArray());
    }

    public static boolean resetAndForwardPlaceholderList(long time, Collection<ICPlaceholder> placeholderList) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(placeholderList.size());
        for (ICPlaceholder placeholder : placeholderList) {
            boolean isBuiltIn = placeholder.isBuildIn();
            out.writeBoolean(isBuiltIn);
            if (isBuiltIn) {
                DataTypeIO.writeString(out, placeholder.getKeyword().pattern(), StandardCharsets.UTF_8);
                DataTypeIO.writeString(out, placeholder.getName(), StandardCharsets.UTF_8);
                DataTypeIO.writeString(out, placeholder.getDescription(), StandardCharsets.UTF_8);
                DataTypeIO.writeString(out, placeholder.getPermission(), StandardCharsets.UTF_8);
                out.writeLong(placeholder.getCooldown());
            } else {
                CustomPlaceholder customPlaceholder = (CustomPlaceholder) placeholder;
                DataTypeIO.writeString(out, customPlaceholder.getKey(), StandardCharsets.UTF_8);
                out.writeByte(customPlaceholder.getParsePlayer().getOrder());
                DataTypeIO.writeString(out, customPlaceholder.getKeyword().pattern(), StandardCharsets.UTF_8);
                out.writeBoolean(customPlaceholder.getParseKeyword());
                out.writeLong(customPlaceholder.getCooldown());

                CustomPlaceholderHoverEvent hover = customPlaceholder.getHover();
                out.writeBoolean(hover.isEnabled());
                DataTypeIO.writeString(out, hover.getText(), StandardCharsets.UTF_8);

                CustomPlaceholderClickEvent click = customPlaceholder.getClick();
                out.writeBoolean(click.isEnabled());
                DataTypeIO.writeString(out, click.getAction() == null ? "" : click.getAction().name(), StandardCharsets.UTF_8);
                DataTypeIO.writeString(out, click.getValue(), StandardCharsets.UTF_8);

                CustomPlaceholderReplaceText replace = customPlaceholder.getReplace();
                out.writeBoolean(replace.isEnabled());
                DataTypeIO.writeString(out, replace.getReplaceText(), StandardCharsets.UTF_8);

                DataTypeIO.writeString(out, placeholder.getName(), StandardCharsets.UTF_8);
                DataTypeIO.writeString(out, placeholder.getDescription(), StandardCharsets.UTF_8);
            }
        }
        return forwardData(time, 0x0C, out.toByteArray());
    }

    public static boolean signalPlayerDataReload(long time, UUID uuid) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        DataTypeIO.writeUUID(out, uuid);
        return forwardData(time, 0x0D, out.toByteArray());
    }

    public static boolean addInventory(long time, SharedType type, String sha1, String title, Inventory inventory) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(type.getValue());
        DataTypeIO.writeString(out, sha1, StandardCharsets.UTF_8);
        DataTypeIO.writeInventory(out, inventoryScheme, title, inventory, StandardCharsets.UTF_8);
        return forwardData(time, 0x0E, out.toByteArray());
    }

    public static boolean requestPlayerInventory(long time, UUID uuid) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(0);
        DataTypeIO.writeUUID(out, uuid);
        return forwardData(time, 0x0F, out.toByteArray());
    }

    public static boolean requestPlayerEnderChest(long time, UUID uuid) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(1);
        DataTypeIO.writeUUID(out, uuid);
        return forwardData(time, 0x0F, out.toByteArray());
    }

    public static boolean requestBungeePlayerlist(long time, CompletableFuture<List<ValueTrios<UUID, String, Integer>>> future) throws Exception {
        UUID uuid = UUID.randomUUID();
        InteractiveChat.bungeeMessageListener.addToComplete(uuid, future);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        DataTypeIO.writeUUID(out, uuid);
        out.writeByte(0);
        return forwardData(time, 0x10, out.toByteArray());
    }

    public static boolean forwardNicknames(long time, UUID player, Set<String> nicknames) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        DataTypeIO.writeUUID(out, player);
        out.writeInt(nicknames.size());
        for (String names : nicknames) {
            DataTypeIO.writeString(out, names, StandardCharsets.UTF_8);
        }
        return forwardData(time, 0x11, out.toByteArray());
    }

    public static boolean requestParsedPlaceholders(long time, UUID player, String placeholders) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        DataTypeIO.writeUUID(out, player);
        DataTypeIO.writeString(out, placeholders, StandardCharsets.UTF_8);
        return forwardData(time, 0x12, out.toByteArray());
    }

    public static boolean updatePlayersVanished(long time, Map<UUID, Boolean> data) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(data.size());
        for (Map.Entry<UUID, Boolean> entry : data.entrySet()) {
            DataTypeIO.writeUUID(out, entry.getKey());
            out.writeBoolean(entry.getValue());
        }
        return forwardData(time, 0x14, out.toByteArray());
    }

    public static boolean executeProxyCommand(long time, UUID player, String command) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        DataTypeIO.writeUUID(out, player);
        DataTypeIO.writeString(out, command, StandardCharsets.UTF_8);
        return forwardData(time, 0x15, out.toByteArray());
    }

}
