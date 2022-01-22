package com.loohp.interactivechat.proxy.velocity;

import java.awt.Color;

public class TextColor {

    public static final char COLOR_CHAR = '\u00a7';

    /**
     * Represents black.
     */
    public static final TextColor BLACK = new TextColor('0', "black", new Color(0x000000));
    /**
     * Represents dark blue.
     */
    public static final TextColor DARK_BLUE = new TextColor('1', "dark_blue", new Color(0x0000AA));
    /**
     * Represents dark green.
     */
    public static final TextColor DARK_GREEN = new TextColor('2', "dark_green", new Color(0x00AA00));
    /**
     * Represents dark blue (aqua).
     */
    public static final TextColor DARK_AQUA = new TextColor('3', "dark_aqua", new Color(0x00AAAA));
    /**
     * Represents dark red.
     */
    public static final TextColor DARK_RED = new TextColor('4', "dark_red", new Color(0xAA0000));
    /**
     * Represents dark purple.
     */
    public static final TextColor DARK_PURPLE = new TextColor('5', "dark_purple", new Color(0xAA00AA));
    /**
     * Represents gold.
     */
    public static final TextColor GOLD = new TextColor('6', "gold", new Color(0xFFAA00));
    /**
     * Represents gray.
     */
    public static final TextColor GRAY = new TextColor('7', "gray", new Color(0xAAAAAA));
    /**
     * Represents dark gray.
     */
    public static final TextColor DARK_GRAY = new TextColor('8', "dark_gray", new Color(0x555555));
    /**
     * Represents blue.
     */
    public static final TextColor BLUE = new TextColor('9', "blue", new Color(0x5555FF));
    /**
     * Represents green.
     */
    public static final TextColor GREEN = new TextColor('a', "green", new Color(0x55FF55));
    /**
     * Represents aqua.
     */
    public static final TextColor AQUA = new TextColor('b', "aqua", new Color(0x55FFFF));
    /**
     * Represents red.
     */
    public static final TextColor RED = new TextColor('c', "red", new Color(0xFF5555));
    /**
     * Represents light purple.
     */
    public static final TextColor LIGHT_PURPLE = new TextColor('d', "light_purple", new Color(0xFF55FF));
    /**
     * Represents yellow.
     */
    public static final TextColor YELLOW = new TextColor('e', "yellow", new Color(0xFFFF55));
    /**
     * Represents white.
     */
    public static final TextColor WHITE = new TextColor('f', "white", new Color(0xFFFFFF));
    /**
     * Represents magical characters that change around randomly.
     */
    public static final TextColor MAGIC = new TextColor('k', "obfuscated");
    /**
     * Makes the text bold.
     */
    public static final TextColor BOLD = new TextColor('l', "bold");
    /**
     * Makes a line appear through the text.
     */
    public static final TextColor STRIKETHROUGH = new TextColor('m', "strikethrough");
    /**
     * Makes the text appear underlined.
     */
    public static final TextColor UNDERLINE = new TextColor('n', "underline");
    /**
     * Makes the text italic.
     */
    public static final TextColor ITALIC = new TextColor('o', "italic");
    /**
     * Resets all previous chat colors or formats.
     */
    public static final TextColor RESET = new TextColor('r', "reset");

    private final char c;
    private final String name;
    private final Color color;

    public TextColor(char c, String name, Color color) {
        this.c = c;
        this.name = name;
        this.color = color;
    }

    public TextColor(char c, String name) {
        this(c, name, null);
    }

    public char getChar() {
        return c;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return String.valueOf(COLOR_CHAR) + c;
    }

}
