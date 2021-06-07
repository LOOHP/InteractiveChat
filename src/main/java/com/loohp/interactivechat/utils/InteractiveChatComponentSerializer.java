package com.loohp.interactivechat.utils;

import java.util.ArrayList;
import java.util.List;

import com.loohp.interactivechat.registry.Registry;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.TranslatableComponent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class InteractiveChatComponentSerializer {
	
	private static final InteractiveChatBungeecordAPILegacyComponentSerializer INSTANCE = new InteractiveChatBungeecordAPILegacyComponentSerializer();
	
	public static InteractiveChatBungeecordAPILegacyComponentSerializer bungeecordApiLegacy() {
		return INSTANCE;
	}
	
	private InteractiveChatComponentSerializer() {
		
	}
	
	public static class InteractiveChatBungeecordAPILegacyComponentSerializer {
		
		private InteractiveChatBungeecordAPILegacyComponentSerializer() {
			
		}
		
		public String serialize(Component component) {
			return BaseComponent.toLegacyText(ComponentSerializer.parse(Registry.ADVENTURE_GSON_SERIALIZER.serialize(component)));
		}
		
		public String serialize(Component component, String language) {
			return serialize(translate(component, language));
		}
		
		public Component translate(Component component, String language) {
			if (component instanceof TranslatableComponent) {
				TranslatableComponent trans = (TranslatableComponent) component;
				Component translated = Component.text(LanguageUtils.getTranslation(trans.key(), language)).style(trans.style()).children(trans.children()).clickEvent(trans.clickEvent()).hoverEvent(trans.hoverEvent());
				for (Component with : trans.args()) {
					translated = translated.replaceText(TextReplacementConfig.builder().matchLiteral("%s").replacement(with).times(1).build());
				}
			}
			List<Component> children = new ArrayList<>(component.children());
			for (int i = 0; i < children.size(); i++) {
				children.set(i, translate(children.get(i), language));
			}
			return component.children(children);
		}
		
	}

}
