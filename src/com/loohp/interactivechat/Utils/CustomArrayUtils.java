package com.loohp.interactivechat.Utils;

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

}
