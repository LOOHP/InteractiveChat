/*
 * This file is part of InteractiveChat4.
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

package com.loohp.interactivechat.nms;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.objectholders.CommandSuggestion;
import com.loohp.interactivechat.objectholders.CustomTabCompletionAction;
import com.loohp.interactivechat.objectholders.IICPlayer;
import com.loohp.interactivechat.objectholders.InternalOfflinePlayerInfo;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.EntityInsentient;
import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.EnumTitleAction;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.Item;
import net.minecraft.server.v1_8_R1.ItemSkull;
import net.minecraft.server.v1_8_R1.MapIcon;
import net.minecraft.server.v1_8_R1.MinecraftKey;
import net.minecraft.server.v1_8_R1.MinecraftServer;
import net.minecraft.server.v1_8_R1.MojangsonParser;
import net.minecraft.server.v1_8_R1.NBTBase;
import net.minecraft.server.v1_8_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import net.minecraft.server.v1_8_R1.PacketPlayInSettings;
import net.minecraft.server.v1_8_R1.PacketPlayOutChat;
import net.minecraft.server.v1_8_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R1.PacketPlayOutMap;
import net.minecraft.server.v1_8_R1.PacketPlayOutSetSlot;
import net.minecraft.server.v1_8_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R1.PacketPlayOutWindowItems;
import net.minecraft.server.v1_8_R1.PlayerConnection;
import net.minecraft.server.v1_8_R1.PlayerInteractManager;
import net.minecraft.server.v1_8_R1.WorldServer;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NamedTag;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R1.CraftServer;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R1.map.CraftMapView;
import org.bukkit.craftbukkit.v1_8_R1.map.RenderData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapView;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class V1_8 extends NMSWrapper {

    private final Method craftMapViewIsContextualMethod;
    private final Method playerConnectionHandleCommandMethod;
    private final Field craftSkullMetaProfileField;
    private final Field entityPlayerCanChatColorField;

    public V1_8() {
        try {
            craftMapViewIsContextualMethod = CraftMapView.class.getDeclaredMethod("isContextual");
            playerConnectionHandleCommandMethod = PlayerConnection.class.getDeclaredMethod("handleCommand", String.class);
            craftSkullMetaProfileField = Class.forName("org.bukkit.craftbukkit.v1_8_R1.inventory.CraftMetaSkull").getDeclaredField("profile");
            entityPlayerCanChatColorField = EntityPlayer.class.getDeclaredField("bQ");
        } catch (NoSuchFieldException | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean getColorSettingsFromClientInformationPacket(PacketContainer packet) {
        PacketPlayInSettings nmsPacket = (PacketPlayInSettings) packet.getHandle();
        return nmsPacket.d();
    }

    @Override
    public CommandSuggestion<?> readCommandSuggestionPacket(PacketContainer packet) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PacketContainer createCommandSuggestionPacket(int id, Object suggestions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCustomTabCompletionSupported() {
        return false;
    }

    @Override
    public PacketContainer createCustomTabCompletionPacket(CustomTabCompletionAction action, List<String> tab) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack toBukkitCopy(Object handle) {
        return CraftItemStack.asBukkitCopy((net.minecraft.server.v1_8_R1.ItemStack) handle);
    }

    @Override
    public net.minecraft.server.v1_8_R1.ItemStack toNMSCopy(ItemStack itemstack) {
        return CraftItemStack.asNMSCopy(itemstack);
    }

    @Override
    public Component getItemStackDisplayName(ItemStack itemStack) {
        net.minecraft.server.v1_8_R1.ItemStack nmsItemStack = toNMSCopy(itemStack);
        return l(nmsItemStack.getName());
    }

    @Override
    public void setItemStackDisplayName(ItemStack itemStack, Component component) {
        net.minecraft.server.v1_8_R1.ItemStack nmsItemStack = toNMSCopy(itemStack);
        String displayName = LegacyComponentSerializer.legacySection().serialize(component);
        nmsItemStack.c(displayName);
        ItemStack modifiedStack = toBukkitCopy(nmsItemStack);
        ItemMeta meta = modifiedStack.getItemMeta();
        if (meta != null) {
            itemStack.setItemMeta(meta);
        }
    }

    @Override
    public List<Component> getItemStackLore(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }
        List<String> lore = meta.getLore();
        if (lore == null) {
            return Collections.emptyList();
        }
        return lore.stream().map(s -> l(s)).collect(Collectors.toList());
    }

    @Override
    public String getItemStackTranslationKey(ItemStack itemStack) {
        net.minecraft.server.v1_8_R1.ItemStack nmsItemStack = toNMSCopy(itemStack);
        return nmsItemStack.getItem().a(nmsItemStack);
    }

    @Override
    public ChatColor getRarityColor(ItemStack itemStack) {
        net.minecraft.server.v1_8_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        String str = nmsItemStack.u().e.toString();
        return ChatColor.getByChar(str.charAt(str.length() - 1));
    }

    @Override
    public Component getSkullOwner(ItemStack itemStack) {
        net.minecraft.server.v1_8_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        ItemSkull skull = (ItemSkull) nmsItemStack.getItem();
        String owner = skull.a(nmsItemStack);
        return l(owner);
    }

    @Override
    public boolean isWearable(ItemStack itemStack) {
        net.minecraft.server.v1_8_R1.ItemStack nmsItemStack = toNMSCopy(itemStack);
        return EntityInsentient.c(nmsItemStack) != 0;
    }

    @Override
    public boolean hasBlockEntityTag(ItemStack itemStack) {
        net.minecraft.server.v1_8_R1.ItemStack nmsItemStack = toNMSCopy(itemStack);
        return nmsItemStack.hasTag() && nmsItemStack.getTag().hasKey("BlockEntityTag");
    }

    @SuppressWarnings("deprecation")
    @Override
    public MapView getMapView(ItemStack itemStack) {
        return Bukkit.getMap(itemStack.getDurability());
    }

    @Override
    public int getMapId(ItemStack itemStack) {
        return itemStack.getDurability();
    }

    @Override
    public boolean isContextual(MapView mapView) {
        try {
            CraftMapView craftMapView = (CraftMapView) mapView;
            craftMapViewIsContextualMethod.setAccessible(true);
            return (boolean) craftMapViewIsContextualMethod.invoke(craftMapView);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getColors(MapView mapView, Player player) {
        CraftMapView craftMapView = (CraftMapView) mapView;
        RenderData renderData = craftMapView.render((CraftPlayer) player);
        return renderData.buffer;
    }

    @Override
    public List<MapCursor> getCursors(MapView mapView, Player player) {
        CraftMapView craftMapView = (CraftMapView) mapView;
        RenderData renderData = craftMapView.render((CraftPlayer) player);
        return renderData.cursors;
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<MapIcon> toNMSMapIconList(List<MapCursor> mapCursors) {
        return mapCursors.stream().map(c -> {
            return new MapIcon(c.getType().getValue(), c.getX(), c.getY(), c.getDirection());
        }).collect(Collectors.toList());
    }

    @Override
    public ItemStack getItemFromNBTJson(String json) {
        try {
            NBTTagCompound nbtTagCompound = MojangsonParser.parse(json);
            net.minecraft.server.v1_8_R1.ItemStack itemStack = net.minecraft.server.v1_8_R1.ItemStack.createStack(nbtTagCompound);
            return toBukkitCopy(itemStack);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNMSItemStackJson(ItemStack itemStack) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        net.minecraft.server.v1_8_R1.ItemStack nmsItemStack = toNMSCopy(itemStack);
        NBTBase nbt = nmsItemStack.save(nbtTagCompound);
        return nbt.toString();
    }

    @Override
    public Map<Key, DataComponentValue> getNMSItemStackDataComponents(ItemStack itemStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack getItemStackFromDataComponents(ItemStack itemStack, Map<Key, DataComponentValue> dataComponents) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public Key getNMSItemStackNamespacedKey(ItemStack itemStack) {
        net.minecraft.server.v1_8_R1.ItemStack nmsItemStack = toNMSCopy(itemStack);
        MinecraftKey minecraftkey = (MinecraftKey) Item.REGISTRY.c(nmsItemStack.getItem());
        return minecraftkey == null ? Key.key("minecraft", "air") : Key.key(minecraftkey.toString());
    }

    @Override
    public String getNMSItemStackTag(ItemStack itemStack) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        net.minecraft.server.v1_8_R1.ItemStack nmsItemStack = toNMSCopy(itemStack);
        NBTTagCompound nbt = nmsItemStack.save(nbtTagCompound);
        NBTBase tag = nbt.get("tag");
        return tag == null ? null : tag.toString();
    }

    @Override
    public NamedTag fromSNBT(String snbt) {
        try {
            NBTTagCompound nbt = MojangsonParser.parse(snbt);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            NBTCompressedStreamTools.a(nbt, (DataOutput) new DataOutputStream(out));
            return new NBTDeserializer(false).fromBytes(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modernChatSigningDetectRateSpam(Player player, String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int modernChatSigningGetChatMessageType(Object chatMessageTypeB) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object modernChatSigningGetPlayerChatMessage(String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object modernChatSigningGetPlayerChatMessage(String message, Component component) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Object> modernChatSigningGetUnsignedContent(Object playerChatMessage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String modernChatSigningGetSignedContent(Object playerChatMessage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean modernChatSigningHasWithResult() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object modernChatSigningWithResult(Object playerChatMessage, Object result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object modernChatSigningWithUnsignedContent(Object playerChatMessage, Object unsignedContent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean modernChatSigningIsArgumentSignatureClass(Object instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<?> modernChatSigningGetArgumentSignatureEntries(Object argumentSignatures) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String modernChatSigningGetSignedMessageBodyAContent(Object signedMessageBodyA) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean modernChatSigningIsChatMessageIllegal(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<?> modernChatSigningGetChatDecorator(Player player, Component message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void chatAsPlayerAsync(Player player, String message, Object unsignedContentOrResult) {
        ((CraftPlayer) player).getHandle().playerConnection.chat(message, true);
    }

    @Override
    public void dispatchCommandAsPlayer(Player player, String command) {
        try {
            playerConnectionHandleCommandMethod.setAccessible(true);
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            playerConnectionHandleCommandMethod.invoke(connection, command.trim());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getPing(Player player) {
        return ((CraftPlayer) player).getHandle().ping;
    }

    @Override
    public boolean canChatColor(Player player) {
        try {
            entityPlayerCanChatColorField.setAccessible(true);
            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
            return entityPlayerCanChatColorField.getBoolean(entityPlayer);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getSkinValue(Player player) {
        Collection<Property> textures = ((CraftPlayer) player).getProfile().getProperties().get("textures");
        if (textures == null || textures.isEmpty()) {
            return null;
        }
        return textures.iterator().next().getValue();
    }

    @Override
    public String getSkinValue(ItemMeta skull) {
        try {
            if (skull instanceof SkullMeta && ((SkullMeta) skull).hasOwner()) {
                craftSkullMetaProfileField.setAccessible(true);
                GameProfile profile = (GameProfile) craftSkullMetaProfileField.get(skull);
                Collection<Property> textures = profile.getProperties().get("textures");
                if (textures == null || textures.isEmpty()) {
                    return null;
                }
                return textures.iterator().next().getValue();
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void sendToast(IICPlayer sender, Player pinged, String message, ItemStack icon) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBossbarTitle(Object bukkitBossbar, Component component) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendTitle(Player player, Component title, Component subtitle, Component actionbar, int fadeIn, int stay, int fadeOut) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        PacketPlayOutTitle packet1 = new PacketPlayOutTitle(EnumTitleAction.RESET, null);
        connection.sendPacket(packet1);

        if (!PlainTextComponentSerializer.plainText().serialize(title).isEmpty()) {
            PacketPlayOutTitle packet2 = new PacketPlayOutTitle(EnumTitleAction.TITLE, ChatSerializer.a(GsonComponentSerializer.gson().serialize(title)));
            connection.sendPacket(packet2);
        }

        if (!PlainTextComponentSerializer.plainText().serialize(subtitle).isEmpty()) {
            PacketPlayOutTitle packet3 = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, ChatSerializer.a(GsonComponentSerializer.gson().serialize(subtitle)));
            connection.sendPacket(packet3);
        }

        if (!PlainTextComponentSerializer.plainText().serialize(actionbar).isEmpty()) {
            PacketPlayOutChat packet4 = new PacketPlayOutChat(ChatSerializer.a(GsonComponentSerializer.gson().serialize(actionbar)), (byte) 2);
            connection.sendPacket(packet4);
        }

        PacketPlayOutTitle packet5 = new PacketPlayOutTitle(fadeIn, stay, fadeOut);
        connection.sendPacket(packet5);
    }

    @Override
    public void sendFakePlayerInventory(Player player, Inventory inventory, boolean armor, boolean offhand) {
        ItemStack[] items = new ItemStack[46];
        Arrays.fill(items, ITEM_STACK_AIR);
        int u = 36;
        for (int i = 0; i < 9; i++) {
            ItemStack item = inventory.getItem(i);
            items[u] = item == null ? ITEM_STACK_AIR : item.clone();
            u++;
        }
        for (int i = 9; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            items[i] = item == null ? ITEM_STACK_AIR : item.clone();
        }
        if (armor) {
            u = 8;
            for (int i = 36; i < 40; i++) {
                ItemStack item = inventory.getItem(i);
                items[u] = item == null ? ITEM_STACK_AIR : item.clone();
                u--;
            }
        }
        if (offhand) {
            ItemStack item = inventory.getItem(40);
            items[45] = item == null ? ITEM_STACK_AIR : item.clone();
        }

        PacketPlayOutWindowItems packet1 = new PacketPlayOutWindowItems(0, Arrays.stream(items).map(i -> toNMSCopy(i)).collect(Collectors.toList()));
        PacketPlayOutSetSlot packet2 = new PacketPlayOutSetSlot(-1, -1, toNMSCopy(ITEM_STACK_AIR));

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(packet1);
        connection.sendPacket(packet2);
    }

    @Override
    public void sendFakeMainHandSlot(Player player, ItemStack item) {
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(player.getEntityId(), 0, toNMSCopy(item));
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void sendFakeMapUpdate(Player player, int mapId, List<MapCursor> mapCursors, byte[] colors) {
        List<MapIcon> mapIcons = toNMSMapIconList(mapCursors);
        PacketPlayOutMap packet = new PacketPlayOutMap(mapId, (byte) 0, mapIcons, colors, 0, 0, 128, 128);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public InternalOfflinePlayerInfo loadOfflinePlayer(UUID uuid, Inventory inventory, Inventory enderchest) {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = server.getWorldServer(0);
        if (worldServer == null) {
            return null;
        }
        OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
        GameProfile profile = new GameProfile(offline.getUniqueId(), offline.getName() != null ? offline.getName() : offline.getUniqueId().toString());
        PlayerInteractManager interactManager = new PlayerInteractManager(worldServer);
        EntityPlayer player = new EntityPlayer(server, worldServer, profile, interactManager);

        NBTTagCompound loadedData = player.server.getPlayerList().playerFileData.load(player);
        if (loadedData == null) {
            return null;
        }

        player.f(loadedData);
        player.a(loadedData);

        Player p = player.getBukkitEntity();
        PlayerInventory playerInventory = p.getInventory();

        int selectedSlot = playerInventory.getHeldItemSlot();
        boolean rightHanded = true;
        int xpLevel = p.getLevel();

        for (int slot = 0; slot < Math.min(playerInventory.getSize(), inventory.getSize()); slot++) {
            inventory.setItem(slot, playerInventory.getItem(slot));
        }
        for (int slot = 0; slot < Math.min(p.getEnderChest().getSize(), enderchest.getSize()); slot++) {
            enderchest.setItem(slot, p.getEnderChest().getItem(slot));
        }

        return new InternalOfflinePlayerInfo(selectedSlot, rightHanded, xpLevel, inventory, enderchest);
    }

    @Override
    public Object deserializeChatComponent(String json) {
        return ChatSerializer.a(json);
    }

    @Override
    public String serializeChatComponent(Object handle) {
        return ChatSerializer.a((IChatBaseComponent) handle);
    }
}
