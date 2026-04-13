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
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.loohp.interactivechat.objectholders.CommandSuggestion;
import com.loohp.interactivechat.objectholders.CustomTabCompletionAction;
import com.loohp.interactivechat.objectholders.IICPlayer;
import com.loohp.interactivechat.objectholders.InternalOfflinePlayerInfo;
import com.loohp.interactivechat.utils.NativeJsonConverter;
import com.loohp.interactivechat.utils.ReflectionUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.util.AttributeKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonDataComponentValue;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NamedTag;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.boss.CraftBossBar;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.map.CraftMapCursor;
import org.bukkit.craftbukkit.map.CraftMapView;
import org.bukkit.craftbukkit.map.RenderData;
import org.bukkit.craftbukkit.util.CraftChatMessage;
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
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class V26_1_1 extends NMSWrapper {

    private final Method craftMapViewIsContextualMethod;
    private final Method playerConnectionDetectRateSpamMethod;
    private final Method playerConnectionIsChatMessageIllegalMethod;
    private final Method playerConnectionHandleCommandMethod;
    private final Field craftSkullMetaProfileField;
    private final Field renderDataCursorsField;
    private final Method nmsEntityPlayerLoadMethod;
    private final Field nmsEntityPlayerServerField;
    private final Field nmsPlayerConnectionNetworkManagerField;

    //paper
    private Method paperChatDecoratorDecorateMethod;
    private Method paperComponentSerializationLocalizedCodec;

    public V26_1_1() {
        try {
            craftMapViewIsContextualMethod = CraftMapView.class.getDeclaredMethod("isContextual");
            //noinspection JavaReflectionMemberAccess
            playerConnectionDetectRateSpamMethod = ServerGamePacketListenerImpl.class.getDeclaredMethod("detectRateSpam", String.class);
            playerConnectionIsChatMessageIllegalMethod = ReflectionUtils.findDeclaredMethod(ServerGamePacketListenerImpl.class, new Class<?>[] {String.class}, "isChatMessageIllegal", "d");
            playerConnectionHandleCommandMethod = ServerGamePacketListenerImpl.class.getDeclaredMethod("handleCommand", String.class);
            craftSkullMetaProfileField = Class.forName("org.bukkit.craftbukkit.inventory.CraftMetaSkull").getDeclaredField("profile");
            renderDataCursorsField = RenderData.class.getField("cursors");
            nmsEntityPlayerLoadMethod = ServerPlayer.class.getDeclaredMethod("readAdditionalSaveData", ValueInput.class);
            nmsEntityPlayerServerField = ReflectionUtils.findDeclaredField(ServerPlayer.class, MinecraftServer.class, "server");
            nmsPlayerConnectionNetworkManagerField = Arrays.stream(ServerCommonPacketListenerImpl.class.getDeclaredFields()).filter(m -> m.getType().equals(Connection.class)).findFirst().orElseThrow(() -> new RuntimeException());
        } catch (NoSuchFieldException | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        //paper
        paperChatDecoratorDecorateMethod = Arrays.stream(ChatDecorator.class.getMethods()).filter(m -> m.getParameterCount() == 3).findFirst().orElse(null);
        paperComponentSerializationLocalizedCodec = Arrays.stream(ComponentSerialization.class.getMethods()).filter(m -> m.getName().equalsIgnoreCase("localizedCodec")).findFirst().orElse(null);
    }

    private Tag toNBT(net.minecraft.world.item.ItemStack itemStack) {
        CompoundTag nbtTagCompound = new CompoundTag();
        return net.minecraft.world.item.ItemStack.CODEC.encode(itemStack, CraftRegistry.getMinecraftRegistry().createSerializationContext(NbtOps.INSTANCE), nbtTagCompound).getOrThrow();
    }

    private Optional<net.minecraft.world.item.ItemStack> fromNBT(Tag nbtbase) {
        return net.minecraft.world.item.ItemStack.CODEC.parse(CraftRegistry.getMinecraftRegistry().createSerializationContext(NbtOps.INSTANCE), nbtbase).resultOrPartial();
    }

    @Override
    public boolean getColorSettingsFromClientInformationPacket(PacketContainer packet) {
        ServerboundClientInformationPacket nmsPacket = (ServerboundClientInformationPacket) packet.getHandle();
        return nmsPacket.information().chatColors();
    }

    @Override
    public CommandSuggestion<Suggestions> readCommandSuggestionPacket(PacketContainer packet) {
        ClientboundCommandSuggestionsPacket nmsPacket = (ClientboundCommandSuggestionsPacket) packet.getHandle();
        return CommandSuggestion.of(nmsPacket.id(), nmsPacket.toSuggestions());
    }

    @Override
    public PacketContainer createCommandSuggestionPacket(int id, Object suggestions) {
        return p(new ClientboundCommandSuggestionsPacket(id, (Suggestions) suggestions));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
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
                nmsAction = ClientboundCustomChatCompletionsPacket.Action.ADD;
                break;
            case REMOVE:
                nmsAction = ClientboundCustomChatCompletionsPacket.Action.REMOVE;
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
        return GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(nmsItemStack.getHoverName()));
    }

    @Override
    public void setItemStackDisplayName(ItemStack itemStack, Component component) {
        net.minecraft.network.chat.Component nmsComponent = CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(component));
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        DataComponentPatch dataComponentPatch = DataComponentPatch.builder().set(DataComponents.CUSTOM_NAME, nmsComponent).build();
        nmsItemStack.applyComponents(dataComponentPatch);
        ItemStack modifiedStack = toBukkitCopy(nmsItemStack);
        ItemMeta meta = modifiedStack.getItemMeta();
        if (meta != null) {
            itemStack.setItemMeta(meta);
        }
    }

    @Override
    public List<Component> getItemStackLore(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        ItemLore lore = nmsItemStack.get(DataComponents.LORE);
        if (lore == null) {
            return Collections.emptyList();
        }
        return lore.lines().stream().map(e -> GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(e))).collect(Collectors.toList());
    }

    @Override
    public String getItemStackTranslationKey(ItemStack itemStack) {
        return itemStack.getTranslationKey();
    }

    @Override
    public ChatColor getRarityColor(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        String str = nmsItemStack.getRarity().color().toString();
        return ChatColor.getByChar(str.charAt(str.length() - 1));
    }

    @Override
    public Component getSkullOwner(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        PlayerHeadItem skull = (PlayerHeadItem) nmsItemStack.getItem();
        net.minecraft.network.chat.Component owner = skull.getName(nmsItemStack);
        return GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(owner));
    }

    @Override
    public boolean isWearable(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        Equippable equippable = nmsItemStack.get(DataComponents.EQUIPPABLE);
        if (equippable == null) {
            return false;
        }
        if (!equippable.allowedEntities().map(a -> a.stream().anyMatch(s -> s.value().equals(EntityType.PLAYER))).orElse(true)) {
            return false;
        }
        EquipmentSlot slot = equippable.slot();
        return slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND;
    }

    @Override
    public boolean hasBlockEntityTag(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        return nmsItemStack.get(DataComponents.BLOCK_ENTITY_DATA) != null;
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

    @SuppressWarnings("unchecked")
    @Override
    public List<MapCursor> getCursors(MapView mapView, Player player) {
        try {
            CraftMapView craftMapView = (CraftMapView) mapView;
            RenderData renderData = craftMapView.render((CraftPlayer) player);
            return (List<MapCursor>) renderDataCursorsField.get(renderData);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<MapDecoration> toNMSMapIconList(List<MapCursor> mapCursors) {
        return mapCursors.stream().map(c -> {
            Holder<MapDecorationType> decorationTypeHolder = CraftMapCursor.CraftType.bukkitToMinecraftHolder(c.getType());
            net.minecraft.network.chat.Component iChat = CraftChatMessage.fromStringOrNull(c.getCaption());
            return new MapDecoration(decorationTypeHolder, c.getX(), c.getY(), c.getDirection(), Optional.ofNullable(iChat));
        }).collect(Collectors.toList());
    }

    @Override
    public ItemStack getItemFromNBTJson(String json) {
        try {
            CompoundTag nbtTagCompound = TagParser.parseCompoundFully(json);
            net.minecraft.world.item.ItemStack itemStack = fromNBT(nbtTagCompound).orElseThrow(() -> new RuntimeException());
            return toBukkitCopy(itemStack);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNMSItemStackJson(ItemStack itemStack) {
        if (itemStack.getType().isAir()) {
            return "{id: \"minecraft:air\", count: 1}";
        }
        return toNBT(toNMSCopy(itemStack)).toString();
    }

    @SuppressWarnings({"PatternValidation", "unchecked", "rawtypes", "DataFlowIssue"})
    @Override
    public Map<Key, DataComponentValue> getNMSItemStackDataComponents(ItemStack itemStack) {
        if (itemStack.getType().isAir()) {
            return Collections.emptyMap();
        }
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        DataComponentPatch dataComponentPatch = nmsItemStack.getComponentsPatch();
        Map<Key, DataComponentValue> convertedComponents = new HashMap<>();
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : dataComponentPatch.entrySet()) {
            DataComponentType<?> type = entry.getKey();
            Optional<?> optValue = entry.getValue();
            Identifier minecraftKey = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
            Key key = Key.key(minecraftKey.getNamespace(), minecraftKey.getPath());
            if (optValue.isPresent()) {
                Codec codec = type.codec();
                if (codec != null) {
                    Object nativeJsonElement = codec.encodeStart(CraftRegistry.getMinecraftRegistry().createSerializationContext(JsonOps.INSTANCE), optValue.get()).getOrThrow();
                    JsonElement jsonElement = NativeJsonConverter.fromNative(nativeJsonElement);
                    DataComponentValue value = GsonDataComponentValue.gsonDataComponentValue(jsonElement);
                    convertedComponents.put(key, value);
                }
            } else {
                convertedComponents.put(key, DataComponentValue.removed());
            }
        }
        return convertedComponents;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ItemStack getItemStackFromDataComponents(ItemStack itemStack, Map<Key, DataComponentValue> dataComponents) {
        if (dataComponents.isEmpty()) {
            return itemStack;
        }
        try {
            net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
            DataComponentPatch.Builder builder = DataComponentPatch.builder();
            for (Map.Entry<Key, DataComponentValue> entry : dataComponents.entrySet()) {
                Key key = entry.getKey();
                DataComponentValue value = entry.getValue();
                Identifier minecraftKey = Identifier.fromNamespaceAndPath(key.namespace(), key.value());
                Optional<DataComponentType<?>> optType = BuiltInRegistries.DATA_COMPONENT_TYPE.getOptional(minecraftKey);
                if (optType.isPresent()) {
                    DataComponentType<?> type = optType.get();
                    if (value instanceof DataComponentValue.Removed) {
                        builder.remove(type);
                    } else if (value instanceof GsonDataComponentValue) {
                        JsonElement jsonElement = ((GsonDataComponentValue) value).element();
                        Object nativeJsonElement = NativeJsonConverter.toNative(jsonElement);
                        Object result = type.codecOrThrow().decode(CraftRegistry.getMinecraftRegistry().createSerializationContext((DynamicOps<Object>) (DynamicOps<?>) JsonOps.INSTANCE), nativeJsonElement).getOrThrow().getFirst();
                        builder.set((DataComponentType) type, result);
                    }
                }
            }
            nmsItemStack.applyComponents(builder.build());
            return toBukkitCopy(nmsItemStack);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"PatternValidation", "deprecation"})
    @Override
    public Key getNMSItemStackNamespacedKey(ItemStack itemStack) {
        if (itemStack.getType().isAir()) {
            return Key.key("minecraft", "air");
        }
        NamespacedKey key = itemStack.getType().getKey();
        return Key.key(key.getNamespace(), key.getKey());
    }

    @Override
    public String getNMSItemStackTag(ItemStack itemStack) {
        if (itemStack.getType().isAir()) {
            return null;
        }
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        Tag nbt = toNBT(nmsItemStack);
        if (nbt instanceof CompoundTag) {
            Tag tag = ((CompoundTag) nbt).get("tag");
            return tag == null ? null : tag.toString();
        }
        return null;
    }

    @Override
    public NamedTag fromSNBT(String snbt) throws IOException {
        try {
            CompoundTag nbt = TagParser.parseCompoundFully(snbt);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            NbtIo.write(nbt, new DataOutputStream(out));
            return new NBTDeserializer(false).fromBytes(out.toByteArray());
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modernChatSigningDetectRateSpam(Player player, String message) {
        try {
            playerConnectionDetectRateSpamMethod.setAccessible(true);
            ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
            playerConnectionDetectRateSpamMethod.invoke(connection, message);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int modernChatSigningGetChatMessageType(Object chatMessageTypeB) {
        return 0;
    }

    @Override
    public PlayerChatMessage modernChatSigningGetPlayerChatMessage(String message) {
        return PlayerChatMessage.system(message);
    }

    @Override
    public PlayerChatMessage modernChatSigningGetPlayerChatMessage(String message, Component component) {
        return PlayerChatMessage.system(message).withUnsignedContent(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(component)));
    }

    @Override
    public Optional<net.minecraft.network.chat.Component> modernChatSigningGetUnsignedContent(Object playerChatMessage) {
        PlayerChatMessage message = (PlayerChatMessage) playerChatMessage;
        return Optional.ofNullable(message.unsignedContent());
    }

    @Override
    public String modernChatSigningGetSignedContent(Object playerChatMessage) {
        PlayerChatMessage message = (PlayerChatMessage) playerChatMessage;
        return message.signedBody().content();
    }

    @Override
    public boolean modernChatSigningHasWithResult() {
        return false;
    }

    @Override
    public Object modernChatSigningWithResult(Object playerChatMessage, Object result) {
        return null;
    }

    @Override
    public PlayerChatMessage modernChatSigningWithUnsignedContent(Object playerChatMessage, Object unsignedContent) {
        PlayerChatMessage message = (PlayerChatMessage) playerChatMessage;
        net.minecraft.network.chat.Component content = (net.minecraft.network.chat.Component) unsignedContent;
        return message.withUnsignedContent(content);
    }

    @Override
    public boolean modernChatSigningIsArgumentSignatureClass(Object instance) {
        return instance instanceof ArgumentSignatures;
    }

    @Override
    public List<ArgumentSignatures.Entry> modernChatSigningGetArgumentSignatureEntries(Object argumentSignatures) {
        ArgumentSignatures signatures = (ArgumentSignatures) argumentSignatures;
        return signatures.entries();
    }

    @Override
    public String modernChatSigningGetSignedMessageBodyAContent(Object signedMessageBodyA) {
        SignedMessageBody.Packed body = (SignedMessageBody.Packed) signedMessageBodyA;
        return body.content();
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

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<net.minecraft.network.chat.Component> modernChatSigningGetChatDecorator(Player player, Component message) {
        try {
            ChatDecorator chatDecorator = ((CraftServer) Bukkit.getServer()).getServer().getChatDecorator();
            ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();
            if (paperChatDecoratorDecorateMethod == null) {
                return CompletableFuture.completedFuture(chatDecorator.decorate(entityPlayer, CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(message))));
            } else {
                return (CompletableFuture<net.minecraft.network.chat.Component>) paperChatDecoratorDecorateMethod.invoke(chatDecorator, entityPlayer, null, CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(message)));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void chatAsPlayerAsync(Player player, String message, Object unsignedContentOrResult) {
        PlayerChatMessage playerChatMessage = modernChatSigningGetPlayerChatMessage(message);
        if (unsignedContentOrResult != null) {
            playerChatMessage = modernChatSigningWithUnsignedContent(playerChatMessage, unsignedContentOrResult);
        }
        ((CraftPlayer) player).getHandle().connection.chat(message, playerChatMessage, true);
    }

    @Override
    public void dispatchCommandAsPlayer(Player player, String command) {
        try {
            playerConnectionHandleCommandMethod.setAccessible(true);
            ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
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
        ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        return entityPlayer.canChatInColor();
    }

    @Override
    public String getSkinValue(Player player) {
        Collection<Property> textures = ((CraftPlayer) player).getProfile().properties().get("textures");
        if (textures == null || textures.isEmpty()) {
            return null;
        }
        return textures.iterator().next().value();
    }

    @Override
    public String getSkinValue(ItemMeta skull) {
        try {
            if (skull instanceof SkullMeta && ((SkullMeta) skull).hasOwner()) {
                craftSkullMetaProfileField.setAccessible(true);
                GameProfile profile = (GameProfile) craftSkullMetaProfileField.get(skull);
                Collection<Property> textures = profile.properties().get("textures");
                if (textures == null || textures.isEmpty()) {
                    return null;
                }
                return textures.iterator().next().value();
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void sendToast(IICPlayer sender, Player pinged, String messageJson, ItemStack icon) {
        Identifier minecraftKey = Identifier.fromNamespaceAndPath("interactivechat", "mentioned/" + sender.getUniqueId());
        AdvancementRewards advancementRewards = new AdvancementRewards(0, Collections.emptyList(), Collections.emptyList(), Optional.empty());
        net.minecraft.network.chat.Component componentTitle = CraftChatMessage.fromJSON(messageJson);
        net.minecraft.network.chat.Component componentSubtitle = net.minecraft.network.chat.Component.literal("");
        DisplayInfo advancementDisplay = new DisplayInfo(ItemStackTemplate.fromNonEmptyStack(toNMSCopy(icon)), componentTitle, componentSubtitle, Optional.empty(), AdvancementType.GOAL, true, false, true);

        Map<String, Criterion<?>> advancementCriteria = new HashMap<>();
        Criterion<ImpossibleTrigger.TriggerInstance> criterion = new Criterion<>(new ImpossibleTrigger(), new ImpossibleTrigger.TriggerInstance());
        advancementCriteria.put("for_free", criterion);

        List<List<String>> fixedRequirements = Collections.singletonList(Collections.singletonList("for_free"));
        AdvancementRequirements advancementRequirements = new AdvancementRequirements(fixedRequirements);

        Advancement advancement = new Advancement(Optional.empty(), Optional.of(advancementDisplay), advancementRewards, advancementCriteria, advancementRequirements, false);

        Map<Identifier, AdvancementProgress> advancementProgresses = new HashMap<>();
        AdvancementProgress advancementProgress = new AdvancementProgress();
        advancementProgress.update(advancementRequirements);
        advancementProgress.getCriterion("for_free").grant();
        advancementProgresses.put(minecraftKey, advancementProgress);

        List<AdvancementHolder> advancements = Collections.singletonList(new AdvancementHolder(minecraftKey, advancement));

        ServerGamePacketListenerImpl connection = ((CraftPlayer) pinged).getHandle().connection;

        ClientboundUpdateAdvancementsPacket packet1 = new ClientboundUpdateAdvancementsPacket(false, advancements, Collections.emptySet(), advancementProgresses, true);
        connection.send(packet1);

        Set<Identifier> removeAdvancements = Collections.singleton(minecraftKey);
        ClientboundUpdateAdvancementsPacket packet2 = new ClientboundUpdateAdvancementsPacket(false, Collections.emptyList(), removeAdvancements, Collections.emptyMap(), true);
        connection.send(packet2);
    }

    @Override
    public void setBossbarTitle(Object bukkitBossbar, Component component) {
        CraftBossBar craftBossBar = (CraftBossBar) bukkitBossbar;
        craftBossBar.getHandle().setName(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(component)));
    }

    @Override
    public void sendTitle(Player player, Component title, Component subtitle, Component actionbar, int fadeIn, int stay, int fadeOut) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;

        ClientboundClearTitlesPacket packet1 = new ClientboundClearTitlesPacket(true);
        connection.send(packet1);

        if (!PlainTextComponentSerializer.plainText().serialize(title).isEmpty()) {
            ClientboundSetTitleTextPacket packet2 = new ClientboundSetTitleTextPacket(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(title)));
            connection.send(packet2);
        }

        if (!PlainTextComponentSerializer.plainText().serialize(subtitle).isEmpty()) {
            ClientboundSetSubtitleTextPacket packet3 = new ClientboundSetSubtitleTextPacket(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(subtitle)));
            connection.send(packet3);
        }

        if (!PlainTextComponentSerializer.plainText().serialize(actionbar).isEmpty()) {
            ClientboundSetActionBarTextPacket packet4 = new ClientboundSetActionBarTextPacket(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(actionbar)));
            connection.send(packet4);
        }

        ClientboundSetTitlesAnimationPacket packet5 = new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);
        connection.send(packet5);
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

        NonNullList<net.minecraft.world.item.ItemStack> itemList = NonNullList.create();
        for (ItemStack itemStack : items) {
            itemList.add(toNMSCopy(itemStack));
        }

        ClientboundContainerSetContentPacket packet1 = new ClientboundContainerSetContentPacket(0, 0, itemList, toNMSCopy(ITEM_STACK_AIR));
        ClientboundContainerSetSlotPacket packet2 = new ClientboundContainerSetSlotPacket(-1, -1, 0, toNMSCopy(ITEM_STACK_AIR));

        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(packet1);
        connection.send(packet2);
    }

    @Override
    public void sendFakeMainHandSlot(Player player, ItemStack item) {
        List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> nmsEquipments = Collections.singletonList(new Pair<>(EquipmentSlot.MAINHAND, toNMSCopy(item)));
        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(player.getEntityId(), nmsEquipments);
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    @Override
    public void sendFakeMapUpdate(Player player, int mapId, List<MapCursor> mapCursors, byte[] colors) {
        List<MapDecoration> mapIcons = toNMSMapIconList(mapCursors);
        MapItemSavedData.MapPatch c = new MapItemSavedData.MapPatch(0, 0, 128, 128, colors);
        ClientboundMapItemDataPacket packet = new ClientboundMapItemDataPacket(new MapId(mapId), (byte) 0, false, Optional.of(mapIcons), Optional.of(c));
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    @Override
    public InternalOfflinePlayerInfo loadOfflinePlayer(UUID uuid, Inventory inventory, Inventory enderchest) {
        try {
            nmsEntityPlayerLoadMethod.setAccessible(true);
            nmsEntityPlayerServerField.setAccessible(true);

            MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
            ServerLevel worldServer = server.overworld();
            OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
            GameProfile profile = new GameProfile(offline.getUniqueId(), offline.getName() != null ? offline.getName() : offline.getUniqueId().toString());
            ClientInformation dummyInfo = new ClientInformation("en_us", 1, ChatVisiblity.HIDDEN, false, 0, ServerPlayer.DEFAULT_MAIN_HAND, true, false, ParticleStatus.MINIMAL);
            ServerPlayer player = new ServerPlayer(server, worldServer, profile, dummyInfo);
            player.getAdvancements().stopListening();

            MinecraftServer minecraftServer = (MinecraftServer) nmsEntityPlayerServerField.get(player);
            CompoundTag loadedNbt = minecraftServer.getPlayerList().playerIo.load(player.nameAndId()).orElse(null);
            if (loadedNbt == null) {
                return null;
            }
            ValueInput loadedData = TagValueInput.create(ProblemReporter.DISCARDING, CraftRegistry.getMinecraftRegistry(), loadedNbt);

            player.load(loadedData);
            nmsEntityPlayerLoadMethod.invoke(player, loadedData);
            player.loadAndSpawnParentVehicle(loadedData);

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
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object deserializeChatComponent(String json) {
        return CraftChatMessage.fromJSON(json);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String serializeChatComponent(Object handle, Player player) {
        if (player != null && paperComponentSerializationLocalizedCodec != null) {
            try {
                nmsPlayerConnectionNetworkManagerField.setAccessible(true);
                ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
                Connection networkManager = ((Connection) nmsPlayerConnectionNetworkManagerField.get(connection));
                Locale locale = (Locale) networkManager.channel.attr(AttributeKey.valueOf("adventure:locale")).get();
                Codec<net.minecraft.network.chat.Component> codec = (Codec<net.minecraft.network.chat.Component>) paperComponentSerializationLocalizedCodec.invoke(null, locale);
                return NativeJsonConverter.toJson(codec.encodeStart(CraftRegistry.getMinecraftRegistry().createSerializationContext(JsonOps.INSTANCE), (net.minecraft.network.chat.Component) handle).getOrThrow(JsonParseException::new));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return CraftChatMessage.toJSON((net.minecraft.network.chat.Component) handle);
    }
}
