package com.loohp.interactivechat.Utils;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class DataStreamIO {
	
	public static String readString(DataInputStream in, Charset charset) throws IOException {
		int length = readVarInt(in);

	    if (length == -1) {
	        throw new IOException("Premature end of stream.");
	    }

	    byte[] b = new byte[length];
	    in.readFully(b);
	    return new String(b, charset);
	}
	
	public static int getStringLength(String string, Charset charset) throws IOException {
	    byte[] bytes = string.getBytes(charset);
	    return bytes.length;
	}
	
	public static void writeString(DataOutputStream out, String string, Charset charset) throws IOException {
	    byte[] bytes = string.getBytes(charset);
	    writeVarInt(out, bytes.length);
	    out.write(bytes);
	}
	
	public static int readVarInt(DataInputStream in) throws IOException {
	    int numRead = 0;
	    int result = 0;
	    byte read;
	    do {
	        read = in.readByte();
	        int value = (read & 0b01111111);
	        result |= (value << (7 * numRead));

	        numRead++;
	        if (numRead > 5) {
	            throw new RuntimeException("VarInt is too big");
	        }
	    } while ((read & 0b10000000) != 0);

	    return result;
	}
	
	public static void writeVarInt(DataOutputStream out, int value) throws IOException {
	    do {
	        byte temp = (byte)(value & 0b01111111);
	        // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
	        value >>>= 7;
	        if (value != 0) {
	            temp |= 0b10000000;
	        }
	        out.writeByte(temp);
	    } while (value != 0);
	}
	
	public static int getVarIntLength(int value) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buffer);
	    do {
	        byte temp = (byte)(value & 0b01111111);
	        // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
	        value >>>= 7;
	        if (value != 0) {
	            temp |= 0b10000000;
	        }
	        out.writeByte(temp);
	    } while (value != 0);
	    return buffer.toByteArray().length;
	}

}
