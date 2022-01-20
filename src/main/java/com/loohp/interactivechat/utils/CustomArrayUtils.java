package com.loohp.interactivechat.utils;

import java.lang.reflect.Array;
import java.util.Arrays;

public class CustomArrayUtils {

	public static byte[][] divideArray(byte[] source, int chunksize) {
		int length = (int) Math.ceil(source.length / (double) chunksize);
		if (length <= 1) {
			return new byte[][] {source};
		}
		byte[][] ret = new byte[length][];
		int start = 0;
		for (int i = 0; i < ret.length; i++) {
			int end = start + chunksize;
			ret[i] = Arrays.copyOfRange(source, start, end > source.length ? source.length : end);
			start += chunksize;
		}
		return ret;
	}
	
	public static boolean allNull(Object src) {
		if (src == null) {
			return true;
		}
		if (!src.getClass().isArray()) {
			return false;
		}
		for (int i = 0; i < Array.getLength(src); i++) {
			if (Array.get(src, i) != null) {
				return false;
			}
		}
		return true;
	}

}
