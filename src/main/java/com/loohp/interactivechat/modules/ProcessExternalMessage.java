package com.loohp.interactivechat.modules;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.ConfigManager;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.data.PlayerDataManager.PlayerData;
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ProcessSenderResult;
import com.loohp.interactivechat.objectholders.WebData;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.ComponentFont;
import com.loohp.interactivechat.utils.CustomStringUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.JsonUtils;
import com.loohp.interactivechat.utils.LanguageUtils;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.NBTUtils;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;
import com.loohp.interactivechat.utils.RarityUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class ProcessExternalMessage {
	
	private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
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
			Player bukkitplayer = Bukkit.getPlayer(senderUUID);
        	if (bukkitplayer != null) {
        		sender = new ICPlayer(bukkitplayer);
        	} else {
        		sender = InteractiveChat.remotePlayers.get(senderUUID);
        	}
		}
		
		message = message.replaceAll("<cmd=" + UUID_REGEX + ">", "").replaceAll("<chat=" + UUID_REGEX + ">", "");
		
		if (sender == null) {
			return message;
		}
        
		long now = System.currentTimeMillis();
		long uniCooldown = InteractiveChatAPI.getPlayerUniversalCooldown(sender.getUniqueId()) - now;
		
		if (!(uniCooldown < 0 || uniCooldown + 100 > InteractiveChat.universalCooldown)) {
			return message;
		}

		if (InteractiveChat.useItem && PlayerUtils.hasPermission(sender.getUniqueId(), "interactivechat.module.item", true, 250)) {
			long cooldown = InteractiveChatAPI.getPlayerPlaceholderCooldown(sender.getUniqueId(), InteractiveChat.itemPlaceholder) - now;
			if (cooldown < 0 || cooldown + 100 > ConfigManager.getConfig().getLong("ItemDisplay.Item.Cooldown") * 1000) {
				String placeholder = InteractiveChat.itemPlaceholder;
				int index = InteractiveChat.itemCaseSensitive ? message.indexOf(placeholder) : message.toLowerCase().indexOf(placeholder.toLowerCase());
				if (index >= 0 && !((index > 0 && message.charAt(index - 1) == '\\') && (index < 2 || message.charAt(index - 2) != '\\'))) {
					ItemStack item = sender.getEquipment().getItemInHand();
					if (item == null) {
						item = new ItemStack(Material.AIR);
					}
					XMaterial xMaterial = XMaterial.matchXMaterial(item);
					String itemStr;
					if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() && !item.getItemMeta().getDisplayName().equals("")) {
						itemStr = item.getItemMeta().getDisplayName();
					} else {
						String itemKey = LanguageUtils.getTranslationKey(item);
						itemStr = LanguageUtils.getTranslation(itemKey, InteractiveChat.language);
						if (xMaterial.equals(XMaterial.PLAYER_HEAD)) {
							String owner = NBTUtils.getString(item, "SkullOwner", "Name");
							if (owner != null) {
								itemStr = itemStr.replaceFirst("%s", owner);
							}
						}
					}
					
					int amount = item.getAmount();
					if (item == null || item.getType().equals(Material.AIR)) {
						amount = 1;
					}
					
					itemStr = RarityUtils.getRarityColor(item) + itemStr;
				
					String replaceText = PlaceholderParser.parse(sender, InteractiveChat.itemReplaceText.replace("{Amount}", String.valueOf(amount)).replace("{Item}", itemStr));
					if (InteractiveChat.itemCaseSensitive) {
						message = CustomStringUtils.replaceRespectColor(message, InteractiveChat.itemPlaceholder, replaceText);
					} else {
						message = CustomStringUtils.replaceRespectColorCaseInsensitive(message, InteractiveChat.itemPlaceholder, replaceText);
					}
				}
			}
		}
		
		if (InteractiveChat.useInventory && PlayerUtils.hasPermission(sender.getUniqueId(), "interactivechat.module.inventory", true, 250)) {
			long cooldown = InteractiveChatAPI.getPlayerPlaceholderCooldown(sender.getUniqueId(), InteractiveChat.invPlaceholder) - now;
			if (cooldown < 0 || cooldown + 100 > ConfigManager.getConfig().getLong("ItemDisplay.Inventory.Cooldown") * 1000) {
				String placeholder = InteractiveChat.invPlaceholder;
				int index = InteractiveChat.invCaseSensitive ? message.indexOf(placeholder) : message.toLowerCase().indexOf(placeholder.toLowerCase());
				if (index >= 0 && !((index > 0 && message.charAt(index - 1) == '\\') && (index < 2 || message.charAt(index - 2) != '\\'))) {
					String replaceText = PlaceholderParser.parse(sender, InteractiveChat.invReplaceText);
					if (InteractiveChat.invCaseSensitive) {
						message = CustomStringUtils.replaceRespectColor(message, InteractiveChat.invPlaceholder, replaceText);
					} else {
						message = CustomStringUtils.replaceRespectColorCaseInsensitive(message, InteractiveChat.invPlaceholder, replaceText);
					}
				}
			}
		}
		
		if (InteractiveChat.useEnder && PlayerUtils.hasPermission(sender.getUniqueId(), "interactivechat.module.enderchest", true, 250)) {
			long cooldown = InteractiveChatAPI.getPlayerPlaceholderCooldown(sender.getUniqueId(), InteractiveChat.enderPlaceholder) - now;
			if (cooldown < 0 || cooldown + 100 > ConfigManager.getConfig().getLong("ItemDisplay.EnderChest.Cooldown") * 1000) {
				String placeholder = InteractiveChat.enderPlaceholder;
				int index = InteractiveChat.enderCaseSensitive ? message.indexOf(placeholder) : message.toLowerCase().indexOf(placeholder.toLowerCase());
				if (index >= 0 && !((index > 0 && message.charAt(index - 1) == '\\') && (index < 2 || message.charAt(index - 2) != '\\'))) {
					String replaceText = PlaceholderParser.parse(sender, InteractiveChat.enderReplaceText);
					if (InteractiveChat.enderCaseSensitive) {
						message = CustomStringUtils.replaceRespectColor(message, InteractiveChat.enderPlaceholder, replaceText);
					} else {
						message = CustomStringUtils.replaceRespectColorCaseInsensitive(message, InteractiveChat.enderPlaceholder, replaceText);
					}
				}
			}
		}
		
		for (ICPlaceholder placeholder : InteractiveChatAPI.getICPlaceholderList()) {
			if (!placeholder.isBuildIn()) {
				CustomPlaceholder customP = placeholder.getCustomPlaceholder().get();
				if (!InteractiveChat.useCustomPlaceholderPermissions || (InteractiveChat.useCustomPlaceholderPermissions && PlayerUtils.hasPermission(sender.getUniqueId(), customP.getPermission(), true, 250))) {
					long cooldown = InteractiveChatAPI.getPlayerPlaceholderCooldown(sender.getUniqueId(), customP.getKeyword()) - now;
					int index = placeholder.isCaseSensitive() ? message.indexOf(placeholder.getKeyword()) : message.toLowerCase().indexOf(placeholder.getKeyword().toLowerCase());
					if (index >= 0 && !((index > 0 && message.charAt(index - 1) == '\\') && (index < 2 || message.charAt(index - 2) != '\\')) && (cooldown < 0 || cooldown + 100 > customP.getCooldown())) {
						String replaceText = customP.getKeyword();
						if (customP.getReplace().isEnabled()) {
							replaceText = ChatColor.WHITE + PlaceholderParser.parse(sender, customP.getReplace().getReplaceText());
							if (customP.isCaseSensitive()) {
								message = CustomStringUtils.replaceRespectColor(message, customP.getKeyword(), replaceText);
							} else {
								message = CustomStringUtils.replaceRespectColorCaseInsensitive(message, customP.getKeyword(), replaceText);
							}
						}
					}
				}
			}
		}
		
		if (InteractiveChat.t && WebData.getInstance() != null) {
			for (CustomPlaceholder customP : WebData.getInstance().getSpecialPlaceholders()) {
				long cooldown = InteractiveChatAPI.getPlayerPlaceholderCooldown(sender.getUniqueId(), customP.getKeyword()) - now;
				int index = customP.isCaseSensitive() ? message.indexOf(customP.getKeyword()) : message.toLowerCase().indexOf(customP.getKeyword().toLowerCase());
				if (index >= 0 && !((index > 0 && message.charAt(index - 1) == '\\') && (index < 2 || message.charAt(index - 2) != '\\')) && (cooldown < 0 || cooldown + 100 > customP.getCooldown())) {
					String replaceText = customP.getKeyword();
					if (customP.getReplace().isEnabled()) {
						replaceText = PlaceholderParser.parse(sender, customP.getReplace().getReplaceText());
						if (customP.isCaseSensitive()) {
							message = CustomStringUtils.replaceRespectColor(message, customP.getKeyword(), replaceText);
						} else {
							message = CustomStringUtils.replaceRespectColorCaseInsensitive(message, customP.getKeyword(), replaceText);
						}
					}
				}
			}
		}
		
		return message;
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
        
        String rawMessageKey = PlainComponentSerializer.plain().serialize(component);
        if (!InteractiveChat.keyTime.containsKey(rawMessageKey)) {
        	InteractiveChat.keyTime.put(rawMessageKey, System.currentTimeMillis());
        }
        
        long unix = InteractiveChat.keyTime.get(rawMessageKey);
        if (!InteractiveChat.cooldownbypass.containsKey(unix)) {
        	InteractiveChat.cooldownbypass.put(unix, new HashSet<String>());
        }
        
        ProcessSenderResult commandSender = ProcessCommands.process(component);
        Optional<ICPlayer> sender = Optional.empty();
        if (commandSender.getSender() != null) {
        	Player bukkitplayer = Bukkit.getPlayer(commandSender.getSender());
        	if (bukkitplayer != null) {
        		sender = Optional.of(new ICPlayer(bukkitplayer));
        	} else {
        		sender = Optional.ofNullable(InteractiveChat.remotePlayers.get(commandSender.getSender()));
        	}
        }
        ProcessSenderResult chatSender = null;
        if (!sender.isPresent()) {
        	if (InteractiveChat.useAccurateSenderFinder) {
        		chatSender = ProcessAccurateSender.process(component);
        		if (chatSender.getSender() != null) {
    	        	Player bukkitplayer = Bukkit.getPlayer(chatSender.getSender());
    	        	if (bukkitplayer != null) {
    	        		sender = Optional.of(new ICPlayer(bukkitplayer));
    	        	} else {
    	        		sender = Optional.ofNullable(InteractiveChat.remotePlayers.get(chatSender.getSender()));
    	        	}
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
        
        component = component.replaceText(TextReplacementConfig.builder().match(Registry.ID_PATTERN).replacement("").build());
        
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
        
        List<ICPlaceholder> serverPlaceholderList = InteractiveChat.remotePlaceholderList.get(server);
        if (server.equals(ICPlayer.LOCAL_SERVER_REPRESENTATION) || serverPlaceholderList == null) {
        	serverPlaceholderList = InteractiveChat.placeholderList;
        }
        component = CustomPlaceholderDisplay.process(component, sender, reciever, serverPlaceholderList, unix);
        
        if (InteractiveChat.clickableCommands) {
        	component = CommandsDisplay.process(component);
        }
        
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
	        if (!sender.isPresent() || (sender.isPresent() && PlayerUtils.hasPermission(sender.get().getUniqueId(), "interactivechat.customfont.translate", true, 5))) {
	        	component = ComponentFont.parseMiniMessageFont(component);
	        }
        }
        
        Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
        	InteractiveChat.keyTime.remove(rawMessageKey);
        	InteractiveChat.keyPlayer.remove(rawMessageKey);
        }, 5);
        
        String newJson = InteractiveChatComponentSerializer.gson().serialize(component);
        if (InteractiveChat.sendOriginalIfTooLong && newJson.length() > 32767) {
        	String originalJson = InteractiveChatComponentSerializer.gson().serialize(originalComponent);
        	if (originalJson.length() > 32767) {
        		return "{\"text\":\"\"}";
        	} else {
        		return originalJson;
        	}
        }
        
		return newJson;
	}

}
