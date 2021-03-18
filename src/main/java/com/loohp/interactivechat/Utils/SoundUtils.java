package com.loohp.interactivechat.Utils;

import java.util.Optional;

import org.bukkit.Sound;

import com.cryptomorin.xseries.XSound;

public class SoundUtils {
	
	public static boolean isValid(String string) {
		Optional<XSound> opt = XSound.matchXSound(string);
		if (opt.isPresent()) {
			XSound xSound = opt.get();
			return xSound.parseSound() != null;
		}
		return false;
	}
	
	public static Sound parseSound(String string) {
		Optional<XSound> opt = XSound.matchXSound(string);
		if (opt.isPresent()) {
			XSound xSound = opt.get();
			return xSound.parseSound();
		}
		return null;
	}

}
