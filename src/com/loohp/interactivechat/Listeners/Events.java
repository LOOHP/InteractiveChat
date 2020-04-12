package com.loohp.interactivechat.Listeners;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.earth2me.essentials.Essentials;
import com.loohp.interactivechat.ConfigManager;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.API.Events.PlayerMentionPlayerEvent;
import com.loohp.interactivechat.API.Events.PostPacketComponentProcessEvent;
import com.loohp.interactivechat.API.Events.PrePacketComponentProcessEvent;
import com.loohp.interactivechat.Utils.ChatColorUtils;
import com.loohp.interactivechat.Utils.CustomStringUtils;
import com.loohp.interactivechat.Utils.ChatColorFilter;
import com.loohp.interactivechat.Utils.JsonUtils;
import com.loohp.interactivechat.Utils.KeyUtils;
import com.loohp.interactivechat.Utils.MaterialUtils;
import com.loohp.interactivechat.Utils.MessageUtils;
import com.loohp.interactivechat.Utils.NMSUtli;
import com.loohp.interactivechat.Utils.RarityUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class Events implements Listener {
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		InteractiveChat.mentionCooldown.put(event.getPlayer(), (System.currentTimeMillis() - 3000));
	}
	
	@EventHandler
	public void onJoin(PlayerQuitEvent event) {
		InteractiveChat.mentionCooldown.remove(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onCommand(PlayerCommandPreprocessEvent event) {
		Player sender = event.getPlayer();
		String message = event.getMessage();
		long unix = System.currentTimeMillis();
		InteractiveChat.lastMessage.put(sender, message);
		InteractiveChat.time.put(sender, unix);
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void addKey(AsyncPlayerChatEvent event) {
		if (event.isCancelled()) {
			return;
		}
		event.setMessage(MessageUtils.preprocessMessage(event.getMessage()));
		
		long unix = System.currentTimeMillis();
		Player sender = event.getPlayer();
		String message = event.getMessage();
		InteractiveChat.lastMessage.put(sender, message);
		InteractiveChat.time.put(sender, unix);		
		
		String key = KeyUtils.getRandomKey();
		event.setMessage(event.getMessage() + key);
		InteractiveChat.messageKey.put(key, event.getPlayer());
		InteractiveChat.messageKeyUUID.put(key, event.getPlayer().getUniqueId());
		//BungeeMessageSender.forwardHashMap(event.getPlayer(), InteractiveChat.messageKeyUUID, 0);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.LOWEST)
    public void onCheckMaxAndMention(AsyncPlayerChatEvent event) {
		String message = event.getMessage();
		if (InteractiveChat.maxPlacholders >= 0) {
			int count = 0;
			for (String findStr : InteractiveChat.placeholderList) {
				int lastIndex = 0;
	
				while(lastIndex != -1){
	
				    lastIndex = message.indexOf(findStr,lastIndex);
	
				    if(lastIndex != -1){
				        count ++;
				        lastIndex += findStr.length();
				    }
				}
			}
			if (count > InteractiveChat.maxPlacholders) {
				event.setCancelled(true);
				String cancelmessage = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(event.getPlayer(), InteractiveChat.limitReachMessage));
				event.getPlayer().sendMessage(cancelmessage);
				return;
			}
		}
				
		Player sender = event.getPlayer();

		if (InteractiveChat.AllowMention == true && sender.hasPermission("interactivechat.mention.player")) {
			for (String each : message.split(" ")) {
				if (Bukkit.getOfflinePlayer(each) != null) {
					InteractiveChat.mentionPair.put(Bukkit.getOfflinePlayer(each).getUniqueId(), sender.getUniqueId());	
					//BungeeMessageSender.forwardHashMap(sender, InteractiveChat.mentionPair, 4);
				}
			}
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (!player.equals(sender)) {
					List<String> playernames = new ArrayList<String>();
        			playernames.add(player.getName());
        			if (!player.getName().equals(player.getDisplayName())) {
        				playernames.add(player.getDisplayName());
        			}
        			if (InteractiveChat.ess3 == true) {
        				Essentials essen = (Essentials)Bukkit.getPluginManager().getPlugin("Essentials");
        				if (essen.getUser(player.getUniqueId()).getNickname() != null) {
        					if (!essen.getUser(player.getUniqueId()).getNickname().equals("")) {
        						String prefix = essen.getConfig().getString("nickname-prefix");
        						String essentialsNick = essen.getUser(player.getUniqueId()).getNickname();
        						playernames.add(prefix + essentialsNick);
        					}
        				}
        			}
        			boolean found = false;
           			for (String name : playernames) {
           				if (message.toLowerCase().contains(name.toLowerCase())) {
           					found = true;
           					break;
           				}
           			}
					if (found == true) {											
						InteractiveChat.mentionPair.put(player.getUniqueId(), sender.getUniqueId());	
						//BungeeMessageSender.forwardHashMap(sender, InteractiveChat.mentionPair, 4);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getClickedInventory() == null) {
			return;
		}
		if (event.getClickedInventory().getType().equals(InventoryType.CREATIVE)) {
			return;
		}
		if (event.getView().getTopInventory() == null) {
			return;
		}
		if (InteractiveChat.inventoryDisplay.containsValue(event.getView().getTopInventory())) {
			event.setCancelled(true);
			return;
		}
		if (InteractiveChat.enderDisplay.containsValue(event.getView().getTopInventory())) {
			event.setCancelled(true);
			return;
		}
		if (InteractiveChat.itemDisplay.containsValue(event.getView().getTopInventory())) {
			event.setCancelled(true);
			return;
		}
 	}
	
	public static void chatMessageListener() {
		
		InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(InteractiveChat.plugin, ListenerPriority.MONITOR, PacketType.Play.Server.CHAT) {
		    @Override
		    public void onPacketSending(PacketEvent event) {
		        if (event.getPacketType() == PacketType.Play.Server.CHAT) {
		        	PacketContainer send = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.CHAT);
		        	PacketContainer packet = event.getPacket();
		        	
		            List<WrappedChatComponent> components = packet.getChatComponents().getValues();		            		        
		            
		            for (WrappedChatComponent component : components) {
		            	if (component != null) {
		            		if (component.getJson().contains("񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸")) {
		            			event.setReadOnly(false);
		            			component.setJson(component.getJson().replace("񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸", "").replace(InteractiveChat.space0, "").replace(InteractiveChat.space1, ""));
		            			packet.getChatComponents().write(0, component);
		            			event.setReadOnly(true);
		            			return;
		            		}
		            	} else {
		            		BaseComponent[] basecomp = (BaseComponent[]) event.getPacket().getModifier().read(1);
		            		List<BaseComponent> base = new ArrayList<BaseComponent>();
			            	base = CustomStringUtils.loadExtras(Arrays.asList(basecomp));
			            	boolean is = false;
			            	event.setReadOnly(false);
			            	for (BaseComponent each : base) {
		            			if (each.toLegacyText().contains("񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸")) {
		            				TextComponent text = (TextComponent) each;
			            			text.setText(text.getText().replace(InteractiveChat.space0, "").replace(InteractiveChat.space1, ""));
		            				text.setText(text.getText().replace("񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸", ""));
		            				is = true;
		            			}
		            		}
		            		if (is == true) {
		            			TextComponent newText = new TextComponent("");
			            		for (BaseComponent each : base) {
			            			newText.addExtra(each);
			            		}			            		
				            	String stringCom = ComponentSerializer.toString(newText);
				            	packet.getModifier().write(1, ComponentSerializer.parse(stringCom));
		            			event.setReadOnly(true);
		            			return;
		            		}
		            		event.setReadOnly(true);
		            	}
		            }
		            
		            event.setReadOnly(false);
		        	event.setCancelled(true);
		        	send = packet.shallowClone();
		        	event.setReadOnly(true);
		        	
		        	components = send.getChatComponents().getValues();			        		    
		        	
		            for (WrappedChatComponent component : components) {
		            	
		            	boolean usealt = false;
		            	WrappedChatComponent alt = null;
		            	int field = 0;

		            	if (component == null) {
		            		BaseComponent[] basecomp = (BaseComponent[]) event.getPacket().getModifier().read(1);
		            		field = 1;            		
		            		List<BaseComponent> base = new ArrayList<BaseComponent>();
			            	base = CustomStringUtils.loadExtras(Arrays.asList(basecomp));
			            	TextComponent newText = new TextComponent("");
	            			newText.copyFormatting(basecomp[0]);
		            		for (BaseComponent each : base) {
		            			each.copyFormatting(newText, false);
		            			newText.addExtra(each);	            				            			
		            		}
			            	String stringCom = ComponentSerializer.toString(newText);			         
			            	alt = WrappedChatComponent.fromJson(stringCom);
			            	usealt = true;
		            	}
		            	
		            	if (usealt == true) {
		            		component = alt.deepClone();
		            	}		            		         
		            	
		            	if (component != null) {
		            		
		            		if (component.getJson().toString().contains("translate")) {
			            		JSONParser parser = new JSONParser();
			            		JSONObject json = null;
								try {
									json = (JSONObject) parser.parse(component.getJson());
								} catch (ParseException e) {
									e.printStackTrace();
								}
			            		if (json.containsKey("translate")) {
			            			event.setReadOnly(false);
			    		        	event.setCancelled(false);
			    		        	event.setReadOnly(true);
			            			return;
			            		}
			            	}
		            		
			            	BaseComponent[] bcs = ComponentSerializer.parse(component.getJson());

			            	List<BaseComponent> base = new ArrayList<BaseComponent>();
			            	base = CustomStringUtils.loadExtras(Arrays.asList(bcs));
			            	String rawMessage = "";
			            	for (BaseComponent each : bcs) {
			            		rawMessage = rawMessage + each.toPlainText();
			            	}			            	
			            	
			            	long unix = System.currentTimeMillis();
			            	
			            	Player sender = null;
			            	String keyRemove = "";
			            	String messageKey = "";
			            	if (rawMessage.contains(InteractiveChat.space0)) {
			            		String key = rawMessage.substring(rawMessage.indexOf(InteractiveChat.space0), rawMessage.lastIndexOf(InteractiveChat.space0) + 1);
			            		if (InteractiveChat.messageKey.containsKey(key)) {
			            			sender = InteractiveChat.messageKey.get(key);
			            			keyRemove = key;
			            			messageKey = key;
			            			if (!InteractiveChat.keyTime.containsKey(key)) {
			            				long time = System.currentTimeMillis();
			            				InteractiveChat.keyTime.put(key, time);
			            				HashMap<String, Long> singleMap = new HashMap<String, Long>();
			            				singleMap.put(key, time);
			            				//BungeeMessageSender.forwardHashMap(event.getPlayer(), singleMap, 5);
			            			}
			            		}
			            	}
			            	
			            	boolean fromBungee = false;
			            	if (sender == null) {
			            		if (rawMessage.contains(InteractiveChat.space0)) {
				            		String key = rawMessage.substring(rawMessage.indexOf(InteractiveChat.space0), rawMessage.lastIndexOf(InteractiveChat.space0) + 1);
				            		if (InteractiveChat.messageKeyUUID.containsKey(key)) {
				            			fromBungee = true;
				            			keyRemove = key;
				            			messageKey = key;				            		
				            		}
			            		}
			            	}
			            	
			            	if (fromBungee == false) {
				            	//if (sender == null) {       
			            		//	for (Entry<Player, Long> entry : CustomMapUtils.entriesSortedByValues(InteractiveChat.time)) {
								//		if ((unix - entry.getValue()) < 10000) {
								//			if (InteractiveChat.lastMessage.containsKey(entry.getKey())) {
								//				if (rawMessage.contains(InteractiveChat.lastMessage.get(entry.getKey()))) {
								//					sender = entry.getKey();
								//					break;
								//				}									
								//			}
								//		}
								//	}
				            	//}
		            			//
		            			//if (sender == null) {
								//	for (Entry<Player, Long> entry : CustomMapUtils.entriesSortedByValues(InteractiveChat.time)) {
								//		if ((unix - entry.getValue()) < 500) {
								//			if (InteractiveChat.lastMessage.containsKey(entry.getKey())) {
								//				sender = entry.getKey();
								//				break;
								//			}
								//		}
								//	}
		            			//}
		            			
		            			UUID uuid = null;
		            			if (sender != null) {
		            				uuid = sender.getUniqueId();
		            			}
		            			PrePacketComponentProcessEvent preEvent = new PrePacketComponentProcessEvent(event.getPlayer(), component, field, uuid);
		            			if (Bukkit.getPlayer(preEvent.getSender()) != null) {
		            				sender = Bukkit.getPlayer(preEvent.getSender());
		            			}
		            			component = preEvent.getChatComponent();
		            			field = preEvent.getField();
		            			
								if (sender != null) {							
									String remove = sender.getUniqueId().toString();
									if (!keyRemove.equals("")) {
										InteractiveChat.timedRemove.put(keyRemove, (System.currentTimeMillis() + 300));
										InteractiveChat.timedRemove.put(remove, (System.currentTimeMillis() + 300));
									} else {
										InteractiveChat.timedRemove.put(remove, (System.currentTimeMillis() + 300));
									}
								}
								if (sender != null) {
									Player playerDC = sender;
									WrappedChatComponent cmp = component;
									new BukkitRunnable() {
										public void run () {
											long time = System.currentTimeMillis();
											HashMap<String, Long> placeholders = new HashMap<String, Long>();
											placeholders.put(InteractiveChat.itemPlaceholder.toLowerCase(), ConfigManager.getConfig().getLong("ItemDisplay.Item.Cooldown"));
											placeholders.put(InteractiveChat.invPlaceholder.toLowerCase(), ConfigManager.getConfig().getLong("ItemDisplay.Inventory.Cooldown"));
											placeholders.put(InteractiveChat.enderPlaceholder.toLowerCase(), ConfigManager.getConfig().getLong("ItemDisplay.EnderChest.Cooldown"));
											for (int customNo = 1; ConfigManager.getConfig().contains("CustomPlaceholders." + String.valueOf(customNo)) == true; customNo = customNo + 1) {
												placeholders.put(ConfigManager.getConfig().getString("CustomPlaceholders." + String.valueOf(customNo) + ".Text").toLowerCase(), ConfigManager.getConfig().getLong("CustomPlaceholders." + String.valueOf(customNo) + ".Cooldown"));
											}
											String message = cmp.getJson().toLowerCase();
											for (Entry<String, Long> entry : placeholders.entrySet()) {								
												if (message.contains(entry.getKey())) {
													if (InteractiveChat.universalCooldowns.containsKey(playerDC)) {
														long timeout = InteractiveChat.universalCooldowns.get(playerDC);
														if (timeout < time) {
															InteractiveChat.universalCooldowns.put(playerDC, (time + (ConfigManager.getConfig().getLong("Settings.UniversalCooldown") * 1000)));
														}
													} else {
														InteractiveChat.universalCooldowns.put(playerDC, (time + (ConfigManager.getConfig().getLong("Settings.UniversalCooldown") * 1000)));
													}
													if (!InteractiveChat.placeholderCooldowns.containsKey(playerDC)) {
														HashMap<String, Long> map = new HashMap<String, Long>();
														InteractiveChat.placeholderCooldowns.put(playerDC, map);
													}
													HashMap<String, Long> map = InteractiveChat.placeholderCooldowns.get(playerDC);
													if (map.containsKey(entry.getKey())) {
														long timeout = map.get(entry.getKey());
														if (timeout < time) {
															map.put(entry.getKey(), (time + (entry.getValue() * 1000)));
														}
													} else {
														map.put(entry.getKey(), (time + (entry.getValue() * 1000)));
													}
												}
											}
										}
									}.runTaskLater(InteractiveChat.plugin, 1);
								}
								TextComponent removeKeyText = new TextComponent("");
								for (BaseComponent each : base) {
									String json = ComponentSerializer.toString(each);
									json = json.replace(InteractiveChat.space0, "").replace(InteractiveChat.space1, "");
									BaseComponent[] newBase = ComponentSerializer.parse(json);
									for (BaseComponent eachNew : newBase) {
										removeKeyText.addExtra(eachNew);
									}
			            		}	  
			            		bcs = ComponentSerializer.parse(ComponentSerializer.toString(removeKeyText));
				            	base = new ArrayList<BaseComponent>();
				            	base = CustomStringUtils.loadExtras(Arrays.asList(bcs));
				            		            	
				            	if (InteractiveChat.usePlayerName == true) {
				            		List<Player> playerlist = new ArrayList<Player>(Bukkit.getOnlinePlayers());
				            		for (Player player : playerlist) {
				            			List<String> playernames = new ArrayList<String>();
				            			playernames.add(player.getName());
				            			if (!player.getName().equals(player.getDisplayName())) {
				            				playernames.add(player.getDisplayName());
				            			}
				            			if (InteractiveChat.ess3 == true) {
				            				Essentials essen = (Essentials)Bukkit.getPluginManager().getPlugin("Essentials");
				            				if (essen.getUser(player.getUniqueId()).getNickname() != null) {
				            					if (!essen.getUser(player.getUniqueId()).getNickname().equals("")) {
				            						String prefix = essen.getConfig().getString("nickname-prefix");
				            						String essentialsNick = essen.getUser(player.getUniqueId()).getNickname();
				            						playernames.add(prefix + essentialsNick);
				            					}
				            				}
				            			}
				            			
				            			for (String name : playernames) {
					            			String placeholder = name;
					            		
						            		TextComponent newText = new TextComponent("");
						            		List<String> join = new ArrayList<String>();
						            		for (BaseComponent each : base) {
						            			
						            			String eachLegacyTextCase = each.toLegacyText();
				            					String placeholderCase = placeholder;
				            					if (InteractiveChat.usePlayerNameCaseSensitive == false) {
				            						eachLegacyTextCase = eachLegacyTextCase.toLowerCase();
				            						placeholderCase = placeholderCase.toLowerCase();
				            					}
						            			
						            			if (!eachLegacyTextCase.contains(placeholderCase)) {
						            				newText.addExtra(each);
						            			} else {
						            				String[] alltext = each.toPlainText().split("");
						            				
						            				for (int i = 0; i <= (alltext.length - 1); i = i + 1) {
						            					String detectString = "";
						            					for (int u = 0; u <= (placeholder.length() - 1) && (i + u) < alltext.length; u = u + 1) {
						            						detectString = detectString + alltext[u + i];
						            					}   	
						            					String detectStringCase = detectString;
						            					if (InteractiveChat.usePlayerNameCaseSensitive == false) {
						            						detectStringCase = detectStringCase.toLowerCase();
						            						placeholderCase = placeholderCase.toLowerCase();
						            					}
						            					
						            					if (detectStringCase.equals(placeholderCase)) {
						            						String lastColor = "";
						            						if (join.isEmpty() == false) {		            							
						            							TextComponent word = new TextComponent(String.join("", join));
						            							lastColor = ChatColorUtils.getLastColors(String.join("", join));
									            				word.copyFormatting(each);
									            				newText.addExtra(word);
									            				join.clear();
						            						}
					            					    
						            						TextComponent message = new TextComponent(detectString);
						            						message.copyFormatting(each);
						            						if (message.getColorRaw() != null) {
							            						if (message.getColorRaw().equals(ChatColor.WHITE) || message.getColorRaw().equals(ChatColor.RESET)) {
							            							if (lastColor.length() > 1) {
							            								ChatColor color = ChatColor.getByChar(lastColor.charAt(lastColor.lastIndexOf('�') + 1));
							            								message.setColor(color);
							            							}
							            						}
						            						} else {
						            							if (lastColor.length() > 1) {
						            								ChatColor color = ChatColor.getByChar(lastColor.charAt(lastColor.lastIndexOf('�') + 1));
						            								message.setColor(color);
						            							}
						            						}
						            						if (InteractiveChat.usePlayerNameHoverEnable == true) {
						            							String text = PlaceholderAPI.setPlaceholders(player, InteractiveChat.usePlayerNameHoverText);
						            							message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text).create()));
						            						}
						            						if (InteractiveChat.usePlayerNameClickEnable == true) {
						            							String text = PlaceholderAPI.setPlaceholders(player, InteractiveChat.usePlayerNameClickValue);
						            							message.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(InteractiveChat.usePlayerNameClickAction), text) );
						            						}
						            						
						            						newText.addExtra(message);
						            						
								            				i = i + placeholder.length() - 1;
						            					} else {
						            						join.add(alltext[i]);
						            					}
						            				}
						            				if (join.isEmpty() == false) {			
				            							TextComponent word = new TextComponent(String.join("", join));
							            				word.copyFormatting(each);
							            				newText.addExtra(word);
							            				join.clear();
				            						}
						            			}
						            		}	  
						            		bcs = ComponentSerializer.parse(ComponentSerializer.toString(newText));
							            	base = new ArrayList<BaseComponent>();
							            	base = CustomStringUtils.loadExtras(Arrays.asList(bcs));
				            			}
				            		}
				            	}
				            	
				            	if (sender != null) {
					            	if (InteractiveChat.mentionPair.containsKey(event.getPlayer().getUniqueId())) {
					            		if (InteractiveChat.mentionPair.get(event.getPlayer().getUniqueId()).equals(sender.getUniqueId())) {
					            			Player player = event.getPlayer();
					            			
					            			String title = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(sender, ConfigManager.getConfig().getString("Chat.MentionedTitle")));
											String subtitle = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(sender, ConfigManager.getConfig().getString("Chat.KnownPlayerMentionSubtitle")));
											Sound sound = null;
											if (Sound.valueOf(ConfigManager.getConfig().getString("Chat.MentionedSound")) != null) {
												player.playSound(player.getLocation(), Sound.valueOf(ConfigManager.getConfig().getString("Chat.MentionedSound")), 3.0F, 1.0F);
											}
											
											boolean inCooldown = true;
											if (InteractiveChat.mentionCooldown.get(player) < unix) {
												inCooldown = false;
											}
											PlayerMentionPlayerEvent mentionEvent = new PlayerMentionPlayerEvent(player, sender.getUniqueId(), title, subtitle, sound, inCooldown);
											Player reciever = mentionEvent.getReciver();
											InteractiveChat.mentionPair.put(reciever.getUniqueId(), sender.getUniqueId());
											if (mentionEvent.isCancelled() == false) {
												title = mentionEvent.getTitle();
												subtitle = mentionEvent.getSubtitle();
												sound = mentionEvent.getMentionSound();
																
												reciever.sendTitle(title, subtitle, 10, 30, 20);
												if (sound != null) {
													reciever.playSound(player.getLocation(), sound, 3.0F, 1.0F);
												}						
												InteractiveChat.mentionCooldown.put(reciever, unix + 3000);
												
								            	List<String> playernames = new ArrayList<String>();
						            			playernames.add(player.getName());
						            			if (!player.getName().equals(player.getDisplayName())) {
						            				playernames.add(player.getDisplayName());
						            			}
						            			if (InteractiveChat.ess3 == true) {
						            				Essentials essen = (Essentials)Bukkit.getPluginManager().getPlugin("Essentials");
						            				if (essen.getUser(player.getUniqueId()).getNickname() != null) {
						            					if (!essen.getUser(player.getUniqueId()).getNickname().equals("")) {
						            						String prefix = essen.getConfig().getString("nickname-prefix");
						            						String essentialsNick = essen.getUser(player.getUniqueId()).getNickname();
						            						playernames.add(prefix + essentialsNick);
						            					}
						            				}
						            			}
						            			
						            			for (String name : playernames) {
							            			String placeholder = name;
							            		
								            		TextComponent newText = new TextComponent("");
								            		List<String> join = new ArrayList<String>();
								            		for (BaseComponent each : base) {
								            			
								            			String eachLegacyTextCase = each.toLegacyText();
						            					String placeholderCase = placeholder;
						            					if (InteractiveChat.usePlayerNameCaseSensitive == false) {
						            						eachLegacyTextCase = eachLegacyTextCase.toLowerCase();
						            						placeholderCase = placeholderCase.toLowerCase();
						            					}
								            			
								            			if (!eachLegacyTextCase.contains(placeholderCase)) {
								            				newText.addExtra(each);
								            			} else {
								            				String[] alltext = each.toPlainText().split("");
								            				
								            				for (int i = 0; i <= (alltext.length - 1); i = i + 1) {
								            					String detectString = "";
								            					for (int u = 0; u <= (placeholder.length() - 1) && (i + u) < alltext.length; u = u + 1) {
								            						detectString = detectString + alltext[u + i];
								            					}   	
								            					String detectStringCase = detectString;
								            					if (InteractiveChat.usePlayerNameCaseSensitive == false) {
								            						detectStringCase = detectStringCase.toLowerCase();
								            						placeholderCase = placeholderCase.toLowerCase();
								            					}
								            					
								            					if (detectStringCase.equals(placeholderCase)) {
								            						
								            						if (join.isEmpty() == false) {		            							
								            							TextComponent word = new TextComponent(String.join("", join));
											            				word.copyFormatting(each);
											            				newText.addExtra(word);
											            				join.clear();
								            						}
							            					    
								            						TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', InteractiveChat.mentionHightlight.replace("{MentionedPlayer}", detectString)));
								            						message = CustomStringUtils.copyFormattingEventsNoReplace(message, each);
								            						String hover = ChatColor.translateAlternateColorCodes('&', InteractiveChat.mentionHover.replace("{Sender}", sender.getDisplayName()).replace("{Reciever}", player.getDisplayName()));
								            						message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
								            										            						
								            						newText.addExtra(message);
								            						
										            				i = i + placeholder.length() - 1;
								            					} else {
								            						join.add(alltext[i]);
								            					}
								            				}
								            				if (join.isEmpty() == false) {			
						            							TextComponent word = new TextComponent(String.join("", join));
									            				word.copyFormatting(each);
									            				newText.addExtra(word);
									            				join.clear();
						            						}
								            			}
								            		}	  
								            		bcs = ComponentSerializer.parse(ComponentSerializer.toString(newText));
									            	base = new ArrayList<BaseComponent>();
									            	base = CustomStringUtils.loadExtras(Arrays.asList(bcs));
									            	
									            	InteractiveChat.mentionPair.remove(event.getPlayer().getUniqueId());
						            			}
											}
					            		}
					            	}
				            	}
				            	
				            	boolean continueThrough = true;
								if (sender != null) {
				            		if (InteractiveChat.universalCooldowns.containsKey(sender)) {
				            			long timeout = InteractiveChat.universalCooldowns.get(sender);		            			
			            				if (unix < timeout) {
			            					if (!sender.hasPermission("interactivechat.cooldown.bypass")) {
			            						continueThrough = false;
			            					}
			            				}
			            			}
			            		}
				            	
								if (continueThrough == true) {			            	
					            	for (int customNo = 1; ConfigManager.getConfig().contains("CustomPlaceholders." + String.valueOf(customNo)) == true; customNo = customNo + 1) {
					            		
					            		Player player = null;
					            		if (ConfigManager.getConfig().getString("CustomPlaceholders." + String.valueOf(customNo) + ".ParsePlayer").equalsIgnoreCase("sender")) {
					            			player = sender;
					            		} else {
					            			player = event.getPlayer();
					            		}
					            		
					            		if (player == null) {
					            			player = event.getPlayer();
					            		}
					            		
					            		String placeholder = ConfigManager.getConfig().getString("CustomPlaceholders." + String.valueOf(customNo) + ".Text");
	
					            		if (ConfigManager.getConfig().getBoolean("CustomPlaceholders." + String.valueOf(customNo) + ".ParseKeyword") == true) {
					            			placeholder = PlaceholderAPI.setPlaceholders(player, placeholder).trim();
					            		}
	
					            		if (player.hasPermission("interactivechat.module.custom")) {
					            			
					            			if (sender != null) {
						            			if (InteractiveChat.placeholderCooldowns.containsKey(sender)) {
						            				HashMap<String, Long> map = InteractiveChat.placeholderCooldowns.get(sender);
						            				if (map.containsKey(placeholder.toLowerCase())) {
						            					long timeout = map.get(placeholder.toLowerCase());
						            					if (unix < timeout) {
						            						if (!sender.hasPermission("interactivechat.cooldown.bypass")) {
						            							continue;
						            						}
						            					}
						            				}
						            			}
					            			}
					            		
						            		TextComponent newText = new TextComponent("");
						            		List<String> join = new ArrayList<String>();
						            		for (BaseComponent each : base) {
						            			
						            			String eachLegacyTextCase = each.toLegacyText();
				            					String placeholderCase = placeholder;
				            					if (ConfigManager.getConfig().getBoolean("CustomPlaceholders." + String.valueOf(customNo) + ".CaseSensitive") == false) {
				            						eachLegacyTextCase = eachLegacyTextCase.toLowerCase();
				            						placeholderCase = placeholderCase.toLowerCase();
				            					}
						            			
						            			if (!eachLegacyTextCase.contains(placeholderCase)) {
						            				newText.addExtra(each);
						            			} else {
						            				String[] alltext = each.toPlainText().split("");
						            				
						            				for (int i = 0; i <= (alltext.length - 1); i = i + 1) {
						            					String detectString = "";
						            					for (int u = 0; u <= (placeholder.length() - 1) && (i + u) < alltext.length; u = u + 1) {
						            						detectString = detectString + alltext[u + i];
						            					}
						            					
						            					String detectStringCase = detectString;
						            					if (ConfigManager.getConfig().getBoolean("CustomPlaceholders." + String.valueOf(customNo) + ".CaseSensitive") == false) {
						            						detectStringCase = detectStringCase.toLowerCase();
						            						placeholderCase = placeholderCase.toLowerCase();
						            					}
						            					
						            					if (detectStringCase.equals(placeholderCase)) {
						            						
						            						if (join.isEmpty() == false) {		            							
						            							TextComponent word = new TextComponent(String.join("", join));
									            				word.copyFormatting(each);
									            				newText.addExtra(word);
									            				join.clear();
						            						}
						            						
						            						String textComp = placeholder;
						            						if (ConfigManager.getConfig().getBoolean("CustomPlaceholders." + String.valueOf(customNo) + ".Replace.Enable") == true) {
						            							textComp = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, ConfigManager.getConfig().getString("CustomPlaceholders." + String.valueOf(customNo) + ".Replace.ReplaceText")));
						            						}
						            						
						            						BaseComponent[] bcJson = ComponentSerializer.parse(JsonUtils.toJSON(textComp));
											            	List<BaseComponent> baseJson = new ArrayList<BaseComponent>();
											            	baseJson = CustomStringUtils.loadExtras(Arrays.asList(bcJson));
											            	
											            	for (BaseComponent baseComponent : baseJson) {
											            		TextComponent message = (TextComponent) baseComponent;
											            		if (ConfigManager.getConfig().getBoolean("CustomPlaceholders." + String.valueOf(customNo) + ".Hover.Enable") == true) {
							            							List<String> hoverList = ConfigManager.getConfig().getStringList("CustomPlaceholders." + String.valueOf(customNo) + ".Hover.Text");
							            							String text = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, String.join("\n", hoverList)));
							            							message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text).create()));
							            						}
							            						
							            						if (ConfigManager.getConfig().getBoolean("CustomPlaceholders." + String.valueOf(customNo) + ".Click.Enable") == true) {
							            							String text = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, ConfigManager.getConfig().getString("CustomPlaceholders." + String.valueOf(customNo) + ".Click.Value")));
							            							message.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(ConfigManager.getConfig().getString("CustomPlaceholders." + String.valueOf(customNo) + ".Click.Action")), text));
							            						}
							            						
							            						newText.addExtra(message);
											            	}					    
						            						
								            				i = i + placeholder.length() - 1;
						            					} else {
						            						join.add(alltext[i]);
						            					}
						            				}
						            				if (join.isEmpty() == false) {			
				            							TextComponent word = new TextComponent(String.join("", join));
							            				word.copyFormatting(each);
							            				newText.addExtra(word);
							            				join.clear();
				            						}
						            			}
						            		}	            
						            		bcs = ComponentSerializer.parse(ComponentSerializer.toString(newText));
						            		
							            	base = new ArrayList<BaseComponent>();
							            	base = CustomStringUtils.loadExtras(Arrays.asList(bcs));
					            		}
					            	}
					                
					            	if (InteractiveChat.useItem == true) {
					            		
					            		String placeholder = InteractiveChat.itemPlaceholder;
					           			            		
					            		boolean parse = true;
					            		if (sender != null) {
						            		if (InteractiveChat.placeholderCooldowns.containsKey(sender)) {
					            				HashMap<String, Long> map = InteractiveChat.placeholderCooldowns.get(sender);
					            				if (map.containsKey(placeholder.toLowerCase())) {
					            					long timeout = map.get(placeholder.toLowerCase());
					            					if (unix < timeout) {
					            						if (!sender.hasPermission("interactivechat.cooldown.bypass")) {
					            							parse = false;
					            						}
					            					}
					            				}
					            			}
					            		}
					            		
					            		if (parse == true) {
					            			
					            			TextComponent newText = new TextComponent("");
						            		List<String> join = new ArrayList<String>();				            		
						            		
						            		for (BaseComponent each : base) {
						            			
						            			String eachLegacyTextCase = each.toLegacyText().toLowerCase();
				            					String placeholderCase = placeholder;
				            					if (InteractiveChat.itemCaseSensitive == false) {
				            						eachLegacyTextCase = eachLegacyTextCase.toLowerCase();
				            						placeholderCase = placeholderCase.toLowerCase();
				            					}
				            					
						            			if (!eachLegacyTextCase.contains(placeholderCase)) {
						            				newText.addExtra(each);
						            			} else {
						            				String[] alltext = each.toPlainText().split("");
						            				
						            				for (int i = 0; i <= (alltext.length - 1); i = i + 1) {
						            					String detectString = "";
						            					for (int u = 0; u <= (placeholder.length() - 1) && (i + u) < alltext.length; u = u + 1) {
						            						detectString = detectString + alltext[u + i];
						            					}
						            					String detectStringCase = detectString;
						            					
						            					if (InteractiveChat.itemCaseSensitive == false) {
						            						detectStringCase = detectStringCase.toLowerCase();
						            						placeholderCase = placeholderCase.toLowerCase();
						            					}
						            					
						            					if (detectStringCase.equals(placeholderCase)) {
						            						
						            						if (join.isEmpty() == false) {		            							
						            							TextComponent word = new TextComponent(String.join("", join));
									            				word.copyFormatting(each);
									            				newText.addExtra(word);
									            				join.clear();
						            						}
						            						
						            						Player player = sender;
						            						if (player != null) {
						            							if (player.hasPermission("interactivechat.module.item")) {
						            											            								
							            							ItemStack item = new ItemStack(Material.STRUCTURE_VOID, 1);
							            							
							            							if (player.getEquipment().getItemInMainHand() == null) {
							            								ItemMeta meta = item.getItemMeta();
							            								ItemStack air = new ItemStack(Material.AIR);
							            								meta.setDisplayName(ChatColor.BLACK + "" + ChatColor.WHITE + MaterialUtils.getMinecraftName(air));
							            								item.setItemMeta(meta);
							            							} else if (player.getEquipment().getItemInMainHand().getType().equals(Material.AIR)) {
							            								ItemMeta meta = item.getItemMeta();
							            								ItemStack air = new ItemStack(Material.AIR);
							            								meta.setDisplayName(ChatColor.BLACK + "" + ChatColor.WHITE + MaterialUtils.getMinecraftName(air));
							            								item.setItemMeta(meta);
							            							} else {
							            								
							            								item = player.getEquipment().getItemInMainHand();
							            							}
			
								            					    String itemJson = NMSUtli.getNMSItemStackJson(item);
			
								            					    String message = "";
								            					    String itemString = "";
								            					    String amountString = "";
			
								            					    if (item.getItemMeta().hasDisplayName()) {
								            					    	if (!item.getItemMeta().getDisplayName().equals("")) {
								            					    		itemString = item.getItemMeta().getDisplayName();
								            					    	} else {
								            					    		itemString = RarityUtils.getRarityColor(item) + MaterialUtils.getMinecraftName(item);
								            					    	}
								            					    } else {
								            					    	itemString = RarityUtils.getRarityColor(item) + MaterialUtils.getMinecraftName(item);
								            					    }
								            					    itemString = ChatColorFilter.filterIllegalColorCodes(itemString);
								            					    
								            					    amountString = String.valueOf(item.getAmount());
								            					    message = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, InteractiveChat.itemReplaceText.replace("{Item}", itemString).replace("{Amount}", amountString)));
								            					    
								            					    BaseComponent[] hoverEventComponents = new BaseComponent[] {new TextComponent(itemJson)};
						
								            					    HoverEvent hoverItem = new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents);
						
								            						String title = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, InteractiveChat.itemTitle));
								            						
								            						long time = InteractiveChat.keyTime.get(messageKey);
								            						
								            						if (!InteractiveChat.itemDisplay.containsKey(time)) {
							            								Inventory inv = Bukkit.createInventory(null, 27, title);
							            								ItemStack empty = new ItemStack(InteractiveChat.itemFrame1, 1);
							            								if (item.getType().equals(InteractiveChat.itemFrame1)) {
							            									empty = new ItemStack(InteractiveChat.itemFrame2, 1);
							            								}
							            								ItemMeta emptyMeta = empty.getItemMeta();
							            								emptyMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "");
							            								empty.setItemMeta(emptyMeta);
								            							for (int j = 0; j < inv.getSize(); j = j + 1) {
								            								inv.setItem(j, empty);
								            							}
								            							inv.setItem(13, item);				            							
								            							InteractiveChat.itemDisplay.put(time, inv);	
								            							HashMap<Long, Inventory> singleMap = new HashMap<Long, Inventory>();
								            							singleMap.put(time, inv);
								            							//BungeeMessageSender.forwardHashMap(sender, 1, singleMap, title);
							            							}								            														        
								            					    
								            					    BaseComponent[] bcJson = ComponentSerializer.parse(JsonUtils.toJSON(message));
													            	List<BaseComponent> baseJson = new ArrayList<BaseComponent>();
													            	baseJson = CustomStringUtils.loadExtras(Arrays.asList(bcJson));
													            	
													            	for (BaseComponent baseComponent : baseJson) {
													            		TextComponent textcomponent = (TextComponent) baseComponent;
									            					    textcomponent.setHoverEvent(hoverItem);
									            					    if (ConfigManager.getConfig().getBoolean("ItemDisplay.Item.GUIEnabled") == true) {
									            							ClickEvent clickItem = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewitem " + time);
									            							textcomponent.setClickEvent(clickItem);
									            					    }
									            					    newText.addExtra(textcomponent);
													            	}
								            					    
						            							} else {
						            								TextComponent message = new TextComponent(placeholder);
							            							
							            							newText.addExtra(message);
						            							}
						            						} else {
						            							TextComponent message = null;
						            							if (InteractiveChat.PlayerNotFoundReplaceEnable == true) {
						            								message = new TextComponent(InteractiveChat.PlayerNotFoundReplaceText.replace("{Placeholer}", placeholder));
						            							} else {
						            								message = new TextComponent(placeholder);
						            							}
						            							if (InteractiveChat.PlayerNotFoundHoverEnable == true) {
						            								message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PlaceholderAPI.setPlaceholders(event.getPlayer(), InteractiveChat.PlayerNotFoundHoverText.replace("{Placeholer}", placeholder))).create()));
						            							}
						            							if (InteractiveChat.PlayerNotFoundClickEnable == true) {
						            								String text = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, InteractiveChat.PlayerNotFoundClickValue.replace("{Placeholer}", placeholder)));
							            							message.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(InteractiveChat.PlayerNotFoundClickAction), text));
						            							}
						            							
						            							newText.addExtra(message);
						            						}
						            					    
								            				i = i + placeholder.length() - 1;
						            					} else {
						            						join.add(alltext[i]);
						            					}
						            				}
						            				if (join.isEmpty() == false) {			
				            							TextComponent word = new TextComponent(String.join("", join));
							            				word.copyFormatting(each);
							            				newText.addExtra(word);
							            				join.clear();
				            						}
						            			}
						            		}
						            		bcs = ComponentSerializer.parse(ComponentSerializer.toString(newText));
							            	base = new ArrayList<BaseComponent>();
							            	base = CustomStringUtils.loadExtras(Arrays.asList(bcs));
					            		}			            				            		
					            	}
					            	
					            	if (InteractiveChat.useInventory == true) {
					            		
					            		String placeholder = InteractiveChat.invPlaceholder;
					            		
					            		boolean parse = true;
					            		if (sender != null) {
						            		if (InteractiveChat.placeholderCooldowns.containsKey(sender)) {
					            				HashMap<String, Long> map = InteractiveChat.placeholderCooldowns.get(sender);
					            				if (map.containsKey(placeholder.toLowerCase())) {
					            					long timeout = map.get(placeholder.toLowerCase());
					            					if (unix < timeout) {
					            						if (!sender.hasPermission("interactivechat.cooldown.bypass")) {
					            							parse = false;
					            						}
					            					}
					            				}
					            			}
					            		}
					            		
					            		if (parse == true) {
						            		TextComponent newText = new TextComponent("");
						            		List<String> join = new ArrayList<String>();
						            		
						            		for (BaseComponent each : base) {
						            			
						            			String eachLegacyTextCase = each.toLegacyText();
				            					String placeholderCase = placeholder;
				            					if (InteractiveChat.invCaseSensitive == false) {
				            						eachLegacyTextCase = eachLegacyTextCase.toLowerCase();
				            						placeholderCase = placeholderCase.toLowerCase();
				            					}
				            					
						            			if (!eachLegacyTextCase.contains(placeholderCase)) {
						            				newText.addExtra(each);
						            			} else {
						            				String[] alltext = each.toPlainText().split("");
						            				
						            				for (int i = 0; i <= (alltext.length - 1); i = i + 1) {
						            					String detectString = "";
						            					for (int u = 0; u <= (placeholder.length() - 1) && (i + u) < alltext.length; u = u + 1) {
						            						detectString = detectString + alltext[u + i];
						            					}
						            					
						            					String detectStringCase = detectString;
						            					if (InteractiveChat.invCaseSensitive == false) {
						            						detectStringCase = detectStringCase.toLowerCase();
						            						placeholderCase = placeholderCase.toLowerCase();
						            					}
						            					
						            					if (detectStringCase.equals(placeholderCase)) {
						            						
						            						if (join.isEmpty() == false) {			
						            							TextComponent word = new TextComponent(String.join("", join));
									            				word.copyFormatting(each);
									            				newText.addExtra(word);
									            				join.clear();
						            						}
						            						
						            						Player player = sender;
						            						
						            						String replaceText = InteractiveChat.invReplaceText;						       
						            						
						            						if (player != null) {
						            							if (player.hasPermission("interactivechat.module.inventory")) {
						            								
						            								long time = InteractiveChat.keyTime.get(messageKey);
						            								
						            								String title = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, InteractiveChat.invTitle));
						            								
							            							if (!InteractiveChat.inventoryDisplay.containsKey(time)) {
							            								Inventory inv = Bukkit.createInventory(null, 45, title);
								            							for (int j = 0; j < player.getInventory().getSize(); j = j + 1) {
								            								if (player.getInventory().getItem(j) != null) {
								            									if (!player.getInventory().getItem(j).getType().equals(Material.AIR)) {
								            										inv.setItem(j, player.getInventory().getItem(j).clone());
								            									}
								            								}
								            							}			            							
								            							InteractiveChat.inventoryDisplay.put(time, inv);	
								            							HashMap<Long, Inventory> singleMap = new HashMap<Long, Inventory>();
								            							singleMap.put(time, inv);
								            							//BungeeMessageSender.forwardHashMap(sender, 2, singleMap, title);
							            							}
							            							
							            							String textComp = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, replaceText));
							            							
							            							BaseComponent[] bcJson = ComponentSerializer.parse(JsonUtils.toJSON(textComp));
													            	List<BaseComponent> baseJson = new ArrayList<BaseComponent>();
													            	baseJson = CustomStringUtils.loadExtras(Arrays.asList(bcJson));
													            	
													            	List<String> hoverList = ConfigManager.getConfig().getStringList("ItemDisplay.Inventory.HoverMessage");
							            							String text = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, String.join("\n", hoverList)));
													            	
													            	for (BaseComponent baseComponent : baseJson) {
													            		TextComponent message = (TextComponent) baseComponent;
													            		message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text).create()));
								            							message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewinv " + time));
								            							newText.addExtra(message);
													            	}
							            												            							
						            							} else {
						            								TextComponent message = new TextComponent(placeholder);
							            							
							            							newText.addExtra(message);
						            							}
						            						} else {
						            							TextComponent message = null;
						            							if (InteractiveChat.PlayerNotFoundReplaceEnable == true) {
						            								message = new TextComponent(InteractiveChat.PlayerNotFoundReplaceText.replace("{Placeholer}", placeholder));
						            							} else {
						            								message = new TextComponent(placeholder);
						            							}
						            							if (InteractiveChat.PlayerNotFoundHoverEnable == true) {
						            								message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PlaceholderAPI.setPlaceholders(event.getPlayer(), InteractiveChat.PlayerNotFoundHoverText.replace("{Placeholer}", placeholder))).create()));
						            							}
						            							if (InteractiveChat.PlayerNotFoundClickEnable == true) {
						            								String text = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, InteractiveChat.PlayerNotFoundClickValue.replace("{Placeholer}", placeholder)));
							            							message.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(InteractiveChat.PlayerNotFoundClickAction), text));
						            							}
						            							
						            							newText.addExtra(message);
						            						}
						            					    
								            				i = i + placeholder.length() - 1;
						            					} else {
						            						join.add(alltext[i]);
						            					}		            					
						            				}
						            				if (join.isEmpty() == false) {			
				            							TextComponent word = new TextComponent(String.join("", join));
							            				word.copyFormatting(each);
							            				newText.addExtra(word);
							            				join.clear();
				            						}
						            			}
						            		}
						            		bcs = ComponentSerializer.parse(ComponentSerializer.toString(newText));
							            	base = new ArrayList<BaseComponent>();
							            	base = CustomStringUtils.loadExtras(Arrays.asList(bcs));
					            		}					         
					            	}
					            	
					            	if (InteractiveChat.useEnder == true) {
					            		
					            		String placeholder = InteractiveChat.enderPlaceholder;
					            		
					            		boolean parse = true;
					            		if (sender != null) {
						            		if (InteractiveChat.placeholderCooldowns.containsKey(sender)) {
					            				HashMap<String, Long> map = InteractiveChat.placeholderCooldowns.get(sender);
					            				if (map.containsKey(placeholder.toLowerCase())) {
					            					long timeout = map.get(placeholder.toLowerCase());
					            					if (unix < timeout) {
					            						if (!sender.hasPermission("interactivechat.cooldown.bypass")) {
					            							parse = false;
					            						}
					            					}
					            				}
					            			}
					            		}
					            		
					            		if (parse == true) {
						            		TextComponent newText = new TextComponent("");
						            		List<String> join = new ArrayList<String>();
						            		
						            		for (BaseComponent each : base) {
						            			
						            			String eachLegacyTextCase = each.toLegacyText();
				            					String placeholderCase = placeholder;
				            					if (InteractiveChat.enderCaseSensitive == false) {
				            						eachLegacyTextCase = eachLegacyTextCase.toLowerCase();
				            						placeholderCase = placeholderCase.toLowerCase();
				            					}
				            					
						            			if (!eachLegacyTextCase.contains(placeholderCase)) {
						            				newText.addExtra(each);
						            			} else {
						            				String[] alltext = each.toPlainText().split("");
						            				
						            				for (int i = 0; i <= (alltext.length - 1); i = i + 1) {
						            					String detectString = "";
						            					for (int u = 0; u <= (placeholder.length() - 1) && (i + u) < alltext.length; u = u + 1) {
						            						detectString = detectString + alltext[u + i];
						            					}
						            					
						            					String detectStringCase = detectString;
						            					if (InteractiveChat.enderCaseSensitive == false) {
						            						detectStringCase = detectStringCase.toLowerCase();
						            						placeholderCase = placeholderCase.toLowerCase();
						            					}
						            					
						            					if (detectStringCase.equals(placeholderCase)) {
						            						
						            						if (join.isEmpty() == false) {			
						            							TextComponent word = new TextComponent(String.join("", join));
									            				word.copyFormatting(each);
									            				newText.addExtra(word);
									            				join.clear();
						            						}
						            						
						            						Player player = sender;
						            						
						            						String replaceText = InteractiveChat.enderReplaceText;						            						
						            						
						            						if (player != null) {
						            							if (player.hasPermission("interactivechat.module.enderchest")) {
						            								
						            								long time = InteractiveChat.keyTime.get(messageKey);
						            								
						            								String title = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, InteractiveChat.enderTitle));
						            								
							            							if (!InteractiveChat.enderDisplay.containsKey(time)) {
							            								Inventory inv = Bukkit.createInventory(null, 27, title);
								            							for (int j = 0; j < player.getEnderChest().getSize(); j = j + 1) {
								            								if (player.getEnderChest().getItem(j) != null) {
								            									if (!player.getEnderChest().getItem(j).getType().equals(Material.AIR)) {
								            										inv.setItem(j, player.getEnderChest().getItem(j).clone());
								            									}
								            								}
								            							}			            							
								            							InteractiveChat.enderDisplay.put(time, inv);	
								            							HashMap<Long, Inventory> singleMap = new HashMap<Long, Inventory>();
								            							singleMap.put(time, inv);
								            							//BungeeMessageSender.forwardHashMap(sender, 3, singleMap, title);
							            							}
							            							
							            							String textComp = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, replaceText));
							            							
							            							BaseComponent[] bcJson = ComponentSerializer.parse(JsonUtils.toJSON(textComp));
													            	List<BaseComponent> baseJson = new ArrayList<BaseComponent>();
													            	baseJson = CustomStringUtils.loadExtras(Arrays.asList(bcJson));
													            	
													            	List<String> hoverList = ConfigManager.getConfig().getStringList("ItemDisplay.EnderChest.HoverMessage");
							            							String text = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, String.join("\n", hoverList)));
													            	
													            	for (BaseComponent baseComponent : baseJson) {
													            		TextComponent message = (TextComponent) baseComponent;
													            		message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text).create()));
								            							message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewender " + time));
								            							newText.addExtra(message);
													            	}
						            							} else {
						            								TextComponent message = new TextComponent(placeholder);
							            							
							            							newText.addExtra(message);
						            							}
						            						} else {
						            							TextComponent message = null;
						            							if (InteractiveChat.PlayerNotFoundReplaceEnable == true) {
						            								message = new TextComponent(InteractiveChat.PlayerNotFoundReplaceText.replace("{Placeholer}", placeholder));
						            							} else {
						            								message = new TextComponent(placeholder);
						            							}
						            							if (InteractiveChat.PlayerNotFoundHoverEnable == true) {
						            								message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PlaceholderAPI.setPlaceholders(event.getPlayer(), InteractiveChat.PlayerNotFoundHoverText.replace("{Placeholer}", placeholder))).create()));
						            							}
						            							if (InteractiveChat.PlayerNotFoundClickEnable == true) {
						            								String text = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, InteractiveChat.PlayerNotFoundClickValue.replace("{Placeholer}", placeholder)));
							            							message.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(InteractiveChat.PlayerNotFoundClickAction), text));
						            							}
						            							
						            							newText.addExtra(message);
						            						}
						            					    
								            				i = i + placeholder.length() - 1;
						            					} else {
						            						join.add(alltext[i]);
						            					}		            					
						            				}
						            				if (join.isEmpty() == false) {			
				            							TextComponent word = new TextComponent(String.join("", join));
							            				word.copyFormatting(each);
							            				newText.addExtra(word);
							            				join.clear();
				            						}
						            			}
						            		}
						            		bcs = ComponentSerializer.parse(ComponentSerializer.toString(newText));
							            	base = new ArrayList<BaseComponent>();
							            	base = CustomStringUtils.loadExtras(Arrays.asList(bcs));
					            		}
					            	}
								}
			            	} else {
			            		BungeeMessage.fromBungee(messageKey, event, component, field, InteractiveChat.messageKeyUUID.get(messageKey));
			            		return;
			            	}
			            	
			            	TextComponent newText = new TextComponent("");
		            		for (BaseComponent each : base) {
		            			newText.addExtra(each);
		            		}
			            	
		            		TextComponent id = new TextComponent("񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸");
		            		newText.addExtra(id);
		            		
			            	String stringCom = ComponentSerializer.toString(newText);
			            	
			            	if (field == 0) {
			            		send.getChatComponents().write(0, WrappedChatComponent.fromJson(stringCom));
			            	} else if (field == 1) {
			            		send.getModifier().write(1, ComponentSerializer.parse(stringCom));
			            	}
			            	
			            	UUID uuid = null;
			            	if (sender != null) {
			            		uuid = sender.getUniqueId();
			            	}
			            	PostPacketComponentProcessEvent postEvent = new PostPacketComponentProcessEvent(event.getPlayer(), send, uuid);
			            	Player reciever = postEvent.getReciver();
			            	send = postEvent.getPacket();
			            	
			            	if (postEvent.isCancelled() == false) {
				            	try {
									InteractiveChat.protocolManager.sendServerPacket(reciever, send, false);
								} catch (InvocationTargetException e) {
									e.printStackTrace();
								}
			            	}
		            	} else {
		            		TextComponent id = new TextComponent("񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸");
		            		
			            	String stringCom = ComponentSerializer.toString(id);
			                
			            	send.getChatComponents().write(0, WrappedChatComponent.fromJson(stringCom));
		            		try {
								InteractiveChat.protocolManager.sendServerPacket(event.getPlayer(), send, false);
							} catch (InvocationTargetException e) {
								e.printStackTrace();
							}	 
		            	}
		            }
		        }
		    }
		});	
	}
	
	
}
