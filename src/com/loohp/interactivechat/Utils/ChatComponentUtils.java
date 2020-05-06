package com.loohp.interactivechat.Utils;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatComponentUtils {
	
	public static boolean areSimilar(BaseComponent base1, BaseComponent base2) {
		if (!areEventsSimilar(base1, base2)) {
			return false;
		}
		if (!base1.getColor().equals(base2.getColor())) {
			return false;
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
		return true;
	}
	
	public static boolean areEventsSimilar(BaseComponent base1, BaseComponent base2) {
		boolean clickSim = false;
		boolean hoverSim = false;
		if (base1.getClickEvent() == null && base2.getClickEvent() == null) {
			clickSim = true;
		} else {
			if (base1.getClickEvent() != null && base2.getClickEvent() != null && base1.getClickEvent().equals(base2.getClickEvent())) {
				clickSim = true;
			}
		}
		if (base1.getHoverEvent() == null && base2.getHoverEvent() == null) {
			hoverSim = true;
		} else {
			if (base1.getHoverEvent() != null && base2.getHoverEvent() != null && base1.getHoverEvent().equals(base2.getHoverEvent())) {
				hoverSim = true;
			}
		}
		
		return clickSim && hoverSim;
	}
	
	public static BaseComponent cleanUpLegacyText(BaseComponent basecomponent) {
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
					newlist.add(newTextComponent);
				} while (text.contains("§"));
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
