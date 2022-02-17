/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
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

package com.loohp.interactivechat.utils;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.objectholders.ICPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ToastUtils {

    private static Class<?> nmsMinecraftKeyClass;
    private static Constructor<?> nmsMinecraftKeyConstructor;
    private static Class<?> nmsAdvancementRewardsClass;
    private static Class<?> nmsCustomFunctionAClass;
    private static Constructor<?> nmsAdvancementRewardsConstructor;
    private static Class<?> nmsAdvancementDisplayClass;
    private static Class<?> nmsItemStackClass;
    private static Class<?> nmsIChatBaseComponentClass;
    private static Class<?> nmsAdvancementFrameTypeClass;
    private static Constructor<?> nmsAdvancementDisplayConstructor;
    private static Object[] nmsAdvancementFrameTypeEnums;
    private static Class<?> nmsCriterionClass;
    private static Class<?> nmsCriterionTriggerImpossibleAClass;
    private static Constructor<?> nmsCriterionTriggerImpossibleAConstructor;
    private static Class<?> nmsCriterionInstanceClass;
    private static Constructor<?> nmsCriterionConstructor;
    private static Class<?> nmsAdvancementClass;
    private static Constructor<?> nmsAdvancementConstructor;
    private static Class<?> nmsAdvancementProgressClass;
    private static Constructor<?> nmsAdvancementProgressConstructor;
    private static Method nmsAdvancementProgressAMethod;
    private static Method nmsAdvancementProgressGetCriterionProgressMethod;
    private static Method nmsAdvancementProgressGetCriterionProgressBMethod;
    private static Class<?> nmsPacketPlayOutAdvancementsClass;
    private static Constructor<?> nmsPacketPlayOutAdvancementsConstuctor;

    static {
        try {
            nmsMinecraftKeyClass = NMSUtils.getNMSClass("net.minecraft.server.%s.MinecraftKey", "net.minecraft.resources.MinecraftKey");
            nmsMinecraftKeyConstructor = nmsMinecraftKeyClass.getConstructor(String.class, String.class);
            nmsAdvancementRewardsClass = NMSUtils.getNMSClass("net.minecraft.server.%s.AdvancementRewards", "net.minecraft.advancements.AdvancementRewards");
            nmsCustomFunctionAClass = NMSUtils.getNMSClass("net.minecraft.server.%s.CustomFunction$a", "net.minecraft.commands.CustomFunction$a");
            nmsAdvancementRewardsConstructor = nmsAdvancementRewardsClass.getConstructor(int.class, ClassUtils.arrayType(nmsMinecraftKeyClass), ClassUtils.arrayType(nmsMinecraftKeyClass), nmsCustomFunctionAClass);
            nmsAdvancementDisplayClass = NMSUtils.getNMSClass("net.minecraft.server.%s.AdvancementDisplay", "net.minecraft.advancements.AdvancementDisplay");
            nmsItemStackClass = NMSUtils.getNMSClass("net.minecraft.server.%s.ItemStack", "net.minecraft.world.item.ItemStack");
            nmsIChatBaseComponentClass = NMSUtils.getNMSClass("net.minecraft.server.%s.IChatBaseComponent", "net.minecraft.network.chat.IChatBaseComponent");
            nmsAdvancementFrameTypeClass = NMSUtils.getNMSClass("net.minecraft.server.%s.AdvancementFrameType", "net.minecraft.advancements.AdvancementFrameType");
            nmsAdvancementDisplayConstructor = nmsAdvancementDisplayClass.getConstructor(nmsItemStackClass, nmsIChatBaseComponentClass, nmsIChatBaseComponentClass, nmsMinecraftKeyClass, nmsAdvancementFrameTypeClass, boolean.class, boolean.class, boolean.class);
            nmsAdvancementFrameTypeEnums = nmsAdvancementFrameTypeClass.getEnumConstants();
            nmsCriterionClass = NMSUtils.getNMSClass("net.minecraft.server.%s.Criterion", "net.minecraft.advancements.Criterion");
            nmsCriterionTriggerImpossibleAClass = NMSUtils.getNMSClass("net.minecraft.server.%s.CriterionTriggerImpossible$a", "net.minecraft.advancements.critereon.CriterionTriggerImpossible$a");
            nmsCriterionTriggerImpossibleAConstructor = nmsCriterionTriggerImpossibleAClass.getConstructor();
            nmsCriterionInstanceClass = NMSUtils.getNMSClass("net.minecraft.server.%s.CriterionInstance", "net.minecraft.advancements.CriterionInstance");
            nmsCriterionConstructor = nmsCriterionClass.getConstructor(nmsCriterionInstanceClass);
            nmsAdvancementClass = NMSUtils.getNMSClass("net.minecraft.server.%s.Advancement", "net.minecraft.advancements.Advancement");
            nmsAdvancementConstructor = nmsAdvancementClass.getConstructor(nmsMinecraftKeyClass, nmsAdvancementClass, nmsAdvancementDisplayClass, nmsAdvancementRewardsClass, Map.class, String[][].class);
            nmsAdvancementProgressClass = NMSUtils.getNMSClass("net.minecraft.server.%s.AdvancementProgress", "net.minecraft.advancements.AdvancementProgress");
            nmsAdvancementProgressConstructor = nmsAdvancementProgressClass.getConstructor();
            nmsAdvancementProgressAMethod = nmsAdvancementProgressClass.getMethod("a", Map.class, String[][].class);
            try {
                nmsAdvancementProgressGetCriterionProgressMethod = nmsAdvancementProgressClass.getMethod("getCriterionProgress", String.class);
            } catch (Exception e) {
                nmsAdvancementProgressGetCriterionProgressMethod = nmsAdvancementProgressClass.getMethod("c", String.class);
            }
            nmsAdvancementProgressGetCriterionProgressBMethod = nmsAdvancementProgressGetCriterionProgressMethod.getReturnType().getMethod("b");
            nmsPacketPlayOutAdvancementsClass = NMSUtils.getNMSClass("net.minecraft.server.%s.PacketPlayOutAdvancements", "net.minecraft.network.protocol.game.PacketPlayOutAdvancements");
            nmsPacketPlayOutAdvancementsConstuctor = nmsPacketPlayOutAdvancementsClass.getConstructor(boolean.class, Collection.class, Set.class, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void mention(ICPlayer sender, Player pinged, String message, ItemStack icon) {
        try {
            Object minecraftKey = nmsMinecraftKeyConstructor.newInstance("interactivechat", "mentioned/" + sender.getUniqueId());
            Object advRewards = nmsAdvancementRewardsConstructor.newInstance(0, Array.newInstance(nmsMinecraftKeyClass, 0), Array.newInstance(nmsMinecraftKeyClass, 0), null);
            Object componentTitle = ChatComponentType.IChatBaseComponent.convertTo(LegacyComponentSerializer.legacySection().deserialize(message), InteractiveChat.version.isLegacy());
            Object componentSubtitle = ChatComponentType.IChatBaseComponent.convertTo(Component.empty(), InteractiveChat.version.isLegacy());
            Object advancementDisplay = nmsAdvancementDisplayConstructor.newInstance(ItemStackUtils.toNMSCopy(icon), componentTitle, componentSubtitle, null, nmsAdvancementFrameTypeEnums[2], true, false, true);

            Map<String, Object> advCriteria = new HashMap<>();
            String[][] advRequirements = new String[][] {};
            advCriteria.put("for_free", nmsCriterionConstructor.newInstance(nmsCriterionTriggerImpossibleAConstructor.newInstance()));
            List<String[]> fixedRequirements = new ArrayList<>();
            fixedRequirements.add(new String[] {"for_free"});
            advRequirements = Arrays.stream(fixedRequirements.toArray()).toArray(String[][]::new);

            Object saveAdv = nmsAdvancementConstructor.newInstance(minecraftKey, null, advancementDisplay, advRewards, advCriteria, advRequirements);

            Map<Object, Object> prg = new HashMap<>();
            Object advPrg = nmsAdvancementProgressConstructor.newInstance();
            nmsAdvancementProgressAMethod.invoke(advPrg, advCriteria, advRequirements);
            nmsAdvancementProgressGetCriterionProgressBMethod.invoke(nmsAdvancementProgressGetCriterionProgressMethod.invoke(advPrg, "for_free"));
            prg.put(minecraftKey, advPrg);

            PacketContainer packet1 = PacketContainer.fromPacket(nmsPacketPlayOutAdvancementsConstuctor.newInstance(false, Arrays.asList(saveAdv), Collections.emptySet(), prg));
            InteractiveChat.protocolManager.sendServerPacket(pinged, packet1);

            Set<Object> rm = new HashSet<>();
            rm.add(minecraftKey);
            prg.clear();
            PacketContainer packet2 = PacketContainer.fromPacket(nmsPacketPlayOutAdvancementsConstuctor.newInstance(false, Collections.emptyList(), rm, prg));
            InteractiveChat.protocolManager.sendServerPacket(pinged, packet2);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
