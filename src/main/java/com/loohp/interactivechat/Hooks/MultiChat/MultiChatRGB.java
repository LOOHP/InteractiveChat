package com.loohp.interactivechat.Hooks.MultiChat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiChatRGB {
	
	public static final Pattern MULTICHAT_RGB = Pattern.compile("&(#[0-9a-fA-F]{6})");
	public static final Pattern MULTICHAT_RAW_RGB = Pattern.compile("§#[0-9a-fA-F]{6}");
	
	public static String convertRGBFormatting(String str) {
		str = str.replaceAll(MULTICHAT_RGB.pattern(), "§$1");
		StringBuilder sb = new StringBuilder(str);
		Matcher matcher = MULTICHAT_RAW_RGB.matcher(str);
		while (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			String group = matcher.group();
			sb.replace(start, end, "§x§" + String.valueOf(group.charAt(2)).toUpperCase() + "§" + String.valueOf(group.charAt(3)).toUpperCase() + "§" + String.valueOf(group.charAt(4)).toUpperCase() + "§" + String.valueOf(group.charAt(5)).toUpperCase() + "§" + String.valueOf(group.charAt(6)).toUpperCase() + "§" + String.valueOf(group.charAt(7)).toUpperCase());
		}
		return sb.toString();
	}

}
