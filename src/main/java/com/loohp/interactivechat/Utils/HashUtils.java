package com.loohp.interactivechat.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class HashUtils {

	public static byte[] createSha1(File file) throws Exception {
		return createSha1(new FileInputStream(file));
	}

	public static byte[] createSha1(InputStream fis) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-1");
		int n = 0;
		byte[] buffer = new byte[8192];
		while (n != -1) {
			n = fis.read(buffer);
			if (n > 0) {
				digest.update(buffer, 0, n);
			}
		}
		fis.close();
		return digest.digest();
	}

	public static String createSha1String(File file) throws Exception {
		byte[] b = createSha1(file);
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

}
