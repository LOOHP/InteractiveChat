package com.loohp.interactivechat.Utils;

public class ChatColorFilter {
	
	public static String filterIllegalColorCodes(String string) {
		string = string.replaceAll("§[g-j,p,q,s-z,A-Z,\\-!$%^&*()_+|~=`{}\\[\\]:\";'<>?,.\\/\\\\]", "§r");
		return string;
	}

}
