package com.loohp.interactivechat.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ReplaceTextBundle;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.CollectionUtils;
import com.loohp.interactivechat.utils.ComponentCompacting;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.CustomStringUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.VanishUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class PlayernameDisplay implements Listener {
	
	private static Random random = new Random();
	private static AtomicInteger flag = new AtomicInteger();
	private static List<ReplaceTextBundle> names = new ArrayList<>();
	
	public static void setup() {
		Bukkit.getPluginManager().registerEvents(new PlayernameDisplay(), InteractiveChat.plugin);
		Bukkit.getScheduler().runTaskTimerAsynchronously(InteractiveChat.plugin, () -> {
			int valid = flag.get();
			List<ReplaceTextBundle> names = getNames();
			Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> {
				if (flag.get() == valid) {
					PlayernameDisplay.names = names;
				}
			});
		}, 0, 100);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerLoginEvent event) {
		if (event.getResult().equals(PlayerLoginEvent.Result.ALLOWED)) {
			flag.set(random.nextInt());
			names = null;
		}
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		flag.set(random.nextInt());
		names = null;
	}
	
	public static Component process(Component component, Optional<ICPlayer> sender, Player reciever, long unix) {
		List<ReplaceTextBundle> names = PlayernameDisplay.names;
		if (names == null) {
			names = getNames();
		}
		for (ReplaceTextBundle entry : names) {
			component = processPlayer(entry.getPlaceholder(), entry.getPlayer(), sender, reciever, component, unix);
		}
		
		return ComponentCompacting.optimize(component);
	}
	
	private static Component processPlayer(String placeholder, ICPlayer player, Optional<ICPlayer> sender, Player reciever, Component component, long unix) {
		String plain = InteractiveChatComponentSerializer.plainText().serialize(component);
		if (InteractiveChat.usePlayerNameCaseSensitive) {
			if (!plain.contains(placeholder)) {
				return component;
			}
		} else {
			if (!plain.toLowerCase().contains(placeholder.toLowerCase())) {
				return component;
			}
		}
		HoverEvent<?> hoverEvent;
		ClickEvent clickEvent;
		if (InteractiveChat.usePlayerNameHoverEnable) {
			String playertext = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.usePlayerNameHoverText));
			hoverEvent = HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(playertext));
		} else {
			hoverEvent = null;
		}
		if (InteractiveChat.usePlayerNameClickEnable) {
			String playertext = PlaceholderParser.parse(player, InteractiveChat.usePlayerNameClickValue);
			clickEvent = ClickEvent.clickEvent(ClickEvent.Action.valueOf(InteractiveChat.usePlayerNameClickAction), playertext);
		} else {
			clickEvent = null;
		}
		String regex = InteractiveChat.usePlayerNameCaseSensitive ? CustomStringUtils.escapeMetaCharacters(placeholder) : "(?i)" + CustomStringUtils.escapeMetaCharacters(placeholder);
		component = ComponentReplacing.replace(component, regex, true, (result, replaced) -> {
			List<Component> children = new ArrayList<>();
			for (Component c : replaced) {
				Component edited = c;
				if (hoverEvent != null) {
					edited = edited.hoverEvent(hoverEvent);
				}
				if (clickEvent != null) {
					edited = edited.clickEvent(clickEvent);
				}
				children.add(edited);
			}
			return Component.empty().children(children);
		});
		List<Component> children = new ArrayList<>(component.children());
		for (int i = 0; i < children.size(); i++) {
			Component child = children.get(i);
			if (child instanceof TranslatableComponent) {
				TranslatableComponent trans = (TranslatableComponent) child;
				List<Component> withs = new ArrayList<>(trans.args());
				for (int u = 0; u < withs.size(); u++) {
					Component with = withs.get(u);
					withs.set(u, processPlayer(placeholder, player, sender, reciever, with, unix));
				}
				trans = trans.args(withs);
				children.set(i, trans);
			}
		}
		return ComponentCompacting.optimize(component.children(children));
	}
	
	private static List<ReplaceTextBundle> getNames() {
		List<ReplaceTextBundle> names = new ArrayList<>();
		Bukkit.getOnlinePlayers().forEach(each -> {
			if (VanishUtils.isVanished(each.getUniqueId())) {
				return;
			}
			ICPlayer icplayer = new ICPlayer(each);
			names.add(new ReplaceTextBundle(ChatColorUtils.stripColor(each.getName()), icplayer, each.getName()));
			if (InteractiveChat.useBukkitDisplayName && !ChatColorUtils.stripColor(each.getName()).equals(ChatColorUtils.stripColor(each.getDisplayName()))) {
				names.add(new ReplaceTextBundle(ChatColorUtils.stripColor(each.getDisplayName()), icplayer, each.getDisplayName()));
			}
			List<String> list = InteractiveChatAPI.getNicknames(each.getUniqueId());
			for (String name : list) {
				names.add(new ReplaceTextBundle(ChatColorUtils.stripColor(name), icplayer, name));
			}
		});	
		InteractiveChat.remotePlayers.values().forEach(each -> {
			if (each.isLocal() || VanishUtils.isVanished(each.getUniqueId())) {
				return;
			}
			names.add(new ReplaceTextBundle(ChatColorUtils.stripColor(each.getName()), each, each.getName()));
			List<String> list = InteractiveChatAPI.getNicknames(each.getUniqueId());
			for (String name : list) {
				names.add(new ReplaceTextBundle(ChatColorUtils.stripColor(name), each, name));
			}
		});
		
		CollectionUtils.filter(names, each -> each.getPlaceholder().length() > 2);
		Collections.sort(names, Collections.reverseOrder());
		
		return names;
	}

}
