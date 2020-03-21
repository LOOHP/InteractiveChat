package com.loohp.interactivechat.Utils;

import java.util.Random;

import com.loohp.interactivechat.InteractiveChat;

public class KeyUtils {
	public static String getRandomKey() {
        String SALTCHARS = InteractiveChat.space0 + InteractiveChat.space1;
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 30) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = InteractiveChat.space0 + salt.toString() + InteractiveChat.space0;
        return saltStr;
    }
}
