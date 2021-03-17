package com.loohp.interactivechat.Modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import org.bukkit.Bukkit;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.API.InteractiveChatAPI;
import com.loohp.interactivechat.ObjectHolders.ICPlayer;
import com.loohp.interactivechat.ObjectHolders.ReplaceTextBundle;
import com.loohp.interactivechat.Utils.ChatColorUtils;
import com.loohp.interactivechat.Utils.ChatComponentUtils;
import com.loohp.interactivechat.Utils.CustomStringUtils;
import com.loohp.interactivechat.Utils.PlaceholderParser;
import com.loohp.interactivechat.Utils.VanishUtils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

public class PlayernameDisplay {
	
	public static BaseComponent process(BaseComponent basecomponent, Optional<ICPlayer> sender, long unix) {
		List<ReplaceTextBundle> names = new ArrayList<>();
		Bukkit.getOnlinePlayers().forEach(each -> {
			if (VanishUtils.isVanished(each.getUniqueId())) {
				return;
			}
			ICPlayer icplayer = new ICPlayer(each);
			names.add(new ReplaceTextBundle(ChatColorUtils.stripColor(each.getName()), new ICPlayer(each), each.getName()));
			if (!ChatColorUtils.stripColor(each.getName()).equals(ChatColorUtils.stripColor(each.getDisplayName()))) {
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
		
		Collections.sort(names);
		Collections.reverse(names);
		
		List<BaseComponent> matched = new ArrayList<>();
		for (ReplaceTextBundle entry : names) {
			basecomponent = processPlayer(entry.getPlaceholder(), entry.getPlayer(), entry.getReplaceText(), basecomponent, matched, unix);
		}
		return basecomponent;
	}
	
	@SuppressWarnings("deprecation")
	public static BaseComponent processPlayer(String placeholder, ICPlayer player, String replaceText, BaseComponent basecomponent, List<BaseComponent> matched, long unix) {
		List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
		List<BaseComponent> newlist = new ArrayList<>();

		for (BaseComponent base : basecomponentlist) {
			if (matched.stream().anyMatch(each -> ChatComponentUtils.areSimilar(each, base, true))) {
				newlist.add(base);
			} else if (!(base instanceof TextComponent)) {
				if (InteractiveChat.usePlayerNameOnTranslatables && base instanceof TranslatableComponent) {
					TranslatableComponent trans = (TranslatableComponent) base;
					List<BaseComponent> withs = trans.getWith();
					if (withs != null) {
						for (int i = 0; i < withs.size(); i++) {
							if (withs.get(i) instanceof TextComponent) {
								TextComponent text = (TextComponent) withs.get(i);
								if (ChatColorUtils.stripColor(text.toLegacyText()).equalsIgnoreCase(placeholder)) {
									TextComponent message = new TextComponent(ChatColorUtils.stripColor(replaceText));
									if (InteractiveChat.usePlayerNameHoverEnable) {
										String playertext = PlaceholderParser.parse(player, InteractiveChat.usePlayerNameHoverText);
										message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(playertext).create()));
									}
									if (InteractiveChat.usePlayerNameClickEnable) {
										String playertext = PlaceholderParser.parse(player, InteractiveChat.usePlayerNameClickValue);
										message.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(InteractiveChat.usePlayerNameClickAction), playertext));
									}
									withs.set(i, message);
								}
							}
						}
					}
					newlist.add(base);
				} else {
					newlist.add(base);
				}
			} else {
				TextComponent textcomponent = (TextComponent) base;
				String text = textcomponent.getText();
				String regex = InteractiveChat.usePlayerNameCaseSensitive ? "(?<!\u00a7)" + CustomStringUtils.getIgnoreColorCodeRegex(CustomStringUtils.escapeMetaCharacters(placeholder)) : "(?i)(?<!\u00a7)(" + CustomStringUtils.getIgnoreColorCodeRegex(CustomStringUtils.escapeMetaCharacters(placeholder)) + ")";
				
				if (!text.matches(".*" + regex + ".*")) {
					newlist.add(textcomponent);
					continue;
				}

				Queue<String> matches = (LinkedList<String>) CustomStringUtils.getAllMatches(regex, text);
				List<String> trim = new LinkedList<String>(Arrays.asList(text.split(regex, -1)));
				if (trim.get(trim.size() - 1).equals("")) {
					trim.remove(trim.size() - 1);
				}
				
				String lastColor = "";
				
				StringBuilder sb = new StringBuilder();
				
				for (int i = 0; i < trim.size(); i++) {
					TextComponent before = new TextComponent(textcomponent);
					before.setText(lastColor + trim.get(i));
					newlist.add(before);
					sb.append(before.getText());
					if ((trim.size() - 1) > i || text.matches(".*" + regex + "$")) {
						lastColor = ChatColorUtils.getLastColors(sb.toString());
				    
						TextComponent message = new TextComponent(matches.isEmpty() ? replaceText : matches.poll());
						message = (TextComponent) CustomStringUtils.copyFormatting(message, before);
						message.setText(lastColor + message.getText());

						if (InteractiveChat.usePlayerNameHoverEnable) {
							String playertext = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.usePlayerNameHoverText));
							message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(playertext).create()));
						}
						if (InteractiveChat.usePlayerNameClickEnable) {
							String playertext = PlaceholderParser.parse(player, InteractiveChat.usePlayerNameClickValue);
							message.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(InteractiveChat.usePlayerNameClickAction), playertext));
						}
						
						matched.add(message);
						newlist.add(message);
						
						lastColor = ChatColorUtils.getLastColors(sb.append(message.getText()).toString());
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
