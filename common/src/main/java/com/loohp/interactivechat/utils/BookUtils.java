/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
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

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;
import java.util.stream.Collectors;

public class BookUtils {

    public static boolean isTextBook(ItemStack item) {
        return item.getItemMeta() instanceof BookMeta;
    }

    public static List<Component> getPages(BookMeta bookMeta) {
        return bookMeta.spigot().getPages().stream().map(each -> ChatComponentType.BaseComponentArray.convertFrom(each)).collect(Collectors.toList());
    }

    public static void setPages(BookMeta bookMeta, List<Component> pages, boolean legacyRGB) {
        bookMeta.spigot().setPages(pages.stream().map(each -> (BaseComponent[]) ChatComponentType.BaseComponentArray.convertTo(each, legacyRGB)).collect(Collectors.toList()));
    }

    public static Book toAdventure(BookMeta meta) {
        return Book.book(LegacyComponentSerializer.legacySection().deserializeOr(meta.getTitle(), Component.empty()), LegacyComponentSerializer.legacySection().deserializeOr(meta.getAuthor(), Component.empty()), getPages(meta));
    }

    public static void fromAdventure(BookMeta bookMeta, Book book, boolean legacyRGB) {
        String title = LegacyComponentSerializer.legacySection().serialize(book.title());
        bookMeta.setTitle(title.isEmpty() ? null : title);
        String author = LegacyComponentSerializer.legacySection().serialize(book.author());
        bookMeta.setAuthor(author.isEmpty() ? null : author);
        setPages(bookMeta, book.pages(), legacyRGB);
    }

}
