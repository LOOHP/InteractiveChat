package com.loohp.interactivechat.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtils {

	public static void removeFolderRecursively(File folder) {
		if (folder.exists()) {
			File[] files = folder.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					removeFolderRecursively(file);
				} else {
					if (!file.delete()) {
						file.deleteOnExit();
					}
				}
			}
			if (!folder.delete()) {
				folder.deleteOnExit();
			}
		}
	}
	
	public static long copy(File from, File to) throws IOException {
		FileInputStream stream = new FileInputStream(from);
		long result = Files.copy(stream, to.toPath());
		stream.close();
		return result;
	}

}
