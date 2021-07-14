package com.loohp.interactivechat.utils;

import java.lang.reflect.Array;
import java.util.Arrays;

public class CustomArrayUtils {

	public static byte[][] divideArray(byte[] source, int chunksize) {
		byte[][] ret = new byte[(int) Math.ceil(source.length / (double) chunksize)][];
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
		boolean isNull = true;
		for (int i = 0; i < Array.getLength(src); i++) {
			if (Array.get(src, i) != null) {
				isNull = false;
				break;
			}
		}
		return isNull;
	}

}
