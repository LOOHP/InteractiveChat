package com.loohp.interactivechat.Utils;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;

import com.loohp.interactivechat.Listeners.ClientSettingPackets;
import com.loohp.interactivechat.Listeners.ClientSettingPackets.ColorSettings;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatComponentUtils {
	
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
	
	public static boolean areEventsSimilar(BaseComponent base1, BaseComponent base2) {
		boolean clickSim = false;
		boolean hoverSim = false;
		if (base1.getClickEvent() == null && base2.getClickEvent() == null) {
			clickSim = true;
		} else {
			if (base1.getClickEvent() != null && base2.getClickEvent() != null) {
				ClickEvent click1 = base1.getClickEvent();
				ClickEvent click2 = base2.getClickEvent();
				if (click1.getAction().equals(click2.getAction())) {
					String value1 = click1.getValue();
					String value2 = click2.getValue();
					if (value1.equals(value2)) {
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
					BaseComponent[] basecomponentarray1 = hover1.getValue();
					BaseComponent[] basecomponentarray2 = hover2.getValue();
					hoverSim = true;
					if (basecomponentarray1.length == basecomponentarray2.length) {
						for (int i = 0; i < basecomponentarray1.length && i < basecomponentarray2.length ; i++) {
							BaseComponent bc1 = basecomponentarray1[i];
							BaseComponent bc2 = basecomponentarray2[i];
							if (!areSimilar(bc1, bc2, true)) {
								hoverSim = false;
								break;
							}
						}
					} else {
						hoverSim = false;
					}
				}
			}
		}

		return clickSim && hoverSim;
	}
	
	public static BaseComponent removeHoverEventColor(BaseComponent baseComponent) {
		if (baseComponent.getHoverEvent() != null) {
			for (BaseComponent each : baseComponent.getHoverEvent().getValue()) {
				each.setColor(ChatColor.WHITE);
				if (each instanceof TextComponent) {
					((TextComponent) each).setText(((TextComponent) each).getText().replaceAll("§[0-9a-e]", "§f"));
				}
				if (each.getHoverEvent() != null) {
					each = removeHoverEventColor(each);
				}
			}
		}
		return baseComponent;
	}
	
	public static BaseComponent cleanUpLegacyText(BaseComponent basecomponent, Player player) {
		List<BaseComponent> newlist = new LinkedList<BaseComponent>();
		for (BaseComponent base : CustomStringUtils.loadExtras(basecomponent)) {
			if (!(base instanceof TextComponent)) {
				newlist.add(base);
			} else {
				TextComponent textcomponent = (TextComponent) base;
				String text = textcomponent.getText();
				do {
					String color = ChatColorUtils.getFirstColors(text);
					int pos = CustomStringUtils.ordinalIndexOf(text, "§", CustomStringUtils.occurrencesOfSubstring(color, "§") + 1);
					pos = pos >= 0 ? pos : text.length();
					String before = text.substring(0, pos);
					text = text.substring(pos);
					TextComponent newTextComponent = new TextComponent(ChatColor.stripColor(before));
					newTextComponent = (TextComponent) CustomStringUtils.copyFormatting(newTextComponent, textcomponent);
					newTextComponent = (TextComponent) ChatColorUtils.applyColor(newTextComponent, color);
					if (!newlist.isEmpty() && areSimilar(newTextComponent, newlist.get(newlist.size() - 1), false) && newlist.get(newlist.size() - 1) instanceof TextComponent) {
						TextComponent lastTextComponent = (TextComponent) newlist.get(newlist.size() - 1);
						lastTextComponent.setText(lastTextComponent.getText() + newTextComponent.getText());
					} else {
						newlist.add(newTextComponent);
					}
				} while (text.contains("§"));
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

}
