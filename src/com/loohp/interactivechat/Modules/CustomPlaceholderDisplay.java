package com.loohp.interactivechat.Modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.ParsePlayer;
import com.loohp.interactivechat.ObjectHolders.ICPlaceholder;
import com.loohp.interactivechat.ObjectHolders.PlayerWrapper;
import com.loohp.interactivechat.Utils.ChatColorUtils;
import com.loohp.interactivechat.Utils.CustomStringUtils;
import com.loohp.interactivechat.Utils.PlaceholderParser;
import com.loohp.interactivechat.Utils.PlayerUtils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CustomPlaceholderDisplay {
	
	private static ConcurrentHashMap<Player, ConcurrentHashMap<String, Long>> placeholderCooldowns = InteractiveChat.placeholderCooldowns;
	private static ConcurrentHashMap<Player, Long> universalCooldowns = InteractiveChat.universalCooldowns;
	private static Random random = new Random();
	
	public static BaseComponent process(BaseComponent basecomponent, Optional<PlayerWrapper> optplayer, Player reciever, String messageKey, long unix) {
		for (int i = 0; i < InteractiveChat.placeholderList.size(); i++) {
			
			ICPlaceholder icplaceholder = InteractiveChat.placeholderList.get(i);
			if (icplaceholder.isBuildIn()) {
				continue;
			}
			CustomPlaceholder cp = icplaceholder.getCustomPlaceholder().get();
			
			PlayerWrapper parseplayer = (cp.getParsePlayer().equals(ParsePlayer.SENDER) && optplayer.isPresent()) ? optplayer.get() : new PlayerWrapper(reciever);
			boolean casesensitive = cp.isCaseSensitive();
			
			if (InteractiveChat.useCustomPlaceholderPermissions && optplayer.isPresent()) {
				PlayerWrapper sender = optplayer.get();
				if (!PlayerUtils.hasPermission(sender.getUniqueId(), "interactivechat.module.custom." + cp.getPosition(), true, 5)) {
					continue;
				}
			}
			
			String placeholder = cp.getKeyword();
			placeholder = (cp.getParseKeyword()) ? PlaceholderParser.parse(parseplayer, placeholder) : placeholder;
			long cooldown = cp.getCooldown();
			boolean hoverEnabled = cp.getHover().isEnabled();
			String hoverText = cp.getHover().getText();
			boolean clickEnabled = cp.getClick().isEnabled();
			Action clickAction = cp.getClick().getAction();
			String clickValue = cp.getClick().getValue();
			boolean replaceEnabled = cp.getReplace().isEnabled();
			String replaceText = cp.getReplace().getReplaceText();
			
			basecomponent = processCustomPlaceholder(parseplayer, casesensitive, placeholder, cooldown, hoverEnabled, hoverText, clickEnabled, clickAction, clickValue, replaceEnabled, replaceText, basecomponent, optplayer, messageKey, unix);
		}
		
		if (InteractiveChat.t) {
			String henry = random.nextInt(100) < 80 ? "§7\"§fTerraria is love, Terraria is life§7\"\n              §7~§a§oHenry §e§o(IC Icon Artist)" : "§fShow §a§oHenry §e§o(IC Icon Artist) §fsome §cLOVE§f!\n§bClick me!\n                       §a~From the IC author";
			basecomponent = processCustomPlaceholder(new PlayerWrapper(reciever), false, "Terraria", 0, true, henry, true, Action.OPEN_URL, "https://www.reddit.com/user/henryauyong", true, "§2Terraria", basecomponent, optplayer, messageKey, unix);
		}
			
		return basecomponent;
	}
	
	@SuppressWarnings("deprecation")
	public static BaseComponent processCustomPlaceholder(PlayerWrapper parseplayer, boolean casesensitive, String placeholder, long cooldown, boolean hoverEnabled, String hoverText, boolean clickEnabled, Action clickAction, String clickValue, boolean replaceEnabled, String replaceText, BaseComponent basecomponent, Optional<PlayerWrapper> optplayer, String messageKey, long unix) {
		boolean contain = (casesensitive) ? (basecomponent.toPlainText().contains(placeholder)) : (basecomponent.toPlainText().toLowerCase().contains(placeholder.toLowerCase()));
		if (!InteractiveChat.cooldownbypass.get(unix).contains(placeholder) && contain) {
			if (optplayer.isPresent() && optplayer.get().isLocal()) {
				Player player = optplayer.get().getLocalPlayer();
				Long uc = universalCooldowns.get(player);
				if (uc != null) {
					if (uc > unix) {
						return basecomponent;
					}
				}
				
				if (!placeholderCooldowns.containsKey(player)) {
					placeholderCooldowns.put(player, new ConcurrentHashMap<String, Long>());
				}
				ConcurrentHashMap<String, Long> spmap = placeholderCooldowns.get(player);
				if (spmap.containsKey(placeholder)) {
					if (spmap.get(placeholder) > unix) {
						if (!player.hasPermission("interactivechat.cooldown.bypass")) {
							return basecomponent;
						}
					}
				}
				spmap.put(placeholder, unix + cooldown);
			}
			InteractiveChat.cooldownbypass.get(unix).add(placeholder);
			InteractiveChat.cooldownbypass.put(unix, InteractiveChat.cooldownbypass.get(unix));
		}
		
		List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
		List<BaseComponent> newlist = new ArrayList<BaseComponent>();
		for (BaseComponent base : basecomponentlist) {
			if (!(base instanceof TextComponent)) {
				newlist.add(base);
			} else {
				TextComponent textcomponent = (TextComponent) base;
				String text = textcomponent.getText();
				if (casesensitive) {
					if (!text.contains(placeholder)) {
						newlist.add(textcomponent);
						continue;
					}
				} else {
					if (!text.toLowerCase().contains(placeholder.toLowerCase())) {
						newlist.add(textcomponent);
						continue;
					}
				}
				
				String regex = casesensitive ? CustomStringUtils.escapeMetaCharacters(placeholder) : "(?i)(" + CustomStringUtils.escapeMetaCharacters(placeholder) + ")";
				List<String> trim = new LinkedList<String>(Arrays.asList(text.split(regex, -1)));
				if (trim.get(trim.size() - 1).equals("")) {
					trim.remove(trim.size() - 1);
				}

				String lastColor = "";
				
				for (int i = 0; i < trim.size(); i++) {
					TextComponent before = new TextComponent(textcomponent);
					before.setText(lastColor + trim.get(i));
					newlist.add(before);
					lastColor = ChatColorUtils.getLastColors(before.getText());
					
					boolean endwith = casesensitive ? text.endsWith(placeholder) : text.toLowerCase().endsWith(placeholder.toLowerCase());
					if ((trim.size() - 1) > i || endwith) {
						if (trim.get(i).endsWith("\\") && !trim.get(i).endsWith("\\\\")) {
							String color = ChatColorUtils.getLastColors(newlist.get(newlist.size() - 1).toLegacyText());
							TextComponent message = new TextComponent(placeholder);
							message = (TextComponent) ChatColorUtils.applyColor(message, color);
							((TextComponent) newlist.get(newlist.size() - 1)).setText(trim.get(i).substring(0, trim.get(i).length() - 1));
							newlist.add(message);
						} else {
							if (trim.get(i).endsWith("\\\\")) {
								((TextComponent) newlist.get(newlist.size() - 1)).setText(trim.get(i).substring(0, trim.get(i).length() - 1));
							}
							PlayerWrapper player = parseplayer;
							
							String textComp = placeholder;
							if (replaceEnabled) {
								textComp = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, replaceText));
							}
							BaseComponent[] bcJson = TextComponent.fromLegacyText(textComp);
			            	List<BaseComponent> baseJson = new ArrayList<BaseComponent>();
			            	baseJson = CustomStringUtils.loadExtras(Arrays.asList(bcJson));
			            	
			            	for (BaseComponent baseComponent : baseJson) {
			            		TextComponent message = (TextComponent) baseComponent;
			            		if (hoverEnabled) {
									message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(PlaceholderParser.parse(player, hoverText)).create()));
								}
								
								if (clickEnabled) {
									String clicktext = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, clickValue));
									message.setClickEvent(new ClickEvent(clickAction, clicktext));
								}
								
								newlist.add(message);
			            	}
						}
					}
				}
			}
		}
		
		TextComponent product = new TextComponent("");
		for (int i = 0; i < newlist.size(); i++) {
			BaseComponent each = newlist.get(i);
			product.addExtra(each);
		}
		return product;
	}

}
