package com.loohp.interactivechat.Utils;

import org.bukkit.Sound;

public class SoundUtils {
	
	public static boolean isValid(String string) {
		try {
			Sound.valueOf(string);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
