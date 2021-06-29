package com.loohp.interactivechat.utils.bossbar;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.ChatComponentType;
import com.loohp.interactivechat.utils.NMSUtils;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

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
