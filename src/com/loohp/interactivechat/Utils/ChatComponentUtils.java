package com.loohp.interactivechat.Utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.Listeners.ClientSettingPackets;
import com.loohp.interactivechat.Listeners.ClientSettingPackets.ColorSettings;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Content;
import net.md_5.bungee.api.chat.HoverEvent.ContentEntity;
import net.md_5.bungee.api.chat.HoverEvent.ContentItem;
import net.md_5.bungee.api.chat.HoverEvent.ContentText;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatComponentUtils {
	
	private static Class<?> chatHoverEventClass;
	private static MethodHandle hoverEventGetValueMethod;
	
	public static void setupLegacy() {
		try {
			chatHoverEventClass = Class.forName("net.md_5.bungee.api.chat.HoverEvent");
			hoverEventGetValueMethod = MethodHandles.lookup().findVirtual(chatHoverEventClass, "getValue", MethodType.methodType(BaseComponent[].class));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public static boolean areSimilar(BaseComponent base1, BaseComponent base2, boolean compareText) {
		if (!areEventsSimilar(base1, base2)) {
			return false;
		}
		if ((base1.getColor() == null && base2.getColor() != null) || (base1.getColor() != null && base2.getColor() == null)) {
			return false;
		}
		if (base1.getColor() != null && base2.getColor() != null) {
			if (!base1.getColor().equals(base2.getColor())) {
				return false;
			}
		}
		if (base1.isBold() != base2.isBold()) {
			return false;
		}
		if (base1.isItalic() != base2.isItalic()) {
			return false;
		}
		if (base1.isObfuscated() != base2.isObfuscated()) {
			return false;
		}
		if (base1.isStrikethrough() != base2.isStrikethrough()) {
			return false;
		}
		if (base1.isUnderlined() != base2.isUnderlined()) {
			return false;
		}
		if (compareText && !base1.toLegacyText().equals(base2.toLegacyText())) {
			return false;
		}
		return true;
	}
	
	public static boolean areSimilarNoEvents(BaseComponent base1, BaseComponent base2, boolean compareText) {
		if ((base1.getColor() == null && base2.getColor() != null) || (base1.getColor() != null && base2.getColor() == null)) {
			return false;
		}
		if (base1.getColor() != null && base2.getColor() != null) {
			if (!base1.getColor().equals(base2.getColor())) {
				return false;
			}
		}
		if (base1.isBold() != base2.isBold()) {
			return false;
		}
		if (base1.isItalic() != base2.isItalic()) {
			return false;
		}
		if (base1.isObfuscated() != base2.isObfuscated()) {
			return false;
		}
		if (base1.isStrikethrough() != base2.isStrikethrough()) {
			return false;
		}
		if (base1.isUnderlined() != base2.isUnderlined()) {
			return false;
		}
		if (compareText && !base1.toLegacyText().equals(base2.toLegacyText())) {
			return false;
		}
		return true;
	}
	
	public static boolean areEventsSimilar(BaseComponent base1, BaseComponent base2) {
		boolean clickSim = false;
		boolean hoverSim = false;
		if (base1.getClickEvent() == null && base2.getClickEvent() == null) {
			clickSim = true;
		} else {
			if (base1.getClickEvent() != null && base2.getClickEvent() != null) {
				ClickEvent click1 = base1.getClickEvent();
				ClickEvent click2 = base2.getClickEvent();
				if (click1.getAction() == null && click2.getAction() == null) {
					clickSim = true;
				} else if ((click1 != null && click2 != null) && click1.getAction().equals(click2.getAction())) {
					String value1 = click1.getValue();
					String value2 = click2.getValue();
					if (value1 == null && value2 == null) {
						clickSim = true;
					} else if ((value1 != null && value2 != null) && value1.equals(value2)) {
						clickSim = true;
					}
				}
			}
		}
		if (base1.getHoverEvent() == null && base2.getHoverEvent() == null) {
			hoverSim = true;
		} else {
			if (base1.getHoverEvent() != null && base2.getHoverEvent() != null) {
				HoverEvent hover1 = base1.getHoverEvent();
				HoverEvent hover2 = base2.getHoverEvent();
				if (hover1.getAction().equals(hover2.getAction())) {
					if (InteractiveChat.legacyChatAPI) {
						try {
							BaseComponent[] basecomponentarray1 = (BaseComponent[]) hoverEventGetValueMethod.invoke(hover1);
							BaseComponent[] basecomponentarray2 = (BaseComponent[]) hoverEventGetValueMethod.invoke(hover2);
							if (basecomponentarray1.length == basecomponentarray2.length) {
								hoverSim = true;
								for (int i = 0; i < basecomponentarray1.length && i < basecomponentarray2.length ; i++) {
									BaseComponent bc1 = basecomponentarray1[i];
									BaseComponent bc2 = basecomponentarray2[i];
									if (!(bc1 == null && bc2 == null) && (bc1 == null || bc2 == null)) {
										hoverSim = false;
										break;
									} else if (!areSimilarNoEvents(bc1, bc2, true)) {
										hoverSim = false;
										break;
									}
								}
							}
						} catch (Throwable e) {
							e.printStackTrace();
						}
					} else {
						List<Content> contents1 = hover1.getContents();
						List<Content> contents2 = hover2.getContents();
						if (hover1.getAction().equals(hover2.getAction()) && contents1.size() == contents2.size()) {
							hoverSim = true;
							for (int i = 0; i < contents1.size() && i < contents2.size() ; i++) {
								Content c1 = contents1.get(i);
								Content c2 = contents2.get(i);
								if (c1 instanceof ContentText && c2 instanceof ContentText) {
									ContentText ct1 = (ContentText) c1;
									ContentText ct2 = (ContentText) c2;
									if (ct1.getValue() instanceof BaseComponent[] && ct2.getValue() instanceof BaseComponent[]) {
										BaseComponent[] basecomponentarray1 = (BaseComponent[]) ct1.getValue();
										BaseComponent[] basecomponentarray2 = (BaseComponent[]) ct2.getValue();
										if (basecomponentarray1.length == basecomponentarray2.length) {
											hoverSim = true;
											for (int j = 0; j < basecomponentarray1.length && j < basecomponentarray2.length ; j++) {
												BaseComponent bc1 = basecomponentarray1[j];
												BaseComponent bc2 = basecomponentarray2[j];
												if (!(bc1 == null && bc2 == null) && (bc1 == null || bc2 == null)) {
													hoverSim = false;
													break;
												} else if (!areSimilarNoEvents(bc1, bc2, true)) {
													hoverSim = false;
													break;
												}
											}
										}
									} else if (ct1.getValue() instanceof String && ct2.getValue() instanceof String) {
										String str1 = (String) ct1.getValue();
										String str2 = (String) ct2.getValue();
										if (!str1.equals(str2)) {
											hoverSim = false;
											break;
										}
									}
								} else if (c1 instanceof ContentEntity && c2 instanceof ContentEntity) {
									ContentEntity ce1 = (ContentEntity) c1;
									ContentEntity ce2 = (ContentEntity) c2;
									if (!(ce1.getId().equals(ce2.getId()) && ce1.getType().equals(ce2.getType()) && areSimilarNoEvents(ce1.getName(), ce2.getName(), true))) {
										hoverSim = false;
										break;
									}
								} else if (c1 instanceof ContentItem && c2 instanceof ContentItem) {
									ContentItem ci1 = (ContentItem) c1;
									ContentItem ci2 = (ContentItem) c2;
									if (!(ci1.getCount() == ci2.getCount() && ci1.getId().equals(ci2.getId()) && ci1.getTag().equals(ci2.getTag()))) {
										hoverSim = false;
										break;
									}
								} else {
									hoverSim = false;
									break;
								}
							}
						}
					}
				}
			}
		}

		return clickSim && hoverSim;
	}
	
	public static BaseComponent removeHoverEventColor(BaseComponent baseComponent) {
		if (baseComponent.getHoverEvent() != null) {
			if (InteractiveChat.legacyChatAPI) {
				try {
					for (BaseComponent each : (BaseComponent[]) hoverEventGetValueMethod.invoke(baseComponent.getHoverEvent())) {
						each.setColor(ChatColor.WHITE);
						if (each instanceof TextComponent) {
							((TextComponent) each).setText(((TextComponent) each).getText().replaceAll("§[0-9a-e]", "§f"));
						}
						if (each.getHoverEvent() != null) {
							each = removeHoverEventColor(each);
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			} else {
				int j = 0;
				List<Content> contents = baseComponent.getHoverEvent().getContents();
				for (Content content : contents) {
					if (content instanceof ContentText) {
						Object value = ((ContentText) content).getValue();
						if (value instanceof BaseComponent[]) {
							for (BaseComponent each : (BaseComponent[]) value) {
								each.setColor(ChatColor.WHITE);
								if (each instanceof TextComponent) {
									((TextComponent) each).setText(((TextComponent) each).getText().replaceAll("§[0-9a-e]", "§f"));
								}
								if (each.getHoverEvent() != null) {
									each = removeHoverEventColor(each);
								}
							}
						} else if (value instanceof String) {
							contents.set(j, new ContentText(((String) value).replaceAll("§[0-9a-e]", "§f")));
						}
					}
					j++;
				}
			}
		}
		return baseComponent;
	}
	
	public static BaseComponent cleanUpLegacyText(BaseComponent basecomponent, Player player) {
		List<BaseComponent> newlist = new LinkedList<BaseComponent>();
		for (BaseComponent base : CustomStringUtils.loadExtras(basecomponent)) {
			if (base instanceof TextComponent) {
				List<TextComponent> texts = Arrays.asList(TextComponent.fromLegacyText(base.toLegacyText())).stream().map(each -> (TextComponent) each).collect(Collectors.toList());
				texts.forEach(each -> {
					if (InteractiveChat.version.isLegacy() && !InteractiveChat.version.equals(MCVersion.V1_12)) {
						each = (TextComponent) CustomStringUtils.copyFormattingEventsNoReplace(each, base);
	 	        	} else {
	 	        		each.copyFormatting(base, FormatRetention.EVENTS, false);
	 	        	}
					//Bukkit.getConsoleSender().sendMessage(ComponentSerializer.toString(each).replace("§", "&"));
				});
				newlist.addAll(texts);
			} else {
				newlist.add(base);
			}
		}

		ColorSettings colorsEnabled = ClientSettingPackets.getSettings(player);
		TextComponent product = new TextComponent("");
		for (int i = 0; i < newlist.size(); i++) {
			BaseComponent each = newlist.get(i);
			if (colorsEnabled.equals(ColorSettings.OFF)) {
				each.setColor(ChatColor.WHITE);
				if (each instanceof TextComponent) {
					((TextComponent) each).setText(((TextComponent) each).getText().replaceAll("§[0-9a-e]", "§f"));
				}
				each = removeHoverEventColor(each);
			}
			product.addExtra(each);
		}

		return product;
	}
	
	public static BaseComponent respectClientColorSettingsWithoutCleanUp(BaseComponent basecomponent, Player player) {
		List<BaseComponent> newlist = CustomStringUtils.loadExtras(basecomponent);
		
		ColorSettings colorsEnabled = ClientSettingPackets.getSettings(player);
		TextComponent product = new TextComponent("");
		for (int i = 0; i < newlist.size(); i++) {
			BaseComponent each = newlist.get(i);
			if (colorsEnabled.equals(ColorSettings.OFF)) {
				each.setColor(ChatColor.WHITE);
				if (each instanceof TextComponent) {
					((TextComponent) each).setText(((TextComponent) each).getText().replaceAll("§[0-9a-e]", "§f"));
				}
				each = removeHoverEventColor(each);
			}
			product.addExtra(each);
		}
		
		return product;
	}
	
	public static BaseComponent join(BaseComponent base, BaseComponent... basecomponentarray) {
		if (basecomponentarray.length <= 0) {
			return base;
		} else {
			BaseComponent product = base;
			for (BaseComponent each : basecomponentarray) {
				product.addExtra(each);
			}
			return product;
		}
	}
	
	public static BaseComponent join(BaseComponent[] basecomponentarray) {
		if (basecomponentarray.length <= 1) {
			return basecomponentarray[0];
		} else {
			return join(basecomponentarray[0], Arrays.copyOfRange(basecomponentarray, 1, basecomponentarray.length));
		}
	}

}
