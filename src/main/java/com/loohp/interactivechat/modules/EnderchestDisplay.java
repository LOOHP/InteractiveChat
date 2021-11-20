package com.loohp.interactivechat.modules;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.api.InteractiveChatAPI.SharedType;
import com.loohp.interactivechat.api.events.InventoryPlaceholderEvent;
import com.loohp.interactivechat.api.events.InventoryPlaceholderEvent.InventoryPlaceholderType;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageSender;
import com.loohp.interactivechat.config.ConfigManager;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.CustomStringUtils;
import com.loohp.interactivechat.utils.HashUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.InventoryUtils;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class EnderchestDisplay {
	
	public static Component process(Component component, Optional<ICPlayer> optplayer, Player reciever, long unix) throws Exception {
		String plain = InteractiveChatComponentSerializer.plainText().serialize(component);
		if (InteractiveChat.enderCaseSensitive ? plain.contains(InteractiveChat.enderPlaceholder) : plain.toLowerCase().contains(InteractiveChat.enderPlaceholder.toLowerCase())) {
			String regex = InteractiveChat.enderCaseSensitive ? CustomStringUtils.escapeMetaCharacters(InteractiveChat.enderPlaceholder) : "(?i)" + CustomStringUtils.escapeMetaCharacters(InteractiveChat.enderPlaceholder);
			if (optplayer.isPresent()) {
				ICPlayer player = optplayer.get();
				if (PlayerUtils.hasPermission(player.getUniqueId(), "interactivechat.module.enderchest", true, 5)) {
					
					String replaceText = InteractiveChat.enderReplaceText;
					String title = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.enderTitle));
					String sha1 = HashUtils.createSha1(title, player.getEnderChest());
					
					if (!InteractiveChat.enderDisplay.containsKey(sha1)) {
						int size = player.getEnderChest().getSize();
						Inventory inv = Bukkit.createInventory(null, InventoryUtils.toMultipleOf9(size), title);
						for (int j = 0; j < size; j++) {
							if (player.getEnderChest().getItem(j) != null) {
								if (!player.getEnderChest().getItem(j).getType().equals(Material.AIR)) {
									inv.setItem(j, player.getEnderChest().getItem(j).clone());
								}
							}
						}
						InventoryPlaceholderEvent event = new InventoryPlaceholderEvent(player, reciever, component, unix, inv, InventoryPlaceholderType.ENDERCHEST);
						Bukkit.getPluginManager().callEvent(event);
						inv = event.getInventory();
						
						InteractiveChatAPI.addInventoryToItemShareList(SharedType.ENDERCHEST, sha1, inv);
						
						if (InteractiveChat.bungeecordMode) {
							if (player.isLocal()) {
								try {
									BungeeMessageSender.forwardEnderchest(unix, player.getUniqueId(), player.isRightHanded(), player.getSelectedSlot(), player.getExperienceLevel(), null, inv);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
					
					String componentText = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, replaceText));
					
					List<String> hoverList = ConfigManager.getConfig().getStringList("ItemDisplay.EnderChest.HoverMessage");
					String hoverText = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, String.join("\n", hoverList)));
					
					String command = "/interactivechat viewender " + sha1;
					
					Component enderComponent = LegacyComponentSerializer.legacySection().deserialize(componentText);
					enderComponent = enderComponent.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(hoverText)));
					enderComponent = enderComponent.clickEvent(ClickEvent.runCommand(command));
					component = ComponentReplacing.replace(component, regex, true, enderComponent);
				}
			} else {
				Component message;
				if (InteractiveChat.playerNotFoundReplaceEnable) {
					message = LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.playerNotFoundReplaceText.replace("{Placeholder}", InteractiveChat.enderPlaceholder));
				} else {
					message = Component.text(InteractiveChat.enderPlaceholder);
				}
				if (InteractiveChat.playerNotFoundHoverEnable) {
					message = message.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.playerNotFoundHoverText.replace("{Placeholder}", InteractiveChat.enderPlaceholder))));
				}
				if (InteractiveChat.playerNotFoundClickEnable) {
					String clickValue = ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.playerNotFoundClickValue.replace("{Placeholder}", InteractiveChat.enderPlaceholder));
					message = message.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.valueOf(InteractiveChat.playerNotFoundClickAction), clickValue));
				}
				component = ComponentReplacing.replace(component, regex, true, message);
			}
			
			return component;
		} else {
			return component;
		}
	}

}
