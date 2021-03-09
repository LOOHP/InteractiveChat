package com.loohp.interactivechat.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.entity.Player;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.Listeners.ClientSettingPackets;
import com.loohp.interactivechat.Listeners.ClientSettingPackets.ColorSettings;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;

public class ChatComponentUtils {
	
	private static Pattern fontFormating = Pattern.compile("(?=(?<!\\\\)|(?<=\\\\\\\\))\\[[^\\]]*?font=[0-9a-zA-Z:_]*[^\\[]*?\\]");
	private static Pattern fontEscape = Pattern.compile("\\\\\\[ *?font=[0-9a-zA-Z:_]* *?\\]");
	
	private static String validFont = "^([0-9a-zA-Z_]+:)?[0-9a-zA-Z_]+$";
	
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
		if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
			if ((base1.getFont() == null && base2.getFont() != null) || (base1.getFont() != null && base2.getFont() == null)) {
				return false;
			}
			if (base1.getFont() != null && base2.getFont() != null) {
				if (!base1.getFont().equals(base2.getFont())) {
					return false;
				}
			}
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
		if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
			if ((base1.getFont() == null && base2.getFont() != null) || (base1.getFont() != null && base2.getFont() == null)) {
				return false;
			}
			if (base1.getFont() != null && base2.getFont() != null) {
				if (!base1.getFont().equals(base2.getFont())) {
					return false;
				}
			}
		}
		if (compareText && !base1.toLegacyText().equals(base2.toLegacyText())) {
			return false;
		}
		return true;
	}
	
	public static boolean areFontsSimilar(BaseComponent base1, BaseComponent base2) {
		if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
			if ((base1.getFont() == null && base2.getFont() != null) || (base1.getFont() != null && base2.getFont() == null)) {
				return false;
			}
			if (base1.getFont() != null && base2.getFont() != null) {
				if (!base1.getFont().equals(base2.getFont())) {
					return false;
				}
			}
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
						@SuppressWarnings("deprecation")
						BaseComponent[] basecomponentarray1 = hover1.getValue();
						@SuppressWarnings("deprecation")
						BaseComponent[] basecomponentarray2 = hover2.getValue();
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
					} else {
						List<Content> contents1 = hover1.getContents();
						List<Content> contents2 = hover2.getContents();
						if (hover1.getAction().equals(hover2.getAction()) && contents1.size() == contents2.size()) {
							hoverSim = true;
							for (int i = 0; i < contents1.size() && i < contents2.size() ; i++) {
								Content c1 = contents1.get(i);
								Content c2 = contents2.get(i);
								if (c1 instanceof Text && c2 instanceof Text) {
									Text ct1 = (Text) c1;
									Text ct2 = (Text) c2;
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
								} else if (c1 instanceof Entity && c2 instanceof Entity) {
									Entity ce1 = (Entity) c1;
									Entity ce2 = (Entity) c2;
									if (!(ce1.getId().equals(ce2.getId()) && ce1.getType().equals(ce2.getType()) && areSimilarNoEvents(ce1.getName(), ce2.getName(), true))) {
										hoverSim = false;
										break;
									}
								} else if (c1 instanceof Item && c2 instanceof Item) {
									Item ci1 = (Item) c1;
									Item ci2 = (Item) c2;
									if (!(ci1.getCount() == ci2.getCount() && ((ci1.getId() == null && ci2.getId() == null) || (ci1.getId() != null && ci2.getId() != null && ci1.getId().equals(ci2.getId()))) && ((ci1.getTag() == null && ci2.getTag() == null) || (ci1.getTag() != null && ci2.getTag() != null && ci1.getTag().equals(ci2.getTag()))))) {
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
	
	@SuppressWarnings("deprecation")
	public static BaseComponent removeHoverEventColor(BaseComponent baseComponent) {
		if (baseComponent.getHoverEvent() != null) {
			if (InteractiveChat.legacyChatAPI) {
				for (BaseComponent each : baseComponent.getHoverEvent().getValue()) {
					each.setColor(ChatColor.WHITE);
					if (each instanceof TextComponent) {
						((TextComponent) each).setText(ChatColorUtils.stripColor(((TextComponent) each).getText()));
					}
				}
			} else {
				int j = 0;
				List<Content> contents = baseComponent.getHoverEvent().getContents();
				for (Content content : contents) {
					if (content instanceof Text) {
						Object value = ((Text) content).getValue();
						if (value instanceof BaseComponent[]) {
							for (BaseComponent each : (BaseComponent[]) value) {
								each.setColor(ChatColor.WHITE);
								if (each instanceof TextComponent) {
									((TextComponent) each).setText(ChatColorUtils.stripColor(((TextComponent) each).getText()));
								}
							}
						} else if (value instanceof String) {
							contents.set(j, new Text(ChatColorUtils.stripColor((String) value)));
						}
					}
					j++;
				}
			}
		}
		return baseComponent;
	}
	
	public static BaseComponent preventFurtherInheritanceFormatting(BaseComponent baseComponent) {
		baseComponent.setBold(baseComponent.isBold());
		baseComponent.setColor(baseComponent.getColor());
		baseComponent.setItalic(baseComponent.isItalic());
		baseComponent.setObfuscated(baseComponent.isObfuscated());
		baseComponent.setStrikethrough(baseComponent.isStrikethrough());
		baseComponent.setUnderlined(baseComponent.isUnderlined());
		return baseComponent;
	}
	
	public static BaseComponent removeFormattingColor(BaseComponent baseComponent) {
		baseComponent.setColor(ChatColor.RESET);
		baseComponent = removeHoverEventColor(baseComponent);
		List<BaseComponent> extras = baseComponent.getExtra();
		if (extras != null) {
			for (BaseComponent extra : extras) {
				removeFormattingColor(extra);
			}
		}
		return baseComponent;
	}
	
	@SuppressWarnings("deprecation")
	public static BaseComponent cleanUpLegacyText(BaseComponent basecomponent, Player player) {
		List<BaseComponent> newlist = new LinkedList<BaseComponent>();
		List<BaseComponent> list = CustomStringUtils.loadExtras(basecomponent);
		if (list.isEmpty()) {
			return new TextComponent("");
		}
		
		for (BaseComponent base : list) {
			if (base.getHoverEvent() != null) {
				HoverEvent event = base.getHoverEvent();
				if (InteractiveChat.legacyChatAPI) {
					if (event.getValue() != null) {
						BaseComponent hover = cleanUpLegacyText(join(event.getValue()), player);
						base.setHoverEvent(new HoverEvent(event.getAction(), new BaseComponent[] {hover}));
						System.out.println(ComponentSerializer.toString(hover).replace(ChatColor.COLOR_CHAR, '&'));
					}
				} else {
					List<Content> newContents = new ArrayList<>();
					for (Content content : event.getContents()) {
						if (content instanceof Text) {
							Text text = (Text) content;
							if (text.getValue() != null) {
								if (text.getValue() instanceof BaseComponent[]) {
									newContents.add(new Text(new BaseComponent[] {cleanUpLegacyText(join((BaseComponent[]) text.getValue()), player)}));
								} else if (text.getValue() instanceof String) {
									newContents.add(new Text(new BaseComponent[] {cleanUpLegacyText(new TextComponent((String) text.getValue()), player)}));
								}
							} else {
								newContents.add(content);
							}
						} else {
							newContents.add(content);
						}
					}
					base.setHoverEvent(new HoverEvent(event.getAction(), newContents.toArray(new Content[0])));
				}
			}
		}
		
		BaseComponent current = null;
		for (BaseComponent base : list) {
			List<BaseComponent> thislist = new LinkedList<>();
			if (base instanceof TextComponent) {
				List<TextComponent> texts = Stream.of(TextComponent.fromLegacyText(base.toLegacyText())).map(each -> (TextComponent) each).collect(Collectors.toList());
				if (!texts.isEmpty()) {
					TextComponent current2 = texts.get(0);
					if (InteractiveChat.version.isLegacy() && !InteractiveChat.version.equals(MCVersion.V1_12)) {
						current2 = (TextComponent) CustomStringUtils.copyFormattingEventsNoReplace(current2, base);
	 	        	} else {
	 	        		current2.copyFormatting(base, FormatRetention.EVENTS, false);
	 	        	}
					if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
						current2.setFont(base.getFont());
					}
					
					for (TextComponent each : texts.subList(1, texts.size())) {
						if (areEventsSimilar(current2, each)) {
							each = (TextComponent) preventFurtherInheritanceFormatting(each);
							current2.addExtra(each);
						} else {
							thislist.add(current2);
							current2 = each;
							
							if (InteractiveChat.version.isLegacy() && !InteractiveChat.version.equals(MCVersion.V1_12)) {
								current2 = (TextComponent) CustomStringUtils.copyFormattingEventsNoReplace(current2, base);
			 	        	} else {
			 	        		current2.copyFormatting(base, FormatRetention.EVENTS, false);
			 	        	}
							if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
								current2.setFont(base.getFont());
							}
							//Bukkit.getConsoleSender().sendMessage(ComponentSerializer.toString(each).replace("\u00a7", "&"));
						}
					}
					thislist.add(current2);
				}
			} else {
				thislist.add(clone(base));
			}
			
			if (current == null) {
				current = new TextComponent("");
				if (InteractiveChat.version.isLegacy() && !InteractiveChat.version.equals(MCVersion.V1_12)) {
					current = (TextComponent) CustomStringUtils.copyFormattingEventsNoReplace(current, base);
 	        	} else {
 	        		current.copyFormatting(base, FormatRetention.EVENTS, false);
 	        	}
				if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
					current.setFont(base.getFont());
				}
				
				for (BaseComponent each : thislist) {
					each.setClickEvent(null);
					each.setHoverEvent(null);
					each = preventFurtherInheritanceFormatting(each);
					current.addExtra(each);
				}
			} else if (areEventsSimilar(current, base)) {
				for (BaseComponent each : thislist) {
					each.setClickEvent(null);
					each.setHoverEvent(null);
					each = preventFurtherInheritanceFormatting(each);
					current.addExtra(each);
				}
			} else {
				newlist.add(current);
				
				current = new TextComponent("");
				if (InteractiveChat.version.isLegacy() && !InteractiveChat.version.equals(MCVersion.V1_12)) {
					current = (TextComponent) CustomStringUtils.copyFormattingEventsNoReplace(current, base);
 	        	} else {
 	        		current.copyFormatting(base, FormatRetention.EVENTS, false);
 	        	}
				if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
					current.setFont(base.getFont());
				}
				
				for (BaseComponent each : thislist) {
					each.setClickEvent(null);
					each.setHoverEvent(null);
					each = preventFurtherInheritanceFormatting(each);
					current.addExtra(each);
				}
			}
		}
		if (current != null) {
			newlist.add(current);
		}

		ColorSettings colorsEnabled = ClientSettingPackets.getSettings(player);
		TextComponent product = new TextComponent("");
		for (int i = 0; i < newlist.size(); i++) {
			BaseComponent each = newlist.get(i);
			if (colorsEnabled.equals(ColorSettings.OFF)) {
				removeFormattingColor(each);
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
					((TextComponent) each).setText(((TextComponent) each).getText().replaceAll("\u00a7[0-9a-e]", "\u00a7f"));
				}
				each = removeHoverEventColor(each);
			}
			product.addExtra(each);
		}
		
		return product;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends BaseComponent> T clone(T chatComponent) {
		try {
			Class<? extends BaseComponent> clazz = chatComponent.getClass();
			return (T) clazz.getConstructor(clazz).newInstance(clazz.cast(chatComponent));
		} catch (Throwable e) {
			throw new UnsupportedOperationException(chatComponent.getClass() + " is not supported to be cloned.", e);
		}
	}
	
	public static BaseComponent join(BaseComponent... toJoin) {
		if (toJoin.length <= 0) {
			return new TextComponent("");
		}
		
		BaseComponent base = toJoin[0];
		for (int i = 1; i < toJoin.length; i++) {
			BaseComponent each = toJoin[i];
			base.addExtra(each);
		}
		
		return base;
	}
	
	public static BaseComponent translatePluginFontFormatting(BaseComponent basecomponent) {
		List<BaseComponent> list = CustomStringUtils.loadExtras(basecomponent);
		List<BaseComponent> newlist = new ArrayList<BaseComponent>();
		Optional<String> currentFont = Optional.empty();
		
		for (BaseComponent each : list) {
			if (each.getFont() != null) {
				currentFont = Optional.of(each.getFont());
			}
			
			if (each instanceof TextComponent) {
				List<TextComponent> textlist = new LinkedList<TextComponent>();
				TextComponent textcomponent = (TextComponent) each;
				String text = textcomponent.getText();
				
				while (true) {
		    		Matcher matcher = fontFormating.matcher(text);
		    		
		    		if (matcher.find()) {
			    	    String foramtedFont = matcher.group().toLowerCase();
			    	    
			    	    String colorCodesInside = ChatColorUtils.getLastColors(foramtedFont);
			    	    String striped = ChatColorUtils.stripColor(foramtedFont);
			    	    
			    	    int lengthDiff = foramtedFont.length() - striped.length();
			    	    
			    	    foramtedFont = striped;
			    	    
			    	    int start = matcher.start();
			    	    int pos = foramtedFont.indexOf("font");
			    	    int end = matcher.end() - lengthDiff;
			    	    
			    	    if (pos < 0) {
			    	    	continue;
			    	    }
			    	    
			    	    int fontLength = foramtedFont.indexOf(",", pos);
			    	    if (fontLength < 0) {
			    	    	fontLength = foramtedFont.indexOf(" ", pos);
			    	    }
			    	    if (fontLength < 0) {
			    	    	fontLength = foramtedFont.indexOf("]", pos);
			    	    }
			    	    fontLength = fontLength - pos;
			    	    
			    	    StringBuilder sb = new StringBuilder(text);
			    	    
			    	    sb.delete(start, matcher.end());
			    	    sb.insert(start, foramtedFont);

			    	    int absPos = sb.indexOf("font", start);
			
			    	    String nextFont = foramtedFont.substring(pos + 5, pos + fontLength);
			    	    
			    	    sb.delete(absPos, absPos + fontLength);
			    	    sb.insert(end - fontLength, colorCodesInside);

			    	    while (sb.charAt(absPos) == ',' || sb.charAt(absPos) == ' ') {
			    	    	sb.deleteCharAt(absPos);
			    	    }
			    	    
			    	    while (sb.charAt(absPos - 1) == ',' || sb.charAt(absPos - 1) == ' ') {
			    	    	sb.deleteCharAt(absPos - 1);
			    	    	absPos--;
			    	    }
			    	    
			    	    if (sb.charAt(absPos) == ']' && sb.charAt(absPos - 1) == '[') {
			    	    	sb.deleteCharAt(absPos - 1);
			    	    	sb.deleteCharAt(absPos - 1);		
			    	    	
			    	    	if (absPos > 2 && sb.charAt(absPos - 2) == '\\' && sb.charAt(absPos - 3) == '\\') {
				    	    	sb.deleteCharAt(absPos - 2);
				    	    	absPos--;
				    	    }
			    	    }			    	
			    	    
			    	    absPos--;
			    	    
			    	    TextComponent before = new TextComponent(textcomponent);
			    	    before.setText(sb.substring(0, absPos));
			    	    if (currentFont.isPresent()) {
			    	    	before.setFont(currentFont.get());
			    	    }
			    	    textlist.add(before);
			    	    
			    	    text = sb.substring(absPos);
			    	    
			    	    currentFont = (nextFont.length() == 0 || nextFont.equalsIgnoreCase("null") || nextFont.equalsIgnoreCase("reset")) ? Optional.empty() : (nextFont.matches(validFont) ? Optional.of(nextFont) : Optional.empty());
		    		} else {
		    			TextComponent before = new TextComponent(textcomponent);
			    	    before.setText(text);
			    	    if (currentFont.isPresent()) {
			    	    	before.setFont(currentFont.get());
			    	    }
			    	    textlist.add(before);
		    			break;
		    		}
		    		
		    	}
				
				newlist.addAll(textlist);				
			} else {
				if (currentFont.isPresent()) {
	    	    	each.setFont(currentFont.get());
	    	    }
				newlist.add(each);
			}
		}
		
		TextComponent product = new TextComponent("");
		for (int i = 0; i < newlist.size(); i++) {
			BaseComponent each = newlist.get(i);
			if (each instanceof TextComponent) {
				TextComponent text = (TextComponent) each;
				String str = text.getText();
				while (true) {
	        		Matcher matcher = fontEscape.matcher(str);  		
	        		if (matcher.find()) {
	    	    	    StringBuilder sb = new StringBuilder(str);
	    	    	    sb.deleteCharAt(matcher.start());
	    	    	    str = sb.toString();
	        		} else {
	        			text.setText(str);
	        			break;
	        		}
	        	}
			}
			product.addExtra(each);
		}
		
		return product;
	}

}
