package com.loohp.interactivechat.modules;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.data.PlayerDataManager.PlayerData;
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.objectholders.ProcessSenderResult;
import com.loohp.interactivechat.objectholders.WebData;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.ComponentFont;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.CustomStringUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.ItemStackUtils;
import com.loohp.interactivechat.utils.JsonUtils;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;
import com.loohp.interactivechat.utils.RarityUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class ProcessExternalMessage {
	
	private static InteractiveChat initPlugin = InteractiveChat.plugin;
	
	private static Plugin plugin;
	
	/**
	 * This is to support /reload
	 */
	private static Object getInstance() throws Exception {
		Field externalProcessorField;
		if (plugin == null || !plugin.isEnabled()) {
			plugin = Bukkit.getPluginManager().getPlugin("InteractiveChat");
		}
		externalProcessorField = plugin.getClass().getField("externalProcessor");
		return externalProcessorField.get(plugin);
	}
	
	public static String processWithoutReviever(String message) {
		if (initPlugin.isEnabled()) {
			return initPlugin.externalProcessor.processWithoutReviever0(message);
		} else {
			try {
				Object obj = getInstance();
				Method processWithoutReviever0Method = obj.getClass().getMethod("processWithoutReviever0", String.class);
				return (String) processWithoutReviever0Method.invoke(obj, message);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return message;
		}
	}
	
	public static String processAndRespond(Player reciever, String component) throws Exception {
		if (initPlugin.isEnabled()) {
			return initPlugin.externalProcessor.processAndRespond0(reciever, component);
		} else {
			try {
				Object obj = getInstance();
				Method processAndRespond0Method = obj.getClass().getMethod("processAndRespond0", Player.class, String.class);
				return (String) processAndRespond0Method.invoke(obj, reciever, component);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return component;
		}
	}
	
	@SuppressWarnings("deprecation")
	public String processWithoutReviever0(String message) {
		UUID senderUUID = ProcessAccurateSender.find(message);
		ICPlayer sender;
		if (senderUUID == null) {
			sender = null;
		} else {
			sender = ICPlayerFactory.getICPlayer(senderUUID);
		}
		
		message = message.replaceAll(ProcessCommands.COLOR_IGNORE_PATTERN_0.pattern(), "").replaceAll(ProcessCommands.COLOR_IGNORE_PATTERN_1.pattern(), "").replaceAll(ProcessAccurateSender.COLOR_IGNORE_PATTERN.pattern(), "");
		
		if (sender == null) {
			return message;
		}
        
		long now = System.currentTimeMillis();
		long uniCooldown = InteractiveChatAPI.getPlayerUniversalCooldown(sender.getUniqueId()) - now;
		
		if (!(uniCooldown < 0 || uniCooldown + 100 > InteractiveChat.universalCooldown)) {
			return message;
		}
		
		if (InteractiveChat.rgbTags) {
			message = CustomStringUtils.clearPluginRGBTags(message);
		}
		if (InteractiveChat.fontTags) {
			message = CustomStringUtils.clearPluginFontTags(message);
		}
		
		Component component = LegacyComponentSerializer.legacySection().deserialize(message);

		if (InteractiveChat.useItem && PlayerUtils.hasPermission(sender.getUniqueId(), "interactivechat.module.item", true, 250)) {
			String placeholder = InteractiveChat.itemPlaceholder;
			if (InteractiveChat.itemCaseSensitive ? message.contains(placeholder) : message.toLowerCase().contains(placeholder.toLowerCase())) {
				ItemStack item = sender.getEquipment().getItemInHand();
				if (item == null) {
					item = new ItemStack(Material.AIR);
				}
				String itemStr = InteractiveChatComponentSerializer.bungeecordApiLegacy().serialize(ItemStackUtils.getDisplayName(item), InteractiveChat.language);
				
				int amount = item.getAmount();
				if (item == null || item.getType().equals(Material.AIR)) {
					amount = 1;
				}
				
				itemStr = RarityUtils.getRarityColor(item) + itemStr;
			
				String replaceText;
				if (amount == 1) {
					replaceText = PlaceholderParser.parse(sender, InteractiveChat.itemSingularReplaceText.replace("{Item}", itemStr));
				} else {
					replaceText = PlaceholderParser.parse(sender, InteractiveChat.itemReplaceText.replace("{Amount}", String.valueOf(amount)).replace("{Item}", itemStr));
				}
				if (InteractiveChat.itemCaseSensitive) {
					component = ComponentReplacing.replace(component, CustomStringUtils.escapeMetaCharacters(placeholder), true, LegacyComponentSerializer.legacySection().deserialize(replaceText));
				} else {
					component = ComponentReplacing.replace(component, "(?i)" + CustomStringUtils.escapeMetaCharacters(placeholder), true, LegacyComponentSerializer.legacySection().deserialize(replaceText));
				}
			}
		}
		
		if (InteractiveChat.useInventory && PlayerUtils.hasPermission(sender.getUniqueId(), "interactivechat.module.inventory", true, 250)) {
			String placeholder = InteractiveChat.invPlaceholder;
			if (InteractiveChat.invCaseSensitive ? message.contains(placeholder) : message.toLowerCase().contains(placeholder.toLowerCase())) {
				String replaceText = PlaceholderParser.parse(sender, InteractiveChat.invReplaceText);
				if (InteractiveChat.invCaseSensitive) {
					component = ComponentReplacing.replace(component, CustomStringUtils.escapeMetaCharacters(placeholder), true, LegacyComponentSerializer.legacySection().deserialize(replaceText));
				} else {
					component = ComponentReplacing.replace(component, "(?i)" + CustomStringUtils.escapeMetaCharacters(placeholder), true, LegacyComponentSerializer.legacySection().deserialize(replaceText));
				}
			}
		}
		
		if (InteractiveChat.useEnder && PlayerUtils.hasPermission(sender.getUniqueId(), "interactivechat.module.enderchest", true, 250)) {
			String placeholder = InteractiveChat.enderPlaceholder;
			if (InteractiveChat.enderCaseSensitive ? message.contains(placeholder) : message.toLowerCase().contains(placeholder.toLowerCase())) {
				String replaceText = PlaceholderParser.parse(sender, InteractiveChat.enderReplaceText);
				if (InteractiveChat.enderCaseSensitive) {
					component = ComponentReplacing.replace(component, CustomStringUtils.escapeMetaCharacters(placeholder), true, LegacyComponentSerializer.legacySection().deserialize(replaceText));
				} else {
					component = ComponentReplacing.replace(component, "(?i)" + CustomStringUtils.escapeMetaCharacters(placeholder), true, LegacyComponentSerializer.legacySection().deserialize(replaceText));
				}
			}
		}
		
		for (ICPlaceholder placeholder : InteractiveChatAPI.getICPlaceholderList()) {
			if (!placeholder.isBuildIn()) {
				CustomPlaceholder customP = (CustomPlaceholder) placeholder;
				if (!InteractiveChat.useCustomPlaceholderPermissions || (InteractiveChat.useCustomPlaceholderPermissions && PlayerUtils.hasPermission(sender.getUniqueId(), customP.getPermission(), true, 250))) {
					if (customP.isCaseSensitive() ? message.contains(customP.getKeyword()) : message.toLowerCase().contains(customP.getKeyword().toLowerCase())) {
						String replaceText = customP.getKeyword();
						if (customP.getReplace().isEnabled()) {
							replaceText = ChatColor.WHITE + PlaceholderParser.parse(sender, customP.getReplace().getReplaceText());
							if (customP.isCaseSensitive()) {
								component = ComponentReplacing.replace(component, CustomStringUtils.escapeMetaCharacters(customP.getKeyword()), true, LegacyComponentSerializer.legacySection().deserialize(replaceText));
							} else {
								component = ComponentReplacing.replace(component, "(?i)" + CustomStringUtils.escapeMetaCharacters(customP.getKeyword()), true, LegacyComponentSerializer.legacySection().deserialize(replaceText));
							}
						}
					}
				}
			}
		}
		
		if (InteractiveChat.t && WebData.getInstance() != null) {
			for (CustomPlaceholder customP : WebData.getInstance().getSpecialPlaceholders()) {
				if (customP.isCaseSensitive() ? message.contains(customP.getKeyword()) : message.toLowerCase().contains(customP.getKeyword().toLowerCase())) {
					String replaceText = customP.getKeyword();
					if (customP.getReplace().isEnabled()) {
						replaceText = ChatColor.WHITE + PlaceholderParser.parse(sender, customP.getReplace().getReplaceText());
						if (customP.isCaseSensitive()) {
							component = ComponentReplacing.replace(component, CustomStringUtils.escapeMetaCharacters(customP.getKeyword()), true, LegacyComponentSerializer.legacySection().deserialize(replaceText));
						} else {
							component = ComponentReplacing.replace(component, "(?i)" + CustomStringUtils.escapeMetaCharacters(customP.getKeyword()), true, LegacyComponentSerializer.legacySection().deserialize(replaceText));
						}
					}
				}
			}
		}
		
		return LegacyComponentSerializer.legacySection().serialize(component);
	}
	
	public String processAndRespond0(Player reciever, String json) throws Exception {
		Component component = InteractiveChatComponentSerializer.gson().deserialize(json);
		Component originalComponent = component;
        
        try {
        	if (LegacyComponentSerializer.legacySection().serialize(component).isEmpty()) {
        		return json;
        	}
        } catch (Exception e) {
        	return json;
        }
        
        if ((InteractiveChat.version.isOld()) && JsonUtils.containsKey(InteractiveChatComponentSerializer.gson().serialize(component), "translate")) {		       
        	return json;
        }
        
        Optional<ICPlayer> sender = Optional.empty();
		String rawMessageKey = InteractiveChatComponentSerializer.plainText().serializeOr(component, "");
        	   
        InteractiveChat.keyTime.putIfAbsent(rawMessageKey, System.currentTimeMillis());

        Long timeKey = InteractiveChat.keyTime.get(rawMessageKey);
        long unix = timeKey == null ? System.currentTimeMillis() : timeKey;
        
        ProcessSenderResult commandSender = ProcessCommands.process(component);
        if (commandSender.getSender() != null) {
        	sender = Optional.ofNullable(ICPlayerFactory.getICPlayer(commandSender.getSender()));
        }
        ProcessSenderResult chatSender = null;
        if (!sender.isPresent()) {
        	if (InteractiveChat.useAccurateSenderFinder) {
        		chatSender = ProcessAccurateSender.process(component);
        		if (chatSender.getSender() != null) {
        			sender = Optional.ofNullable(ICPlayerFactory.getICPlayer(chatSender.getSender()));
    	        }
        	}
        }
        if (!sender.isPresent()) {
        	sender = SenderFinder.getSender(component, rawMessageKey);
        }
        
        component = commandSender.getComponent();
        if (chatSender != null) {
        	component = chatSender.getComponent();
        }
        
        String text = LegacyComponentSerializer.legacySection().serialize(component);
        if (InteractiveChat.messageToIgnore.stream().anyMatch(each -> text.matches(each))) {
        	return json;
        }
        
        if (sender.isPresent()) {
        	InteractiveChat.keyPlayer.put(rawMessageKey, sender.get());
        }
        
        String server;
        if (sender.isPresent() && !sender.get().isLocal()) {
        	try {
				TimeUnit.MILLISECONDS.sleep(InteractiveChat.remoteDelay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	server = sender.get().getServer();
        } else {
        	server = ICPlayer.LOCAL_SERVER_REPRESENTATION;
        }
        
        component = ComponentReplacing.replace(component, Registry.ID_PATTERN.pattern(), Component.empty());
        
        if (InteractiveChat.usePlayerName) {
        	component = PlayernameDisplay.process(component, sender, reciever, unix);
        }
        
        if (InteractiveChat.allowMention && sender.isPresent()) {
        	PlayerData data = InteractiveChat.playerDataManager.getPlayerData(reciever);
        	if (data == null || !data.isMentionDisabled()) {
        		component = MentionDisplay.process(component, reciever, sender.get(), unix, !Bukkit.isPrimaryThread());
        	}
        }
        
        if (InteractiveChat.useItem) {
        	component = ItemDisplay.process(component, sender, reciever, unix);
        }
        
        if (InteractiveChat.useInventory) {
        	component = InventoryDisplay.process(component, sender, reciever, unix);
        }
        
        if (InteractiveChat.useEnder) {
        	component = EnderchestDisplay.process(component, sender, reciever, unix);
        }
        
        Collection<ICPlaceholder> serverPlaceholderList = InteractiveChat.remotePlaceholderList.get(server);
        if (server.equals(ICPlayer.LOCAL_SERVER_REPRESENTATION) || serverPlaceholderList == null) {
        	serverPlaceholderList = InteractiveChat.placeholderList.values();
        }
        component = CustomPlaceholderDisplay.process(component, sender, reciever, serverPlaceholderList, unix);
        
        if (InteractiveChat.clickableCommands) {
        	component = CommandsDisplay.process(component);
        }
        
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16) && InteractiveChat.fontTags) {
	        if (!sender.isPresent() || (sender.isPresent() && PlayerUtils.hasPermission(sender.get().getUniqueId(), "interactivechat.customfont.translate", true, 5))) {
	        	component = ComponentFont.parseFont(component);
	        }
        }
        
        Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
        	InteractiveChat.keyTime.remove(rawMessageKey);
        	InteractiveChat.keyPlayer.remove(rawMessageKey);
        }, 5);
        
        String newJson = InteractiveChatComponentSerializer.gson().serialize(component);
        if (InteractiveChat.sendOriginalIfTooLong && newJson.length() > InteractiveChat.packetStringMaxLength) {
        	String originalJson = InteractiveChatComponentSerializer.gson().serialize(originalComponent);
        	if (originalJson.length() > InteractiveChat.packetStringMaxLength) {
        		return "{\"text\":\"\"}";
        	} else {
        		return originalJson;
        	}
        }
        
		return newJson;
	}

}
