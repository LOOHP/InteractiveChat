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

package com.loohp.interactivechat.utils.bossbar;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.ChatComponentType;
import com.loohp.interactivechat.utils.NMSUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BossBarUpdaterV1_16Impl extends BossBarUpdaterV1_9Impl {

    private static Class<?> craftBossBarClass;
    private static Field craftBossBarHandleField;
    private static Class<?> nmsIChatBaseComponentClass;
    private static Method nmsBossBattleServerSetNameMethod;

    static {
        try {
            craftBossBarClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.boss.CraftBossBar");
            craftBossBarHandleField = craftBossBarClass.getDeclaredField("handle");
            craftBossBarHandleField.setAccessible(true);
            nmsIChatBaseComponentClass = NMSUtils.getNMSClass("net.minecraft.server.%s.IChatBaseComponent", "net.minecraft.network.chat.IChatBaseComponent");
            nmsBossBattleServerSetNameMethod = craftBossBarHandleField.getType().getMethod("a", nmsIChatBaseComponentClass);
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    protected Object nmsBossBattleServer;

    public BossBarUpdaterV1_16Impl(BossBar bossbar, Player[] players) {
        super(bossbar, players);
        try {
            Object craftBossBar = craftBossBarClass.cast(bukkitBossbar);
            craftBossBarHandleField.setAccessible(true);
            nmsBossBattleServer = craftBossBarHandleField.get(craftBossBar);
            nmsBossBattleServerSetNameMethod.invoke(nmsBossBattleServer, ChatComponentType.IChatBaseComponent.convertTo(bossbar.name(), InteractiveChat.version.isLegacyRGB()));
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void bossBarNameChanged(BossBar bar, Component oldName, Component newName) {
        try {
            nmsBossBattleServerSetNameMethod.invoke(nmsBossBattleServer, ChatComponentType.IChatBaseComponent.convertTo(newName, InteractiveChat.version.isLegacyRGB()));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
