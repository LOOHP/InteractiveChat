package com.loohp.interactivechat.Utils;

public class ChatColorFilter {
	
	public static String removeUselessColorCodes(String string) {
		return string.replaceAll("^(§[0-9,a-f,l-o,r])*(?=§[0-9,a-f,r])", "");
	}
	
	public static String filterIllegalColorCodes(String string) {
		return string.replaceAll("§[g-j,p,q,s-z,A-Z,\\-!$%^&*()_+|~=`{}\\[\\]:\";'<>?,.\\/\\\\]", "§r");
	}

}
