package com.loohp.interactivechat.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlaceholderAPIUtils {

    public static Component setPlaceholders(OfflinePlayer player, Component component) {
        component = ComponentFlattening.flatten(component);
        List<Component> children = new ArrayList<>(component.children());
        for (int i = 0; i < children.size(); i++) {
            Component child = children.get(i);
            if (child instanceof TextComponent) {
                TextComponent text = (TextComponent) child;
                String content = PlaceholderAPI.setPlaceholders(player, text.content());
                children.set(i, text.content(content));
            } else if (child instanceof TranslatableComponent) {
                TranslatableComponent translatable = (TranslatableComponent) child;
                List<ComponentLike> args = translatable.arguments().stream()
                        .map(arg -> setPlaceholders(player, arg.asComponent()))
                        .collect(Collectors.toList());
                children.set(i, translatable.arguments(args));
            }
        }
        return ComponentCompacting.optimize(component.children(children));
    }

}
