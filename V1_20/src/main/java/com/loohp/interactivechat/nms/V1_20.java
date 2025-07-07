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
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.datafixers.util.Pair;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementDisplay;
import net.minecraft.advancements.AdvancementFrameType;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.CriterionTriggerImpossible;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.PacketPlayInSettings;
import net.minecraft.network.protocol.game.PacketPlayOutAdvancements;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.network.protocol.game.PacketPlayOutSetSlot;
import net.minecraft.network.protocol.game.PacketPlayOutTabComplete;
import net.minecraft.network.protocol.game.PacketPlayOutWindowItems;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemSkullPlayer;
import net.minecraft.world.level.World;
import net.minecraft.world.level.saveddata.maps.MapIcon;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NamedTag;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.boss.CraftBossBar;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.map.CraftMapView;
import org.bukkit.craftbukkit.v1_20_R1.map.RenderData;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapView;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class V1_20 extends NMSWrapper {

    private final Method craftMapViewIsContextualMethod;
    private final Method playerConnectionDetectRateSpamMethod;
    private final Method playerConnectionIsChatMessageIllegalMethod;
    private final Method playerConnectionHandleCommandMethod;
    private final Field craftSkullMetaProfileField;

    //paper
    private Method paperChatDecoratorDecorateMethod;
    private Method paperPlayerChatMessageWithResultMethod;

    public V1_20() {
        try {
            craftMapViewIsContextualMethod = CraftMapView.class.getDeclaredMethod("isContextual");
            //noinspection JavaReflectionMemberAccess
            playerConnectionDetectRateSpamMethod = PlayerConnection.class.getDeclaredMethod("detectRateSpam", String.class);
            playerConnectionIsChatMessageIllegalMethod = PlayerConnection.class.getDeclaredMethod("c", String.class);
            playerConnectionHandleCommandMethod = PlayerConnection.class.getDeclaredMethod("handleCommand", String.class);
            craftSkullMetaProfileField = Class.forName("org.bukkit.craftbukkit.v1_20_R1.inventory.CraftMetaSkull").getDeclaredField("profile");
        } catch (NoSuchFieldException | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            //paper
            paperChatDecoratorDecorateMethod = Arrays.stream(ChatDecorator.class.getMethods()).filter(m -> m.getParameterCount() == 3).findFirst().orElse(null);
            //noinspection JavaReflectionMemberAccess
            paperPlayerChatMessageWithResultMethod = PlayerChatMessage.class.getMethod("withResult", Class.forName("net.minecraft.network.chat.ChatDecorator$Result"));
        } catch (NoSuchMethodException | ClassNotFoundException ignore) {
        }
    }

    @Override
    public boolean getColorSettingsFromClientInformationPacket(PacketContainer packet) {
        PacketPlayInSettings nmsPacket = (PacketPlayInSettings) packet.getHandle();
        return nmsPacket.e();
    }

    @Override
    public CommandSuggestion<Suggestions> readCommandSuggestionPacket(PacketContainer packet) {
        PacketPlayOutTabComplete nmsPacket = (PacketPlayOutTabComplete) packet.getHandle();
        return CommandSuggestion.of(nmsPacket.a(), nmsPacket.c());
    }

    @Override
    public PacketContainer createCommandSuggestionPacket(int id, Object suggestions) {
        return p(new PacketPlayOutTabComplete(id, (Suggestions) suggestions));
    }

    @Override
    public boolean isCustomTabCompletionSupported() {
        try {
            ClientboundCustomChatCompletionsPacket.class.getName();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public PacketContainer createCustomTabCompletionPacket(CustomTabCompletionAction action, List<String> tab) {
        ClientboundCustomChatCompletionsPacket.Action nmsAction = null;
        switch (action) {
            case ADD:
                nmsAction = ClientboundCustomChatCompletionsPacket.Action.a;
                break;
            case REMOVE:
                nmsAction = ClientboundCustomChatCompletionsPacket.Action.b;
                break;
        }
        return p(new ClientboundCustomChatCompletionsPacket(nmsAction, tab));
    }

    @Override
    public ItemStack toBukkitCopy(Object handle) {
        return CraftItemStack.asBukkitCopy((net.minecraft.world.item.ItemStack) handle);
    }

    @Override
    public net.minecraft.world.item.ItemStack toNMSCopy(ItemStack itemstack) {
        return CraftItemStack.asNMSCopy(itemstack);
    }

    @Override
    public Component getItemStackDisplayName(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        return GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(nmsItemStack.y()));
    }

    @Override
    public void setItemStackDisplayName(ItemStack itemStack, Component component) {
        IChatBaseComponent nmsComponent = CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(component));
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        nmsItemStack.a(nmsComponent);
        ItemStack modifiedStack = toBukkitCopy(nmsItemStack);
        ItemMeta meta = modifiedStack.getItemMeta();
        if (meta != null) {
            itemStack.setItemMeta(meta);
        }
    }

    @Override
    public List<Component> getItemStackLore(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        NBTTagCompound nbttagcompound = nmsItemStack.b("display");
        if (nbttagcompound.d("Lore") == 9) {
            List<Component> lore = new ArrayList<>();
            NBTTagList nbtLore = nbttagcompound.c("Lore", 8);
            for (int i = 0; i < nbtLore.size(); i++) {
                String json = nbtLore.j(i);
                lore.add(GsonComponentSerializer.gson().deserialize(json));
            }
            return lore;
        }
        return Collections.emptyList();
    }

    @Override
    public String getItemStackTranslationKey(ItemStack itemStack) {
        return itemStack.getTranslationKey();
    }

    @Override
    public ChatColor getRarityColor(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        String str = nmsItemStack.C().e.toString();
        return ChatColor.getByChar(str.charAt(str.length() - 1));
    }

    @Override
    public Component getSkullOwner(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        ItemSkullPlayer skull = (ItemSkullPlayer) nmsItemStack.d();
        IChatBaseComponent owner = skull.m(nmsItemStack);
        return GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(owner));
    }

    @Override
    public boolean isWearable(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        EnumItemSlot slot = EntityInsentient.h(nmsItemStack);
        return slot != EnumItemSlot.a && slot != EnumItemSlot.b;
    }

    @Override
    public boolean hasBlockEntityTag(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        return nmsItemStack.b("BlockEntityTag") != null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public MapView getMapView(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta instanceof MapMeta) {
            return Bukkit.getMap(((MapMeta) meta).getMapId());
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getMapId(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta instanceof MapMeta) {
            return ((MapMeta) meta).getMapId();
        }
        return -1;
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
            MapIcon.Type decorationTypeHolder = MapIcon.Type.a(c.getType().getValue());
            IChatBaseComponent iChat = CraftChatMessage.fromStringOrNull(c.getCaption());
            return new MapIcon(decorationTypeHolder, c.getX(), c.getY(), c.getDirection(), iChat);
        }).collect(Collectors.toList());
    }

    @Override
    public ItemStack getItemFromNBTJson(String json) {
        try {
            NBTTagCompound nbtTagCompound = MojangsonParser.a(json);
            net.minecraft.world.item.ItemStack itemStack = net.minecraft.world.item.ItemStack.a(nbtTagCompound);
            return toBukkitCopy(itemStack);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNMSItemStackJson(ItemStack itemStack) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        NBTBase nbt = nmsItemStack.b(nbtTagCompound);
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
        NamespacedKey key = itemStack.getType().getKey();
        return Key.key(key.getNamespace(), key.getKey());
    }

    @Override
    public String getNMSItemStackTag(ItemStack itemStack) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        NBTTagCompound nbt = nmsItemStack.b(nbtTagCompound);
        NBTBase tag = nbt.p("tag");
        return tag == null ? null : tag.toString();
    }

    @Override
    public NamedTag fromSNBT(String snbt) throws IOException {
        try {
            NBTTagCompound nbt = MojangsonParser.a(snbt);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            NBTCompressedStreamTools.a(nbt, (DataOutput) new DataOutputStream(out));
            return new NBTDeserializer(false).fromBytes(out.toByteArray());
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modernChatSigningDetectRateSpam(Player player, String message) {
        try {
            playerConnectionDetectRateSpamMethod.setAccessible(true);
            PlayerConnection connection = ((CraftPlayer) player).getHandle().c;
            playerConnectionDetectRateSpamMethod.invoke(connection, message);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int modernChatSigningGetChatMessageType(Object chatMessageTypeB) {
        ChatMessageType.b type = (ChatMessageType.b) chatMessageTypeB;
        return type.a();
    }

    @Override
    public PlayerChatMessage modernChatSigningGetPlayerChatMessage(String message) {
        return PlayerChatMessage.a(message);
    }

    @Override
    public PlayerChatMessage modernChatSigningGetPlayerChatMessage(String message, Component component) {
        return PlayerChatMessage.a(message).a(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(component)));
    }

    @Override
    public Optional<IChatBaseComponent> modernChatSigningGetUnsignedContent(Object playerChatMessage) {
        PlayerChatMessage message = (PlayerChatMessage) playerChatMessage;
        return Optional.ofNullable(message.m());
    }

    @Override
    public String modernChatSigningGetSignedContent(Object playerChatMessage) {
        PlayerChatMessage message = (PlayerChatMessage) playerChatMessage;
        return message.l().a();
    }

    @Override
    public boolean modernChatSigningHasWithResult() {
        return paperPlayerChatMessageWithResultMethod != null;
    }

    @Override
    public PlayerChatMessage modernChatSigningWithResult(Object playerChatMessage, Object result) {
        try {
            return (PlayerChatMessage) paperPlayerChatMessageWithResultMethod.invoke(playerChatMessage, result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PlayerChatMessage modernChatSigningWithUnsignedContent(Object playerChatMessage, Object unsignedContent) {
        PlayerChatMessage message = (PlayerChatMessage) playerChatMessage;
        IChatBaseComponent content = (IChatBaseComponent) unsignedContent;
        return message.a(content);
    }

    @Override
    public boolean modernChatSigningIsArgumentSignatureClass(Object instance) {
        return instance instanceof ArgumentSignatures;
    }

    @Override
    public List<ArgumentSignatures.a> modernChatSigningGetArgumentSignatureEntries(Object argumentSignatures) {
        ArgumentSignatures signatures = (ArgumentSignatures) argumentSignatures;
        return signatures.a();
    }

    @Override
    public String modernChatSigningGetSignedMessageBodyAContent(Object signedMessageBodyA) {
        SignedMessageBody.a body = (SignedMessageBody.a) signedMessageBodyA;
        return body.a();
    }

    @Override
    public boolean modernChatSigningIsChatMessageIllegal(String s) {
        try {
            playerConnectionIsChatMessageIllegalMethod.setAccessible(true);
            return (boolean) playerConnectionIsChatMessageIllegalMethod.invoke(null, s);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<?> modernChatSigningGetChatDecorator(Player player, Component message) {
        try {
            ChatDecorator chatDecorator = ((CraftServer) Bukkit.getServer()).getServer().be();
            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
            if (paperChatDecoratorDecorateMethod == null) {
                return chatDecorator.decorate(entityPlayer, CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(message)));
            } else {
                return (CompletableFuture<?>) paperChatDecoratorDecorateMethod.invoke(chatDecorator, entityPlayer, null, CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(message)));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void chatAsPlayerAsync(Player player, String message, Object unsignedContentOrResult) {
        PlayerChatMessage playerChatMessage = modernChatSigningGetPlayerChatMessage(message);
        if (unsignedContentOrResult != null) {
            if (unsignedContentOrResult instanceof IChatBaseComponent) {
                playerChatMessage = modernChatSigningWithUnsignedContent(playerChatMessage, unsignedContentOrResult);
            } else {
                playerChatMessage = modernChatSigningWithResult(playerChatMessage, unsignedContentOrResult);
            }
        }
        ((CraftPlayer) player).getHandle().c.chat(message, playerChatMessage, true);
    }

    @Override
    public void dispatchCommandAsPlayer(Player player, String command) {
        try {
            playerConnectionHandleCommandMethod.setAccessible(true);
            PlayerConnection connection = ((CraftPlayer) player).getHandle().c;
            playerConnectionHandleCommandMethod.invoke(connection, command.trim());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getPing(Player player) {
        return player.getPing();
    }

    @Override
    public boolean canChatColor(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        return entityPlayer.z();
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
        MinecraftKey minecraftKey = new MinecraftKey("interactivechat", "mentioned/" + sender.getUniqueId());
        AdvancementRewards advancementRewards = new AdvancementRewards(0, new MinecraftKey[0], new MinecraftKey[0], null);
        IChatBaseComponent componentTitle = CraftChatMessage.fromStringOrNull(message);
        IChatBaseComponent componentSubtitle = IChatBaseComponent.a("");
        AdvancementDisplay advancementDisplay = new AdvancementDisplay(toNMSCopy(icon), componentTitle, componentSubtitle, null, AdvancementFrameType.c, true, false, true);

        Map<String, Criterion> advancementCriteria = new HashMap<>();
        Criterion criterion = new Criterion(new CriterionTriggerImpossible.a());
        advancementCriteria.put("for_free", criterion);

        String[][] advancementRequirements = new String[][] {new String[] {"for_free"}};

        Advancement advancement = new Advancement(minecraftKey, null, advancementDisplay, advancementRewards, advancementCriteria, advancementRequirements, false);

        Map<MinecraftKey, AdvancementProgress> advancementProgresses = new HashMap<>();
        AdvancementProgress advancementProgress = new AdvancementProgress();
        advancementProgress.a(advancementCriteria, advancementRequirements);
        advancementProgress.c("for_free").b();
        advancementProgresses.put(minecraftKey, advancementProgress);

        List<Advancement> advancements = Collections.singletonList(advancement);

        PlayerConnection connection = ((CraftPlayer) pinged).getHandle().c;

        PacketPlayOutAdvancements packet1 = new PacketPlayOutAdvancements(false, advancements, Collections.emptySet(), advancementProgresses);
        connection.a(packet1);

        Set<MinecraftKey> removeAdvancements = Collections.singleton(minecraftKey);
        PacketPlayOutAdvancements packet2 = new PacketPlayOutAdvancements(false, Collections.emptyList(), removeAdvancements, Collections.emptyMap());
        connection.a(packet2);
    }

    @Override
    public void setBossbarTitle(Object bukkitBossbar, Component component) {
        CraftBossBar craftBossBar = (CraftBossBar) bukkitBossbar;
        craftBossBar.getHandle().a(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(component)));
    }

    @Override
    public void sendTitle(Player player, Component title, Component subtitle, Component actionbar, int fadeIn, int stay, int fadeOut) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().c;

        ClientboundClearTitlesPacket packet1 = new ClientboundClearTitlesPacket(true);
        connection.a(packet1);

        if (!PlainTextComponentSerializer.plainText().serialize(title).isEmpty()) {
            ClientboundSetTitleTextPacket packet2 = new ClientboundSetTitleTextPacket(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(title)));
            connection.a(packet2);
        }

        if (!PlainTextComponentSerializer.plainText().serialize(subtitle).isEmpty()) {
            ClientboundSetSubtitleTextPacket packet3 = new ClientboundSetSubtitleTextPacket(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(subtitle)));
            connection.a(packet3);
        }

        if (!PlainTextComponentSerializer.plainText().serialize(actionbar).isEmpty()) {
            ClientboundSetActionBarTextPacket packet4 = new ClientboundSetActionBarTextPacket(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(actionbar)));
            connection.a(packet4);
        }

        ClientboundSetTitlesAnimationPacket packet5 = new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);
        connection.a(packet5);
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

        NonNullList<net.minecraft.world.item.ItemStack> itemList = NonNullList.a();
        for (ItemStack itemStack : items) {
            itemList.add(toNMSCopy(itemStack));
        }

        PacketPlayOutWindowItems packet1 = new PacketPlayOutWindowItems(0, 0, itemList, toNMSCopy(ITEM_STACK_AIR));
        PacketPlayOutSetSlot packet2 = new PacketPlayOutSetSlot(-1, -1, 0, toNMSCopy(ITEM_STACK_AIR));

        PlayerConnection connection = ((CraftPlayer) player).getHandle().c;
        connection.a(packet1);
        connection.a(packet2);
    }

    @Override
    public void sendFakeMainHandSlot(Player player, ItemStack item) {
        List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> nmsEquipments = Collections.singletonList(new Pair<>(EnumItemSlot.a, toNMSCopy(item)));
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(player.getEntityId(), nmsEquipments);
        ((CraftPlayer) player).getHandle().c.a(packet);
    }

    @Override
    public void sendFakeMapUpdate(Player player, int mapId, List<MapCursor> mapCursors, byte[] colors) {
        List<MapIcon> mapIcons = toNMSMapIconList(mapCursors);
        WorldMap.b b = new WorldMap.b(0, 0, 128, 128, colors);
        PacketPlayOutMap packet = new PacketPlayOutMap(mapId, (byte) 0, false, mapIcons, b);
        ((CraftPlayer) player).getHandle().c.a(packet);
    }

    @Override
    public InternalOfflinePlayerInfo loadOfflinePlayer(UUID uuid, Inventory inventory, Inventory enderchest) {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = server.a(World.i);
        if (worldServer == null) {
            return null;
        }
        OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
        GameProfile profile = new GameProfile(offline.getUniqueId(), offline.getName() != null ? offline.getName() : offline.getUniqueId().toString());
        EntityPlayer player = new EntityPlayer(server, worldServer, profile);
        player.M().a();

        NBTTagCompound loadedData = player.d.ac().s.b(player);
        if (loadedData == null) {
            return null;
        }

        player.g(loadedData);
        player.a(loadedData);
        player.c(loadedData);

        Player p = player.getBukkitEntity();
        PlayerInventory playerInventory = p.getInventory();

        int selectedSlot = playerInventory.getHeldItemSlot();
        boolean rightHanded = p.getMainHand().equals(MainHand.RIGHT);
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
        return CraftChatMessage.fromJSON(json);
    }

    @Override
    public String serializeChatComponent(Object handle) {
        return CraftChatMessage.toJSON((IChatBaseComponent) handle);
    }
}
