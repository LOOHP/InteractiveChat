/*
 * This file is part of InteractiveChat4.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactivechat.utils;

import com.loohp.interactivechat.objectholders.Either;
import com.loohp.interactivechat.objectholders.ValuePairs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ComponentReplacing {

    public static final String ESCAPE_PREPEND_PATTERN = "(?:(?<=\\\\)(\\\\)|(?<!\\\\))";
    public static final String ESCAPE_PLACEHOLDER_PATTERN = "\\\\(%s)";

    public static Component replace(Component component, String regex, Component replace) {
        return replace(component, regex, false, groups -> replace);
    }

    public static Component replace(Component component, String regex, boolean escaping, Component replace) {
        return replace(component, regex, escaping, groups -> replace);
    }

    public static Component replace(Component component, String regex, Function<ComponentMatchResult, Component> replaceFunction) {
        return replace(component, regex, false, replaceFunction);
    }

    public static Component replace(Component component, String regex, boolean escaping, Function<ComponentMatchResult, Component> replaceFunction) {
        return replace(component, regex, escaping, (result, replaced) -> replaceFunction.apply(result));
    }

    public static Component replace(Component component, String regex, boolean escaping, BiFunction<ComponentMatchResult, List<Component>, Component> replaceFunction) {
        String regexOriginal = regex;
        if (escaping) {
            regex = ESCAPE_PREPEND_PATTERN + regex;
        }
        component = ComponentFlattening.flatten(component);
        List<Component> children = new ArrayList<>(component.children());
        for (int i = 0; i < children.size(); i++) {
            Component child = children.get(i);
            if (child instanceof TranslatableComponent) {
                TranslatableComponent translatable = (TranslatableComponent) child;
                List<ComponentLike> args = translatable.arguments().stream()
                        .map(arg -> replace(arg.asComponent(), regexOriginal, escaping, replaceFunction))
                        .collect(Collectors.toList());
                translatable = translatable.arguments(args);
                children.set(i, translatable);
            }
        }
        component = component.children(children);

        List<Either<ValuePairs<String, List<Component>>, Component>> sections = breakdown(component);
        Pattern pattern = Pattern.compile(regex);
        children = new ArrayList<>();
        for (Either<ValuePairs<String, List<Component>>, Component> either : sections) {
            if (either.isRight()) {
                children.add(either.getRight());
            } else {
                ValuePairs<String, List<Component>> pair = either.getLeft();
                List<Component> componentCharacters = pair.getSecond();
                String str = pair.getFirst();
                Matcher matcher = pattern.matcher(str);
                int lastEnd = 0;
                while (matcher.find()) {
                    int start = toComponentIndex(matcher.start(), str);
                    int end = toComponentIndex(matcher.end(), str);
                    List<Component> componentGroup = Collections.unmodifiableList(componentCharacters.subList(start, end));
                    Component result = replaceFunction.apply(new ComponentMatchResult(matcher, str, componentCharacters), componentGroup);
                    children.addAll(componentCharacters.subList(lastEnd, start));
                    children.add(result);
                    lastEnd = end;
                }
                children.addAll(componentCharacters.subList(lastEnd, componentCharacters.size()));
            }
        }

        component = ComponentCompacting.optimize(component.children(children));

        if (escaping) {
            component = replace(component, ESCAPE_PLACEHOLDER_PATTERN.replace("%s", regexOriginal), false, (result, replaced) -> result.componentGroup(1));
        }

        return component;
    }

    private static int toComponentIndex(int pos, String str) {
        if (pos < 0) {
            return pos;
        }
        int actual = 0;
        for (int i = 0; i < pos;) {
            int codePoint = str.codePointAt(i);
            String character = new String(Character.toChars(codePoint));
            i += character.length();
            actual++;
        }
        return actual;
    }

    private static List<Either<ValuePairs<String, List<Component>>, Component>> breakdown(Component component) {
        List<Either<ValuePairs<String, List<Component>>, Component>> result = new ArrayList<>();
        Component flatten = ComponentFlattening.flatten(component);
        StringBuilder sb = new StringBuilder();
        List<Component> components = new ArrayList<>();
        for (Component c : flatten.children()) {
            if (c instanceof TextComponent) {
                TextComponent textComponent = (TextComponent) c;
                String content = textComponent.content();
                if (!content.isEmpty()) {
                    for (int i = 0; i < content.length();) {
                        int codePoint = content.codePointAt(i);
                        String character = new String(Character.toChars(codePoint));
                        i += character.length();
                        components.add(textComponent.content(character));
                        sb.append(character);
                    }
                }
            } else {
                if (!components.isEmpty()) {
                    result.add(Either.left(new ValuePairs<>(sb.toString(), components)));
                    sb = new StringBuilder();
                    components = new ArrayList<>();
                }
                result.add(Either.right(c));
            }
        }
        if (!components.isEmpty()) {
            result.add(Either.left(new ValuePairs<>(sb.toString(), components)));
        }
        return result;
    }

    public static final class ComponentMatchResult implements MatchResult {

        private final String str;
        private final MatchResult backingResult;
        private final List<Component> componentCharacters;

        public ComponentMatchResult(MatchResult backingResult, String str, List<Component> componentCharacters) {
            this.backingResult = backingResult;
            this.str = str;
            this.componentCharacters = componentCharacters;
        }

        @Override
        public int start() {
            return backingResult.start();
        }

        @Override
        public int start(int group) {
            return backingResult.start(group);
        }

        @Override
        public int end() {
            return backingResult.end();
        }

        @Override
        public int end(int group) {
            return backingResult.end(group);
        }

        public int componentStart() {
            return toComponentIndex(backingResult.start(), str);
        }

        public int componentStart(int group) {
            return toComponentIndex(backingResult.start(group), str);
        }

        public int componentEnd() {
            return toComponentIndex(backingResult.end(), str);
        }

        public int componentEnd(int group) {
            return toComponentIndex(backingResult.end(group), str);
        }

        @Override
        public String group() {
            return backingResult.group();
        }

        @Override
        public String group(int group) {
            return backingResult.group(group);
        }

        public Component componentGroup() {
            int start = componentStart();
            int end = componentEnd();
            if (start < 0 || end < 0) {
                return null;
            }
            return ComponentCompacting.optimize(Component.empty().children(componentCharacters.subList(start, end)));
        }

        public Component componentGroup(int group) {
            int start = componentStart(group);
            int end = componentEnd(group);
            if (start < 0 || end < 0) {
                return null;
            }
            return ComponentCompacting.optimize(Component.empty().children(componentCharacters.subList(start, end)));
        }

        @Override
        public int groupCount() {
            return backingResult.groupCount();
        }

    }

}
