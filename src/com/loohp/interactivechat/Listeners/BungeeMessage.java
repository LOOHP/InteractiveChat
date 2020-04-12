package com.loohp.interactivechat.Listeners;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.earth2me.essentials.Essentials;
import com.loohp.interactivechat.ConfigManager;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.API.Events.PlayerMentionPlayerEvent;
import com.loohp.interactivechat.API.Events.PostPacketComponentProcessEvent;
import com.loohp.interactivechat.API.Events.PrePacketComponentProcessEvent;
import com.loohp.interactivechat.Utils.CustomStringUtils;
import com.loohp.interactivechat.Utils.JsonUtils;
import com.loohp.interactivechat.Utils.MaterialUtils;
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

public class BungeeMessage {
	public static void fromBungee(String messageKey, PacketEvent event, WrappedChatComponent preComponent, int preField, UUID senderFromBungee) {
		new BukkitRunnable() {
			public void run() {
				OfflinePlayer sender = Bukkit.getOfflinePlayer(senderFromBungee);
				PrePacketComponentProcessEvent preEvent = new PrePacketComponentProcessEvent(event.getPlayer(), preComponent, preField, sender.getUniqueId());
				sender = Bukkit.getOfflinePlayer(preEvent.getSender());
				WrappedChatComponent component = preEvent.getChatComponent();
				int field = preEvent.getField();
				
				long unix = System.currentTimeMillis();
				
				BaseComponent[] bcs = ComponentSerializer.parse(component.getJson());
		    	List<BaseComponent> base = new ArrayList<BaseComponent>();
		    	base = CustomStringUtils.loadExtras(Arrays.asList(bcs));
				
				TextComponent removeKeyText = new TextComponent("");
				for (BaseComponent each : base) {
					String json = ComponentSerializer.toString(each);
					json = json.replaceAll(InteractiveChat.space0, "").replaceAll(InteractiveChat.space1, "");
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
		            						
		            						if (join.isEmpty() == false) {		            							
		            							TextComponent word = new TextComponent(String.join("", join));
					            				word.copyFormatting(each);
					            				newText.addExtra(word);
					            				join.clear();
		            						}
		        					    
		            						TextComponent message = new TextComponent(detectString);
		            						message.copyFormatting(each);
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
			            						String hover = ChatColor.translateAlternateColorCodes('&', InteractiveChat.mentionHover.replace("{Sender}", sender.getName()).replace("{Reciever}", player.getName()));
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
		    	for (int customNo = 1; ConfigManager.getConfig().contains("CustomPlaceholders." + String.valueOf(customNo)) == true; customNo = customNo + 1) {
		    		
		    		OfflinePlayer player = null;
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
		        
		    	Bukkit.getConsoleSender().sendMessage("sender " + sender.getName());
		    	if (InteractiveChat.useItem == true) {
		    		Bukkit.getConsoleSender().sendMessage("000");
		    		String placeholder = InteractiveChat.itemPlaceholder;
		    		Bukkit.getConsoleSender().sendMessage("111");
		    		TextComponent newText = new TextComponent("");
		    		List<String> join = new ArrayList<String>();				            		
		    		Bukkit.getConsoleSender().sendMessage("222");
		    		for (BaseComponent each : base) {
		    			Bukkit.getConsoleSender().sendMessage("333");
		    			String eachLegacyTextCase = each.toLegacyText().toLowerCase();
						String placeholderCase = placeholder;
						if (InteractiveChat.itemCaseSensitive == false) {
							eachLegacyTextCase = eachLegacyTextCase.toLowerCase();
							placeholderCase = placeholderCase.toLowerCase();
						}
						Bukkit.getConsoleSender().sendMessage("444");
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
		    					Bukkit.getConsoleSender().sendMessage("555");
		    					if (InteractiveChat.itemCaseSensitive == false) {
		    						detectStringCase = detectStringCase.toLowerCase();
		    						placeholderCase = placeholderCase.toLowerCase();
		    					}
		    					Bukkit.getConsoleSender().sendMessage("666");
		    					if (detectStringCase.equals(placeholderCase)) {
		    						Bukkit.getConsoleSender().sendMessage("777");
		    						if (join.isEmpty() == false) {		            							
		    							TextComponent word = new TextComponent(String.join("", join));
			            				word.copyFormatting(each);
			            				newText.addExtra(word);
			            				join.clear();
		    						}
		    						Bukkit.getConsoleSender().sendMessage("888");
		    						OfflinePlayer player = null;
		    						if (InteractiveChat.itemDisplay.containsKey(InteractiveChat.keyTime.get(messageKey))) {
		    							player = sender;
		    							Bukkit.getConsoleSender().sendMessage("Find " + player.getName());
		    						}   				
		    						Bukkit.getConsoleSender().sendMessage("999");
		    						if (player != null) { 
		    							ItemStack item = InteractiveChat.itemDisplay.get(InteractiveChat.keyTime.get(messageKey)).getItem(13);		
		    							Bukkit.getConsoleSender().sendMessage(item.getType().toString());

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
		        					    
		        					    amountString = String.valueOf(item.getAmount());
		        					    message = ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, InteractiveChat.itemReplaceText.replace("{Item}", itemString).replace("{Amount}", amountString)));
		        					    
		        					    BaseComponent[] hoverEventComponents = new BaseComponent[] {new TextComponent(itemJson)};

		        					    HoverEvent hoverItem = new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents);

		        						String title = InteractiveChat.itemTitle;
		        						
		        						long time = InteractiveChat.keyTime.get(messageKey) + 1;
		        						
		        						if (!InteractiveChat.itemDisplay.containsKey(time)) {
		    								Inventory inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, title)));
		    								ItemStack empty = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE, 1);
		    								if (item.getType().equals(Material.MAGENTA_STAINED_GLASS_PANE)) {
		    									empty = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
		    								}
		    								ItemMeta emptyMeta = empty.getItemMeta();
		    								emptyMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "");
		    								empty.setItemMeta(emptyMeta);
		        							for (int j = 0; j < inv.getSize(); j = j + 1) {
		        								inv.setItem(j, empty);
		        							}
		        							inv.setItem(13, item);
		        							InteractiveChat.itemDisplay.put(time, inv);	
		    							}
		        						
		        						ClickEvent clickItem = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/interactivechat viewitem " + time);
		        					    
		        					    BaseComponent[] bcJson = ComponentSerializer.parse(JsonUtils.toJSON(message));
						            	List<BaseComponent> baseJson = new ArrayList<BaseComponent>();
						            	baseJson = CustomStringUtils.loadExtras(Arrays.asList(bcJson));
						            	
						            	for (BaseComponent baseComponent : baseJson) {
						            		TextComponent textcomponent = (TextComponent) baseComponent;
		            					    textcomponent.setHoverEvent(hoverItem);
		            					    textcomponent.setClickEvent(clickItem);
		            					    newText.addExtra(textcomponent);
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
		    	
		    	if (InteractiveChat.useInventory == true) {
		    		
		    		String placeholder = InteractiveChat.invPlaceholder;
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
		    						
		    						OfflinePlayer player = null;
		    						if (InteractiveChat.inventoryDisplay.containsKey(InteractiveChat.keyTime.get(messageKey))) {
		    							player = sender;
		    						}   	
		    						
		    						String replaceText = InteractiveChat.invReplaceText;
		    						String title = InteractiveChat.invTitle;
		    						
		    						if (player != null) {
		    							
		    							Inventory playerInv = InteractiveChat.inventoryDisplay.get(InteractiveChat.keyTime.get(messageKey));
		    								
		    							long time = InteractiveChat.keyTime.get(messageKey) + 1;
										
		    							if (!InteractiveChat.inventoryDisplay.containsKey(time)) {
		    								Inventory inv = Bukkit.createInventory(null, 45, ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, title)));
		        							for (int j = 0; j < playerInv.getSize(); j = j + 1) {
		        								if (playerInv.getItem(j) != null) {
		        									if (!playerInv.getItem(j).getType().equals(Material.AIR)) {
		        										inv.setItem(j, playerInv.getItem(j).clone());
		        									}
		        								}
		        							}			            							
		        							InteractiveChat.inventoryDisplay.put(time, inv);	
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
		    	
		    	if (InteractiveChat.useEnder == true) {
		    		
		    		String placeholder = InteractiveChat.enderPlaceholder;
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
		    						
		    						OfflinePlayer player = null;
		    						if (InteractiveChat.enderDisplay.containsKey(InteractiveChat.keyTime.get(messageKey))) {
		    							player = sender;
		    						}   	
		    						
		    						String replaceText = InteractiveChat.enderReplaceText;
		    						String title = InteractiveChat.enderTitle;
		    						
		    						if (player != null) {
		    							
		    							Inventory playerEnder = InteractiveChat.inventoryDisplay.get(InteractiveChat.keyTime.get(messageKey));
		    								
		    							long time = InteractiveChat.keyTime.get(messageKey) + 1;
										
		    							if (!InteractiveChat.enderDisplay.containsKey(time)) {
		    								Inventory inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, title)));
		        							for (int j = 0; j < playerEnder.getSize(); j = j + 1) {
		        								if (playerEnder.getItem(j) != null) {
		        									if (!playerEnder.getItem(j).getType().equals(Material.AIR)) {
		        										inv.setItem(j, playerEnder.getItem(j).clone());
		        									}
		        								}
		        							}			            							
		        							InteractiveChat.enderDisplay.put(time, inv);	
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
		    	
		    	TextComponent newText = new TextComponent("");
        		for (BaseComponent each : base) {
        			newText.addExtra(each);
        		}
            	
        		TextComponent id = new TextComponent("񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸񘄸");
        		newText.addExtra(id);
        		
            	String stringCom = ComponentSerializer.toString(newText);
            	
            	PacketContainer send = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.CHAT);
            	send.getModifier().writeDefaults();
            	
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
            	
			}
		}.runTaskLater(InteractiveChat.plugin, 10);
	}
}
