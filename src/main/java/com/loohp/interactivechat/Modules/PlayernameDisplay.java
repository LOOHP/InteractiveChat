package com.loohp.interactivechat.Modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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

public class PlayernameDisplay implements Listener {
	
	private static final String PROCESSED_IDENTIFIER = "<PROCESSED-4d898488-7e0a-42b1-b782-cd7ca66bfc75>";	
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
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		flag.set(random.nextInt());
		names = null;
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		flag.set(random.nextInt());
		names = null;
	}
	
	public static BaseComponent process(BaseComponent basecomponent, Optional<ICPlayer> sender, Player reciever, long unix) {
		List<BaseComponent> matched = new ArrayList<>();
		List<ReplaceTextBundle> names = PlayernameDisplay.names;
		if (names == null) {
			names = getNames();
		}
		for (ReplaceTextBundle entry : names) {
			basecomponent = processPlayer(entry.getPlaceholder(), entry.getPlayer(), sender, reciever, entry.getReplaceText(), basecomponent, matched, unix);
		}
		
		//clean
		
		List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
		
		TextComponent product = new TextComponent("");
		for (int i = 0; i < basecomponentlist.size(); i++) {
			BaseComponent each = basecomponentlist.get(i);
			if (each instanceof TextComponent) {
				((TextComponent) each).setText(((TextComponent) each).getText().replace(PROCESSED_IDENTIFIER, ""));
			}
			product.addExtra(each);
		}
		
		return product;
	}
	
	@SuppressWarnings("deprecation")
	private static BaseComponent processPlayer(String placeholder, ICPlayer player, Optional<ICPlayer> sender, Player reciever, String replaceText, BaseComponent basecomponent, List<BaseComponent> matched, long unix) {
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
									TextComponent message = new TextComponent(text.toLegacyText());
									if (InteractiveChat.usePlayerNameHoverEnable) {
										String playertext = PlaceholderParser.parse(player, InteractiveChat.usePlayerNameHoverText);
										message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(playertext).create()));
									}
									if (InteractiveChat.usePlayerNameClickEnable) {
										String playertext = PlaceholderParser.parse(player, InteractiveChat.usePlayerNameClickValue);
										message.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(InteractiveChat.usePlayerNameClickAction), playertext));
									}
									withs.set(i, ChatComponentUtils.cleanUpLegacyText(message, reciever));
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
				HoverEvent hoverEvent = base.getHoverEvent();
				if (isMention(hoverEvent, sender, reciever)) {
					newlist.add(base);
				} else {
					String text = textcomponent.getText();
					String regex = InteractiveChat.usePlayerNameCaseSensitive ? "(?<!\u00a7)" + CustomStringUtils.getIgnoreColorCodeRegex(CustomStringUtils.escapeMetaCharacters(placeholder)) : "(?i)(?<!\u00a7)(" + CustomStringUtils.getIgnoreColorCodeRegex(CustomStringUtils.escapeMetaCharacters(placeholder)) + ")";
					
					if (!text.matches(".*" + regex + ".*")) {
						newlist.add(textcomponent);
						continue;
					}

					Queue<String> matches = (LinkedList<String>) CustomStringUtils.getAllMatches(regex, text);
					List<String> trim = new LinkedList<>(Arrays.asList(text.split(regex, -1)));
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
					    
							String replacement = matches.isEmpty() ? replaceText : matches.poll();
							replacement = replacement.replace("", PROCESSED_IDENTIFIER);
							TextComponent message = new TextComponent(replacement);
							message = (TextComponent) CustomStringUtils.copyFormatting(message, before);
							message.setText(lastColor + message.getText());

							if (InteractiveChat.usePlayerNameHoverEnable) {
								String playertext = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.usePlayerNameHoverText));
								message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent(playertext)}));
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
		}
		
		TextComponent product = new TextComponent("");
		for (int i = 0; i < newlist.size(); i++) {
			BaseComponent each = newlist.get(i);
			product.addExtra(each);
		}
		return product;
	}
	
	@SuppressWarnings("deprecation")
	private static boolean isMention(HoverEvent hover, Optional<ICPlayer> sender, Player reciever) {
		if (hover == null || !sender.isPresent()) {
			return false;
		}
		if (!hover.getAction().equals(HoverEvent.Action.SHOW_TEXT)) {
			return false;
		}
		BaseComponent[] component = hover.getValue();
		if (component.length <= 0) {
			return false;
		}
		if (!(component[0] instanceof TextComponent)) {
			return false;
		}
		TextComponent text = (TextComponent) component[0];
		String hoverText = ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.mentionHover.replace("{Sender}", sender.get().getDisplayName()).replace("{Reciever}", reciever.getDisplayName()));
		if (text == null || text.getText() == null) {
			return false;
		}
		return text.getText().equals(hoverText);
	}
	
	private static List<ReplaceTextBundle> getNames() {
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
		
		return names;
	}

}
