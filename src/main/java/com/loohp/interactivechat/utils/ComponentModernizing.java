package com.loohp.interactivechat.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.Style.Merge;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public class ComponentModernizing {

    public static Component modernize(Component component) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        component = ComponentFlattening.flatten(component);
        List<Component> children = new ArrayList<>(component.children());
        for (int i = 0; i < children.size(); i++) {
            Component child = children.get(i);
            if (child instanceof TextComponent) {
                TextComponent text = (TextComponent) child;
                String content = text.content();
                TextComponent modern = serializer.deserialize(content);
                modern = modern.style(modern.style().merge(text.style(), Merge.Strategy.IF_ABSENT_ON_TARGET));
                children.set(i, modern);
            } else if (child instanceof TranslatableComponent) {
                TranslatableComponent trans = (TranslatableComponent) child;
                List<Component> withs = new ArrayList<>(trans.args());
                int u = -1;
                for (Component with : withs) {
                    u++;
                    withs.set(u, modernize(with));
                }
                trans = trans.args(withs);
                children.set(i, trans);
            }
        }
        return ComponentCompacting.optimize(component.children(children));
    }

}
