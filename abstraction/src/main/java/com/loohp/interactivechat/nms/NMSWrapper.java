/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2024. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2024. Contributors
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

package com.loohp.interactivechat.nms;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.objectholders.CustomTabCompletionAction;
import com.loohp.interactivechat.objectholders.IICPlayer;
import com.loohp.interactivechat.objectholders.ValuePairs;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.DataComponentValue;
import net.md_5.bungee.api.ChatColor;
import net.querz.nbt.io.NamedTag;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class NMSWrapper {

    private static Plugin plugin;
    private static NMSWrapper instance;

    @Deprecated
    public static Plugin getPlugin() {
        return plugin;
    }

    @Deprecated
    public static NMSWrapper getInstance() {
        return instance;
    }

    @Deprecated
    public static void setup(NMSWrapper instance, Plugin plugin) {
        NMSWrapper.instance = instance;
        NMSWrapper.plugin = plugin;
    }

    static final ItemStack ITEM_STACK_AIR = new ItemStack(Material.AIR);

    static PacketContainer p(Object packet) {
        return PacketContainer.fromPacket(packet);
    }

    public abstract boolean getColorSettingsFromClientInformationPacket(PacketContainer packet);

    public abstract ValuePairs<Integer, ?> readCommandSuggestionPacket(PacketContainer packet);

    public abstract PacketContainer createCommandSuggestionPacket(int id, Object suggestions);

    public abstract boolean isCustomTabCompletionSupported();

    public abstract PacketContainer createCustomTabCompletionPacket(CustomTabCompletionAction action, List<String> tab);

    public abstract ItemStack toBukkitCopy(Object handle);

    public abstract Object toNMSCopy(ItemStack itemstack);

    public abstract Component getItemStackDisplayName(ItemStack itemStack);

    public abstract void setItemStackDisplayName(ItemStack itemStack, Component component);

    public abstract List<Component> getItemStackLore(ItemStack itemStack);

    public abstract String getItemStackTranslationKey(ItemStack itemStack);

    public abstract ChatColor getRarityColor(ItemStack itemStack);

    public abstract Component getSkullOwner(ItemStack itemStack);

    public abstract boolean isArmor(ItemStack itemStack);

    public abstract boolean isWearable(ItemStack itemStack);

    public abstract boolean hasBlockEntityTag(ItemStack itemStack);

    public abstract MapView getMapView(ItemStack itemStack);

    public abstract int getMapId(ItemStack itemStack);

    public abstract boolean isContextual(MapView mapView);

    public abstract byte[] getColors(MapView mapView, Player player);

    public abstract List<MapCursor> getCursors(MapView mapView, Player player);

    public abstract List<?> toNMSMapIconList(List<MapCursor> mapCursors);

    public abstract ItemStack getItemFromNBTJson(String json);

    public abstract String getNMSItemStackJson(ItemStack itemStack);

    public abstract Map<Key, DataComponentValue> getNMSItemStackDataComponents(ItemStack itemStack);

    public abstract ItemStack getItemStackFromDataComponents(ItemStack itemStack, Map<Key, DataComponentValue> dataComponents);

    public abstract Key getNMSItemStackNamespacedKey(ItemStack itemStack);

    public abstract String getNMSItemStackTag(ItemStack itemStack);

    public abstract NamedTag fromSNBT(String snbt) throws IOException;

    public abstract void modernChatSigningDetectRateSpam(Player player, String message);

    public abstract int modernChatSigningGetChatMessageType(Object chatMessageTypeB);

    public abstract Object modernChatSigningGetPlayerChatMessage(String message);

    public abstract Object modernChatSigningGetPlayerChatMessage(String message, Component component);

    public abstract Optional<?> modernChatSigningGetUnsignedContent(Object playerChatMessage);

    public abstract Object modernChatSigningGetSignedContent(Object playerChatMessage);

    public abstract boolean modernChatSigningHasWithResult();

    public abstract Object modernChatSigningWithResult(Object playerChatMessage, Object result);

    public abstract Object modernChatSigningWithUnsignedContent(Object playerChatMessage, Object unsignedContent);

    public abstract boolean modernChatSigningIsArgumentSignatureClass(Object instance);

    public abstract List<?> modernChatSigningGetArgumentSignatureEntries(Object argumentSignatures);

    public abstract String modernChatSigningGetSignedMessageBodyAContent(Object signedMessageBodyA);

    public abstract boolean modernChatSigningIsChatMessageIllegal(String s);

    public abstract CompletableFuture<?> modernChatSigningGetChatDecorator(Player player, Component message);

    public abstract void chatAsPlayerAsync(Player player, String message, Object unsignedContentOrResult);

    public abstract void dispatchCommandAsPlayer(Player player, String command);

    public abstract int getPing(Player player);

    public abstract boolean canChatColor(Player player);

    public abstract String getSkinValue(Player player);

    public abstract String getSkinValue(ItemMeta skull);

    public abstract void sendToast(IICPlayer sender, Player pinged, String message, ItemStack icon);

    public abstract void setBossbarTitle(Object bukkitBossbar, Component component);

    public abstract void sendTitle(Player player, Component title, Component subtitle, Component actionbar, int fadeIn, int stay, int fadeOut);

    public abstract void sendFakePlayerInventory(Player player, Inventory inventory, boolean armor, boolean offhand);

    public abstract void sendFakeMainHandSlot(Player player, ItemStack item);

    public abstract void sendFakeMapUpdate(Player player, int mapId, List<MapCursor> mapCursors, byte[] colors);

}
