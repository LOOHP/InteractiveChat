package com.loohp.interactivechat.utils;

import com.cryptomorin.xseries.XSound;
import org.bukkit.Sound;

import java.util.Optional;

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
